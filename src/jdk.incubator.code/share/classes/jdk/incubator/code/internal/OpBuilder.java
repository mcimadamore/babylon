/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
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

package jdk.incubator.code.internal;

import jdk.incubator.code.*;
import jdk.incubator.code.dialect.core.CoreType;
import jdk.incubator.code.dialect.core.FunctionType;
import jdk.incubator.code.dialect.java.*;
import jdk.incubator.code.extern.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static jdk.incubator.code.dialect.core.CoreOp.*;
import static jdk.incubator.code.dialect.core.CoreType.functionType;
import static jdk.incubator.code.dialect.java.JavaOp.*;
import static jdk.incubator.code.dialect.java.JavaType.*;

/**
 * A transformer of code models to models that build them.
 * <p>
 * A building code model when executed will construct the same code model it was transformed from.
 * Such a building code model could be transformed to bytecode and stored in class files.
 */
public class OpBuilder {

    static final JavaType J_C_E_EXTERNALIZED_OP = type(ExternalizedOp.class);

    static final MethodRef DIALECT_FACTORY_OP_FACTORY = MethodRef.method(DialectFactory.class, "opFactory",
            OpFactory.class);

    static final MethodRef DIALECT_FACTORY_TYPE_ELEMENT_FACTORY = MethodRef.method(DialectFactory.class, "typeElementFactory",
            TypeElementFactory.class);

    static final MethodRef OP_FACTORY_CONSTRUCT = MethodRef.method(OpFactory.class, "constructOp",
            Op.class, ExternalizedOp.class);

    static final MethodRef TYPE_ELEMENT_FACTORY_CONSTRUCT = MethodRef.method(TypeElementFactory.class, "constructType",
            TypeElement.class, ExternalizedTypeElement.class);

    static final MethodRef BODY_BUILDER_OF = MethodRef.method(Body.Builder.class, "of",
            Body.Builder.class, Body.Builder.class, FunctionType.class);

    static final MethodRef BODY_BUILDER_ENTRY_BLOCK = MethodRef.method(Body.Builder.class, "entryBlock",
            Block.Builder.class);

    // instance varargs
    static final MethodRef BLOCK_BUILDER_SUCCESSOR = MethodRef.method(Block.Builder.class, "successor",
            Block.Reference.class, Value[].class);

    static final MethodRef BLOCK_BUILDER_OP = MethodRef.method(Block.Builder.class, "op",
            Op.Result.class, Op.class);

    // instance varargs
    static final MethodRef BLOCK_BUILDER_BLOCK = MethodRef.method(Block.Builder.class, "block",
            Block.Builder.class, TypeElement[].class);

    static final MethodRef BLOCK_BUILDER_PARAMETER = MethodRef.method(Block.Builder.class, "parameter",
            Block.Parameter.class, TypeElement.class);

    // static varargs
    static final MethodRef FUNCTION_TYPE_FUNCTION_TYPE = MethodRef.method(CoreType.class, "functionType",
            FunctionType.class, TypeElement.class, TypeElement[].class);


    static final JavaType J_U_LIST = type(List.class);

    static final MethodRef LIST_OF_ARRAY = MethodRef.method(J_U_LIST, "of",
            J_U_LIST, array(J_L_OBJECT, 1));

    static final JavaType J_U_MAP = type(Map.class);

    static final JavaType J_U_MAP_ENTRY = type(Map.Entry.class);

    static final MethodRef MAP_ENTRY = MethodRef.method(J_U_MAP, "entry",
            J_U_MAP, J_L_OBJECT, J_L_OBJECT);

    static final MethodRef MAP_OF_ARRAY = MethodRef.method(J_U_MAP, "of",
            J_U_MAP, array(J_U_MAP_ENTRY, 1));


    static final JavaType EX_TYPE_ELEM = type(ExternalizedTypeElement.class);

    static final MethodRef EX_TYPE_ELEM_OF_LIST = MethodRef.method(EX_TYPE_ELEM, "of",
            EX_TYPE_ELEM, J_L_STRING, J_U_LIST);


    static final JavaType J_C_LOCATION = type(Location.class);

    static final MethodRef LOCATION_FROM_STRING = MethodRef.method(J_C_LOCATION, "fromString",
            J_C_LOCATION, J_L_STRING);

    static final FunctionType EXTERNALIZED_OP_F_TYPE = functionType(
            J_C_E_EXTERNALIZED_OP,
            J_L_STRING,
            J_C_LOCATION,
            J_U_LIST,
            J_U_LIST,
            type(TypeElement.class),
            J_U_MAP,
            J_U_LIST);

    static final FunctionType BUILDER_F_TYPE = functionType(type(Op.class));


    final Map<Value, Value> valueMap;

    final Map<Block, Value> blockMap;

    final Map<ExternalizedTypeElement, Value> exTypeElementMap;

    final Map<TypeElement, Value> typeElementMap;

    final Block.Builder builder;

    final Value dialectFactory;

    final Value opFactory;

    final Value typeElementFactory;

    /**
     * Transform the given code model to one that builds it.
     * <p>
     * This method initially applies the function {@code dialectFactoryF} to
     * the block builder that is used to build resulting code model. The result
     * is a dialect factory value which is subsequently used to build operations
     * that construct type elements and operations present in the given code model.
     *
     * @param op the code model.
     * @param dialectFactoryF a function that builds code items to produce a dialect factory value.
     * @return the building code model.
     */
    public static FuncOp createBuilderFunction(Op op, Function<Block.Builder, Value> dialectFactoryF) {
        return new OpBuilder(dialectFactoryF).build(op);
    }

    OpBuilder(Function<Block.Builder, Value> dialectFactoryF) {
        this.valueMap = new HashMap<>();
        this.blockMap = new HashMap<>();
        this.exTypeElementMap = new HashMap<>();
        this.typeElementMap = new HashMap<>();

        Body.Builder body = Body.Builder.of(null, BUILDER_F_TYPE);
        this.builder = body.entryBlock();
        this.dialectFactory = dialectFactoryF.apply(builder);
        this.opFactory = builder.op(invoke(DIALECT_FACTORY_OP_FACTORY, dialectFactory));
        this.typeElementFactory = builder.op(invoke(DIALECT_FACTORY_TYPE_ELEMENT_FACTORY, dialectFactory));
    }

    FuncOp build(Op op) {
        Value ancestorBody = builder.op(constant(type(Body.Builder.class), null));
        Value result = buildOp(ancestorBody, op);
        builder.op(return_(result));

        return func("builder." + op.opName(), builder.parentBody());
    }


    Value buildOp(Value ancestorBody, Op inputOp) {
        List<Value> bodies = new ArrayList<>();
        for (Body inputBody : inputOp.bodies()) {
            Value body = buildBody(ancestorBody, inputBody);
            bodies.add(body);
        }

        List<Value> operands = new ArrayList<>();
        for (Value inputOperand : inputOp.operands()) {
            Value operand = valueMap.get(inputOperand);
            operands.add(operand);
        }

        List<Value> successors = new ArrayList<>();
        for (Block.Reference inputSuccessor : inputOp.successors()) {
            List<Value> successorArgs = new ArrayList<>();
            for (Value inputOperand : inputSuccessor.arguments()) {
                Value operand = valueMap.get(inputOperand);
                successorArgs.add(operand);
            }
            Value referencedBlock = blockMap.get(inputSuccessor.targetBlock());

            List<Value> args = new ArrayList<>();
            args.add(referencedBlock);
            args.addAll(successorArgs);
            Value successor = builder.op(invoke(
                    InvokeOp.InvokeKind.INSTANCE, true,
                    BLOCK_BUILDER_SUCCESSOR.type().returnType(),
                    BLOCK_BUILDER_SUCCESSOR, args));
            successors.add(successor);
        }

        Value opDef = buildOpDefinition(
                inputOp,
                inputOp.opName(),
                inputOp.location(),
                operands,
                successors,
                inputOp.resultType(),
                inputOp.externalize(),
                bodies);
        return builder.op(invoke(OP_FACTORY_CONSTRUCT, opFactory, opDef));
    }


    Value buildOpDefinition(Op inputOp,
                            String name,
                            Location location,
                            List<Value> operands,
                            List<Value> successors,
                            TypeElement resultType,
                            Map<String, Object> attributes,
                            List<Value> bodies) {
        List<Value> args = List.of(
                builder.op(constant(J_L_STRING, name)),
                buildLocation(location),
                buildList(type(Value.class), operands),
                buildList(type(Block.Reference.class), successors),
                buildType(resultType),
                buildAttributeMap(inputOp, attributes),
                buildList(type(Body.Builder.class), bodies));
        return builder.op(new_(ConstructorRef.constructor(EXTERNALIZED_OP_F_TYPE), args));
    }

    Value buildLocation(Location l) {
        if (l == null) {
            return builder.op(constant(J_C_LOCATION, null));
        } else {
            return builder.op(invoke(LOCATION_FROM_STRING,
                    builder.op(constant(J_L_STRING, l.toString()))));
        }
    }

    Value buildBody(Value ancestorBodyValue, Body inputBody) {
        Value yieldType = buildType(inputBody.yieldType());
        Value bodyType = builder.op(invoke(
                InvokeOp.InvokeKind.STATIC, true,
                FUNCTION_TYPE_FUNCTION_TYPE.type().returnType(),
                FUNCTION_TYPE_FUNCTION_TYPE, List.of(yieldType)));
        Value body = builder.op(invoke(BODY_BUILDER_OF, ancestorBodyValue, bodyType));

        Value entryBlock = null;
        for (Block inputBlock : inputBody.blocks()) {
            Value block;
            if (inputBlock.isEntryBlock()) {
                block = entryBlock = builder.op(invoke(BODY_BUILDER_ENTRY_BLOCK, body));
            } else {
                assert entryBlock != null;
                block = builder.op(invoke(InvokeOp.InvokeKind.INSTANCE, true,
                        BLOCK_BUILDER_BLOCK.type().returnType(),
                        BLOCK_BUILDER_BLOCK, List.of(entryBlock)));
            }
            blockMap.put(inputBlock, block);

            for (Block.Parameter inputP : inputBlock.parameters()) {
                Value type = buildType(inputP.type());
                Value blockParameter = builder.op(invoke(BLOCK_BUILDER_PARAMETER, block, type));
                valueMap.put(inputP, blockParameter);
            }
        }

        for (Block inputBlock : inputBody.blocks()) {
            Value block = blockMap.get(inputBlock);
            for (Op inputOp : inputBlock.ops()) {
                Value op = buildOp(body, inputOp);
                Value result = builder.op(invoke(BLOCK_BUILDER_OP, block, op));
                valueMap.put(inputOp.result(), result);
            }
        }

        return body;
    }

    Value buildType(TypeElement _t) {
        return typeElementMap.computeIfAbsent(_t, t -> {
            Value exTypeElem = buildExternalizedType(t.externalize());
            return builder.op(invoke(TYPE_ELEMENT_FACTORY_CONSTRUCT, typeElementFactory, exTypeElem));
        });
    }

    Value buildExternalizedType(ExternalizedTypeElement e) {
        // Cannot use computeIfAbsent due to recursion
        if (exTypeElementMap.get(e) instanceof Value v) {
            return v;
        }

        List<Value> arguments = new ArrayList<>();
        for (ExternalizedTypeElement a : e.arguments()) {
            arguments.add(buildExternalizedType(a));
        }

        Value identifier = builder.op(constant(J_L_STRING, e.identifier()));
        Value ve;
        if (e.arguments().size() < 5) {
            MethodRef elemOf = MethodRef.method(EX_TYPE_ELEM, "of",
                    EX_TYPE_ELEM, Stream.concat(Stream.of(J_L_STRING),
                            Collections.nCopies(e.arguments().size(), EX_TYPE_ELEM).stream()).toList());
            arguments.addFirst(identifier);
            ve = builder.op(invoke(elemOf, arguments));
        } else {
            Value list = buildList(EX_TYPE_ELEM, arguments);
            ve = builder.op(invoke(EX_TYPE_ELEM_OF_LIST, identifier, list));
        }

        exTypeElementMap.put(e, ve);
        return ve;
    }

    Value buildAttributeMap(Op inputOp, Map<String, Object> attributes) {
        List<Value> keysAndValues = new ArrayList<>();
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            Value key = builder.op(constant(J_L_STRING, entry.getKey()));
            Value value = buildAttributeValue(entry.getValue());
            keysAndValues.add(key);
            keysAndValues.add(value);
        }
        return buildMap(J_L_STRING, J_L_OBJECT, keysAndValues);
    }

    Value buildAttributeValue(Object value) {
        return switch (value) {
            case Boolean v -> {
                yield builder.op(constant(BOOLEAN, value));
            }
            case Byte v -> {
                yield builder.op(constant(BYTE, value));
            }
            case Short v -> {
                yield builder.op(constant(SHORT, value));
            }
            case Character v -> {
                yield builder.op(constant(CHAR, value));
            }
            case Integer v -> {
                yield builder.op(constant(INT, value));
            }
            case Long v -> {
                yield builder.op(constant(LONG, value));
            }
            case Float v -> {
                yield builder.op(constant(FLOAT, value));
            }
            case Double v -> {
                yield builder.op(constant(DOUBLE, value));
            }
            case Class<?> v -> {
                yield buildType(JavaType.type(v));
            }
            case String s -> {
                yield builder.op(constant(J_L_STRING, value));
            }
            case TypeElement f -> {
                yield buildType(f);
            }
            case InvokeOp.InvokeKind ik -> {
                FieldRef enumValueRef = FieldRef.field(InvokeOp.InvokeKind.class, ik.name(), InvokeOp.InvokeKind.class);
                yield builder.op(fieldLoad(enumValueRef));
            }
            case Object o when value == ExternalizedOp.NULL_ATTRIBUTE_VALUE -> {
                yield builder.op(fieldLoad(FieldRef.field(ExternalizedOp.class,
                        "NULL_ATTRIBUTE_VALUE", Object.class)));
            }
            default -> {
                // @@@ use the result of value.toString()?
                throw new UnsupportedOperationException("Unsupported attribute value: " + value);
            }
        };
    }


    Value buildMap(JavaType keyType, JavaType valueType, List<Value> keysAndValues) {
        JavaType mapType = parameterized(J_U_MAP, keyType, valueType);
        if (keysAndValues.size() < 21) {
            MethodRef mapOf = MethodRef.method(J_U_MAP, "of",
                    J_U_MAP, Collections.nCopies(keysAndValues.size(), J_L_OBJECT));
            return builder.op(invoke(mapType, mapOf, keysAndValues));
        } else {
            JavaType mapEntryType = parameterized(J_U_MAP_ENTRY, keyType, valueType);
            List<Value> elements = new ArrayList<>(keysAndValues.size() / 2);
            for (int i = 0; i < keysAndValues.size(); i += 2) {
                Value key = keysAndValues.get(i);
                Value value = keysAndValues.get(i + 1);
                Value entry = builder.op(invoke(mapEntryType, MAP_ENTRY, key, value));
                elements.add(entry);
            }
            Value array = buildArray(mapEntryType, elements);
            return builder.op(invoke(mapType, MAP_OF_ARRAY, array));
        }
    }


    Value buildList(JavaType elementType, List<Value> elements) {
        JavaType listType = parameterized(J_U_LIST, elementType);
        if (elements.size() < 11) {
            MethodRef listOf = MethodRef.method(J_U_LIST, "of",
                    J_U_LIST, Collections.nCopies(elements.size(), J_L_OBJECT));
            return builder.op(invoke(listType, listOf, elements));
        } else {
            Value array = buildArray(elementType, elements);
            return builder.op(invoke(listType, LIST_OF_ARRAY, array));
        }
    }


    Value buildArray(JavaType elementType, List<Value> elements) {
        Value array = builder.op(newArray(JavaType.array(elementType),
                builder.op(constant(INT, elements.size()))));
        for (int i = 0; i < elements.size(); i++) {
            builder.op(arrayStoreOp(
                    array,
                    builder.op(constant(INT, i)),
                    elements.get(i)));
        }
        return array;
    }
}
