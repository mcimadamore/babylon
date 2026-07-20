/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

/*
 * @test
 * @modules jdk.incubator.code
 * @run junit TestImplicitConversionTransformer
 */

import jdk.incubator.code.Block;
import jdk.incubator.code.Body;
import jdk.incubator.code.Op;
import jdk.incubator.code.dialect.core.CoreOp;
import jdk.incubator.code.dialect.core.CoreType;
import jdk.incubator.code.dialect.java.ImplicitConversionTransformer;
import jdk.incubator.code.dialect.java.JavaOp;
import jdk.incubator.code.dialect.java.JavaType;
import jdk.incubator.code.dialect.java.MethodRef;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestImplicitConversionTransformer {

    static void consume(int i) { }

    static int rhs() {
        return 0;
    }

    static final MethodRef CONSUME = MethodRef.method(
            JavaType.type(TestImplicitConversionTransformer.class),
            "consume", JavaType.VOID, JavaType.INT);

    @Test
    public void testConvertBeforeLaterSubexpression() {
        Body.Builder body = Body.Builder.of(null,
                CoreType.functionType(JavaType.VOID, JavaType.type(Integer.class)));
        Block.Builder block = body.entryBlock();
        Block.Parameter value = block.parameters().getFirst();
        Op.Result later = block.add(JavaOp.invoke(MethodRef.method(
                JavaType.type(TestImplicitConversionTransformer.class),
                "rhs", JavaType.INT)));
        block.add(JavaOp.invoke(MethodRef.method(
                JavaType.type(TestImplicitConversionTransformer.class),
                "consumeBoth", JavaType.VOID, JavaType.INT, JavaType.INT), value, later));
        block.add(CoreOp.return_());

        CoreOp.FuncOp transformed = CoreOp.func("f", body)
                .transform(new ImplicitConversionTransformer());
        List<Op> ops = transformed.body().entryBlock().ops();

        assertTrue(ops.get(0) instanceof JavaOp.InvokeOp invoke
                && invoke.invokeReference().name().equals("intValue"));
        assertTrue(ops.get(1) instanceof JavaOp.InvokeOp invoke
                && invoke.invokeReference().name().equals("rhs"));
    }

    static void consumeBoth(int i, int j) { }

    @Test
    public void testConvertCapturedValueAtEachConsumer() {
        Body.Builder body = Body.Builder.of(null,
                CoreType.functionType(JavaType.VOID, JavaType.type(Integer.class)));
        Block.Builder block = body.entryBlock();
        Block.Parameter captured = block.parameters().getFirst();

        for (int i = 0; i < 2; i++) {
            block.add(JavaOp.lambda(body, CoreType.functionType(JavaType.VOID),
                    JavaType.type(Runnable.class)).body(lambdaBlock -> {
                lambdaBlock.add(JavaOp.invoke(CONSUME, captured));
                lambdaBlock.add(CoreOp.return_());
            }));
        }
        block.add(CoreOp.return_());

        CoreOp.FuncOp transformed = CoreOp.func("f", body)
                .transform(new ImplicitConversionTransformer());
        for (Op op : transformed.body().entryBlock().ops().subList(0, 2)) {
            JavaOp.LambdaOp lambda = (JavaOp.LambdaOp) op;
            List<Op> ops = lambda.body().entryBlock().ops();
            JavaOp.InvokeOp conversion = (JavaOp.InvokeOp) ops.get(0);
            JavaOp.InvokeOp consumer = (JavaOp.InvokeOp) ops.get(1);

            assertEquals(conversion.invokeReference().name(), "intValue");
            assertSame(consumer.operands().getFirst(), conversion.result());
        }
    }

    @Test
    public void testConvertCapturedValueAtConsumer() {
        Body.Builder body = Body.Builder.of(null,
                CoreType.functionType(JavaType.VOID, JavaType.type(Integer.class)));
        Block.Builder block = body.entryBlock();
        Block.Parameter captured = block.parameters().getFirst();

        block.add(JavaOp.lambda(body, CoreType.functionType(JavaType.VOID),
                JavaType.type(Runnable.class)).body(lambdaBlock -> {
                    lambdaBlock.add(JavaOp.invoke(CONSUME, captured));
                    lambdaBlock.add(CoreOp.return_());
                }));
        block.add(CoreOp.return_());

        CoreOp.FuncOp transformed = CoreOp.func("f", body)
                .transform(new ImplicitConversionTransformer());
        JavaOp.LambdaOp lambda = (JavaOp.LambdaOp) transformed.body().entryBlock().ops().getFirst();
        List<Op> ops = lambda.body().entryBlock().ops();
        JavaOp.InvokeOp conversion = (JavaOp.InvokeOp) ops.get(0);
        JavaOp.InvokeOp consumer = (JavaOp.InvokeOp) ops.get(1);

        assertEquals(conversion.invokeReference().name(), "intValue");
        assertSame(consumer.operands().getFirst(), conversion.result());
    }
}
