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
                var.store %1 %3;
                %4 : java.type:"int" = constant @2;
                var.store %2 %4;
                %5 : java.type:"int" = var.load %1;
                %6 : java.type:"int" = var.load %2;
                %7 : java.type:"int" = add %5 %6 @func<java.type:"int", java.type:"int", java.type:"int">;
                return %7;
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
                %8 : Var<java.type:"int"> = var %7 @"$value";
                %9 : java.type:"int" = var.load %8;
                var.store %4 %9;
                %10 : java.type:"int" = var.load %8;
                var.store %6 %10;
                %11 : java.type:"int" = var.load %6;
                return %11;
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
                %3 : java.type:"int" = var.load %2;
                %4 : java.type:"int" = constant @2;
                %5 : java.type:"int" = add %3 %4 @func<java.type:"int", java.type:"int", java.type:"int">;
                %6 : Var<java.type:"int"> = var %5 @"$value";
                %7 : java.type:"int" = var.load %6;
                var.store %2 %7;
                %8 : java.type:"int" = var.load %6;
                %9 : Var<java.type:"int"> = var %8 @"y";
                %10 : java.type:"int" = var.load %9;
                %11 : java.type:"int" = constant @3;
                %12 : java.type:"int" = add %10 %11 @func<java.type:"int", java.type:"int", java.type:"int">;
                %13 : Var<java.type:"int"> = var %12 @"$value";
                %14 : java.type:"int" = var.load %13;
                var.store %9 %14;
                %15 : java.type:"int" = var.load %13;
                %16 : java.type:"int" = var.load %2;
                %17 : java.type:"int" = constant @4;
                %18 : java.type:"int" = add %16 %17 @func<java.type:"int", java.type:"int", java.type:"int">;
                %19 : Var<java.type:"int"> = var %18 @"$value";
                %20 : java.type:"int" = var.load %19;
                var.store %2 %20;
                %21 : java.type:"int" = var.load %19;
                %22 : java.type:"int" = add %15 %21 @func<java.type:"int", java.type:"int", java.type:"int">;
                return %22;
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
                %4 : Var<java.type:"int"> = var %3 @"$old";
                %5 : java.type:"int" = var.load %4;
                %6 : java.type:"int" = constant @1;
                %7 : java.type:"int" = add %5 %6 @func<java.type:"int", java.type:"int", java.type:"int">;
                var.store %2 %7;
                %8 : java.type:"int" = var.load %4;
                %9 : Var<java.type:"int"> = var %8 @"x";
                %10 : java.type:"int" = var.load %2;
                %11 : Var<java.type:"int"> = var %10 @"$old";
                %12 : java.type:"int" = var.load %11;
                %13 : java.type:"int" = constant @1;
                %14 : java.type:"int" = sub %12 %13 @func<java.type:"int", java.type:"int", java.type:"int">;
                var.store %2 %14;
                %15 : java.type:"int" = var.load %11;
                %16 : Var<java.type:"int"> = var %15 @"y";
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
                %4 : java.type:"int" = constant @1;
                %5 : java.type:"int" = add %3 %4 @func<java.type:"int", java.type:"int", java.type:"int">;
                %6 : Var<java.type:"int"> = var %5 @"$value";
                %7 : java.type:"int" = var.load %6;
                var.store %2 %7;
                %8 : java.type:"int" = var.load %6;
                %9 : Var<java.type:"int"> = var %8 @"x";
                %10 : java.type:"int" = var.load %2;
                %11 : java.type:"int" = constant @1;
                %12 : java.type:"int" = sub %10 %11 @func<java.type:"int", java.type:"int", java.type:"int">;
                %13 : Var<java.type:"int"> = var %12 @"$value";
                %14 : java.type:"int" = var.load %13;
                var.store %2 %14;
                %15 : java.type:"int" = var.load %13;
                %16 : Var<java.type:"int"> = var %15 @"y";
                return;
            };
            """)
    void test9(int i) {
        int x = ++i;
        int y = --i;
    }
}
