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
import java.util.List;

/*
 * @test
 * @summary Smoke test for code reflection with for loops.
 * @modules jdk.incubator.code
 * @build BreakContinueTest
 * @build CodeReflectionTester
 * @run main CodeReflectionTester BreakContinueTest
 */


public class BreakContinueTest {
    @Reflect
    @IR("""
            func @"test1" (%0 : java.type:"BreakContinueTest")java.type:"void" -> {
                java.for
                    ()Var<java.type:"int"> -> {
                        %1 : java.type:"int" = constant @0;
                        %2 : Var<java.type:"int"> = var %1 @"i";
                        yield %2;
                    }
                    (%3 : Var<java.type:"int">)java.type:"boolean" -> {
                        %4 : java.type:"int" = var.load %3;
                        %5 : java.type:"int" = constant @10;
                        %6 : java.type:"boolean" = lt %4 %5 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %6;
                    }
                    (%7 : Var<java.type:"int">)java.type:"void" -> {
                        %8 : java.type:"int" = var.load %7;
                        %9 : Var<java.type:"int"> = var %8 @"$old";
                        %10 : java.type:"int" = var.load %9;
                        %11 : java.type:"int" = constant @1;
                        %12 : java.type:"int" = add %10 %11 @func<java.type:"int", java.type:"int", java.type:"int">;
                        %13 : java.type:"int" = var.assign %7 %12;
                        %14 : java.type:"int" = var.load %9;
                        yield;
                    }
                    (%15 : Var<java.type:"int">)java.type:"void" -> {
                        java.if
                            ()java.type:"boolean" -> {
                                %16 : java.type:"boolean" = constant @true;
                                yield %16;
                            }
                            ()java.type:"void" -> {
                                java.continue;
                            }
                            ()java.type:"void" -> {
                                yield;
                            };
                        java.if
                            ()java.type:"boolean" -> {
                                %17 : java.type:"boolean" = constant @true;
                                yield %17;
                            }
                            ()java.type:"void" -> {
                                java.break;
                            }
                            ()java.type:"void" -> {
                                yield;
                            };
                        java.for
                            ()Var<java.type:"int"> -> {
                                %18 : java.type:"int" = constant @0;
                                %19 : Var<java.type:"int"> = var %18 @"j";
                                yield %19;
                            }
                            (%20 : Var<java.type:"int">)java.type:"boolean" -> {
                                %21 : java.type:"int" = var.load %20;
                                %22 : java.type:"int" = constant @10;
                                %23 : java.type:"boolean" = lt %21 %22 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                                yield %23;
                            }
                            (%24 : Var<java.type:"int">)java.type:"void" -> {
                                %25 : java.type:"int" = var.load %24;
                                %26 : Var<java.type:"int"> = var %25 @"$old";
                                %27 : java.type:"int" = var.load %26;
                                %28 : java.type:"int" = constant @1;
                                %29 : java.type:"int" = add %27 %28 @func<java.type:"int", java.type:"int", java.type:"int">;
                                %30 : java.type:"int" = var.assign %24 %29;
                                %31 : java.type:"int" = var.load %26;
                                yield;
                            }
                            (%32 : Var<java.type:"int">)java.type:"void" -> {
                                java.if
                                    ()java.type:"boolean" -> {
                                        %33 : java.type:"boolean" = constant @true;
                                        yield %33;
                                    }
                                    ()java.type:"void" -> {
                                        java.continue;
                                    }
                                    ()java.type:"void" -> {
                                        yield;
                                    };
                                java.if
                                    ()java.type:"boolean" -> {
                                        %34 : java.type:"boolean" = constant @true;
                                        yield %34;
                                    }
                                    ()java.type:"void" -> {
                                        java.break;
                                    }
                                    ()java.type:"void" -> {
                                        yield;
                                    };
                                java.continue;
                            };
                        java.continue;
                    };
                return;
            };
            """)
    void test1() {
        for (int i = 0; i < 10; i++) {
            if (true) {
                continue;
            }
            if (true) {
                break;
            }
            for (int j = 0; j < 10; j++) {
                if (true) {
                    continue;
                }
                if (true) {
                    break;
                }
            }
        }
    }

    @Reflect
    @IR("""
            func @"test2" (%0 : java.type:"BreakContinueTest")java.type:"void" -> {
                java.labeled ()java.type:"void" -> {
                    %1 : java.type:"java.lang.String" = constant @"outer";
                    java.for
                        ()Var<java.type:"int"> -> {
                            %2 : java.type:"int" = constant @0;
                            %3 : Var<java.type:"int"> = var %2 @"i";
                            yield %3;
                        }
                        (%4 : Var<java.type:"int">)java.type:"boolean" -> {
                            %5 : java.type:"int" = var.load %4;
                            %6 : java.type:"int" = constant @10;
                            %7 : java.type:"boolean" = lt %5 %6 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                            yield %7;
                        }
                        (%8 : Var<java.type:"int">)java.type:"void" -> {
                            %9 : java.type:"int" = var.load %8;
                            %10 : Var<java.type:"int"> = var %9 @"$old";
                            %11 : java.type:"int" = var.load %10;
                            %12 : java.type:"int" = constant @1;
                            %13 : java.type:"int" = add %11 %12 @func<java.type:"int", java.type:"int", java.type:"int">;
                            %14 : java.type:"int" = var.assign %8 %13;
                            %15 : java.type:"int" = var.load %10;
                            yield;
                        }
                        (%16 : Var<java.type:"int">)java.type:"void" -> {
                            java.if
                                ()java.type:"boolean" -> {
                                    %17 : java.type:"boolean" = constant @true;
                                    yield %17;
                                }
                                ()java.type:"void" -> {
                                    java.continue %1;
                                }
                                ()java.type:"void" -> {
                                    yield;
                                };
                            java.if
                                ()java.type:"boolean" -> {
                                    %18 : java.type:"boolean" = constant @true;
                                    yield %18;
                                }
                                ()java.type:"void" -> {
                                    java.break %1;
                                }
                                ()java.type:"void" -> {
                                    yield;
                                };
                            java.labeled ()java.type:"void" -> {
                                %19 : java.type:"java.lang.String" = constant @"inner";
                                java.for
                                    ()Var<java.type:"int"> -> {
                                        %20 : java.type:"int" = constant @0;
                                        %21 : Var<java.type:"int"> = var %20 @"j";
                                        yield %21;
                                    }
                                    (%22 : Var<java.type:"int">)java.type:"boolean" -> {
                                        %23 : java.type:"int" = var.load %22;
                                        %24 : java.type:"int" = constant @10;
                                        %25 : java.type:"boolean" = lt %23 %24 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                                        yield %25;
                                    }
                                    (%26 : Var<java.type:"int">)java.type:"void" -> {
                                        %27 : java.type:"int" = var.load %26;
                                        %28 : Var<java.type:"int"> = var %27 @"$old";
                                        %29 : java.type:"int" = var.load %28;
                                        %30 : java.type:"int" = constant @1;
                                        %31 : java.type:"int" = add %29 %30 @func<java.type:"int", java.type:"int", java.type:"int">;
                                        %32 : java.type:"int" = var.assign %26 %31;
                                        %33 : java.type:"int" = var.load %28;
                                        yield;
                                    }
                                    (%34 : Var<java.type:"int">)java.type:"void" -> {
                                        java.if
                                            ()java.type:"boolean" -> {
                                                %35 : java.type:"boolean" = constant @true;
                                                yield %35;
                                            }
                                            ()java.type:"void" -> {
                                                java.continue;
                                            }
                                            ()java.type:"void" -> {
                                                yield;
                                            };
                                        java.if
                                            ()java.type:"boolean" -> {
                                                %36 : java.type:"boolean" = constant @true;
                                                yield %36;
                                            }
                                            ()java.type:"void" -> {
                                                java.break;
                                            }
                                            ()java.type:"void" -> {
                                                yield;
                                            };
                                        java.if
                                            ()java.type:"boolean" -> {
                                                %37 : java.type:"boolean" = constant @true;
                                                yield %37;
                                            }
                                            ()java.type:"void" -> {
                                                java.continue %1;
                                            }
                                            ()java.type:"void" -> {
                                                yield;
                                            };
                                        java.if
                                            ()java.type:"boolean" -> {
                                                %38 : java.type:"boolean" = constant @true;
                                                yield %38;
                                            }
                                            ()java.type:"void" -> {
                                                java.break %1;
                                            }
                                            ()java.type:"void" -> {
                                                yield;
                                            };
                                        java.continue;
                                    };
                                yield;
                            };
                            java.continue;
                        };
                    yield;
                };
                return;
            };
            """)
    void test2() {
        outer:
        for (int i = 0; i < 10; i++) {
            if (true) {
                continue outer;
            }
            if (true) {
                break outer;
            }
            inner:
            for (int j = 0; j < 10; j++) {
                if (true) {
                    continue;
                }
                if (true) {
                    break;
                }
                if (true) {
                    continue outer;
                }
                if (true) {
                    break outer;
                }
            }
        }
    }

    @Reflect
    @IR("""
            func @"test3" (%0 : java.type:"BreakContinueTest")java.type:"void" -> {
                java.labeled ()java.type:"void" -> {
                    %1 : java.type:"java.lang.String" = constant @"b1";
                    java.block ()java.type:"void" -> {
                        java.labeled ()java.type:"void" -> {
                            %2 : java.type:"java.lang.String" = constant @"b2";
                            java.block ()java.type:"void" -> {
                                java.if
                                    ()java.type:"boolean" -> {
                                        %3 : java.type:"boolean" = constant @true;
                                        yield %3;
                                    }
                                    ()java.type:"void" -> {
                                        java.break %1;
                                    }
                                    ()java.type:"void" -> {
                                        yield;
                                    };
                                java.if
                                    ()java.type:"boolean" -> {
                                        %4 : java.type:"boolean" = constant @true;
                                        yield %4;
                                    }
                                    ()java.type:"void" -> {
                                        java.break %2;
                                    }
                                    ()java.type:"void" -> {
                                        yield;
                                    };
                                yield;
                            };
                            yield;
                        };
                        yield;
                    };
                    yield;
                };
                return;
            };
            """)
    void test3() {
        b1:
        {
            b2:
            {
                if (true) {
                    break b1;
                }
                if (true) {
                    break b2;
                }
            }
        }
    }

    @Reflect
    @IR("""
            func @"test4" (%0 : java.type:"BreakContinueTest")java.type:"void" -> {
                java.labeled ()java.type:"void" -> {
                    %1 : java.type:"java.lang.String" = constant @"b";
                    java.break %1;
                };
                %2 : java.type:"int" = constant @0;
                %3 : Var<java.type:"int"> = var %2 @"i";
                java.labeled ()java.type:"void" -> {
                    %4 : java.type:"java.lang.String" = constant @"b";
                    %5 : java.type:"int" = var.load %3;
                    %6 : Var<java.type:"int"> = var %5 @"$old";
                    %7 : java.type:"int" = var.load %6;
                    %8 : java.type:"int" = constant @1;
                    %9 : java.type:"int" = add %7 %8 @func<java.type:"int", java.type:"int", java.type:"int">;
                    %10 : java.type:"int" = var.assign %3 %9;
                    %11 : java.type:"int" = var.load %6;
                    yield;
                };
                java.labeled ()java.type:"void" -> {
                    %12 : java.type:"java.lang.String" = constant @"a";
                    java.labeled ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = constant @"b";
                        java.block ()java.type:"void" -> {
                            yield;
                        };
                        yield;
                    };
                    yield;
                };
                return;
            };
            """)
    void test4() {
        b:
        break b;

        int i = 0;
        b:
        i++;

        a: b: {
        }
    }
}
