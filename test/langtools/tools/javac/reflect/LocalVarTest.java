/*
 * Copyright (c) 2024, 2025, Oracle and/or its affiliates. All rights reserved.
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

import jdk.incubator.code.Reflect;

/*
 * @test
 * @summary Smoke test for code reflection with local variables.
 * @modules jdk.incubator.code
 * @build LocalVarTest
 * @build CodeReflectionTester
 * @run main CodeReflectionTester LocalVarTest
 */

public class LocalVarTest {

    @Reflect
    @IR("""
            func @"test1" (%0 : java.type:"LocalVarTest")java.type:"int" -> {
                %1 : java.type:"int" = constant @1;
                %2 : Var<java.type:"int"> = var %1 @"x";
                %3 : java.type:"int" = constant @2;
                %4 : Var<java.type:"int"> = var %3 @"y";
                %5 : java.type:"int" = var.load %2;
                %6 : java.type:"int" = var.load %4;
                %7 : java.type:"int" = add %5 %6;
                return %7;
            };
            """)
    int test1() {
        int x = 1;
        int y = 2;
        return x + y;
    }

    @Reflect
    @IR("""
            func @"test2" (%0 : java.type:"LocalVarTest", %1 : java.type:"int", %2 : java.type:"int")java.type:"int" -> {
                %3 : Var<java.type:"int"> = var %1 @"x";
                %4 : Var<java.type:"int"> = var %2 @"y";
                %5 : java.type:"int" = var.load %3;
                %6 : java.type:"int" = var.load %4;
                %7 : java.type:"int" = add %5 %6;
                return %7;
            };
            """)
    int test2(int x, int y) {
        return x + y;
    }

    @Reflect
    @IR("""
            func @"test3" (%0 : java.type:"LocalVarTest")java.type:"int" -> {
                %1 : Var<java.type:"int"> = var @"x";
                %2 : Var<java.type:"int"> = var @"y";
                %3 : java.type:"int" = constant @1;
                %4 : java.type:"int" = var.assign %1 %3;
                %5 : java.type:"int" = constant @2;
                %6 : java.type:"int" = var.assign %2 %5;
                %7 : java.type:"int" = var.load %1;
                %8 : java.type:"int" = var.load %2;
                %9 : java.type:"int" = add %7 %8 @func<java.type:"int", java.type:"int", java.type:"int">;
                return %9;
            };
            """)
    int test3() {
        int x;
        int y;
        x = 1;
        y = 2;
        return x + y;
    }

    @Reflect
    @IR("""
            func @"test4" (%0 : java.type:"LocalVarTest")java.type:"int" -> {
                %1 : java.type:"int" = constant @1;
                %2 : Var<java.type:"int"> = var %1 @"x";
                %3 : java.type:"int" = var.load %2;
                %4 : java.type:"int" = constant @1;
                %5 : java.type:"int" = add %3 %4;
                %6 : Var<java.type:"int"> = var %5 @"y";
                %7 : java.type:"int" = var.load %6;
                return %7;
            };
            """)
    int test4() {
        int x = 1;
        int y = x + 1;
        return y;
    }

    @Reflect
    @IR("""
            func @"test5" (%0 : java.type:"LocalVarTest")java.type:"int" -> {
                %1 : java.type:"int" = constant @1;
                %2 : Var<java.type:"int"> = var %1 @"x";
                %3 : java.type:"int" = var.load %2;
                %4 : Var<java.type:"int"> = var %3 @"y";
                %5 : java.type:"int" = var.load %4;
                return %5;
            };
            """)
    int test5() {
        int x = 1;
        int y = x;
        return y;
    }

    @Reflect
    @IR("""
            func @"test6" (%0 : java.type:"LocalVarTest")java.type:"int" -> {
                %1 : java.type:"int" = constant @1;
                %2 : Var<java.type:"int"> = var %1 @"x";
                %3 : java.type:"int" = constant @1;
                %4 : Var<java.type:"int"> = var %3 @"y";
                %5 : java.type:"int" = constant @1;
                %6 : Var<java.type:"int"> = var %5 @"z";
                %7 : java.type:"int" = var.load %2;
                %8 : java.type:"int" = var.assign %4 %7;
                %9 : java.type:"int" = var.assign %6 %8;
                %10 : java.type:"int" = var.load %6;
                return %10;
            };
            """)
    int test6() {
        int x = 1;
        int y = 1;
        int z = 1;
        z = y = x;
        return z;
    }

    @Reflect
    @IR("""
            func @"test7" (%0 : java.type:"LocalVarTest")java.type:"int" -> {
                %1 : java.type:"int" = constant @1;
                %2 : Var<java.type:"int"> = var %1 @"x";
                %3 : java.type:"int" = var.compound.assign %2 @operator.type=func<java.type:"int", java.type:"int", java.type:"int"> @compound.kind="ADD" ()java.type:"int" -> {
                    %4 : java.type:"int" = constant @2;
                    yield %4;
                };
                %5 : Var<java.type:"int"> = var %3 @"y";
                %6 : java.type:"int" = var.compound.assign %5 @operator.type=func<java.type:"int", java.type:"int", java.type:"int"> @compound.kind="ADD" ()java.type:"int" -> {
                    %7 : java.type:"int" = constant @3;
                    yield %7;
                };
                %8 : java.type:"int" = var.compound.assign %2 @operator.type=func<java.type:"int", java.type:"int", java.type:"int"> @compound.kind="ADD" ()java.type:"int" -> {
                    %9 : java.type:"int" = constant @4;
                    yield %9;
                };
                %10 : java.type:"int" = add %6 %8 @func<java.type:"int", java.type:"int", java.type:"int">;
                return %10;
            };
            """)
    int test7() {
        int x = 1;
        int y = x += 2;
        return (y += 3) + (x += 4);
    }

    @Reflect
    @IR("""
            func @"test8" (%0 : java.type:"LocalVarTest", %1 : java.type:"int")java.type:"void" -> {
                %2 : Var<java.type:"int"> = var %1 @"i";
                %3 : java.type:"int" = var.load %2;
                %4 : java.type:"int" = var.update %2 %3 @operator.type=func<java.type:"int", java.type:"int", java.type:"int"> @update.kind="POSTINC";
                %5 : Var<java.type:"int"> = var %4 @"x";
                %6 : java.type:"int" = var.load %2;
                %7 : java.type:"int" = var.update %2 %6 @operator.type=func<java.type:"int", java.type:"int", java.type:"int"> @update.kind="POSTDEC";
                %8 : Var<java.type:"int"> = var %7 @"y";
                return;
            };
            """)
    void test8(int i) {
        int x = i++;
        int y = i--;
    }

    @Reflect
    @IR("""
            func @"test9" (%0 : java.type:"LocalVarTest", %1 : java.type:"int")java.type:"void" -> {
                %2 : Var<java.type:"int"> = var %1 @"i";
                %3 : java.type:"int" = var.load %2;
                %4 : java.type:"int" = var.update %2 %3 @operator.type=func<java.type:"int", java.type:"int", java.type:"int"> @update.kind="PREINC";
                %5 : Var<java.type:"int"> = var %4 @"x";
                %6 : java.type:"int" = var.load %2;
                %7 : java.type:"int" = var.update %2 %6 @operator.type=func<java.type:"int", java.type:"int", java.type:"int"> @update.kind="PREDEC";
                %8 : Var<java.type:"int"> = var %7 @"y";
                return;
            };
            """)
    void test9(int i) {
        int x = ++i;
        int y = --i;
    }
}
