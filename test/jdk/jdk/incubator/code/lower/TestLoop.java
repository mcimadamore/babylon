/*
 * Copyright (c) 2024, 2026, Oracle and/or its affiliates. All rights reserved.
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
 * @modules jdk.incubator.code
 * @summary test lowering of loops
 * @build TestLoop
 * @build CodeReflectionTester
 * @run main CodeReflectionTester TestLoop
 */

public class TestLoop {
    @Reflect
    @LoweredModel(value = """
            func @"testFor" (%0 : java.type:"int[]")java.type:"int" -> {
                %1 : Var<java.type:"int[]"> = var %0 @"a";
                %2 : java.type:"int" = constant @0;
                %3 : Var<java.type:"int"> = var %2 @"sum";
                %4 : java.type:"int" = constant @0;
                %5 : Var<java.type:"int"> = var %4 @"i";
                branch ^block_1;

              ^block_1:
                %6 : java.type:"int" = var.load %5;
                %7 : java.type:"int[]" = var.load %1;
                %8 : java.type:"int" = array.length %7;
                %9 : java.type:"boolean" = lt %6 %8;
                cbranch %9 ^block_2 ^block_5;

              ^block_2:
                %10 : java.type:"int" = var.load %3;
                %11 : Var<java.type:"int"> = var;
                %12 : java.type:"int[]" = var.load %1;
                %13 : java.type:"int" = var.load %5;
                %14 : java.type:"int" = array.load %12 %13;
                var.store %11 %14;
                branch ^block_3;

              ^block_3:
                %15 : java.type:"int" = var.load %11;
                %16 : java.type:"int" = add %10 %15;
                var.store %3 %16;
                branch ^block_4;

              ^block_4:
                %17 : java.type:"int" = var.load %5;
                %18 : java.type:"int" = constant @1;
                %19 : java.type:"int" = add %17 %18;
                var.store %5 %19;
                branch ^block_1;

              ^block_5:
                %20 : java.type:"int" = var.load %3;
                return %20;
            };
            """, ssa = false)
    static int testFor(int[] a) {
        int sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i];
        }
        return sum;
    }

    @Reflect
    @LoweredModel(value = """
            func @"testForSSA" (%0 : java.type:"int[]")java.type:"int" -> {
                %1 : java.type:"int" = constant @0;
                %2 : java.type:"int" = constant @0;
                branch ^block_1(%2, %1);

              ^block_1(%3 : java.type:"int", %4 : java.type:"int"):
                %5 : java.type:"int" = array.length %0;
                %6 : java.type:"boolean" = lt %3 %5;
                cbranch %6 ^block_2 ^block_5;

              ^block_2:
                %7 : java.type:"int" = array.load %0 %3;

                branch ^block_3;

              ^block_3:
                %8 : java.type:"int" = add %4 %7;
                branch ^block_4;

              ^block_4:
                %9 : java.type:"int" = constant @1;
                %10 : java.type:"int" = add %3 %9;
                branch ^block_1(%10, %8);

              ^block_5:
                return %4;
            };
            """, ssa = true)
    static int testForSSA(int[] a) {
        int sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i];
        }
        return sum;
    }
}
