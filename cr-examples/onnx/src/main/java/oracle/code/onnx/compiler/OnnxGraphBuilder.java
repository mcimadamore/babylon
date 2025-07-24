package oracle.code.onnx.compiler;

import jdk.incubator.code.Block;
import jdk.incubator.code.Block.Parameter;
import jdk.incubator.code.Op;
import jdk.incubator.code.OpTransformer;
import jdk.incubator.code.Quotable;
import jdk.incubator.code.Quoted;
import jdk.incubator.code.TypeElement;
import jdk.incubator.code.Value;
import jdk.incubator.code.dialect.core.CoreOp;
import jdk.incubator.code.dialect.core.CoreOp.FuncOp;
import jdk.incubator.code.dialect.core.CoreOp.ModuleOp;
import jdk.incubator.code.dialect.core.CoreOp.VarOp;
import jdk.incubator.code.dialect.core.CoreType;
import jdk.incubator.code.dialect.core.FunctionType;
import jdk.incubator.code.dialect.java.ClassType;
import jdk.incubator.code.dialect.java.FieldRef;
import jdk.incubator.code.dialect.java.JavaOp;
import jdk.incubator.code.dialect.java.JavaType;
import oracle.code.onnx.Tensor;
import oracle.code.onnx.ir.OnnxOp;
import oracle.code.onnx.ir.OnnxOp.OnnxParameter;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static oracle.code.onnx.compiler.OnnxTransformer.*;

public class OnnxGraphBuilder {

    Map<Value, Object> valueMap = new HashMap<>();
    Set<OnnxInitializer> initializers = new LinkedHashSet<>();
    Map<String, Integer> nameCounts = new HashMap<>();
    TypeConvertor tc = new TypeConvertor(MethodHandles.lookup());
    OnnxPartialEvaluator pe = new OnnxPartialEvaluator();
    FuncOp op;

    public OnnxGraphBuilder(Quoted q) {
        FuncOp func = toFunc(q);
        ModuleOp moduleOp = OnnxTransformer.collectModuleFunctions(MethodHandles.lookup(), func);
        func = moduleOp.functionTable().sequencedValues().getFirst();
        OnnxPartialEvaluator pe = new OnnxPartialEvaluator();
        pe.evaluate(MethodHandles.lookup(), func);
        int i = 0;
        List<OnnxValueInfo> inputs = new ArrayList<>();
        for (Parameter p : func.parameters()) {
            if (p.type() instanceof ClassType ct && ct.toClassName().equals(Tensor.class.getName())) {
                OnnxValueInfo valueInfo = new OnnxValueInfo("p" + i, p.type());
                inputs.add(valueInfo);
                valueMap.put(p, valueInfo);
            }
        }
        this.op = func;
    }

    public FuncOp op() {
        return op;
    }

    void toOnnxOpTransformer(Op op, Consumer<? super OnnxOperation> downstream) {
        if (!pe.unevaluatedOperations.contains(op)) {
            return;
        }
        switch (op) {
            // Transform invocation to ONNX operator to operation modeling the operator
            case JavaOp.InvokeOp io when io.invokeDescriptor().refType().equals(ONNX_OPERATORS_CLASS) -> {
                String operatorName = io.invokeDescriptor().name();
                Class<? extends OnnxOp> opClass = onnxOpClassFromName(operatorName);
                OnnxOp.OnnxSchema schema = schemaFromOnnxOpClass(opClass);

                List<OnnxConstant> opAttributes = new ArrayList<>();
                List<Object> attributeValues = pe.evaluatedAttributes.get(io);
                for (int i = 0 ; i < schema.attributes().size() ; i++) {
                    OnnxOp.OnnxAttribute attribute = schema.attributes().get(i);
                    Object attributeValue = attributeValues.get(i);
                    if (attribute.isOptional() && attributeValue instanceof Optional<?> o && o.isPresent()) {
                        opAttributes.add(new OnnxConstant(attribute.name(), attribute.type(), o.get()));
                    } else if (!attribute.isOptional()) {
                        opAttributes.add(new OnnxConstant(attribute.name(), attribute.type(), attributeValue));
                    }
                }

                List<OnnxValue> opInputs = new ArrayList<>();
                for (int i = 0; i < schema.inputs().size(); i++) {
                    OnnxOp.OnnxParameter p = schema.inputs().get(i);
                    Value v = io.operands().get(i);

                    switch (p.quantifier()) {
                        case REQUIRED -> {
                            opInputs.add((OnnxValue) valueMap.get(v));
                        }
                        case OPTIONAL -> {
                            // Evaluation of expressions Optional.empty and Optional.of() with symbolic values
                            if (v instanceof Op.Result r && r.op() instanceof JavaOp.InvokeOp optionalInvoke
                                    && optionalInvoke.invokeDescriptor().refType().equals(JavaType.type(Optional.class))) {
                                switch (optionalInvoke.invokeDescriptor().name()) {
                                    case "of" -> {
                                        opInputs.add((OnnxValue) valueMap.get(optionalInvoke.operands().getFirst()));
                                    }
                                    case "empty" -> {
                                        // do nothing
                                    }
                                    default -> throw new UnsupportedOperationException();
                                }
                            } else {
                                throw new UnsupportedOperationException();
                            }
                        }
                        case VARIADIC -> throw new UnsupportedOperationException(); // for now
                    }
                }

                List<OnnxValue> opOutputs = new ArrayList<>();
                for (int i = 0 ; i < schema.outputs().size() ; i++) {
                    OnnxParameter output = schema.outputs().get(i);
                    opOutputs.add(OnnxValue.of(output.name() + nextIndex(output.name())));
                }

                if (opOutputs.size() == 1) {
                    valueMap.put(op.result(), opOutputs.get(0));
                } else {
                    valueMap.put(op.result(), opOutputs);
                }

                OnnxOperation operation = new OnnxOperation(operatorName, opInputs, opOutputs, opAttributes);
                downstream.accept(operation);
            }
            // Transform access to the result of an operator that is a record access
            case JavaOp.InvokeOp io when
                    tc.recordComponentAccessToTupleIndex(io.invokeDescriptor()) instanceof Integer index -> {
                @SuppressWarnings("unchecked")
                List<OnnxValue> values = (List)valueMap.get(io.operands().getFirst());
                valueMap.put(io.result(), values.get(index));
            }
            case CoreOp.VarAccessOp.VarLoadOp vlp when vlp.operands().get(0) instanceof Op.Result r && r.op() instanceof VarOp vop && valueMap.containsKey(vop.initOperand()) -> {
                valueMap.put(vlp.result(), valueMap.get(vop.initOperand()));
            }
            case JavaOp.FieldAccessOp.FieldLoadOp flo -> {
                OnnxInitializer initializer = new OnnxInitializer(flo.resultType(), flo.fieldDescriptor());
                valueMap.put(flo.result(), initializer);
                initializers.add(initializer);
            }
            default -> { }
        }
    }

    int nextIndex(String name) {
        Integer index = nameCounts.getOrDefault(name, 0);
        nameCounts.put(name, index + 1);
        return index;
    }

    public OnnxGraph buildGraph() {
        pe.evaluate(MethodHandles.lookup(), op);
        List<OnnxValueInfo> inputs = new ArrayList<>();
        for (int i = 0 ; i < op.parameters().size() ; i++) {
            Parameter param = op.parameters().get(i);
            if (param.type() instanceof ClassType ct && ct.toClassName().equals(Tensor.class.getName())) {
                OnnxValueInfo valueInfo = new OnnxValueInfo("p" + i, param.type());
                inputs.add(valueInfo);
                valueMap.put(param, valueInfo);
            }
        }
        List<OnnxOperation> ops = op.body().entryBlock().ops().stream()
                .mapMulti(this::toOnnxOpTransformer).toList();

        OnnxGraph graph = new OnnxGraph(op.funcName(), initializers.stream().toList(), inputs, ops.getLast().outputs(), ops);
        System.out.println(graph); // DEBUG
        return graph;
    }

    public static FuncOp toFunc(Quoted quotedLambda) {
        JavaOp.LambdaOp lambda = (JavaOp.LambdaOp) quotedLambda.op();
        assert lambda.parameters().isEmpty();

        List<Value> captures = lambda.capturedValues();
        List<TypeElement> normalizedCaptureTypes = captures.stream()
                .map(v -> v instanceof Op.Result r &&
                        r.op() instanceof CoreOp.VarOp vop &&
                        vop.initOperand() instanceof Block.Parameter p ? p : v)
                .map(Value::type)
                .toList();
        FunctionType ft = CoreType.functionType(lambda.invokableType().returnType(), normalizedCaptureTypes);

        CoreOp.FuncOp f = CoreOp.FuncOp.func("", ft).body(b -> {
            // Map input captured values
            for (int i = 0; i < captures.size(); i++) {
                Value inputCapture = captures.get(i);
                Value output;
                if (inputCapture instanceof Op.Result r &&
                        r.op() instanceof CoreOp.VarOp vop &&
                        vop.initOperand() instanceof Block.Parameter) {
                    output = b.op(CoreOp.var(b.parameters().get(i)));
                } else {
                    output = b.parameters().get(i);
                }
                b.context().mapValue(inputCapture, output);
            }

            b.transformBody(lambda.body(), List.of(), OpTransformer.COPYING_TRANSFORMER);
        });
        return f;
    }

    public interface OnnxValue {
        String name();

        static OnnxValue of(String name) {
            return new OnnxValue() {
                @Override
                public String name() {
                    return name;
                }

                @Override
                public String toString() {
                    return "OnnxValue[name=" + name + "]";
                }
            };
        }
    }

    public record OnnxOperation(String name, List<OnnxValue> inputs, List<OnnxValue> outputs, List<OnnxConstant> attributes) {
        @Override
        public String toString() {
            return String.format("%s = %s(%s) %s",
                    outputs.stream().map(OnnxValue::name).collect(Collectors.joining(", ")),
                    name,
                    inputs.stream().map(OnnxValue::name).collect(Collectors.joining(", ")),
                    attributes.stream().map(a -> "@" + a.name() + "=" + a.valueString()).collect(Collectors.joining(", ")));
        }
    }

    public record OnnxValueInfo(String name, TypeElement type) implements OnnxValue { }

    public record OnnxInitializer(TypeElement type, FieldRef ref) implements OnnxValue {
        @Override
        public String name() {
            return ref.name();
        }
    }

    public record OnnxConstant(String name, Class<?> type, Object value) implements OnnxValue {

        @Override
        public String toString() {
            return "OnnxConstant[" +
                    "name=" + name +
                    ", value=" + valueString() +
                    "]";
        }

        String valueString() {
            if (value instanceof long[]) {
                return Arrays.toString((long[]) value);
            } else {
                return value.toString();
            }
        }
    }

    public record OnnxGraph(String name, List<OnnxInitializer> initializers, List<OnnxValueInfo> inputs, List<OnnxValue> outputs, List<OnnxOperation> operations) {
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("graph " + name + " {\n");
            buf.append("  inputs:\n");
            for (OnnxValueInfo input : inputs) {
                buf.append("    " + input.name + " : " + input.type + "\n");
            }
            buf.append("  initializers:\n");
            for (OnnxInitializer init : initializers) {
                buf.append("    " + init.name() + " : " + init.type() + " @ " + init.ref().refType() + "\n");
            }
            buf.append("  outputs: " + outputs.stream().map(OnnxValue::name).toList() + "\n");
            buf.append("  operations:\n");
            for (OnnxOperation operation : operations) {
                buf.append("    " + operation + "\n");
            }
            buf.append("}");
            return buf.toString();
        }
    }
}
