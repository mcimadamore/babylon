package oracle.code.onnx;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.Optional;
import java.util.stream.Stream;
import jdk.incubator.code.CodeReflection;
import jdk.incubator.code.Op;
import jdk.incubator.code.op.CoreOp;
import oracle.code.onnx.compiler.OnnxTransformer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleTest {

    @CodeReflection
    public static Tensor<Float> add(Tensor<Float> a, Tensor<Float> b) {
        return OnnxOperators.Add(a, b);
    }

    @Test
    public void testAdd() throws Exception {
        var a = Tensor.ofFlat(1f, 2, 3);
        var b = Tensor.ofFlat(6f, 5, 4);
        assertEquals(
                add(a, b),
                runModel("add", a, b));
    }

    @CodeReflection
    public static Tensor<Float> fconstant() {
        return OnnxOperators.Constant(-1f);
    }

    @Test
    public void testFconstant() throws Exception {
        // tests the numbers are encoded correctly
        var expected = Tensor.ofScalar(-1f);
        assertEquals(expected, fconstant());
        assertEquals(expected, runModel("fconstant"));
    }

    @CodeReflection
    public static Tensor<Float> fconstants() {
        return OnnxOperators.Constant(new float[]{-1f, 0, 1, Float.MIN_VALUE, Float.MAX_VALUE});
    }

    @Test
    public void testFconstants() throws Exception {
        // tests the numbers are encoded correctly
        var expected = Tensor.ofFlat(-1f, 0, 1, Float.MIN_VALUE, Float.MAX_VALUE);
        assertEquals(expected, fconstants());
        assertEquals(expected, runModel("fconstants"));
    }

    @CodeReflection
    public static Tensor<Long> lconstant() {
        return OnnxOperators.Constant(-1l);
    }

    @Test
    public void testLconstant() throws Exception {
        // tests the numbers are encoded correctly
        var expected = Tensor.ofScalar(-1l);
        assertEquals(expected, lconstant());
        assertEquals(expected, runModel("lconstant"));
    }

    @CodeReflection
    public static Tensor<Long> lconstants() {
        return OnnxOperators.Constant(new long[]{-1, 0, 1, Long.MIN_VALUE, Long.MAX_VALUE});
    }

    @Test
    public void testLconstants() throws Exception {
        // tests the numbers are encoded correctly
        var expected = Tensor.ofFlat(-1l, 0, 1, Long.MIN_VALUE, Long.MAX_VALUE);
        assertEquals(expected, lconstants());
        assertEquals(expected, runModel("lconstants"));
    }

    @CodeReflection
    public static Tensor<Long> reshapeAndShape(Tensor<Float> data, Tensor<Long> shape) {
        return OnnxOperators.Shape(OnnxOperators.Reshape(data, shape, Optional.empty()), Optional.empty(), Optional.empty());
    }

    @Test
    public void testReshapeAndShape() throws Exception {
        var data = Tensor.ofFlat(1f, 2, 3, 4, 5, 6, 7, 8);
        var shape = Tensor.ofFlat(2l, 2, 2);
        assertEquals(
                reshapeAndShape(data, shape),
                runModel("reshapeAndShape", data, shape));
    }

    @CodeReflection
    public static Tensor<Long> indicesOfMaxPool(Tensor<Float> x) {
        // testing secondary output
        return OnnxOperators.MaxPool(x, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),  new long[]{2}).Indices();
    }

    @Test
    public void testIndicesOfMaxPool() throws Exception {
        var x = Tensor.ofShape(new long[]{2, 2, 2}, 1f, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(
                indicesOfMaxPool(x),
                runModel("indicesOfMaxPool", x));
    }

    private static Tensor runModel(String name, Tensor... params) throws NoSuchMethodException {
        return new Tensor(OnnxRuntime.getInstance().runFunc(
                getOnnxModel(name),
                Stream.of(params).map(t -> Optional.ofNullable(t.tensorAddr)).toList()).getFirst());
    }

    private static CoreOp.FuncOp getOnnxModel(String name) throws NoSuchMethodException {
        return OnnxTransformer.transform(MethodHandles.lookup(),
                Op.ofMethod(Stream.of(SimpleTest.class.getDeclaredMethods()).filter(m -> m.getName().equals(name)).findFirst().get()).get());
    }

    static void assertEquals(Tensor expected, Tensor actual) {
        assertEquals(expected.tensorAddr, actual.tensorAddr);
    }

    static void assertEquals(MemorySegment expectedTensorAddr, MemorySegment actualTensorAddr) {

        var rt = OnnxRuntime.getInstance();

        var expectedType = rt.tensorElementType(expectedTensorAddr);
        var expectedShape = rt.tensorShape(expectedTensorAddr);
        var expectedBB = rt.tensorBuffer(expectedTensorAddr);

        var actualType = rt.tensorElementType(actualTensorAddr);
        var actualShape = rt.tensorShape(actualTensorAddr);
        var actualBB = rt.tensorBuffer(actualTensorAddr);

        Assertions.assertSame(expectedType, actualType);

        Assertions.assertArrayEquals(expectedShape, actualShape);

        switch (actualType) {
            case UINT8, INT8, UINT16, INT16, INT32, INT64, STRING, BOOL, UINT32, UINT64, UINT4, INT4 ->
                assertEquals(expectedBB, actualBB);
            case FLOAT ->
                assertEquals(expectedBB.asFloatBuffer(), actualBB.asFloatBuffer());
            case DOUBLE ->
                assertEquals(expectedBB.asDoubleBuffer(), actualBB.asDoubleBuffer());
            default ->
                throw new UnsupportedOperationException("Unsupported tensor element type " + actualType);
        }
    }

    static void assertEquals(ByteBuffer expectedData, ByteBuffer actualData) {
        Assertions.assertEquals(expectedData.capacity(), actualData.capacity());
        for (int i = 0; i < expectedData.capacity(); i++) {
            Assertions.assertEquals(expectedData.get(i), actualData.get(i));
        }
    }

    static void assertEquals(FloatBuffer expectedData, FloatBuffer actualData) {
        Assertions.assertEquals(expectedData.capacity(), actualData.capacity());
        for (int i = 0; i < expectedData.capacity(); i++) {
            Assertions.assertEquals(expectedData.get(i), actualData.get(i), 1e-6f);
        }
    }

    static void assertEquals(DoubleBuffer expectedData, DoubleBuffer actualData) {
        Assertions.assertEquals(expectedData.capacity(), actualData.capacity());
        for (int i = 0; i < expectedData.capacity(); i++) {
            Assertions.assertEquals(expectedData.get(i), actualData.get(i), 1e-6f);
        }
    }
}
