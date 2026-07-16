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
import jdk.incubator.code.CodeType;
import jdk.incubator.code.CodeTransformer;
import jdk.incubator.code.Op;
import jdk.incubator.code.Value;
import jdk.incubator.code.dialect.core.CoreOp;
import jdk.incubator.code.dialect.core.VarType;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the implicit Java conversions required by a high-level Java code model.
 *
 * The transformer intentionally obtains conversion targets from the model: a
 * variable, field, array component, invocation signature, or enclosing body.
 * Operator conversion targets are obtained from the resolved function type
 * carried by each unary and binary operation.
 */
public final class ImplicitConversionTransformer implements CodeTransformer {

    /** Creates an implicit-conversion lowering transformer. */
    public ImplicitConversionTransformer() {}

    @Override
    public void acceptBlock(Block.Builder block, Block input) {
        for (Op op : input.ops()) {
            if (!(op instanceof JavaOp.ArithmeticOperation arithmetic)) continue;
            List<CodeType> targets = arithmetic.functionType().parameterTypes();
            for (int i = 0; i < op.operands().size(); i++) {
                Value operand = op.operands().get(i);
                if (!(operand instanceof Block.Parameter) ||
                        !requiresConversion(operand.type(), targets.get(i))) continue;
                Value value = block.context().getValue(operand);
                block.context().mapValue(operand, convert(block, value, targets.get(i)));
            }
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
        List<CodeType> targets = op.functionType().parameterTypes();
        Value left = convertedOperand(block, op, 0, values.get(0), targets.get(0));
        Value right = convertedOperand(block, op, 1, values.get(1), targets.get(1));
        return binary(op, left, right);
    }

    private Op unary(Block.Builder block, JavaOp.UnaryOp op, Value value) {
        CodeType target = op.functionType().parameterTypes().getFirst();
        value = convertedOperand(block, op, 0, value, target);
        return op instanceof JavaOp.NegOp ? JavaOp.neg(op.functionType(), value) :
                op instanceof JavaOp.PosOp ? JavaOp.pos(op.functionType(), value) :
                op instanceof JavaOp.ComplOp ? JavaOp.compl(op.functionType(), value) :
                JavaOp.not(op.functionType(), value);
    }

    private static Op binary(JavaOp.ArithmeticOperation op, Value left, Value right) {
        return switch (op) {
            case JavaOp.AddOp _ -> JavaOp.add(op.functionType(), left, right);
            case JavaOp.SubOp _ -> JavaOp.sub(op.functionType(), left, right);
            case JavaOp.MulOp _ -> JavaOp.mul(op.functionType(), left, right);
            case JavaOp.DivOp _ -> JavaOp.div(op.functionType(), left, right);
            case JavaOp.ModOp _ -> JavaOp.mod(op.functionType(), left, right);
            case JavaOp.OrOp _ -> JavaOp.or(op.functionType(), left, right);
            case JavaOp.AndOp _ -> JavaOp.and(op.functionType(), left, right);
            case JavaOp.XorOp _ -> JavaOp.xor(op.functionType(), left, right);
            case JavaOp.LshlOp _ -> JavaOp.lshl(op.functionType(), left, right);
            case JavaOp.AshrOp _ -> JavaOp.ashr(op.functionType(), left, right);
            case JavaOp.LshrOp _ -> JavaOp.lshr(op.functionType(), left, right);
            case JavaOp.EqOp _ -> JavaOp.eq(op.functionType(), left, right);
            case JavaOp.NeqOp _ -> JavaOp.neq(op.functionType(), left, right);
            case JavaOp.LtOp _ -> JavaOp.lt(op.functionType(), left, right);
            case JavaOp.LeOp _ -> JavaOp.le(op.functionType(), left, right);
            case JavaOp.GtOp _ -> JavaOp.gt(op.functionType(), left, right);
            case JavaOp.GeOp _ -> JavaOp.ge(op.functionType(), left, right);
            default -> throw new IllegalArgumentException("not a binary operation: " + op);
        };
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
            case JavaOp.ArithmeticOperation arithmetic -> arithmetic.functionType().parameterTypes();
            default -> List.of();
        };
        if (operand >= targets.size()) return output;
        CodeType target = targets.get(operand);
        return target != null && requiresConversion(output.type(), target)
                ? convert(block, output, target) : output;
    }

    private Value convertedOperand(Block.Builder block, Op op, int index, Value value, CodeType target) {
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
