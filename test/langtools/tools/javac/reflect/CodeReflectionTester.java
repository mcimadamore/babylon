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

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import jdk.incubator.code.*;
import jdk.incubator.code.op.ExtendedOp;
import jdk.incubator.code.parser.OpParser;
import jdk.incubator.code.writer.OpWriter;
import jdk.incubator.code.CodeReflection;

import static jdk.incubator.code.op.CoreOp._return;
import static jdk.incubator.code.op.CoreOp.func;
import static jdk.incubator.code.type.FunctionType.VOID;

public class CodeReflectionTester {

    static int nErrors = 0;

    public static void main(String[] args) throws ReflectiveOperationException {
        if (args.length != 1) {
            System.err.println("Usage: CodeReflectionTester <classname>");
            System.exit(1);
        }
        Class<?> clazz = Class.forName(args[0]);
        for (Method m : clazz.getDeclaredMethods()) {
            check(m);
        }
        for (Field f : clazz.getDeclaredFields()) {
            check(f);
        }
        if (nErrors > 0) {
            throw new AssertionError("Test failed with " + nErrors + " errors");
        }
    }

    static void error(String msg, Object... args) {
        nErrors++;
        System.err.println("error: " + String.format(msg, args));
    }

    static void check(Method method) throws ReflectiveOperationException {
        if (!method.isAnnotationPresent(CodeReflection.class)) return;
        String found = canonicalizeModel(method, Op.ofMethod(method).orElseThrow());
        IR ir = method.getAnnotation(IR.class);
        if (ir == null) {
            error("No @IR annotation found on reflective method");
            return;
        }
        String expected = canonicalizeModel(method, ir.value());
        if (!found.equals(expected)) {
            error("Bad IR\nFound:\n%s\n\nExpected:\n%s", found, expected);
            return;
        }
    }

    static void check(Field field) throws ReflectiveOperationException {
        IR ir = field.getAnnotation(IR.class);
        if (ir == null) return;
        if (field.getType().equals(Quoted.class)) {
            // transitional
            Quoted quoted = (Quoted) field.get(null);
            String found = canonicalizeModel(field, getModelOfQuotedOp(quoted));
            String expected = canonicalizeModel(field, ir.value());
            if (!found.equals(expected)) {
                error("Bad IR\nFound:\n%s\n\nExpected:\n%s", found, expected);
                return;
            }
        } else if (Quotable.class.isAssignableFrom(field.getType())) {
            Quotable quotable = (Quotable) field.get(null);
            String found = canonicalizeModel(field, getModelOfQuotedOp(Op.ofQuotable(quotable).get()));
            String expected = canonicalizeModel(field, ir.value());
            if (!found.equals(expected)) {
                error("Bad IR\nFound:\n%s\n\nExpected:\n%s", found, expected);
                return;
            }
        } else {
            error("Field annotated with @IR should be of a quotable type (Quoted/Quotable)");
            return;
        }
    }

    // serializes dropping location information, parses, and then serializes, dropping location information
    static String canonicalizeModel(Member m, Op o) {
        return canonicalizeModel(m, serialize(o));
    }

    // parses, and then serializes, dropping location information
    static String canonicalizeModel(Member m, String d) {
        Op o;
        try {
            o = OpParser.fromString(ExtendedOp.FACTORY, d).get(0);
        } catch (Exception e) {
            throw new IllegalStateException(m.toString(), e);
        }
        return serialize(o);
    }

    // serializes, dropping location information
    static String serialize(Op o) {
        StringWriter w = new StringWriter();
        OpWriter.writeTo(w, o, OpWriter.LocationOption.DROP_LOCATION);
        return w.toString();
    }

    static Op getModelOfQuotedOp(Quoted quoted) {
        return func("f", VOID).body(fblock -> {
            CopyContext cc = fblock.context();
            for (Value cv : quoted.capturedValues().keySet()) {
                Block.Parameter p = fblock.parameter(cv.type());
                cc.mapValue(cv, p);
            }

            Op qOp = quoted.op();
            // Associate the quoted ops ancestor body's entry block
            // with the function's entry block, thereby ensuring that
            // captured values mapped to the function's parameters
            // are reachable
            cc.mapBlock(qOp.ancestorBody().entryBlock(), fblock);
            fblock.op(qOp);

            fblock.op(_return());
        });
    }
}
