/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
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

package oracle.code.triton;

import jdk.incubator.code.extern.DialectFactory;
import jdk.incubator.code.dialect.core.CoreOp;
import jdk.incubator.code.dialect.java.JavaOp;
import jdk.incubator.code.dialect.java.JavaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.IOException;
import java.lang.ScopedValue;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import jdk.incubator.code.TypeElement;
import jdk.incubator.code.Op;
import jdk.incubator.code.extern.OpParser;
import jdk.incubator.code.CodeReflection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TritonTestExtension implements ParameterResolver {

    @Target({ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Kernel {
        String value();
    }

    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
        return pc.getParameter().getType() == TritonTestData.class;
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
        Kernel k = ec.getRequiredTestMethod().getAnnotation(Kernel.class);
        String kernelName = (k != null)
            ? k.value()
            : ec.getRequiredTestMethod().getName();

        return new TritonTestData(ec.getRequiredTestClass(), kernelName);
    }

    public static class TritonTestData {
        final Class<?> testClass;
        final String javaKernelName;

        public TritonTestData(Class<?> testClass, String javaKernelName) {
            this.testClass = testClass;
            this.javaKernelName = javaKernelName;
        }

        public void test(List<? extends TypeElement> argTypes) {
            Optional<Method> om = Stream.of(testClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals(javaKernelName))
                    .filter(m -> m.getAnnotation(CodeReflection.class) != null)
                    .findFirst();
            Method m = om.get();
            TritonCodeModel tcm = m.getAnnotation(TritonCodeModel.class);
            boolean doSSA = tcm != null ? tcm.SSA() : true;
            test(Op.ofMethod(m).get(), argTypes, expectedTritonKernel(tcm), doSSA);
        }

        public TritonOps.ModuleOp expectedTritonKernel(TritonCodeModel tcm) {
            if (tcm == null || tcm.value().isEmpty()) {
                return null;
            }


            return (TritonOps.ModuleOp) OpParser.fromString(
                    new DialectFactory(
                            TritonOps.DIALECT_FACTORY.opFactory().andThen(TritonTestOps.FACTORY),
                            TritonOps.DIALECT_FACTORY.typeElementFactory()),
                    tcm.value()).get(0);
        }

        void test(CoreOp.FuncOp javaKernel,
                  List<? extends TypeElement> argTypes,
                  TritonOps.ModuleOp expectedTritonKernel,
                  boolean doSSA) {
            TritonOps.ModuleOp actualTritonKernel = ScopedValue.where(TritonTransformer.SV_SSA, doSSA).call(() -> {
                return TritonTransformer.tritonModule(javaKernel, JavaType.VOID, argTypes);
            });

            String mlirText = MLIRGenerator.transform(actualTritonKernel);
            Path buildDir = Path.of(System.getProperty("project.build.directory", ""));
            Path mlirDir = buildDir.resolve("mlir");
            try {
                Files.createDirectories(mlirDir);
                Files.writeString(mlirDir.resolve(javaKernelName + ".mlir"), mlirText, StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Assertions.assertEquals(
                    expectedTritonKernel == null ? "NO @TritonCodeModel" : expectedTritonKernel.toText(),
                    actualTritonKernel.toText());
        }
    }

}
