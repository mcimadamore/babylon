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

/*
 * @test
 * @summary Smoke test for code reflection with synchronized blocks.
 * @modules jdk.incubator.code
 * @build SynchronizedTest
 * @build CodeReflectionTester
 * @run main CodeReflectionTester SynchronizedTest
 */

import jdk.incubator.code.Reflect;

public class SynchronizedTest {
    @Reflect
    @IR("""
            func @"test1" (%0 : java.type:"SynchronizedTest", %1 : java.type:"int")java.type:"int" -> {
                %2 : Var<java.type:"int"> = var %1 @"i";
                java.synchronized
                    ()java.type:"SynchronizedTest" -> {
                        yield %0;
                    }
                    ()java.type:"void" -> {
                        %3 : java.type:"int" = var.load %2;
                        %4 : Var<java.type:"int"> = var %3 @"$old";
                        %5 : java.type:"int" = var.load %4;
                        %6 : java.type:"int" = constant @1;
                        %7 : java.type:"int" = add %5 %6 @func<java.type:"int", java.type:"int", java.type:"int">;
                        %8 : java.type:"int" = var.assign %2 %7;
                        %9 : java.type:"int" = var.load %4;
                        yield;
                    };
                %10 : java.type:"int" = var.load %2;
                return %10;
            };
            """)
    int test1(int i) {
        synchronized (this) {
            i++;
        }
        return i;
    }

    static Object m() {
        return null;
    }

    @Reflect
    @IR("""
            func @"test2" (%0 : java.type:"SynchronizedTest", %1 : java.type:"int")java.type:"int" -> {
                %2 : Var<java.type:"int"> = var %1 @"i";
                java.synchronized
                    ()java.type:"java.lang.Object" -> {
                        %3 : java.type:"java.lang.Object" = invoke @java.ref:"SynchronizedTest::m():java.lang.Object";
                        yield %3;
                    }
                    ()java.type:"void" -> {
                        %4 : java.type:"int" = var.load %2;
                        %5 : Var<java.type:"int"> = var %4 @"$old";
                        %6 : java.type:"int" = var.load %5;
                        %7 : java.type:"int" = constant @1;
                        %8 : java.type:"int" = add %6 %7 @func<java.type:"int", java.type:"int", java.type:"int">;
                        %9 : java.type:"int" = var.assign %2 %8;
                        %10 : java.type:"int" = var.load %5;
                        yield;
                    };
                %11 : java.type:"int" = var.load %2;
                return %11;
            };
            """)
    int test2(int i) {
        synchronized (m()) {
            i++;
        }
        return i;
    }

}
