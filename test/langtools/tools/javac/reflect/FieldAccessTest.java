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

import java.io.PrintStream;
import jdk.incubator.code.Reflect;
import java.util.Spliterator;
import java.util.Spliterator.OfInt;

import static java.lang.System.out;
import static java.util.Spliterator.OfInt.*;

/*
 * @test
 * @summary Smoke test for code reflection with field access.
 * @modules jdk.incubator.code
 * @build FieldAccessTest
 * @build CodeReflectionTester
 * @run main CodeReflectionTester FieldAccessTest
 */

public class FieldAccessTest {
    static int s_f;
    int f;

    @Reflect
    @IR("""
            func @"test1" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = constant @1;
                %2 : java.type:"int" = field.assign %1 @java.ref:"FieldAccessTest::s_f:int";
                %3 : java.type:"int" = constant @1;
                %4 : java.type:"int" = field.assign %0 %3 @java.ref:"FieldAccessTest::f:int";
                return;
            };
            """)
    void test1() {
        s_f = 1;
        f = 1;
    }

    @Reflect
    @IR("""
            func @"test1_1" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                %2 : java.type:"int" = constant @1;
                %3 : java.type:"int" = add %1 %2 @func<java.type:"int", java.type:"int", java.type:"int">;
                %4 : java.type:"int" = field.assign %0 %3 @java.ref:"FieldAccessTest::f:int";
                %5 : java.type:"int" = field.load @java.ref:"FieldAccessTest::s_f:int";
                %6 : java.type:"int" = constant @1;
                %7 : java.type:"int" = add %5 %6 @func<java.type:"int", java.type:"int", java.type:"int">;
                %8 : java.type:"int" = field.assign %7 @java.ref:"FieldAccessTest::s_f:int";
                return;
            };
            """)
    void test1_1() {
        f += 1;
        s_f += 1;
    }

    @Reflect
    @IR("""
            func @"test2" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = constant @1;
                %2 : java.type:"int" = field.assign %0 %1 @java.ref:"FieldAccessTest::f:int";
                %3 : java.type:"int" = field.assign %2 @java.ref:"FieldAccessTest::s_f:int";
                return;
            };
            """)
    void test2() {
        s_f = f = 1;
    }

    @Reflect
    @IR("""
            func @"test2_1" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = constant @1;
                %2 : java.type:"int" = field.assign %0 %1 @java.ref:"FieldAccessTest::f:int";
                return;
            };
            """)
    void test2_1() {
        this.f = 1;
    }

    @Reflect
    @IR("""
            func @"test2_2" (%0 : java.type:"FieldAccessTest")java.type:"int" -> {
                %1 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                return %1;
            };
            """)
    int test2_2() {
        return this.f;
    }

    @Reflect
    @IR("""
            func @"test2_3" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = constant @1;
                %2 : java.type:"int" = field.assign %0 %1 @java.ref:"FieldAccessTest::f:int";
                return;
            };
            """)
    void test2_3() {
        FieldAccessTest.this.f = 1;
    }

    @Reflect
    @IR("""
            func @"test2_4" (%0 : java.type:"FieldAccessTest")java.type:"int" -> {
                %1 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                return %1;
            };
            """)
    int test2_4() {
        return FieldAccessTest.this.f;
    }

    @Reflect
    @IR("""
            func @"test3" (%0 : java.type:"FieldAccessTest")java.type:"int" -> {
                %1 : java.type:"int" = field.load @java.ref:"FieldAccessTest::s_f:int";
                %2 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                %3 : java.type:"int" = add %1 %2;
                return %3;
            };
            """)
    int test3() {
        return s_f + f;
    }

    static class A {
        B b;
    }

    static class B {
        C c;
    }

    static class C {
        int f;
    }

    @Reflect
    @IR("""
            func @"test4" (%0 : java.type:"FieldAccessTest", %1 : java.type:"FieldAccessTest$A")java.type:"void" -> {
                %2 : Var<java.type:"FieldAccessTest$A"> = var %1 @"a";
                %3 : java.type:"FieldAccessTest$A" = var.load %2;
                %4 : java.type:"FieldAccessTest$B" = field.load %3 @java.ref:"FieldAccessTest$A::b:FieldAccessTest$B";
                %5 : java.type:"FieldAccessTest$C" = field.load %4 @java.ref:"FieldAccessTest$B::c:FieldAccessTest$C";
                %6 : java.type:"int" = constant @1;
                %7 : java.type:"int" = field.assign %5 %6 @java.ref:"FieldAccessTest$C::f:int";
                return;
            };
            """)
    void test4(A a) {
        a.b.c.f = 1;
    }

    static class X {
        int f;
        static int s_f;
    }

    @Reflect
    @IR("""
            func @"test5" (%0 : java.type:"FieldAccessTest")java.type:"int" -> {
                %1 : java.type:"int" = field.load @java.ref:"FieldAccessTest$X::s_f:int";
                return %1;
            };
            """)
    int test5() {
        return X.s_f;
    }

    @Reflect
    @IR("""
            func @"test6" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = constant @1;
                %2 : java.type:"int" = field.assign %1 @java.ref:"FieldAccessTest$X::s_f:int";
                return;
            };
            """)
    void test6() {
        X.s_f = 1;
    }


    @Reflect
    @IR("""
            func @"test7" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                %2 : java.type:"int" = constant @1;
                %3 : java.type:"int" = add %1 %2 @func<java.type:"int", java.type:"int", java.type:"int">;
                %4 : java.type:"int" = field.assign %0 %3 @java.ref:"FieldAccessTest::f:int";
                %5 : java.type:"int" = field.load @java.ref:"FieldAccessTest::s_f:int";
                %6 : java.type:"int" = constant @1;
                %7 : java.type:"int" = add %5 %6 @func<java.type:"int", java.type:"int", java.type:"int">;
                %8 : java.type:"int" = field.assign %7 @java.ref:"FieldAccessTest::s_f:int";
                return;
            };
            """)
    void test7() {
        f += 1;
        s_f += 1;
    }

    @Reflect
    @IR("""
            func @"test8" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                %2 : java.type:"int" = constant @1;
                %3 : java.type:"int" = add %1 %2 @func<java.type:"int", java.type:"int", java.type:"int">;
                %4 : java.type:"int" = field.assign %0 %3 @java.ref:"FieldAccessTest::f:int";
                %5 : java.type:"int" = field.load @java.ref:"FieldAccessTest::s_f:int";
                %6 : java.type:"int" = constant @1;
                %7 : java.type:"int" = add %5 %6 @func<java.type:"int", java.type:"int", java.type:"int">;
                %8 : java.type:"int" = field.assign %7 @java.ref:"FieldAccessTest::s_f:int";
                return;
            };
            """)
    void test8() {
        this.f += 1;
        this.s_f += 1;
    }

    @Reflect
    @IR("""
            func @"test9" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = field.load @java.ref:"FieldAccessTest$X::s_f:int";
                %2 : java.type:"int" = constant @1;
                %3 : java.type:"int" = add %1 %2 @func<java.type:"int", java.type:"int", java.type:"int">;
                %4 : java.type:"int" = field.assign %3 @java.ref:"FieldAccessTest$X::s_f:int";
                return;
            };
            """)
    void test9() {
        X.s_f += 1;
    }

    @Reflect
    @IR("""
            func @"test10" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                %2 : java.type:"int" = constant @1;
                %3 : java.type:"int" = add %1 %2 @func<java.type:"int", java.type:"int", java.type:"int">;
                %4 : java.type:"int" = field.assign %0 %3 @java.ref:"FieldAccessTest::f:int";
                %5 : java.type:"int" = field.assign %4 @java.ref:"FieldAccessTest::s_f:int";
                return;
            };
            """)
    void test10() {
        s_f = f += 1;
    }

    @Reflect
    @IR("""
            func @"test11" (%0 : java.type:"FieldAccessTest", %1 : java.type:"FieldAccessTest$A")java.type:"void" -> {
                %2 : Var<java.type:"FieldAccessTest$A"> = var %1 @"a";
                %3 : java.type:"FieldAccessTest$A" = var.load %2;
                %4 : java.type:"FieldAccessTest$B" = field.load %3 @java.ref:"FieldAccessTest$A::b:FieldAccessTest$B";
                %5 : java.type:"FieldAccessTest$C" = field.load %4 @java.ref:"FieldAccessTest$B::c:FieldAccessTest$C";
                %6 : java.type:"int" = field.load %5 @java.ref:"FieldAccessTest$C::f:int";
                %7 : java.type:"int" = constant @1;
                %8 : java.type:"int" = add %6 %7 @func<java.type:"int", java.type:"int", java.type:"int">;
                %9 : java.type:"int" = field.assign %5 %8 @java.ref:"FieldAccessTest$C::f:int";
                return;
            };
            """)
    void test11(A a) {
        a.b.c.f += 1;
    }

    @Reflect
    @IR("""
            func @"test12" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                %2 : Var<java.type:"int"> = var %1 @"$old";
                %3 : java.type:"int" = var.load %2;
                %4 : java.type:"int" = constant @1;
                %5 : java.type:"int" = add %3 %4 @func<java.type:"int", java.type:"int", java.type:"int">;
                %6 : java.type:"int" = field.assign %0 %5 @java.ref:"FieldAccessTest::f:int";
                %7 : java.type:"int" = var.load %2;
                %8 : Var<java.type:"int"> = var %7 @"x";
                %9 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                %10 : Var<java.type:"int"> = var %9 @"$old";
                %11 : java.type:"int" = var.load %10;
                %12 : java.type:"int" = constant @1;
                %13 : java.type:"int" = sub %11 %12 @func<java.type:"int", java.type:"int", java.type:"int">;
                %14 : java.type:"int" = field.assign %0 %13 @java.ref:"FieldAccessTest::f:int";
                %15 : java.type:"int" = var.load %10;
                %16 : Var<java.type:"int"> = var %15 @"y";
                return;
            };
            """)
    void test12() {
        int x = f++;
        int y = f--;
    }

    @Reflect
    @IR("""
            func @"test13" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                %2 : Var<java.type:"int"> = var %1 @"$old";
                %3 : java.type:"int" = var.load %2;
                %4 : java.type:"int" = constant @1;
                %5 : java.type:"int" = add %3 %4 @func<java.type:"int", java.type:"int", java.type:"int">;
                %6 : java.type:"int" = field.assign %0 %5 @java.ref:"FieldAccessTest::f:int";
                %7 : java.type:"int" = var.load %2;
                %8 : Var<java.type:"int"> = var %7 @"x";
                %9 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                %10 : Var<java.type:"int"> = var %9 @"$old";
                %11 : java.type:"int" = var.load %10;
                %12 : java.type:"int" = constant @1;
                %13 : java.type:"int" = sub %11 %12 @func<java.type:"int", java.type:"int", java.type:"int">;
                %14 : java.type:"int" = field.assign %0 %13 @java.ref:"FieldAccessTest::f:int";
                %15 : java.type:"int" = var.load %10;
                %16 : Var<java.type:"int"> = var %15 @"y";
                return;
            };
            """)
    void test13() {
        int x = this.f++;
        int y = this.f--;
    }

    @Reflect
    @IR("""
            func @"test14" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = field.load @java.ref:"FieldAccessTest::s_f:int";
                %2 : Var<java.type:"int"> = var %1 @"$old";
                %3 : java.type:"int" = var.load %2;
                %4 : java.type:"int" = constant @1;
                %5 : java.type:"int" = add %3 %4 @func<java.type:"int", java.type:"int", java.type:"int">;
                %6 : java.type:"int" = field.assign %5 @java.ref:"FieldAccessTest::s_f:int";
                %7 : java.type:"int" = var.load %2;
                %8 : Var<java.type:"int"> = var %7 @"x";
                %9 : java.type:"int" = field.load @java.ref:"FieldAccessTest::s_f:int";
                %10 : Var<java.type:"int"> = var %9 @"$old";
                %11 : java.type:"int" = var.load %10;
                %12 : java.type:"int" = constant @1;
                %13 : java.type:"int" = sub %11 %12 @func<java.type:"int", java.type:"int", java.type:"int">;
                %14 : java.type:"int" = field.assign %13 @java.ref:"FieldAccessTest::s_f:int";
                %15 : java.type:"int" = var.load %10;
                %16 : Var<java.type:"int"> = var %15 @"y";
                return;
            };
            """)
    void test14() {
        int x = s_f++;
        int y = s_f--;
    }

    @Reflect
    @IR("""
            func @"test15" (%0 : java.type:"FieldAccessTest", %1 : java.type:"FieldAccessTest$X")java.type:"void" -> {
                %2 : Var<java.type:"FieldAccessTest$X"> = var %1 @"h";
                %3 : java.type:"FieldAccessTest$X" = var.load %2;
                %4 : java.type:"int" = field.load %3 @java.ref:"FieldAccessTest$X::f:int";
                %5 : Var<java.type:"int"> = var %4 @"$old";
                %6 : java.type:"int" = var.load %5;
                %7 : java.type:"int" = constant @1;
                %8 : java.type:"int" = add %6 %7 @func<java.type:"int", java.type:"int", java.type:"int">;
                %9 : java.type:"int" = field.assign %3 %8 @java.ref:"FieldAccessTest$X::f:int";
                %10 : java.type:"int" = var.load %5;
                %11 : Var<java.type:"int"> = var %10 @"x";
                %12 : java.type:"FieldAccessTest$X" = var.load %2;
                %13 : java.type:"int" = field.load %12 @java.ref:"FieldAccessTest$X::f:int";
                %14 : Var<java.type:"int"> = var %13 @"$old";
                %15 : java.type:"int" = var.load %14;
                %16 : java.type:"int" = constant @1;
                %17 : java.type:"int" = sub %15 %16 @func<java.type:"int", java.type:"int", java.type:"int">;
                %18 : java.type:"int" = field.assign %12 %17 @java.ref:"FieldAccessTest$X::f:int";
                %19 : java.type:"int" = var.load %14;
                %20 : Var<java.type:"int"> = var %19 @"y";
                return;
            };
            """)
    void test15(X h) {
        int x = h.f++;
        int y = h.f--;
    }




    @Reflect
    @IR("""
            func @"test16" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                %2 : java.type:"int" = constant @1;
                %3 : java.type:"int" = add %1 %2 @func<java.type:"int", java.type:"int", java.type:"int">;
                %4 : java.type:"int" = field.assign %0 %3 @java.ref:"FieldAccessTest::f:int";
                %5 : Var<java.type:"int"> = var %4 @"x";
                %6 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                %7 : java.type:"int" = constant @1;
                %8 : java.type:"int" = sub %6 %7 @func<java.type:"int", java.type:"int", java.type:"int">;
                %9 : java.type:"int" = field.assign %0 %8 @java.ref:"FieldAccessTest::f:int";
                %10 : Var<java.type:"int"> = var %9 @"y";
                return;
            };
            """)
    void test16() {
        int x = ++f;
        int y = --f;
    }

    @Reflect
    @IR("""
            func @"test17" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                %2 : java.type:"int" = constant @1;
                %3 : java.type:"int" = add %1 %2 @func<java.type:"int", java.type:"int", java.type:"int">;
                %4 : java.type:"int" = field.assign %0 %3 @java.ref:"FieldAccessTest::f:int";
                %5 : Var<java.type:"int"> = var %4 @"x";
                %6 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest::f:int";
                %7 : java.type:"int" = constant @1;
                %8 : java.type:"int" = sub %6 %7 @func<java.type:"int", java.type:"int", java.type:"int">;
                %9 : java.type:"int" = field.assign %0 %8 @java.ref:"FieldAccessTest::f:int";
                %10 : Var<java.type:"int"> = var %9 @"y";
                return;
            };
            """)
    void test17() {
        int x = ++this.f;
        int y = --this.f;
    }

    @Reflect
    @IR("""
            func @"test18" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = field.load @java.ref:"FieldAccessTest::s_f:int";
                %2 : java.type:"int" = constant @1;
                %3 : java.type:"int" = add %1 %2 @func<java.type:"int", java.type:"int", java.type:"int">;
                %4 : java.type:"int" = field.assign %3 @java.ref:"FieldAccessTest::s_f:int";
                %5 : Var<java.type:"int"> = var %4 @"x";
                %6 : java.type:"int" = field.load @java.ref:"FieldAccessTest::s_f:int";
                %7 : java.type:"int" = constant @1;
                %8 : java.type:"int" = sub %6 %7 @func<java.type:"int", java.type:"int", java.type:"int">;
                %9 : java.type:"int" = field.assign %8 @java.ref:"FieldAccessTest::s_f:int";
                %10 : Var<java.type:"int"> = var %9 @"y";
                return;
            };
            """)
    void test18() {
        int x = ++s_f;
        int y = --s_f;
    }

    @Reflect
    @IR("""
            func @"test19" (%0 : java.type:"FieldAccessTest", %1 : java.type:"FieldAccessTest$X")java.type:"void" -> {
                %2 : Var<java.type:"FieldAccessTest$X"> = var %1 @"h";
                %3 : java.type:"FieldAccessTest$X" = var.load %2;
                %4 : java.type:"int" = field.load %3 @java.ref:"FieldAccessTest$X::f:int";
                %5 : java.type:"int" = constant @1;
                %6 : java.type:"int" = add %4 %5 @func<java.type:"int", java.type:"int", java.type:"int">;
                %7 : java.type:"int" = field.assign %3 %6 @java.ref:"FieldAccessTest$X::f:int";
                %8 : Var<java.type:"int"> = var %7 @"x";
                %9 : java.type:"FieldAccessTest$X" = var.load %2;
                %10 : java.type:"int" = field.load %9 @java.ref:"FieldAccessTest$X::f:int";
                %11 : java.type:"int" = constant @1;
                %12 : java.type:"int" = sub %10 %11 @func<java.type:"int", java.type:"int", java.type:"int">;
                %13 : java.type:"int" = field.assign %9 %12 @java.ref:"FieldAccessTest$X::f:int";
                %14 : Var<java.type:"int"> = var %13 @"y";
                return;
            };
            """)
    void test19(X h) {
        int x = ++h.f;
        int y = --h.f;
    }

    static class Y extends X {
        int yf;
        static int s_yf;

        @Reflect
        @IR("""
                func @"x_test" (%0 : java.type:"FieldAccessTest$Y")java.type:"void" -> {
                    %1 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest$Y::f:int";
                    %2 : Var<java.type:"int"> = var %1 @"x";
                    %3 : java.type:"int" = field.load @java.ref:"FieldAccessTest$Y::s_f:int";
                    %4 : java.type:"int" = var.assign %2 %3;
                    return;
                };
                """)
        void x_test() {
            int x = f;
            x = s_f;
        }

        @Reflect
        @IR("""
                func @"x_test2" (%0 : java.type:"FieldAccessTest$Y")java.type:"void" -> {
                    %1 : java.type:"int" = constant @1;
                    %2 : java.type:"int" = field.assign %0 %1 @java.ref:"FieldAccessTest$Y::f:int";
                    %3 : java.type:"int" = constant @1;
                    %4 : java.type:"int" = field.assign %3 @java.ref:"FieldAccessTest$Y::s_f:int";
                    return;
                };
                """)
        void x_test2() {
            f = 1;
            s_f = 1;
        }

        @Reflect
        @IR("""
                func @"x_test3" (%0 : java.type:"FieldAccessTest$Y")java.type:"void" -> {
                    %1 : java.type:"int" = field.load %0 @java.ref:"FieldAccessTest$Y::f:int";
                    %2 : Var<java.type:"int"> = var %1 @"$old";
                    %3 : java.type:"int" = var.load %2;
                    %4 : java.type:"int" = constant @1;
                    %5 : java.type:"int" = add %3 %4 @func<java.type:"int", java.type:"int", java.type:"int">;
                    %6 : java.type:"int" = field.assign %0 %5 @java.ref:"FieldAccessTest$Y::f:int";
                    %7 : java.type:"int" = var.load %2;
                    %8 : java.type:"int" = field.load @java.ref:"FieldAccessTest$Y::s_f:int";
                    %9 : Var<java.type:"int"> = var %8 @"$old";
                    %10 : java.type:"int" = var.load %9;
                    %11 : java.type:"int" = constant @1;
                    %12 : java.type:"int" = add %10 %11 @func<java.type:"int", java.type:"int", java.type:"int">;
                    %13 : java.type:"int" = field.assign %12 @java.ref:"FieldAccessTest$Y::s_f:int";
                    %14 : java.type:"int" = var.load %9;
                    return;
                };
                """)
        void x_test3() {
            f++;
            s_f++;
        }
    }

    @Reflect
    @IR("""
            func @"test20" (%0 : java.type:"FieldAccessTest", %1 : java.type:"FieldAccessTest$Y")java.type:"void" -> {
                %2 : Var<java.type:"FieldAccessTest$Y"> = var %1 @"y";
                %3 : java.type:"FieldAccessTest$Y" = var.load %2;
                %4 : java.type:"int" = field.load %3 @java.ref:"FieldAccessTest$Y::f:int";
                %5 : Var<java.type:"int"> = var %4 @"x";
                %6 : java.type:"FieldAccessTest$Y" = var.load %2;
                %7 : java.type:"int" = field.load %6 @java.ref:"FieldAccessTest$Y::yf:int";
                %8 : java.type:"int" = var.assign %5 %7;
                %9 : java.type:"FieldAccessTest$Y" = var.load %2;
                %10 : java.type:"int" = field.load @java.ref:"FieldAccessTest$Y::s_yf:int";
                %11 : java.type:"int" = var.assign %5 %10;
                %12 : java.type:"int" = field.load @java.ref:"FieldAccessTest$Y::s_yf:int";
                %13 : java.type:"int" = var.assign %5 %12;
                %14 : java.type:"FieldAccessTest$Y" = var.load %2;
                %15 : java.type:"int" = field.load @java.ref:"FieldAccessTest$Y::s_f:int";
                %16 : java.type:"int" = var.assign %5 %15;
                %17 : java.type:"int" = field.load @java.ref:"FieldAccessTest$Y::s_f:int";
                %18 : java.type:"int" = var.assign %5 %17;
                return;
            };
            """)
    void test20(Y y) {
        int x = y.f;
        x = y.yf;
        x = y.s_yf;
        x = Y.s_yf;
        x = y.s_f;
        x = Y.s_f;
    }

    @Reflect
    @IR("""
            func @"test21" (%0 : java.type:"FieldAccessTest", %1 : java.type:"FieldAccessTest$Y")java.type:"void" -> {
                %2 : Var<java.type:"FieldAccessTest$Y"> = var %1 @"y";
                %3 : java.type:"FieldAccessTest$Y" = var.load %2;
                %4 : java.type:"int" = constant @1;
                %5 : java.type:"int" = field.assign %3 %4 @java.ref:"FieldAccessTest$Y::f:int";
                %6 : java.type:"FieldAccessTest$Y" = var.load %2;
                %7 : java.type:"int" = constant @1;
                %8 : java.type:"int" = field.assign %6 %7 @java.ref:"FieldAccessTest$Y::yf:int";
                %9 : java.type:"FieldAccessTest$Y" = var.load %2;
                %10 : java.type:"int" = constant @1;
                %11 : java.type:"int" = field.assign %10 @java.ref:"FieldAccessTest$Y::s_yf:int";
                %12 : java.type:"int" = constant @1;
                %13 : java.type:"int" = field.assign %12 @java.ref:"FieldAccessTest$Y::s_yf:int";
                %14 : java.type:"FieldAccessTest$Y" = var.load %2;
                %15 : java.type:"int" = constant @1;
                %16 : java.type:"int" = field.assign %15 @java.ref:"FieldAccessTest$Y::s_f:int";
                %17 : java.type:"int" = constant @1;
                %18 : java.type:"int" = field.assign %17 @java.ref:"FieldAccessTest$Y::s_f:int";
                return;
            };
            """)
    void test21(Y y) {
        y.f = 1;
        y.yf = 1;
        y.s_yf = 1;
        Y.s_yf = 1;
        y.s_f = 1;
        Y.s_f = 1;
    }

    @Reflect
    @IR("""
            func @"test22" (%0 : java.type:"FieldAccessTest", %1 : java.type:"FieldAccessTest$Y")java.type:"void" -> {
                %2 : Var<java.type:"FieldAccessTest$Y"> = var %1 @"y";
                %3 : java.type:"FieldAccessTest$Y" = var.load %2;
                %4 : java.type:"int" = field.load %3 @java.ref:"FieldAccessTest$Y::f:int";
                %5 : Var<java.type:"int"> = var %4 @"$old";
                %6 : java.type:"int" = var.load %5;
                %7 : java.type:"int" = constant @1;
                %8 : java.type:"int" = add %6 %7 @func<java.type:"int", java.type:"int", java.type:"int">;
                %9 : java.type:"int" = field.assign %3 %8 @java.ref:"FieldAccessTest$Y::f:int";
                %10 : java.type:"int" = var.load %5;
                %11 : java.type:"FieldAccessTest$Y" = var.load %2;
                %12 : java.type:"int" = field.load %11 @java.ref:"FieldAccessTest$Y::yf:int";
                %13 : Var<java.type:"int"> = var %12 @"$old";
                %14 : java.type:"int" = var.load %13;
                %15 : java.type:"int" = constant @1;
                %16 : java.type:"int" = add %14 %15 @func<java.type:"int", java.type:"int", java.type:"int">;
                %17 : java.type:"int" = field.assign %11 %16 @java.ref:"FieldAccessTest$Y::yf:int";
                %18 : java.type:"int" = var.load %13;
                %19 : java.type:"FieldAccessTest$Y" = var.load %2;
                %20 : java.type:"int" = field.load @java.ref:"FieldAccessTest$Y::s_yf:int";
                %21 : Var<java.type:"int"> = var %20 @"$old";
                %22 : java.type:"int" = var.load %21;
                %23 : java.type:"int" = constant @1;
                %24 : java.type:"int" = add %22 %23 @func<java.type:"int", java.type:"int", java.type:"int">;
                %25 : java.type:"int" = field.assign %24 @java.ref:"FieldAccessTest$Y::s_yf:int";
                %26 : java.type:"int" = var.load %21;
                %27 : java.type:"int" = field.load @java.ref:"FieldAccessTest$Y::s_yf:int";
                %28 : Var<java.type:"int"> = var %27 @"$old";
                %29 : java.type:"int" = var.load %28;
                %30 : java.type:"int" = constant @1;
                %31 : java.type:"int" = add %29 %30 @func<java.type:"int", java.type:"int", java.type:"int">;
                %32 : java.type:"int" = field.assign %31 @java.ref:"FieldAccessTest$Y::s_yf:int";
                %33 : java.type:"int" = var.load %28;
                %34 : java.type:"FieldAccessTest$Y" = var.load %2;
                %35 : java.type:"int" = field.load @java.ref:"FieldAccessTest$Y::s_f:int";
                %36 : Var<java.type:"int"> = var %35 @"$old";
                %37 : java.type:"int" = var.load %36;
                %38 : java.type:"int" = constant @1;
                %39 : java.type:"int" = add %37 %38 @func<java.type:"int", java.type:"int", java.type:"int">;
                %40 : java.type:"int" = field.assign %39 @java.ref:"FieldAccessTest$Y::s_f:int";
                %41 : java.type:"int" = var.load %36;
                %42 : java.type:"int" = field.load @java.ref:"FieldAccessTest$Y::s_f:int";
                %43 : Var<java.type:"int"> = var %42 @"$old";
                %44 : java.type:"int" = var.load %43;
                %45 : java.type:"int" = constant @1;
                %46 : java.type:"int" = add %44 %45 @func<java.type:"int", java.type:"int", java.type:"int">;
                %47 : java.type:"int" = field.assign %46 @java.ref:"FieldAccessTest$Y::s_f:int";
                %48 : java.type:"int" = var.load %43;
                return;
            };
            """)
    void test22(Y y) {
        y.f++;
        y.yf++;
        y.s_yf++;
        Y.s_yf++;
        y.s_f++;
        Y.s_f++;
    }

    // @@@ Should propagate as constant value?
    @Reflect
    @IR("""
            func @"test23" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"int" = field.load @java.ref:"java.util.Spliterator$OfInt::CONCURRENT:int";
                %2 : Var<java.type:"int"> = var %1 @"x";
                %3 : java.type:"int" = field.load @java.ref:"java.util.Spliterator$OfInt::CONCURRENT:int";
                %4 : java.type:"int" = var.assign %2 %3;
                %5 : java.type:"int" = field.load @java.ref:"java.util.Spliterator$OfInt::CONCURRENT:int";
                %6 : java.type:"int" = var.assign %2 %5;
                return;
            };
            """)
    void test23() {
        int x = Spliterator.OfInt.CONCURRENT;
        x = OfInt.CONCURRENT;
        x = CONCURRENT;
    }

    @Reflect
    @IR("""
            func @"test24" (%0 : java.type:"FieldAccessTest")java.type:"void" -> {
                %1 : java.type:"java.io.PrintStream" = field.load @java.ref:"java.lang.System::out:java.io.PrintStream";
                %2 : Var<java.type:"java.io.PrintStream"> = var %1 @"ps";
                return;
            };
            """)
    void test24() {
        PrintStream ps = out;
    }

    static class Box<T> {
        public T v;

        public Box(T v) {
            this.v = v;
        }
    }

    @Reflect
    @IR("""
            func @"test25" ()java.type:"void" -> {
                %0 : java.type:"java.lang.String" = constant @"abc";
                %1 : java.type:"FieldAccessTest$Box<java.lang.String>" = new %0 @java.ref:"FieldAccessTest$Box::(java.lang.Object)";
                %2 : Var<java.type:"FieldAccessTest$Box<java.lang.String>"> = var %1 @"b";
                %3 : java.type:"FieldAccessTest$Box<java.lang.String>" = var.load %2;
                %4 : java.type:"java.lang.String" = field.load %3 @java.ref:"FieldAccessTest$Box::v:java.lang.Object";
                %5 : Var<java.type:"java.lang.String"> = var %4 @"s";
                return;
            };
            """)
    static void test25() {
        Box<String> b = new Box<>("abc");
        String s = b.v;
    }

    //@@@ unqualified access to field of generic type needs to be tested
    // waiting for a new way of modeling types, so that type variables are captured in the IR
}
