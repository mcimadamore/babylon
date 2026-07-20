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
import java.util.List;
import java.util.function.Consumer;

/*
 * @test
 * @summary Smoke test for code reflection with blocks.
 * @modules jdk.incubator.code
 * @build BlockTest
 * @build CodeReflectionTester
 * @run main CodeReflectionTester BlockTest
 */

public class BlockTest {
    @Reflect
    @IR("""
            func @"test1" (%0 : java.type:"BlockTest")java.type:"void" -> {
                java.block ()java.type:"void" -> {
                    %1 : java.type:"int" = constant @0;
                    %2 : Var<java.type:"int"> = var %1 @"i";
                    yield;
                };
                java.block ()java.type:"void" -> {
                    %3 : java.type:"int" = constant @1;
                    %4 : Var<java.type:"int"> = var %3 @"i";
                    java.block ()java.type:"void" -> {
                        %5 : java.type:"int" = constant @2;
                        %6 : Var<java.type:"int"> = var %5 @"j";
                        yield;
                    };
                    yield;
                };
                return;
            };
            """)
    void test1() {
        {
            int i = 0;
        }

        {
            int i = 1;

            {
                int j = 2;
            }
        }
    }

    @Reflect
    @IR("""
            func @"test2" (%0 : java.type:"BlockTest", %1 : java.type:"int")java.type:"void" -> {
                %2 : Var<java.type:"int"> = var %1 @"i";
                java.if
                    ()java.type:"boolean" -> {
                        %3 : java.type:"int" = var.load %2;
                        %4 : java.type:"int" = constant @1;
                        %5 : java.type:"boolean" = lt %3 %4 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %5;
                    }
                    ()java.type:"void" -> {
                        java.block ()java.type:"void" -> {
                            %6 : java.type:"int" = var.compound.assign %2 @compound.kind="ADD" @operator.type=func<java.type:"int", java.type:"int", java.type:"int"> ()java.type:"int" -> {
                                %7 : java.type:"int" = constant @1;
                                yield %7;
                            };
                            yield;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %8 : java.type:"int" = var.load %2;
                        %9 : java.type:"int" = constant @2;
                        %10 : java.type:"boolean" = lt %8 %9 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %10;
                    }
                    ()java.type:"void" -> {
                        java.block ()java.type:"void" -> {
                            %11 : java.type:"int" = var.compound.assign %2 @compound.kind="ADD" @operator.type=func<java.type:"int", java.type:"int", java.type:"int"> ()java.type:"int" -> {
                                %12 : java.type:"int" = constant @2;
                                yield %12;
                            };
                            yield;
                        };
                        yield;
                    }
                    ()java.type:"void" -> {
                        java.block ()java.type:"void" -> {
                            %13 : java.type:"int" = var.compound.assign %2 @compound.kind="ADD" @operator.type=func<java.type:"int", java.type:"int", java.type:"int"> ()java.type:"int" -> {
                                %14 : java.type:"int" = constant @3;
                                yield %14;
                            };
                            yield;
                        };
                        yield;
                    };
                return;
            };
            """)
    void test2(int i) {
        if (i < 1) {
            {
                i += 1;
            }
        } else if (i < 2) {
            {
                i += 2;
            }
        } else {
            {
                i += 3;
            }
        }
    }

    @Reflect
    @IR("""
            func @"test3" (%0 : java.type:"BlockTest")java.type:"void" -> {
                java.for
                    ()java.type:"void" -> {
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %1 : java.type:"boolean" = constant @true;
                        yield %1;
                    }
                    ()java.type:"void" -> {
                        yield;
                    }
                    ()java.type:"void" -> {
                        java.block ()java.type:"void" -> {
                            %2 : java.type:"int" = constant @0;
                            %3 : Var<java.type:"int"> = var %2 @"i";
                            yield;
                        };
                        java.continue;
                    };
                unreachable;
            };
            """)
    void test3() {
        for (;;) {
            {
                int i = 0;
            }
        }
    }

    @Reflect
    @IR("""
            func @"test4" (%0 : java.type:"BlockTest", %1 : java.type:"int[]")java.type:"void" -> {
                %2 : Var<java.type:"int[]"> = var %1 @"ia";
                java.enhancedFor
                    ()java.type:"int[]" -> {
                        %3 : java.type:"int[]" = var.load %2;
                        yield %3;
                    }
                    (%4 : java.type:"int")Var<java.type:"int"> -> {
                        %5 : Var<java.type:"int"> = var %4 @"i";
                        yield %5;
                    }
                    (%6 : Var<java.type:"int">)java.type:"void" -> {
                        java.block ()java.type:"void" -> {
                            %7 : java.type:"int" = var.load %6;
                            %8 : java.type:"int" = var.update %6 %7 @operator.type=func<java.type:"int", java.type:"int", java.type:"int"> @update.kind="POSTINC";
                            yield;
                        };
                        java.continue;
                    };
                return;
            };
            """)
    void test4(int[] ia) {
        for (int i : ia) {
            {
                i++;
            }
        }
   }

    @Reflect
    @IR("""
            func @"test5" (%0 : java.type:"BlockTest")java.type:"void" -> {
                %1 : java.type:"java.util.function.Consumer<java.lang.String>" = lambda @lambda.isReflectable=true (%2 : java.type:"java.lang.String")java.type:"void" -> {
                    %3 : Var<java.type:"java.lang.String"> = var %2 @"s";
                    java.block ()java.type:"void" -> {
                        %4 : java.type:"java.io.PrintStream" = field.load @java.ref:"java.lang.System::out:java.io.PrintStream";
                        %5 : java.type:"java.lang.String" = var.load %3;
                        invoke %4 %5 @java.ref:"java.io.PrintStream::println(java.lang.String):void";
                        yield;
                    };
                    return;
                };
                %6 : Var<java.type:"java.util.function.Consumer<java.lang.String>"> = var %1 @"c";
                return;
            };
            """)
   void test5() {
        Consumer<String> c = s -> {
            {
                System.out.println(s);
            }
        };
    }


    @Reflect
    @IR("""
            func @"test6" (%0 : java.type:"BlockTest")java.type:"void" -> {
                java.if
                    ()java.type:"boolean" -> {
                        %1 : java.type:"boolean" = constant @true;
                        yield %1;
                    }
                    ()java.type:"void" -> {
                        java.block ()java.type:"void" -> {
                            return;
                        };
                        yield;
                    }
                    ()java.type:"void" -> {
                        yield;
                    };
                java.if
                    ()java.type:"boolean" -> {
                        %2 : java.type:"boolean" = constant @true;
                        yield %2;
                    }
                    ()java.type:"void" -> {
                        java.block ()java.type:"void" -> {
                            %3 : java.type:"java.lang.RuntimeException" = new @java.ref:"java.lang.RuntimeException::()";
                            throw %3;
                        };
                        yield;
                    }
                    ()java.type:"void" -> {
                        yield;
                    };
                return;
            };
            """)
    void test6() {
        if (true) {
            {
                return;
            }
        }

        if (true) {
            {
                throw new RuntimeException();
            }
        }
    }
}
