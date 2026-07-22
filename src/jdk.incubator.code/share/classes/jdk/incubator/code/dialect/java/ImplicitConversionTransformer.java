/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.incubator.code.dialect.java;

import jdk.incubator.code.Block;
import jdk.incubator.code.Body;
import jdk.incubator.code.CodeTransformer;
import jdk.incubator.code.CodeType;
import jdk.incubator.code.Op;
import jdk.incubator.code.Value;
import jdk.incubator.code.dialect.core.CoreOp;
import jdk.incubator.code.dialect.core.VarType;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adds the implicit Java conversions required by a high-level Java code model.
 *
 * The transformer intentionally obtains conversion targets from the model: a
 * variable, field, array component, invocation signature, or enclosing body.
 * Operator conversion targets are obtained from the resolved function type
 * carried by each unary and binary operation.
 * <p>
 * This transformer is intended for javac-generated, high-level Java models
 * containing {@link JavaOp Java operations}. A contextual conversion is
 * appended to an operation that produces a value, or prepended to a block
 * that introduces a parameter, or to the innermost body that directly
 * captures a value. The first body that directly uses a value determines the
 * conversion target for all its descendant bodies; independent sibling bodies may
 * use the same value with different conversion targets.
 */
public final class ImplicitConversionTransformer implements CodeTransformer {

    /** Creates an implicit-conversion lowering transformer. */
    public ImplicitConversionTransformer() {}

    @Override
    public void acceptBlock(Block.Builder block, Block input) {
        // Block parameters are introduced at block entry.
        for (Block.Parameter parameter : input.parameters()) {
            convertAfterIntroduction(block, input.ancestorBody(), parameter);
        }

        if (input.isEntryBlock()) {
            // A captured value is introduced again in the innermost body that
            // directly uses it, so that body's entry is another producer-side
            // conversion point.  Uses in nested bodies are handled there.
            Body body = input.ancestorBody();
            Set<Value> captures = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Block bodyBlock : body.blocks()) {
                for (Op op : bodyBlock.ops()) {
                    for (Value operand : op.operands()) {
                        if (operand.declaringBlock().ancestorBody() != body) {
                            captures.add(operand);
                        }
                    }
                }
            }
            for (Value capture : captures) {
                convertAfterIntroduction(block, body, capture);
            }
        }
        CodeTransformer.super.acceptBlock(block, input);
    }

    @Override
    public Block.Builder acceptOp(Block.Builder block, Op op) {
        Op.Result output = block.add(op);
        block.context().mapValue(op.result(), output);

        CodeType target = commonConversionTarget(op.result(), op.ancestorBody());
        if (target != null && requiresConversion(output.type(), target)) {
            block.context().mapValue(op.result(), convert(block, output, target));
        }
        return block;
    }

    private static void convertAfterIntroduction(Block.Builder block, Body body, Value value) {
        CodeType target = commonConversionTarget(value, body);
        if (target != null) {
            Value output = block.context().getValue(value);
            if (!requiresConversion(output.type(), target)) return;
            block.context().mapValue(value, convert(block, output, target));
        }
    }

    private static CodeType commonConversionTarget(Value value, Body body) {
        // A variable is storage, not a Java value.  Its initializer and stores
        // already conform to its declared type; conversions apply to loads.
        if (value.type() instanceof VarType) return null;

        Map<Body, CodeType> targets = new IdentityHashMap<>();
        for (Op.Result use : value.uses()) {
            Op consumer = use.op();
            Body consumingBody = consumer.ancestorBody();
            for (int i = 0; i < consumer.operands().size(); i++) {
                if (consumer.operands().get(i) != value) continue;
                CodeType useTarget = conversionTarget(consumer, i);
                if (useTarget == null) useTarget = value.type();
                addConversionTarget(targets, consumingBody, useTarget);
            }

            for (Block.Reference successor : consumer.successors()) {
                for (int i = 0; i < successor.arguments().size(); i++) {
                    if (successor.arguments().get(i) != value) continue;
                    CodeType useTarget = successor.targetBlock().parameters().get(i).type();
                    addConversionTarget(targets, consumingBody, useTarget);
                }
            }
        }

        for (Map.Entry<Body, CodeType> use : targets.entrySet()) {
            Body useRoot = use.getKey();
            for (Body enclosingBody = useRoot.ancestorBody();
                 enclosingBody != null;
                 enclosingBody = enclosingBody.ancestorBody()) {
                if (targets.containsKey(enclosingBody)) useRoot = enclosingBody;
            }
            if (!targets.get(useRoot).equals(use.getValue())) {
                throw new IllegalArgumentException(
                        "all uses under an initial use body must have the same conversion target");
            }
        }
        return targets.get(body);
    }

    private static void addConversionTarget(Map<Body, CodeType> targets,
                                            Body body, CodeType target) {
        CodeType previous = targets.putIfAbsent(body, target);
        if (previous != null && !previous.equals(target)) {
            throw new IllegalArgumentException(
                    "one value cannot be converted differently for multiple uses in a body");
        }
    }

    /**
     * Emits the Java conversions required to adapt a value to a target type.
     *
     * @param block the block receiving any conversion operations
     * @param value the value to convert
     * @param target the target type
     * @return the converted value, or {@code value} if no conversion is required
     */
    public static Value convert(Block.Builder block, Value value, CodeType target) {
        if (value.type().equals(target)) return value;
        if (target instanceof PrimitiveType primitive) {
            if (primitive.isVoid()) return value;
            PrimitiveType sourcePrimitive = primitiveType(value.type());
            value = sourcePrimitive != null && value.type() instanceof ClassType
                    ? unbox(block, value, sourcePrimitive)
                    : unbox(block, value, primitive);
            return value.type().equals(primitive) ? value : block.add(JavaOp.conv(primitive, value));
        }
        if (value.type() instanceof PrimitiveType primitive) {
            ClassType wrapper = primitive.box().orElseThrow();
            value = block.add(JavaOp.invoke(MethodRef.method(wrapper, "valueOf", wrapper, primitive), value));
        }
        return value;
    }

    private static CodeType conversionTarget(Op op, int operand) {
        return switch (op) {
            case CoreOp.VarOp var when operand == 0 -> var.varValueType();
            case CoreOp.VarAccessOp.VarStoreOp store when operand == 1 -> store.varType().valueType();
            case CoreOp.ReturnOp _ when operand == 0 ->
                    op.ancestorBlock().ancestorBody().bodySignature().returnType();
            case CoreOp.YieldOp _ when operand == 0 -> javaYieldTarget(op);
            case JavaOp.YieldOp _ when operand == 0 -> javaYieldTarget(op);
            case JavaOp.FieldAccessOp.FieldStoreOp store when operand == op.operands().size() - 1 ->
                    store.fieldReference().type();
            case JavaOp.ArrayAccessOp.ArrayStoreOp _ when operand == 1 -> JavaType.INT;
            case JavaOp.ArrayAccessOp.ArrayStoreOp _ when operand == 2 ->
                    ((ArrayType) op.operands().getFirst().type()).componentType();
            case JavaOp.ArrayAccessOp.ArrayLoadOp _ when operand == 1 -> JavaType.INT;
            case JavaOp.InvokeOp invoke -> invocationTarget(invoke, operand);
            case JavaOp.NewOp new_ -> constructorTarget(new_, operand);
            case JavaOp.PatternOps.MatchOp match when operand == 0 -> patternTarget(match);
            case JavaOp.ArithmeticOperation arithmetic -> arithmetic.functionType().parameterTypes().get(operand);
            default -> null;
        };
    }

    private static CodeType javaYieldTarget(Op op) {
        CodeType target = op.ancestorBlock().ancestorBody().bodySignature().returnType();
        return target instanceof JavaType ? target : null;
    }

    private static CodeType invocationTarget(JavaOp.InvokeOp op, int operand) {
        int offset = op.hasReceiver() ? 1 : 0;
        if (offset != 0 && operand == 0) return op.invokeReference().refType();
        List<CodeType> parameters = op.invokeReference().signature().parameterTypes();
        int argument = operand - offset;
        CodeType target = parameters.get(Math.min(argument, parameters.size() - 1));
        return op.isVarArgs() && argument >= parameters.size() - 1
                ? ((ArrayType) target).componentType() : target;
    }

    private static CodeType constructorTarget(JavaOp.NewOp op, int operand) {
        List<CodeType> parameters = op.constructorReference().signature().parameterTypes();
        CodeType target = parameters.get(Math.min(operand, parameters.size() - 1));
        return op.isVarargs() && operand >= parameters.size() - 1
                ? ((ArrayType) target).componentType() : target;
    }

    private static CodeType patternTarget(JavaOp.PatternOps.MatchOp op) {
        // A match's input target is declared by the root pattern yielded from
        // its pattern-producing body.
        CoreOp.YieldOp yield = (CoreOp.YieldOp) op.patternBody().entryBlock().terminatingOp();
        if (!(yield.yieldValue() instanceof Op.Result result)) return null;
        CodeType target = switch (result.op()) {
            case JavaOp.PatternOps.TypePatternOp pattern -> pattern.targetType();
            case JavaOp.PatternOps.RecordPatternOp pattern -> pattern.targetType();
            default -> null;
        };
        return target instanceof ClassType ? target : null;
    }

    private static PrimitiveType primitiveType(CodeType type) {
        return switch (type) {
            case PrimitiveType p -> p;
            case ClassType c -> c.unbox().orElse(null);
            default -> null;
        };
    }

    private static boolean requiresConversion(CodeType source, CodeType target) {
        return !source.equals(target) && (source instanceof PrimitiveType || target instanceof PrimitiveType);
    }

    private static Value unbox(Block.Builder block, Value value, PrimitiveType primitive) {
        if (value.type() instanceof PrimitiveType) return value;
        ClassType wrapper = primitive.box().orElseThrow();
        value = value.type().equals(wrapper) ? value : block.add(JavaOp.cast(wrapper, value));
        String name = switch (primitive) {
            case PrimitiveType _ when primitive == JavaType.BOOLEAN -> "booleanValue";
            case PrimitiveType _ when primitive == JavaType.BYTE -> "byteValue";
            case PrimitiveType _ when primitive == JavaType.SHORT -> "shortValue";
            case PrimitiveType _ when primitive == JavaType.CHAR -> "charValue";
            case PrimitiveType _ when primitive == JavaType.INT -> "intValue";
            case PrimitiveType _ when primitive == JavaType.LONG -> "longValue";
            case PrimitiveType _ when primitive == JavaType.FLOAT -> "floatValue";
            case PrimitiveType _ when primitive == JavaType.DOUBLE -> "doubleValue";
            default -> throw new IllegalArgumentException("not a boxed primitive: " + primitive);
        };
        return block.add(JavaOp.invoke(MethodRef.method(wrapper, name, primitive), value));
    }
}
