/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 */

package jdk.incubator.code.dialect.java;

import jdk.incubator.code.Block;
import jdk.incubator.code.Body;
import jdk.incubator.code.CodeType;
import jdk.incubator.code.CodeTransformer;
import jdk.incubator.code.Op;
import jdk.incubator.code.Value;
import jdk.incubator.code.dialect.core.CoreOp;
import jdk.incubator.code.dialect.core.VarType;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds the implicit Java conversions required by a high-level Java code model.
 *
 * The transformer intentionally obtains conversion targets from the model: a
 * variable, field, array component, invocation signature, or enclosing body.
 * Numeric operators are the exception: their targets are specified by the JLS
 * promotion rules, since the current binary operation representation does not
 * carry a resolved operator signature.
 */
public final class ImplicitConversionTransformer implements CodeTransformer {

    private final Map<Op, Value> switchSelectorConversions = new IdentityHashMap<>();

    /** Creates an implicit-conversion lowering transformer. */
    public ImplicitConversionTransformer() {}

    @Override
    public void acceptBlock(Block.Builder block, Block input) {
        for (Op op : input.ops()) {
            if (!(op instanceof JavaOp.EqOp)) continue;
            SwitchMatch match = switchMatch(op);
            if (match == null || match.target().equals(match.selector().type())) continue;
            Value selector = block.context().getValue(match.selector());
            switchSelectorConversions.put(op, convert(block, selector, match.target()));
        }
        CodeTransformer.super.acceptBlock(block, input);
    }

    @Override
    public Block.Builder acceptOp(Block.Builder block, Op op) {
        List<Value> values = op.operands().stream()
                .map(v -> block.context().getValue(v)).toList();
        Op replacement = switch (op) {
            case CoreOp.VarOp v when !v.isUninitialized() -> var(block, v, values.getFirst());
            case CoreOp.VarAccessOp.VarStoreOp v ->
                    CoreOp.varStore(values.getFirst(), convert(block, values.get(1), v.varType().valueType()));
            case CoreOp.ReturnOp r when !values.isEmpty() ->
                    CoreOp.return_(convert(block, values.getFirst(), block.parentBody().bodySignature().returnType()));
            case CoreOp.YieldOp y when !values.isEmpty() ->
                    CoreOp.core_yield(convertYield(block, values.getFirst(), block.parentBody().bodySignature().returnType()));
            case JavaOp.YieldOp y ->
                    JavaOp.java_yield(convertYield(block, values.getFirst(), block.parentBody().bodySignature().returnType()));
            case JavaOp.FieldAccessOp.FieldStoreOp f -> fieldStore(block, f, values);
            case JavaOp.ArrayAccessOp.ArrayStoreOp a -> JavaOp.arrayStoreOp(
                    values.get(0), convert(block, values.get(1), JavaType.INT),
                    convert(block, values.get(2), ((ArrayType) values.get(0).type()).componentType()));
            case JavaOp.ArrayAccessOp.ArrayLoadOp a -> JavaOp.arrayLoadOp(
                    values.get(0), convert(block, values.get(1), JavaType.INT), a.resultType());
            case JavaOp.InvokeOp i -> invoke(block, i, values);
            case JavaOp.NewOp n -> new_(block, n, values);
            case JavaOp.PatternOps.MatchOp m -> match(block, m, values.getFirst());
            case JavaOp.BinaryOp b -> binary(block, b, values);
            case JavaOp.CompareOp c -> binary(block, c, values);
            case JavaOp.UnaryOp u -> unary(block, u, values.getFirst());
            default -> op;
        };
        Op.Result output = block.add(replacement);
        block.context().mapValue(op.result(), preconvert(block, op, output));
        return block;
    }

    private static Op fieldStore(Block.Builder block, JavaOp.FieldAccessOp.FieldStoreOp op, List<Value> values) {
        Value value = convert(block, values.getLast(), op.fieldReference().type());
        return values.size() == 1 ? JavaOp.fieldStore(op.fieldReference(), value) :
                JavaOp.fieldStore(op.fieldReference(), values.getFirst(), value);
    }

    private static Op var(Block.Builder block, CoreOp.VarOp op, Value init) {
        CodeType type = yieldedVariableType(op);
        return CoreOp.var(op.varName(), type, convert(block, init, type));
    }

    private static CodeType yieldedVariableType(CoreOp.VarOp op) {
        var body = op.ancestorBlock().ancestorBody();
        if (body.parent() instanceof JavaOp.EnhancedForOp enhancedFor && enhancedFor.initBody() == body) {
            CodeType type = enhancedFor.loopBody().entryBlock().parameters().getFirst().type();
            if (type instanceof VarType variable) return variable.valueType();
        }
        for (Op.Result use : op.result().uses()) {
            if (use.op() instanceof CoreOp.YieldOp) {
                CodeType type = use.op().ancestorBlock().ancestorBody().yieldType();
                if (type instanceof VarType variable) return variable.valueType();
            }
        }
        return op.varValueType();
    }

    private Op invoke(Block.Builder block, JavaOp.InvokeOp op, List<Value> values) {
        List<Value> args = new ArrayList<>(values);
        int offset = op.hasReceiver() ? 1 : 0;
        if (op.hasReceiver()) {
            args.set(0, convert(block, args.getFirst(), op.invokeReference().refType()));
        }
        List<CodeType> parameters = op.invokeReference().signature().parameterTypes();
        for (int i = offset; i < args.size(); i++) {
            CodeType target = parameters.get(Math.min(i - offset, parameters.size() - 1));
            if (op.isVarArgs() && i - offset >= parameters.size() - 1) {
                target = ((ArrayType) target).componentType();
            }
            args.set(i, convertedOperand(block, op, i, args.get(i), target));
        }
        return JavaOp.invoke(op.invokeKind(), op.isVarArgs(), op.resultType(), op.invokeReference(), args);
    }

    private Op new_(Block.Builder block, JavaOp.NewOp op, List<Value> values) {
        List<Value> args = new ArrayList<>(values);
        List<CodeType> parameters = op.constructorReference().signature().parameterTypes();
        for (int i = 0; i < args.size(); i++) {
            CodeType target = parameters.get(Math.min(i, parameters.size() - 1));
            if (op.isVarargs() && i >= parameters.size() - 1) {
                target = ((ArrayType) target).componentType();
            }
            args.set(i, convertedOperand(block, op, i, args.get(i), target));
        }
        return JavaOp.new_(op.isVarargs(), op.resultType(), op.constructorReference(), args);
    }

    private Op match(Block.Builder block, JavaOp.PatternOps.MatchOp op, Value value) {
        CodeType target = patternTarget(op);
        value = convertedOperand(block, op, 0, value, target);
        return JavaOp.match(value,
                op.patternBody().transform(block.context(), this),
                op.matchBody().transform(block.context(), this));
    }

    private static CodeType patternTarget(JavaOp.PatternOps.MatchOp op) {
        CoreOp.YieldOp yield = (CoreOp.YieldOp) op.patternBody().entryBlock().terminatingOp();
        if (!(yield.yieldValue() instanceof Op.Result result)) return null;
        CodeType target = switch (result.op()) {
            case JavaOp.PatternOps.TypePatternOp pattern -> pattern.targetType();
            case JavaOp.PatternOps.RecordPatternOp pattern -> pattern.targetType();
            default -> null;
        };
        return target instanceof ClassType ? target : null;
    }

    private Op binary(Block.Builder block, JavaOp.ArithmeticOperation op, List<Value> values) {
        List<CodeType> targets = arithmeticTargets(op);
        Value left = convertedOperand(block, op, 0, values.get(0), targets.get(0));
        Value right = convertedOperand(block, op, 1, values.get(1), targets.get(1));
        if (op instanceof JavaOp.LshlOp || op instanceof JavaOp.AshrOp || op instanceof JavaOp.LshrOp) {
            left = unaryNumeric(block, left);
            right = unaryNumeric(block, right);
        } else if (op instanceof JavaOp.AndOp || op instanceof JavaOp.OrOp || op instanceof JavaOp.XorOp) {
            if (primitiveType(left.type()) == JavaType.BOOLEAN && primitiveType(right.type()) == JavaType.BOOLEAN) {
                left = convert(block, left, JavaType.BOOLEAN);
                right = convert(block, right, JavaType.BOOLEAN);
            } else {
                Value[] promoted = binaryNumeric(block, left, right);
                left = promoted[0]; right = promoted[1];
            }
        } else if (switchMatch(op) == null &&
                (!(op instanceof JavaOp.EqOp || op instanceof JavaOp.NeqOp) ||
                primitiveType(left.type()) != null || primitiveType(right.type()) != null)) {
            Value[] promoted = binaryNumeric(block, left, right);
            left = promoted[0]; right = promoted[1];
        }
        return binary(op, left, right);
    }

    private Op unary(Block.Builder block, JavaOp.UnaryOp op, Value value) {
        CodeType target = unaryTarget(op, op.operand().type());
        value = convertedOperand(block, op, 0, value, target);
        if (op instanceof JavaOp.NotOp) {
            value = convert(block, value, JavaType.BOOLEAN);
        } else {
            value = unaryNumeric(block, value);
        }
        return op instanceof JavaOp.NegOp ? JavaOp.neg(value) :
                op instanceof JavaOp.ComplOp ? JavaOp.compl(value) : JavaOp.not(value);
    }

    private static Op binary(JavaOp.ArithmeticOperation op, Value left, Value right) {
        return switch (op) {
            case JavaOp.AddOp _ -> JavaOp.add(left, right);
            case JavaOp.SubOp _ -> JavaOp.sub(left, right);
            case JavaOp.MulOp _ -> JavaOp.mul(left, right);
            case JavaOp.DivOp _ -> JavaOp.div(left, right);
            case JavaOp.ModOp _ -> JavaOp.mod(left, right);
            case JavaOp.OrOp _ -> JavaOp.or(left, right);
            case JavaOp.AndOp _ -> JavaOp.and(left, right);
            case JavaOp.XorOp _ -> JavaOp.xor(left, right);
            case JavaOp.LshlOp _ -> JavaOp.lshl(left, right);
            case JavaOp.AshrOp _ -> JavaOp.ashr(left, right);
            case JavaOp.LshrOp _ -> JavaOp.lshr(left, right);
            case JavaOp.EqOp _ -> JavaOp.eq(left, right);
            case JavaOp.NeqOp _ -> JavaOp.neq(left, right);
            case JavaOp.LtOp _ -> JavaOp.lt(left, right);
            case JavaOp.LeOp _ -> JavaOp.le(left, right);
            case JavaOp.GtOp _ -> JavaOp.gt(left, right);
            case JavaOp.GeOp _ -> JavaOp.ge(left, right);
            default -> throw new IllegalArgumentException("not a binary operation: " + op);
        };
    }

    private static Value[] binaryNumeric(Block.Builder block, Value left, Value right) {
        PrimitiveType l = primitiveType(left.type());
        PrimitiveType r = primitiveType(right.type());
        if (l == null || r == null) return new Value[] { left, right };
        PrimitiveType target = l == JavaType.DOUBLE || r == JavaType.DOUBLE ? JavaType.DOUBLE :
                l == JavaType.FLOAT || r == JavaType.FLOAT ? JavaType.FLOAT :
                l == JavaType.LONG || r == JavaType.LONG ? JavaType.LONG : JavaType.INT;
        return new Value[] { convert(block, left, target), convert(block, right, target) };
    }

    private static Value unaryNumeric(Block.Builder block, Value value) {
        PrimitiveType type = primitiveType(value.type());
        return type == JavaType.BYTE || type == JavaType.SHORT || type == JavaType.CHAR
                ? convert(block, value, JavaType.INT) : unbox(block, value);
    }

    private static PrimitiveType primitiveType(CodeType type) {
        return switch (type) {
            case PrimitiveType p -> p;
            case ClassType c -> c.unbox().orElse(null);
            default -> null;
        };
    }

    private static Value convert(Block.Builder block, Value value, CodeType target) {
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
        // The model does not provide an assignability relation for reference
        // descriptors.  In particular, erasure-compatible and widening
        // conversions need no cast.  Preserve an explicit Java cast emitted by
        // ReflectMethods, but do not invent one for an implicit conversion.
        return value;
    }

    private static Value convertYield(Block.Builder block, Value value, CodeType target) {
        return target instanceof JavaType ? convert(block, value, target) : value;
    }

    private Value preconvert(Block.Builder block, Op producer, Value output) {
        // A source temporary has one direct operand use.  Var is a declaration
        // boundary; its initializer is converted to the variable's type.
        if (producer.result().uses().size() != 1) return output;

        Op consumer = producer.result().uses().getFirst().op();
        if (consumer instanceof CoreOp.VarOp || consumer.ancestorBlock() != producer.ancestorBlock()) return output;

        int operand = -1;
        for (int i = 0; i < consumer.operands().size(); i++) {
            if (consumer.operands().get(i) != producer.result()) continue;
            if (operand != -1) return output;
            operand = i;
        }
        if (operand == -1) return output;

        List<CodeType> targets = switch (consumer) {
            case JavaOp.InvokeOp invoke -> invocationTargets(invoke);
            case JavaOp.NewOp new_ -> constructorTargets(new_);
            case JavaOp.PatternOps.MatchOp match ->
                    java.util.Collections.singletonList(patternTarget(match));
            case JavaOp.UnaryOp unary -> java.util.Collections.singletonList(unaryTarget(unary, unary.operand().type()));
            case JavaOp.ArithmeticOperation arithmetic -> arithmeticTargets(arithmetic);
            default -> List.of();
        };
        if (operand >= targets.size()) return output;
        if (consumer instanceof JavaOp.ArithmeticOperation arithmetic &&
                operand == 0 && isCompoundStoreback(arithmetic)) return output;
        CodeType target = targets.get(operand);
        return target != null && requiresConversion(output.type(), target)
                ? convert(block, output, target) : output;
    }

    private Value convertedOperand(Block.Builder block, Op op, int index, Value value, CodeType target) {
        SwitchMatch match = switchMatch(op);
        if (match != null && index == match.selectorOperand()) {
            Value converted = switchSelectorConversions.get(op);
            if (converted != null) return converted;
        }
        return target == null ? value : convert(block, value, target);
    }

    private static List<CodeType> invocationTargets(JavaOp.InvokeOp op) {
        List<CodeType> targets = new ArrayList<>();
        int offset = op.hasReceiver() ? 1 : 0;
        if (offset != 0) targets.add(op.invokeReference().refType());
        List<CodeType> parameters = op.invokeReference().signature().parameterTypes();
        for (int i = offset; i < op.operands().size(); i++) {
            int argument = i - offset;
            CodeType target = parameters.get(Math.min(argument, parameters.size() - 1));
            targets.add(op.isVarArgs() && argument >= parameters.size() - 1
                    ? ((ArrayType) target).componentType() : target);
        }
        return targets;
    }

    private static List<CodeType> constructorTargets(JavaOp.NewOp op) {
        List<CodeType> parameters = op.constructorReference().signature().parameterTypes();
        List<CodeType> targets = new ArrayList<>();
        for (int i = 0; i < op.operands().size(); i++) {
            CodeType target = parameters.get(Math.min(i, parameters.size() - 1));
            targets.add(op.isVarargs() && i >= parameters.size() - 1
                    ? ((ArrayType) target).componentType() : target);
        }
        return targets;
    }

    private static List<CodeType> arithmeticTargets(JavaOp.ArithmeticOperation op) {
        SwitchMatch match = switchMatch(op);
        if (match != null) return List.of(match.target(), match.target());

        CodeType left = op.operands().get(0).type();
        CodeType right = op.operands().get(1).type();
        if (op instanceof JavaOp.LshlOp || op instanceof JavaOp.AshrOp || op instanceof JavaOp.LshrOp) {
            return List.of(unaryPromotion(left), unaryPromotion(right));
        }
        if (op instanceof JavaOp.AndOp || op instanceof JavaOp.OrOp || op instanceof JavaOp.XorOp) {
            if (primitiveType(left) == JavaType.BOOLEAN && primitiveType(right) == JavaType.BOOLEAN) {
                return List.of(JavaType.BOOLEAN, JavaType.BOOLEAN);
            }
        }
        if ((op instanceof JavaOp.EqOp || op instanceof JavaOp.NeqOp) &&
                (primitiveType(left) == null || primitiveType(right) == null)) {
            return java.util.Arrays.asList(null, null);
        }
        PrimitiveType target = binaryPromotion(left, right);
        return target == null ? java.util.Arrays.asList(null, null) : List.of(target, target);
    }

    private record SwitchMatch(Value selector, CodeType target, int selectorOperand) {}

    private static SwitchMatch switchMatch(Op op) {
        if (!(op instanceof JavaOp.EqOp)) return null;

        Body body = op.ancestorBlock().ancestorBody();
        while (body.parent() instanceof JavaOp.ConditionalOrOp conditionalOr) {
            body = conditionalOr.ancestorBody();
        }
        if (!(body.parent() instanceof JavaOp.JavaSwitchOp javaSwitch)) {
            return null;
        }
        int bodyIndex = javaSwitch.bodies().indexOf(body);
        if (bodyIndex < 0 || (bodyIndex & 1) != 0) return null;

        Value selector = body.entryBlock().parameters().getFirst();
        CodeType selectorType = selector.type();
        CodeType target = selectorType.equals(JavaType.J_L_CHARACTER) ||
                selectorType.equals(JavaType.J_L_BYTE) ||
                selectorType.equals(JavaType.J_L_SHORT) ||
                selectorType.equals(JavaType.J_L_INTEGER)
                ? primitiveType(selectorType) : selectorType;
        // ReflectMethods.processConstantLabel emits eq(selector, labelValue).
        return new SwitchMatch(selector, target, 0);
    }

    private static CodeType unaryTarget(JavaOp.UnaryOp op, CodeType operand) {
        return op instanceof JavaOp.NotOp ? JavaType.BOOLEAN : unaryPromotion(operand);
    }

    private static PrimitiveType binaryPromotion(CodeType left, CodeType right) {
        PrimitiveType l = primitiveType(left);
        PrimitiveType r = primitiveType(right);
        if (l == null || r == null) return null;
        return l == JavaType.DOUBLE || r == JavaType.DOUBLE ? JavaType.DOUBLE :
                l == JavaType.FLOAT || r == JavaType.FLOAT ? JavaType.FLOAT :
                l == JavaType.LONG || r == JavaType.LONG ? JavaType.LONG : JavaType.INT;
    }

    private static CodeType unaryPromotion(CodeType type) {
        PrimitiveType primitive = primitiveType(type);
        if (primitive == null) return null;
        return primitive == JavaType.BYTE || primitive == JavaType.SHORT || primitive == JavaType.CHAR
                ? JavaType.INT : primitive;
    }

    private static boolean requiresConversion(CodeType source, CodeType target) {
        return !source.equals(target) && (source instanceof PrimitiveType || target instanceof PrimitiveType);
    }

    private static boolean isCompoundStoreback(JavaOp.ArithmeticOperation op) {
        return switch (op.operands().getFirst()) {
            case Op.Result result when result.op() instanceof CoreOp.VarAccessOp.VarLoadOp load ->
                    op.result().uses().stream().anyMatch(use ->
                            use.op() instanceof CoreOp.VarAccessOp.VarStoreOp store &&
                                    store.storeOperand() == op.result() && store.varOperand() == load.varOperand());
            case Op.Result result when result.op() instanceof JavaOp.FieldAccessOp.FieldLoadOp load ->
                    op.result().uses().stream().anyMatch(use ->
                            use.op() instanceof JavaOp.FieldAccessOp.FieldStoreOp store &&
                                    store.valueOperand() == op.result() &&
                                    store.fieldReference().equals(load.fieldReference()) &&
                                    store.receiverOperand() == load.receiverOperand());
            case Op.Result result when result.op() instanceof JavaOp.ArrayAccessOp.ArrayLoadOp load ->
                    op.result().uses().stream().anyMatch(use ->
                            use.op() instanceof JavaOp.ArrayAccessOp.ArrayStoreOp store &&
                                    store.valueOperand() == op.result() &&
                                    store.arrayOperand() == load.arrayOperand() &&
                                    store.indexOperand() == load.indexOperand());
            default -> false;
        };
    }

    private static Value unbox(Block.Builder block, Value value) {
        PrimitiveType primitive = primitiveType(value.type());
        if (primitive == null || value.type() instanceof PrimitiveType) return value;
        return unbox(block, value, primitive);
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
