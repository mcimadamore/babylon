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
import java.util.Collection;
import java.util.RandomAccess;
import java.util.Stack;

/*
 * @test
 * @modules jdk.incubator.code
 * @build SwitchStatementTest
 * @build CodeReflectionTester
 * @run main CodeReflectionTester SwitchStatementTest
 */
public class SwitchStatementTest {

    @IR("""
            func @"caseConstantRuleExpression" (%0 : java.type:"java.lang.String")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.String"> = var %0 @"r";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"s";
                %4 : java.type:"java.lang.String" = var.load %1;
                java.switch.statement %4
                    (%5 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %6 : java.type:"java.lang.String" = constant @"FOO";
                        %7 : java.type:"boolean" = invoke %5 %6 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %7;
                    }
                    ()java.type:"void" -> {
                        %8 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %9 : java.type:"java.lang.String" = constant @"BAR";
                            yield %9;
                        };
                        yield;
                    }
                    (%10 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %11 : java.type:"java.lang.String" = constant @"BAR";
                        %12 : java.type:"boolean" = invoke %10 %11 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %12;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %14 : java.type:"java.lang.String" = constant @"BAZ";
                            yield %14;
                        };
                        yield;
                    }
                    (%15 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %16 : java.type:"java.lang.String" = constant @"BAZ";
                        %17 : java.type:"boolean" = invoke %15 %16 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %17;
                    }
                    ()java.type:"void" -> {
                        %18 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %19 : java.type:"java.lang.String" = constant @"FOO";
                            yield %19;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %20 : java.type:"boolean" = constant @true;
                        yield %20;
                    }
                    ()java.type:"void" -> {
                        %21 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %22 : java.type:"java.lang.String" = constant @"else";
                            yield %22;
                        };
                        yield;
                    };
                %23 : java.type:"java.lang.String" = var.load %3;
                return %23;
            };
            """)
    @Reflect
    public static String caseConstantRuleExpression(String r) {
        String s = "";
        switch (r) {
            case "FOO" -> s += "BAR";
            case "BAR" -> s += "BAZ";
            case "BAZ" -> s += "FOO";
            default -> s += "else";
        }
        return s;
    }

    @IR("""
            func @"caseConstantRuleBlock" (%0 : java.type:"java.lang.String")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.String"> = var %0 @"r";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"s";
                %4 : java.type:"java.lang.String" = var.load %1;
                java.switch.statement %4
                    (%5 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %6 : java.type:"java.lang.String" = constant @"FOO";
                        %7 : java.type:"boolean" = invoke %5 %6 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %7;
                    }
                    ()java.type:"void" -> {
                        %8 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %9 : java.type:"java.lang.String" = constant @"BAR";
                            yield %9;
                        };
                        yield;
                    }
                    (%10 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %11 : java.type:"java.lang.String" = constant @"BAR";
                        %12 : java.type:"boolean" = invoke %10 %11 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %12;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %14 : java.type:"java.lang.String" = constant @"BAZ";
                            yield %14;
                        };
                        yield;
                    }
                    (%15 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %16 : java.type:"java.lang.String" = constant @"BAZ";
                        %17 : java.type:"boolean" = invoke %15 %16 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %17;
                    }
                    ()java.type:"void" -> {
                        %18 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %19 : java.type:"java.lang.String" = constant @"FOO";
                            yield %19;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %20 : java.type:"boolean" = constant @true;
                        yield %20;
                    }
                    ()java.type:"void" -> {
                        %21 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %22 : java.type:"java.lang.String" = constant @"else";
                            yield %22;
                        };
                        yield;
                    };
                %23 : java.type:"java.lang.String" = var.load %3;
                return %23;
            };
            """)
    @Reflect
    public static String caseConstantRuleBlock(String r) {
        String s = "";
        switch (r) {
            case "FOO" -> {
                s += "BAR";
            }
            case "BAR" -> {
                s += "BAZ";
            }
            case "BAZ" -> {
                s += "FOO";
            }
            default -> {
                s += "else";
            }
        }
        return s;
    }

    @IR("""
            func @"caseConstantStatement" (%0 : java.type:"java.lang.String")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.String"> = var %0 @"s";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.String" = var.load %1;
                java.switch.statement %4
                    (%5 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %6 : java.type:"java.lang.String" = constant @"FOO";
                        %7 : java.type:"boolean" = invoke %5 %6 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %7;
                    }
                    ()java.type:"void" -> {
                        %8 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %9 : java.type:"java.lang.String" = constant @"BAR";
                            yield %9;
                        };
                        java.break;
                    }
                    (%10 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %11 : java.type:"java.lang.String" = constant @"BAR";
                        %12 : java.type:"boolean" = invoke %10 %11 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %12;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %14 : java.type:"java.lang.String" = constant @"BAZ";
                            yield %14;
                        };
                        java.break;
                    }
                    (%15 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %16 : java.type:"java.lang.String" = constant @"BAZ";
                        %17 : java.type:"boolean" = invoke %15 %16 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %17;
                    }
                    ()java.type:"void" -> {
                        %18 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %19 : java.type:"java.lang.String" = constant @"FOO";
                            yield %19;
                        };
                        java.break;
                    }
                    ()java.type:"boolean" -> {
                        %20 : java.type:"boolean" = constant @true;
                        yield %20;
                    }
                    ()java.type:"void" -> {
                        %21 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %22 : java.type:"java.lang.String" = constant @"else";
                            yield %22;
                        };
                        yield;
                    };
                %23 : java.type:"java.lang.String" = var.load %3;
                return %23;
            };
            """)
    @Reflect
    private static String caseConstantStatement(String s) {
        String r = "";
        switch (s) {
            case "FOO":
                r += "BAR";
                break;
            case "BAR":
                r += "BAZ";
                break;
            case "BAZ":
                r += "FOO";
                break;
            default:
                r += "else";
        }
        return r;
    }

    @IR("""
            func @"caseConstantMultiLabels" (%0 : java.type:"char")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"char"> = var %0 @"c";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"char" = var.load %1;
                %5 : java.type:"char" = invoke %4 @java.ref:"java.lang.Character::toLowerCase(char):char";
                java.switch.statement %5
                    (%6 : java.type:"char")java.type:"boolean" -> {
                        %7 : java.type:"boolean" = java.cor
                            ()java.type:"boolean" -> {
                                %8 : java.type:"char" = constant @'a';
                                %9 : java.type:"boolean" = eq %6 %8 @func<java.type:"boolean", java.type:"char", java.type:"char">;
                                yield %9;
                            }
                            ()java.type:"boolean" -> {
                                %10 : java.type:"char" = constant @'e';
                                %11 : java.type:"boolean" = eq %6 %10 @func<java.type:"boolean", java.type:"char", java.type:"char">;
                                yield %11;
                            }
                            ()java.type:"boolean" -> {
                                %12 : java.type:"char" = constant @'i';
                                %13 : java.type:"boolean" = eq %6 %12 @func<java.type:"boolean", java.type:"char", java.type:"char">;
                                yield %13;
                            }
                            ()java.type:"boolean" -> {
                                %14 : java.type:"char" = constant @'o';
                                %15 : java.type:"boolean" = eq %6 %14 @func<java.type:"boolean", java.type:"char", java.type:"char">;
                                yield %15;
                            }
                            ()java.type:"boolean" -> {
                                %16 : java.type:"char" = constant @'u';
                                %17 : java.type:"boolean" = eq %6 %16 @func<java.type:"boolean", java.type:"char", java.type:"char">;
                                yield %17;
                            };
                        yield %7;
                    }
                    ()java.type:"void" -> {
                        %18 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %19 : java.type:"java.lang.String" = constant @"vowel";
                            yield %19;
                        };
                        java.break;
                    }
                    ()java.type:"boolean" -> {
                        %20 : java.type:"boolean" = constant @true;
                        yield %20;
                    }
                    ()java.type:"void" -> {
                        %21 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %22 : java.type:"java.lang.String" = constant @"consonant";
                            yield %22;
                        };
                        yield;
                    };
                %23 : java.type:"java.lang.String" = var.load %3;
                return %23;
            };
            """)
    @Reflect
    private static String caseConstantMultiLabels(char c) {
        String r = "";
        switch (Character.toLowerCase(c)) {
            case 'a', 'e', 'i', 'o', 'u':
                r += "vowel";
                break;
            default:
                r += "consonant";
        }
        return r;
    }

    @IR("""
            func @"caseConstantThrow" (%0 : java.type:"java.lang.Integer")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.Integer"> = var %0 @"i";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.Integer" = var.load %1;
                java.switch.statement %4
                    (%5 : java.type:"java.lang.Integer")java.type:"boolean" -> {
                        %6 : java.type:"int" = invoke %5 @java.ref:"java.lang.Integer::intValue():int";
                        %7 : java.type:"int" = constant @8;
                        %8 : java.type:"boolean" = eq %6 %7 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %8;
                    }
                    ()java.type:"void" -> {
                        %9 : java.type:"java.lang.IllegalArgumentException" = new @java.ref:"java.lang.IllegalArgumentException::()";
                        throw %9;
                    }
                    (%10 : java.type:"java.lang.Integer")java.type:"boolean" -> {
                        %11 : java.type:"int" = invoke %10 @java.ref:"java.lang.Integer::intValue():int";
                        %12 : java.type:"int" = constant @9;
                        %13 : java.type:"boolean" = eq %11 %12 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %13;
                    }
                    ()java.type:"void" -> {
                        %14 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %15 : java.type:"java.lang.String" = constant @"Nine";
                            yield %15;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %16 : java.type:"boolean" = constant @true;
                        yield %16;
                    }
                    ()java.type:"void" -> {
                        %17 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %18 : java.type:"java.lang.String" = constant @"An integer";
                            yield %18;
                        };
                        yield;
                    };
                %19 : java.type:"java.lang.String" = var.load %3;
                return %19;
            };
            """)
    @Reflect
    private static String caseConstantThrow(Integer i) {
        String r = "";
        switch (i) {
            case 8 -> throw new IllegalArgumentException();
            case 9 -> r += "Nine";
            default -> r += "An integer";
        }
        return r;
    }

    @IR("""
            func @"caseConstantNullLabel" (%0 : java.type:"java.lang.String")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.String"> = var %0 @"s";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.String" = var.load %1;
                java.switch.statement %4 @switch.handle.nulls=true
                    (%5 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %6 : java.type:"java.lang.Object" = constant @null;
                        %7 : java.type:"boolean" = invoke %5 %6 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %7;
                    }
                    ()java.type:"void" -> {
                        %8 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %9 : java.type:"java.lang.String" = constant @"null";
                            yield %9;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %10 : java.type:"boolean" = constant @true;
                        yield %10;
                    }
                    ()java.type:"void" -> {
                        %11 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %12 : java.type:"java.lang.String" = constant @"non null";
                            yield %12;
                        };
                        yield;
                    };
                %13 : java.type:"java.lang.String" = var.load %3;
                return %13;
            };
            """)
    @Reflect
    private static String caseConstantNullLabel(String s) {
        String r = "";
        switch (s) {
            case null -> r += "null";
            default -> r += "non null";
        }
        return r;
    }

    @IR("""
            func @"caseConstantNullAndDefault" (%0 : java.type:"java.lang.String")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.String"> = var %0 @"s";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.String" = var.load %1;
                java.switch.statement %4 @switch.handle.nulls=true
                    (%5 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %6 : java.type:"java.lang.String" = constant @"abc";
                        %7 : java.type:"boolean" = invoke %5 %6 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %7;
                    }
                    ()java.type:"void" -> {
                        %8 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %9 : java.type:"java.lang.String" = constant @"alphabet";
                            yield %9;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %10 : java.type:"boolean" = constant @true;
                        yield %10;
                    }
                    ()java.type:"void" -> {
                        %11 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %12 : java.type:"java.lang.String" = constant @"null or default";
                            yield %12;
                        };
                        yield;
                    };
                %13 : java.type:"java.lang.String" = var.load %3;
                return %13;
            };
            """)
    @Reflect
    private static String caseConstantNullAndDefault(String s) {
        String r = "";
        switch (s) {
            case "abc" -> r += "alphabet";
            case null, default -> r += "null or default";
        }
        return r;
    }

    @IR("""
            func @"caseConstantFallThrough" (%0 : java.type:"char")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"char"> = var %0 @"c";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"char" = var.load %1;
                java.switch.statement %4
                    (%5 : java.type:"char")java.type:"boolean" -> {
                        %6 : java.type:"char" = constant @'A';
                        %7 : java.type:"boolean" = eq %5 %6 @func<java.type:"boolean", java.type:"char", java.type:"char">;
                        yield %7;
                    }
                    ()java.type:"void" -> {
                        java.switch.fallthrough;
                    }
                    (%8 : java.type:"char")java.type:"boolean" -> {
                        %9 : java.type:"char" = constant @'B';
                        %10 : java.type:"boolean" = eq %8 %9 @func<java.type:"boolean", java.type:"char", java.type:"char">;
                        yield %10;
                    }
                    ()java.type:"void" -> {
                        %11 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %12 : java.type:"java.lang.String" = constant @"A or B";
                            yield %12;
                        };
                        java.break;
                    }
                    ()java.type:"boolean" -> {
                        %13 : java.type:"boolean" = constant @true;
                        yield %13;
                    }
                    ()java.type:"void" -> {
                        %14 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %15 : java.type:"java.lang.String" = constant @"Neither A nor B";
                            yield %15;
                        };
                        yield;
                    };
                %16 : java.type:"java.lang.String" = var.load %3;
                return %16;
            };
            """)
    @Reflect
    private static String caseConstantFallThrough(char c) {
        String r = "";
        switch (c) {
            case 'A':
            case 'B':
                r += "A or B";
                break;
            default:
                r += "Neither A nor B";
        }
        return r;
    }

    enum Day {
        MON, TUE, WED, THU, FRI, SAT, SUN
    }
    @IR("""
            func @"caseConstantEnum" (%0 : java.type:"SwitchStatementTest$Day")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"SwitchStatementTest$Day"> = var %0 @"d";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"SwitchStatementTest$Day" = var.load %1;
                java.switch.statement %4
                    (%5 : java.type:"SwitchStatementTest$Day")java.type:"boolean" -> {
                        %6 : java.type:"boolean" = java.cor
                            ()java.type:"boolean" -> {
                                %7 : java.type:"SwitchStatementTest$Day" = field.load @java.ref:"SwitchStatementTest$Day::MON:SwitchStatementTest$Day";
                                %8 : java.type:"boolean" = eq %5 %7 @func<java.type:"boolean", java.type:"SwitchStatementTest$Day", java.type:"SwitchStatementTest$Day">;
                                yield %8;
                            }
                            ()java.type:"boolean" -> {
                                %9 : java.type:"SwitchStatementTest$Day" = field.load @java.ref:"SwitchStatementTest$Day::FRI:SwitchStatementTest$Day";
                                %10 : java.type:"boolean" = eq %5 %9 @func<java.type:"boolean", java.type:"SwitchStatementTest$Day", java.type:"SwitchStatementTest$Day">;
                                yield %10;
                            }
                            ()java.type:"boolean" -> {
                                %11 : java.type:"SwitchStatementTest$Day" = field.load @java.ref:"SwitchStatementTest$Day::SUN:SwitchStatementTest$Day";
                                %12 : java.type:"boolean" = eq %5 %11 @func<java.type:"boolean", java.type:"SwitchStatementTest$Day", java.type:"SwitchStatementTest$Day">;
                                yield %12;
                            };
                        yield %6;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %14 : java.type:"int" = constant @6;
                            yield %14;
                        };
                        yield;
                    }
                    (%15 : java.type:"SwitchStatementTest$Day")java.type:"boolean" -> {
                        %16 : java.type:"SwitchStatementTest$Day" = field.load @java.ref:"SwitchStatementTest$Day::TUE:SwitchStatementTest$Day";
                        %17 : java.type:"boolean" = eq %15 %16 @func<java.type:"boolean", java.type:"SwitchStatementTest$Day", java.type:"SwitchStatementTest$Day">;
                        yield %17;
                    }
                    ()java.type:"void" -> {
                        %18 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %19 : java.type:"int" = constant @7;
                            yield %19;
                        };
                        yield;
                    }
                    (%20 : java.type:"SwitchStatementTest$Day")java.type:"boolean" -> {
                        %21 : java.type:"boolean" = java.cor
                            ()java.type:"boolean" -> {
                                %22 : java.type:"SwitchStatementTest$Day" = field.load @java.ref:"SwitchStatementTest$Day::THU:SwitchStatementTest$Day";
                                %23 : java.type:"boolean" = eq %20 %22 @func<java.type:"boolean", java.type:"SwitchStatementTest$Day", java.type:"SwitchStatementTest$Day">;
                                yield %23;
                            }
                            ()java.type:"boolean" -> {
                                %24 : java.type:"SwitchStatementTest$Day" = field.load @java.ref:"SwitchStatementTest$Day::SAT:SwitchStatementTest$Day";
                                %25 : java.type:"boolean" = eq %20 %24 @func<java.type:"boolean", java.type:"SwitchStatementTest$Day", java.type:"SwitchStatementTest$Day">;
                                yield %25;
                            };
                        yield %21;
                    }
                    ()java.type:"void" -> {
                        %26 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %27 : java.type:"int" = constant @8;
                            yield %27;
                        };
                        yield;
                    }
                    (%28 : java.type:"SwitchStatementTest$Day")java.type:"boolean" -> {
                        %29 : java.type:"SwitchStatementTest$Day" = field.load @java.ref:"SwitchStatementTest$Day::WED:SwitchStatementTest$Day";
                        %30 : java.type:"boolean" = eq %28 %29 @func<java.type:"boolean", java.type:"SwitchStatementTest$Day", java.type:"SwitchStatementTest$Day">;
                        yield %30;
                    }
                    ()java.type:"void" -> {
                        %31 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %32 : java.type:"int" = constant @9;
                            yield %32;
                        };
                        yield;
                    };
                %33 : java.type:"java.lang.String" = var.load %3;
                return %33;
            };
            """)
    @Reflect
    private static String caseConstantEnum(Day d) {
        String r = "";
        switch (d) {
            case MON, FRI, SUN -> r += 6;
            case TUE -> r += 7;
            case THU, SAT -> r += 8;
            case WED -> r += 9;
        }
        return r;
    }

    static class Constants {
        static final int c1 = 12;
    }
    @IR("""
            func @"caseConstantOtherKindsOfExpr" (%0 : java.type:"int")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"int"> = var %0 @"i";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"int" = constant @11;
                %5 : Var<java.type:"int"> = var %4 @"eleven";
                %6 : java.type:"int" = var.load %1;
                java.switch.statement %6
                    (%7 : java.type:"int")java.type:"boolean" -> {
                        %8 : java.type:"int" = constant @1;
                        %9 : java.type:"int" = constant @15;
                        %10 : java.type:"int" = and %8 %9 @func<java.type:"int", java.type:"int", java.type:"int">;
                        %11 : java.type:"boolean" = eq %7 %10 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %11;
                    }
                    ()java.type:"void" -> {
                        %12 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %13 : java.type:"int" = constant @1;
                            yield %13;
                        };
                        yield;
                    }
                    (%14 : java.type:"int")java.type:"boolean" -> {
                        %15 : java.type:"int" = constant @4;
                        %16 : java.type:"int" = constant @1;
                        %17 : java.type:"int" = ashr %15 %16 @func<java.type:"int", java.type:"int", java.type:"int">;
                        %18 : java.type:"boolean" = eq %14 %17 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %18;
                    }
                    ()java.type:"void" -> {
                        %19 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %20 : java.type:"java.lang.String" = constant @"2";
                            yield %20;
                        };
                        yield;
                    }
                    (%21 : java.type:"int")java.type:"boolean" -> {
                        %22 : java.type:"long" = constant @3L;
                        %23 : java.type:"int" = conv %22;
                        %24 : java.type:"boolean" = eq %21 %23 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %24;
                    }
                    ()java.type:"void" -> {
                        %25 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %26 : java.type:"int" = constant @3;
                            yield %26;
                        };
                        yield;
                    }
                    (%27 : java.type:"int")java.type:"boolean" -> {
                        %28 : java.type:"int" = constant @2;
                        %29 : java.type:"int" = constant @1;
                        %30 : java.type:"int" = lshl %28 %29 @func<java.type:"int", java.type:"int", java.type:"int">;
                        %31 : java.type:"boolean" = eq %27 %30 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %31;
                    }
                    ()java.type:"void" -> {
                        %32 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %33 : java.type:"int" = constant @4;
                            yield %33;
                        };
                        yield;
                    }
                    (%34 : java.type:"int")java.type:"boolean" -> {
                        %35 : java.type:"int" = constant @10;
                        %36 : java.type:"int" = constant @2;
                        %37 : java.type:"int" = div %35 %36 @func<java.type:"int", java.type:"int", java.type:"int">;
                        %38 : java.type:"boolean" = eq %34 %37 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %38;
                    }
                    ()java.type:"void" -> {
                        %39 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %40 : java.type:"int" = constant @5;
                            yield %40;
                        };
                        yield;
                    }
                    (%41 : java.type:"int")java.type:"boolean" -> {
                        %42 : java.type:"int" = constant @12;
                        %43 : java.type:"int" = constant @6;
                        %44 : java.type:"int" = sub %42 %43 @func<java.type:"int", java.type:"int", java.type:"int">;
                        %45 : java.type:"boolean" = eq %41 %44 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %45;
                    }
                    ()java.type:"void" -> {
                        %46 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %47 : java.type:"int" = constant @6;
                            yield %47;
                        };
                        yield;
                    }
                    (%48 : java.type:"int")java.type:"boolean" -> {
                        %49 : java.type:"int" = constant @3;
                        %50 : java.type:"int" = constant @4;
                        %51 : java.type:"int" = add %49 %50 @func<java.type:"int", java.type:"int", java.type:"int">;
                        %52 : java.type:"boolean" = eq %48 %51 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %52;
                    }
                    ()java.type:"void" -> {
                        %53 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %54 : java.type:"int" = constant @7;
                            yield %54;
                        };
                        yield;
                    }
                    (%55 : java.type:"int")java.type:"boolean" -> {
                        %56 : java.type:"int" = constant @2;
                        %57 : java.type:"int" = constant @2;
                        %58 : java.type:"int" = mul %56 %57 @func<java.type:"int", java.type:"int", java.type:"int">;
                        %59 : java.type:"int" = constant @2;
                        %60 : java.type:"int" = mul %58 %59 @func<java.type:"int", java.type:"int", java.type:"int">;
                        %61 : java.type:"boolean" = eq %55 %60 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %61;
                    }
                    ()java.type:"void" -> {
                        %62 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %63 : java.type:"int" = constant @8;
                            yield %63;
                        };
                        yield;
                    }
                    (%64 : java.type:"int")java.type:"boolean" -> {
                        %65 : java.type:"int" = constant @8;
                        %66 : java.type:"int" = constant @1;
                        %67 : java.type:"int" = or %65 %66 @func<java.type:"int", java.type:"int", java.type:"int">;
                        %68 : java.type:"boolean" = eq %64 %67 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %68;
                    }
                    ()java.type:"void" -> {
                        %69 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %70 : java.type:"int" = constant @9;
                            yield %70;
                        };
                        yield;
                    }
                    (%71 : java.type:"int")java.type:"boolean" -> {
                        %72 : java.type:"int" = constant @10;
                        %73 : java.type:"boolean" = eq %71 %72 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %73;
                    }
                    ()java.type:"void" -> {
                        %74 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %75 : java.type:"int" = constant @10;
                            yield %75;
                        };
                        yield;
                    }
                    (%76 : java.type:"int")java.type:"boolean" -> {
                        %77 : java.type:"int" = var.load %5;
                        %78 : java.type:"boolean" = eq %76 %77 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %78;
                    }
                    ()java.type:"void" -> {
                        %79 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %80 : java.type:"int" = constant @11;
                            yield %80;
                        };
                        yield;
                    }
                    (%81 : java.type:"int")java.type:"boolean" -> {
                        %82 : java.type:"int" = field.load @java.ref:"SwitchStatementTest$Constants::c1:int";
                        %83 : java.type:"boolean" = eq %81 %82 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %83;
                    }
                    ()java.type:"void" -> {
                        %84 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %85 : java.type:"int" = field.load @java.ref:"SwitchStatementTest$Constants::c1:int";
                            yield %85;
                        };
                        yield;
                    }
                    (%86 : java.type:"int")java.type:"boolean" -> {
                        %87 : java.type:"int" = java.cexpression
                            ()java.type:"boolean" -> {
                                %88 : java.type:"int" = constant @1;
                                %89 : java.type:"int" = constant @0;
                                %90 : java.type:"boolean" = gt %88 %89 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                                yield %90;
                            }
                            ()java.type:"int" -> {
                                %91 : java.type:"int" = constant @13;
                                yield %91;
                            }
                            ()java.type:"int" -> {
                                %92 : java.type:"int" = constant @133;
                                yield %92;
                            };
                        %93 : java.type:"boolean" = eq %86 %87 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %93;
                    }
                    ()java.type:"void" -> {
                        %94 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %95 : java.type:"int" = constant @13;
                            yield %95;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %96 : java.type:"boolean" = constant @true;
                        yield %96;
                    }
                    ()java.type:"void" -> {
                        %97 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %98 : java.type:"java.lang.String" = constant @"an int";
                            yield %98;
                        };
                        yield;
                    };
                %99 : java.type:"java.lang.String" = var.load %3;
                return %99;
            };
            """)
    @Reflect
    private static String caseConstantOtherKindsOfExpr(int i) {
        String r = "";
        final int eleven = 11;
        switch (i) {
            case 1 & 0xF -> r += 1;
            case 4>>1 -> r += "2";
            case (int) 3L -> r += 3;
            case 2<<1 -> r += 4;
            case 10 / 2 -> r += 5;
            case 12 - 6 -> r += 6;
            case 3 + 4 -> r += 7;
            case 2 * 2 * 2 -> r += 8;
            case 8 | 1 -> r += 9;
            case (10) -> r += 10;
            case eleven -> r += 11;
            case Constants.c1 -> r += Constants.c1;
            case 1 > 0 ? 13 : 133 -> r += 13;
            default -> r += "an int";
        }
        return r;
    }

    @IR("""
            func @"caseConstantConv" (%0 : java.type:"short")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"short"> = var %0 @"a";
                %2 : java.type:"int" = constant @1;
                %3 : java.type:"short" = conv %2;
                %4 : Var<java.type:"short"> = var %3 @"s";
                %5 : java.type:"int" = constant @2;
                %6 : java.type:"byte" = conv %5;
                %7 : Var<java.type:"byte"> = var %6 @"b";
                %8 : java.type:"java.lang.String" = constant @"";
                %9 : Var<java.type:"java.lang.String"> = var %8 @"r";
                %10 : java.type:"short" = var.load %1;
                java.switch.statement %10
                    (%11 : java.type:"short")java.type:"boolean" -> {
                        %12 : java.type:"short" = var.load %4;
                        %13 : java.type:"boolean" = eq %11 %12 @func<java.type:"boolean", java.type:"short", java.type:"short">;
                        yield %13;
                    }
                    ()java.type:"void" -> {
                        %14 : java.type:"java.lang.String" = var.compound.assign %9 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %15 : java.type:"java.lang.String" = constant @"one";
                            yield %15;
                        };
                        yield;
                    }
                    (%16 : java.type:"short")java.type:"boolean" -> {
                        %17 : java.type:"byte" = var.load %7;
                        %18 : java.type:"short" = conv %17;
                        %19 : java.type:"boolean" = eq %16 %18 @func<java.type:"boolean", java.type:"short", java.type:"short">;
                        yield %19;
                    }
                    ()java.type:"void" -> {
                        %20 : java.type:"java.lang.String" = var.compound.assign %9 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %21 : java.type:"java.lang.String" = constant @"two";
                            yield %21;
                        };
                        yield;
                    }
                    (%22 : java.type:"short")java.type:"boolean" -> {
                        %23 : java.type:"int" = constant @3;
                        %24 : java.type:"short" = conv %23;
                        %25 : java.type:"boolean" = eq %22 %24 @func<java.type:"boolean", java.type:"short", java.type:"short">;
                        yield %25;
                    }
                    ()java.type:"void" -> {
                        %26 : java.type:"java.lang.String" = var.compound.assign %9 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %27 : java.type:"java.lang.String" = constant @"three";
                            yield %27;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %28 : java.type:"boolean" = constant @true;
                        yield %28;
                    }
                    ()java.type:"void" -> {
                        %29 : java.type:"java.lang.String" = var.compound.assign %9 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %30 : java.type:"java.lang.String" = constant @"else";
                            yield %30;
                        };
                        yield;
                    };
                %31 : java.type:"java.lang.String" = var.load %9;
                return %31;
            };
            """)
    @Reflect
    static String caseConstantConv(short a) {
        final short s = 1;
        final byte b = 2;
        String r = "";
        switch (a) {
            case s -> r += "one"; // identity, short -> short
            case b -> r += "two"; // widening primitive conversion, byte -> short
            case 3 -> r += "three"; // narrowing primitive conversion, int -> short
            default -> r += "else";
        }
        return r;
    }

    @IR("""
            func @"caseConstantConv2" (%0 : java.type:"java.lang.Byte")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.Byte"> = var %0 @"a";
                %2 : java.type:"int" = constant @2;
                %3 : java.type:"byte" = conv %2;
                %4 : Var<java.type:"byte"> = var %3 @"b";
                %5 : java.type:"java.lang.String" = constant @"";
                %6 : Var<java.type:"java.lang.String"> = var %5 @"r";
                %7 : java.type:"java.lang.Byte" = var.load %1;
                java.switch.statement %7
                    (%8 : java.type:"java.lang.Byte")java.type:"boolean" -> {
                        %9 : java.type:"byte" = invoke %8 @java.ref:"java.lang.Byte::byteValue():byte";
                        %10 : java.type:"int" = constant @1;
                        %11 : java.type:"byte" = conv %10;
                        %12 : java.type:"boolean" = eq %9 %11 @func<java.type:"boolean", java.type:"byte", java.type:"byte">;
                        yield %12;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = var.compound.assign %6 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %14 : java.type:"java.lang.String" = constant @"one";
                            yield %14;
                        };
                        yield;
                    }
                    (%15 : java.type:"java.lang.Byte")java.type:"boolean" -> {
                        %16 : java.type:"byte" = invoke %15 @java.ref:"java.lang.Byte::byteValue():byte";
                        %17 : java.type:"byte" = var.load %4;
                        %18 : java.type:"boolean" = eq %16 %17 @func<java.type:"boolean", java.type:"byte", java.type:"byte">;
                        yield %18;
                    }
                    ()java.type:"void" -> {
                        %19 : java.type:"java.lang.String" = var.compound.assign %6 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %20 : java.type:"java.lang.String" = constant @"two";
                            yield %20;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %21 : java.type:"boolean" = constant @true;
                        yield %21;
                    }
                    ()java.type:"void" -> {
                        %22 : java.type:"java.lang.String" = var.compound.assign %6 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %23 : java.type:"java.lang.String" = constant @"default";
                            yield %23;
                        };
                        yield;
                    };
                %24 : java.type:"java.lang.String" = var.load %6;
                return %24;
            };
            """)
    @Reflect
    static String caseConstantConv2(Byte a) {
        final byte b = 2;
        String r = "";
        switch (a) {
            case 1 -> r+= "one";
            case b -> r+= "two";
            default -> r+= "default";
        }
        return r;
    }

    @IR("""
            func @"nonEnhancedSwStatNoDefault" (%0 : java.type:"int")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"int"> = var %0 @"a";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"int" = var.load %1;
                java.switch.statement %4
                    (%5 : java.type:"int")java.type:"boolean" -> {
                        %6 : java.type:"int" = constant @1;
                        %7 : java.type:"boolean" = eq %5 %6 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %7;
                    }
                    ()java.type:"void" -> {
                        %8 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %9 : java.type:"java.lang.String" = constant @"1";
                            yield %9;
                        };
                        yield;
                    }
                    (%10 : java.type:"int")java.type:"boolean" -> {
                        %11 : java.type:"int" = constant @2;
                        %12 : java.type:"boolean" = eq %10 %11 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %12;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"int"> @compound.kind="CONCAT" ()java.type:"int" -> {
                            %14 : java.type:"int" = constant @2;
                            yield %14;
                        };
                        yield;
                    };
                %15 : java.type:"java.lang.String" = var.load %3;
                return %15;
            };
            """)
    @Reflect
    static String nonEnhancedSwStatNoDefault(int a) {
        String r = "";
        switch (a) {
            case 1 -> r += "1";
            case 2 -> r += 2;
        }
        return r;
    }

    enum E {A, B}
    @IR("""
            func @"enhancedSwStatNoDefault1" (%0 : java.type:"SwitchStatementTest$E")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"SwitchStatementTest$E"> = var %0 @"e";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"SwitchStatementTest$E" = var.load %1;
                java.switch.statement %4 @switch.handle.nulls=true
                    (%5 : java.type:"SwitchStatementTest$E")java.type:"boolean" -> {
                        %6 : java.type:"SwitchStatementTest$E" = field.load @java.ref:"SwitchStatementTest$E::A:SwitchStatementTest$E";
                        %7 : java.type:"boolean" = eq %5 %6 @func<java.type:"boolean", java.type:"SwitchStatementTest$E", java.type:"SwitchStatementTest$E">;
                        yield %7;
                    }
                    ()java.type:"void" -> {
                        %8 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.Object"> @compound.kind="CONCAT" ()java.type:"java.lang.Object" -> {
                            %9 : java.type:"SwitchStatementTest$E" = field.load @java.ref:"SwitchStatementTest$E::A:SwitchStatementTest$E";
                            yield %9;
                        };
                        yield;
                    }
                    (%10 : java.type:"SwitchStatementTest$E")java.type:"boolean" -> {
                        %11 : java.type:"SwitchStatementTest$E" = field.load @java.ref:"SwitchStatementTest$E::B:SwitchStatementTest$E";
                        %12 : java.type:"boolean" = eq %10 %11 @func<java.type:"boolean", java.type:"SwitchStatementTest$E", java.type:"SwitchStatementTest$E">;
                        yield %12;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.Object"> @compound.kind="CONCAT" ()java.type:"java.lang.Object" -> {
                            %14 : java.type:"SwitchStatementTest$E" = field.load @java.ref:"SwitchStatementTest$E::B:SwitchStatementTest$E";
                            yield %14;
                        };
                        yield;
                    }
                    (%15 : java.type:"SwitchStatementTest$E")java.type:"boolean" -> {
                        %16 : java.type:"java.lang.Object" = constant @null;
                        %17 : java.type:"boolean" = eq %15 %16 @func<java.type:"boolean", java.type:"SwitchStatementTest$E", java.type:"java.lang.Object">;
                        yield %17;
                    }
                    ()java.type:"void" -> {
                        %18 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %19 : java.type:"java.lang.String" = constant @"null";
                            yield %19;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %20 : java.type:"boolean" = constant @true;
                        yield %20;
                    }
                    ()java.type:"void" -> {
                        %21 : java.type:"java.lang.MatchException" = new @java.ref:"java.lang.MatchException::()";
                        throw %21;
                    };
                %22 : java.type:"java.lang.String" = var.load %3;
                return %22;
            };
            """)
    @Reflect
    static String enhancedSwStatNoDefault1(E e) {
        String r = "";
        switch (e) {
            case A -> r += E.A;
            case B -> r += E.B;
            case null -> r += "null";
        }
        return r;
    }

    sealed interface I permits K, J {}
    record K() implements I {}
    static final class J implements I {}
    @IR("""
            func @"enhancedSwStatNoDefault2" (%0 : java.type:"SwitchStatementTest$I")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"SwitchStatementTest$I"> = var %0 @"i";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"SwitchStatementTest$I" = var.load %1;
                %5 : java.type:"SwitchStatementTest$K" = constant @null;
                %6 : Var<java.type:"SwitchStatementTest$K"> = var %5 @"k";
                %7 : java.type:"SwitchStatementTest$J" = constant @null;
                %8 : Var<java.type:"SwitchStatementTest$J"> = var %7 @"j";
                java.switch.statement %4
                    (%9 : java.type:"SwitchStatementTest$I")java.type:"boolean" -> {
                        %10 : java.type:"boolean" = pattern.match %9
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<SwitchStatementTest$K>" -> {
                                %11 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<SwitchStatementTest$K>" = pattern.type @"k";
                                yield %11;
                            }
                            (%12 : java.type:"SwitchStatementTest$K")java.type:"void" -> {
                                var.store %6 %12;
                                yield;
                            };
                        yield %10;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %14 : java.type:"java.lang.String" = constant @"K";
                            yield %14;
                        };
                        yield;
                    }
                    (%15 : java.type:"SwitchStatementTest$I")java.type:"boolean" -> {
                        %16 : java.type:"boolean" = pattern.match %15
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<SwitchStatementTest$J>" -> {
                                %17 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<SwitchStatementTest$J>" = pattern.type @"j";
                                yield %17;
                            }
                            (%18 : java.type:"SwitchStatementTest$J")java.type:"void" -> {
                                var.store %8 %18;
                                yield;
                            };
                        yield %16;
                    }
                    ()java.type:"void" -> {
                        %19 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %20 : java.type:"java.lang.String" = constant @"J";
                            yield %20;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %21 : java.type:"boolean" = constant @true;
                        yield %21;
                    }
                    ()java.type:"void" -> {
                        %22 : java.type:"java.lang.MatchException" = new @java.ref:"java.lang.MatchException::()";
                        throw %22;
                    };
                %23 : java.type:"java.lang.String" = var.load %3;
                return %23;
            };
            """)
    @Reflect
    static String enhancedSwStatNoDefault2(I i) {
        String r = "";
        switch (i) {
            case K k -> r += "K";
            case J j -> r += "J";
        }
        return r;
    }

    @IR("""
            func @"enhancedSwStatUnconditionalPattern" (%0 : java.type:"java.lang.String")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.String"> = var %0 @"s";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.String" = var.load %1;
                %5 : java.type:"java.lang.Object" = constant @null;
                %6 : Var<java.type:"java.lang.Object"> = var %5 @"o";
                java.switch.statement %4
                    (%7 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %8 : java.type:"java.lang.String" = constant @"A";
                        %9 : java.type:"boolean" = invoke %7 %8 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %9;
                    }
                    ()java.type:"void" -> {
                        %10 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %11 : java.type:"java.lang.String" = constant @"A";
                            yield %11;
                        };
                        yield;
                    }
                    (%12 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %13 : java.type:"boolean" = pattern.match %12
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Object>" -> {
                                %14 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Object>" = pattern.type @"o";
                                yield %14;
                            }
                            (%15 : java.type:"java.lang.Object")java.type:"void" -> {
                                var.store %6 %15;
                                yield;
                            };
                        yield %13;
                    }
                    ()java.type:"void" -> {
                        %16 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %17 : java.type:"java.lang.String" = constant @"obj";
                            yield %17;
                        };
                        yield;
                    };
                %18 : java.type:"java.lang.String" = var.load %3;
                return %18;
            };
            """)
    @Reflect
    static String enhancedSwStatUnconditionalPattern(String s) {
        String r = "";
        switch (s) {
            case "A" -> r += "A";
            case Object o -> r += "obj";
        }
        return r;
    }

    @IR("""
            func @"casePatternRuleExpression" (%0 : java.type:"java.lang.Object")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.Object"> = var %0 @"o";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.Object" = var.load %1;
                %5 : java.type:"java.lang.Integer" = constant @null;
                %6 : Var<java.type:"java.lang.Integer"> = var %5 @"i";
                %7 : java.type:"java.lang.String" = constant @null;
                %8 : Var<java.type:"java.lang.String"> = var %7 @"s";
                java.switch.statement %4
                    (%9 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %10 : java.type:"boolean" = pattern.match %9
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" -> {
                                %11 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" = pattern.type @"i";
                                yield %11;
                            }
                            (%12 : java.type:"java.lang.Integer")java.type:"void" -> {
                                var.store %6 %12;
                                yield;
                            };
                        yield %10;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %14 : java.type:"java.lang.String" = constant @"integer";
                            yield %14;
                        };
                        yield;
                    }
                    (%15 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %16 : java.type:"boolean" = pattern.match %15
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.String>" -> {
                                %17 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.String>" = pattern.type @"s";
                                yield %17;
                            }
                            (%18 : java.type:"java.lang.String")java.type:"void" -> {
                                var.store %8 %18;
                                yield;
                            };
                        yield %16;
                    }
                    ()java.type:"void" -> {
                        %19 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %20 : java.type:"java.lang.String" = constant @"string";
                            yield %20;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %21 : java.type:"boolean" = constant @true;
                        yield %21;
                    }
                    ()java.type:"void" -> {
                        %22 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %23 : java.type:"java.lang.String" = constant @"else";
                            yield %23;
                        };
                        yield;
                    };
                %24 : java.type:"java.lang.String" = var.load %3;
                return %24;
            };
            """)
    @Reflect
    private static String casePatternRuleExpression(Object o) {
        String r = "";
        switch (o) {
            case Integer i -> r += "integer";
            case String s -> r+= "string";
            default -> r+= "else";
        }
        return r;
    }

    @IR("""
            func @"casePatternRuleBlock" (%0 : java.type:"java.lang.Object")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.Object"> = var %0 @"o";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.Object" = var.load %1;
                %5 : java.type:"java.lang.Integer" = constant @null;
                %6 : Var<java.type:"java.lang.Integer"> = var %5 @"i";
                %7 : java.type:"java.lang.String" = constant @null;
                %8 : Var<java.type:"java.lang.String"> = var %7 @"s";
                java.switch.statement %4
                    (%9 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %10 : java.type:"boolean" = pattern.match %9
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" -> {
                                %11 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" = pattern.type @"i";
                                yield %11;
                            }
                            (%12 : java.type:"java.lang.Integer")java.type:"void" -> {
                                var.store %6 %12;
                                yield;
                            };
                        yield %10;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %14 : java.type:"java.lang.String" = constant @"integer";
                            yield %14;
                        };
                        yield;
                    }
                    (%15 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %16 : java.type:"boolean" = pattern.match %15
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.String>" -> {
                                %17 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.String>" = pattern.type @"s";
                                yield %17;
                            }
                            (%18 : java.type:"java.lang.String")java.type:"void" -> {
                                var.store %8 %18;
                                yield;
                            };
                        yield %16;
                    }
                    ()java.type:"void" -> {
                        %19 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %20 : java.type:"java.lang.String" = constant @"string";
                            yield %20;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %21 : java.type:"boolean" = constant @true;
                        yield %21;
                    }
                    ()java.type:"void" -> {
                        %22 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %23 : java.type:"java.lang.String" = constant @"else";
                            yield %23;
                        };
                        yield;
                    };
                %24 : java.type:"java.lang.String" = var.load %3;
                return %24;
            };
            """)
    @Reflect
    private static String casePatternRuleBlock(Object o) {
        String r = "";
        switch (o) {
            case Integer i -> {
                r += "integer";
            }
            case String s -> {
                r += "string";
            }
            default -> {
                r += "else";
            }
        }
        return r;
    }

    @IR("""
            func @"casePatternStatement" (%0 : java.type:"java.lang.Object")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.Object"> = var %0 @"o";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.Object" = var.load %1;
                %5 : java.type:"java.lang.Integer" = constant @null;
                %6 : Var<java.type:"java.lang.Integer"> = var %5 @"i";
                %7 : java.type:"java.lang.String" = constant @null;
                %8 : Var<java.type:"java.lang.String"> = var %7 @"s";
                java.switch.statement %4
                    (%9 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %10 : java.type:"boolean" = pattern.match %9
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" -> {
                                %11 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" = pattern.type @"i";
                                yield %11;
                            }
                            (%12 : java.type:"java.lang.Integer")java.type:"void" -> {
                                var.store %6 %12;
                                yield;
                            };
                        yield %10;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %14 : java.type:"java.lang.String" = constant @"integer";
                            yield %14;
                        };
                        java.break;
                    }
                    (%15 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %16 : java.type:"boolean" = pattern.match %15
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.String>" -> {
                                %17 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.String>" = pattern.type @"s";
                                yield %17;
                            }
                            (%18 : java.type:"java.lang.String")java.type:"void" -> {
                                var.store %8 %18;
                                yield;
                            };
                        yield %16;
                    }
                    ()java.type:"void" -> {
                        %19 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %20 : java.type:"java.lang.String" = constant @"string";
                            yield %20;
                        };
                        java.break;
                    }
                    ()java.type:"boolean" -> {
                        %21 : java.type:"boolean" = constant @true;
                        yield %21;
                    }
                    ()java.type:"void" -> {
                        %22 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %23 : java.type:"java.lang.String" = constant @"else";
                            yield %23;
                        };
                        yield;
                    };
                %24 : java.type:"java.lang.String" = var.load %3;
                return %24;
            };
            """)
    @Reflect
    private static String casePatternStatement(Object o) {
        String r = "";
        switch (o) {
            case Integer i:
                r += "integer";
                break;
            case String s:
                r += "string";
                break;
            default:
                r += "else";
        }
        return r;
    }

    @IR("""
            func @"casePatternThrow" (%0 : java.type:"java.lang.Object")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.Object"> = var %0 @"o";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.Object" = var.load %1;
                %5 : java.type:"java.lang.Number" = constant @null;
                %6 : Var<java.type:"java.lang.Number"> = var %5 @"n";
                %7 : java.type:"java.lang.String" = constant @null;
                %8 : Var<java.type:"java.lang.String"> = var %7 @"s";
                java.switch.statement %4
                    (%9 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %10 : java.type:"boolean" = pattern.match %9
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Number>" -> {
                                %11 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Number>" = pattern.type @"n";
                                yield %11;
                            }
                            (%12 : java.type:"java.lang.Number")java.type:"void" -> {
                                var.store %6 %12;
                                yield;
                            };
                        yield %10;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.IllegalArgumentException" = new @java.ref:"java.lang.IllegalArgumentException::()";
                        throw %13;
                    }
                    (%14 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %15 : java.type:"boolean" = pattern.match %14
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.String>" -> {
                                %16 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.String>" = pattern.type @"s";
                                yield %16;
                            }
                            (%17 : java.type:"java.lang.String")java.type:"void" -> {
                                var.store %8 %17;
                                yield;
                            };
                        yield %15;
                    }
                    ()java.type:"void" -> {
                        %18 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %19 : java.type:"java.lang.String" = constant @"a string";
                            yield %19;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %20 : java.type:"boolean" = constant @true;
                        yield %20;
                    }
                    ()java.type:"void" -> {
                        %21 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %22 : java.type:"java.lang.Object" = var.load %1;
                            %23 : java.type:"java.lang.Class<?>" = invoke %22 @java.ref:"java.lang.Object::getClass():java.lang.Class";
                            %24 : java.type:"java.lang.String" = invoke %23 @java.ref:"java.lang.Class::getName():java.lang.String";
                            yield %24;
                        };
                        yield;
                    };
                %25 : java.type:"java.lang.String" = var.load %3;
                return %25;
            };
            """)
    @Reflect
    private static String casePatternThrow(Object o) {
        String r = "";
        switch (o) {
            case Number n -> throw new IllegalArgumentException();
            case String s -> r += "a string";
            default -> r += o.getClass().getName();
        }
        return r;
    }

    @IR("""
            func @"casePatternMultiLabel" (%0 : java.type:"java.lang.Object")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.Object"> = var %0 @"o";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.Object" = var.load %1;
                %5 : java.type:"java.lang.Integer" = constant @null;
                %6 : Var<java.type:"java.lang.Integer"> = var %5;
                %7 : java.type:"java.lang.Long" = constant @null;
                %8 : Var<java.type:"java.lang.Long"> = var %7;
                %9 : java.type:"java.lang.Character" = constant @null;
                %10 : Var<java.type:"java.lang.Character"> = var %9;
                %11 : java.type:"java.lang.Byte" = constant @null;
                %12 : Var<java.type:"java.lang.Byte"> = var %11;
                %13 : java.type:"java.lang.Short" = constant @null;
                %14 : Var<java.type:"java.lang.Short"> = var %13;
                java.switch.statement %4
                    (%15 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %16 : java.type:"boolean" = java.cor
                            ()java.type:"boolean" -> {
                                %17 : java.type:"boolean" = pattern.match %15
                                    ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" -> {
                                        %18 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" = pattern.type;
                                        yield %18;
                                    }
                                    (%19 : java.type:"java.lang.Integer")java.type:"void" -> {
                                        var.store %6 %19;
                                        yield;
                                    };
                                yield %17;
                            }
                            ()java.type:"boolean" -> {
                                %20 : java.type:"boolean" = pattern.match %15
                                    ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Long>" -> {
                                        %21 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Long>" = pattern.type;
                                        yield %21;
                                    }
                                    (%22 : java.type:"java.lang.Long")java.type:"void" -> {
                                        var.store %8 %22;
                                        yield;
                                    };
                                yield %20;
                            }
                            ()java.type:"boolean" -> {
                                %23 : java.type:"boolean" = pattern.match %15
                                    ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Character>" -> {
                                        %24 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Character>" = pattern.type;
                                        yield %24;
                                    }
                                    (%25 : java.type:"java.lang.Character")java.type:"void" -> {
                                        var.store %10 %25;
                                        yield;
                                    };
                                yield %23;
                            }
                            ()java.type:"boolean" -> {
                                %26 : java.type:"boolean" = pattern.match %15
                                    ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Byte>" -> {
                                        %27 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Byte>" = pattern.type;
                                        yield %27;
                                    }
                                    (%28 : java.type:"java.lang.Byte")java.type:"void" -> {
                                        var.store %12 %28;
                                        yield;
                                    };
                                yield %26;
                            }
                            ()java.type:"boolean" -> {
                                %29 : java.type:"boolean" = pattern.match %15
                                    ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Short>" -> {
                                        %30 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Short>" = pattern.type;
                                        yield %30;
                                    }
                                    (%31 : java.type:"java.lang.Short")java.type:"void" -> {
                                        var.store %14 %31;
                                        yield;
                                    };
                                yield %29;
                            };
                        yield %16;
                    }
                    ()java.type:"void" -> {
                        %32 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %33 : java.type:"java.lang.String" = constant @"integral type";
                            yield %33;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %34 : java.type:"boolean" = constant @true;
                        yield %34;
                    }
                    ()java.type:"void" -> {
                        %35 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %36 : java.type:"java.lang.String" = constant @"non integral type";
                            yield %36;
                        };
                        yield;
                    };
                %37 : java.type:"java.lang.String" = var.load %3;
                return %37;
            };
            """)
    @Reflect
    private static String casePatternMultiLabel(Object o) {
        String r = "";
        switch (o) {
            case Integer _, Long _, Character _, Byte _, Short _-> r += "integral type";
            default -> r += "non integral type";
        }
        return r;
    }

    @IR("""
            func @"casePatternGuardedMultiLabel" (%0 : java.type:"java.lang.Object")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.Object"> = var %0 @"o";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.Object" = var.load %1;
                %5 : java.type:"java.lang.Integer" = constant @null;
                %6 : Var<java.type:"java.lang.Integer"> = var %5;
                %7 : java.type:"java.lang.Long" = constant @null;
                %8 : Var<java.type:"java.lang.Long"> = var %7;
                %9 : java.type:"java.lang.Byte" = constant @null;
                %10 : Var<java.type:"java.lang.Byte"> = var %9;
                %11 : java.type:"java.lang.Short" = constant @null;
                %12 : Var<java.type:"java.lang.Short"> = var %11;
                java.switch.statement %4
                    (%13 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %14 : java.type:"boolean" = java.cand
                            ()java.type:"boolean" -> {
                                %15 : java.type:"boolean" = java.cor
                                    ()java.type:"boolean" -> {
                                        %16 : java.type:"boolean" = pattern.match %13
                                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" -> {
                                                %17 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" = pattern.type;
                                                yield %17;
                                            }
                                            (%18 : java.type:"java.lang.Integer")java.type:"void" -> {
                                                var.store %6 %18;
                                                yield;
                                            };
                                        yield %16;
                                    }
                                    ()java.type:"boolean" -> {
                                        %19 : java.type:"boolean" = pattern.match %13
                                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Long>" -> {
                                                %20 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Long>" = pattern.type;
                                                yield %20;
                                            }
                                            (%21 : java.type:"java.lang.Long")java.type:"void" -> {
                                                var.store %8 %21;
                                                yield;
                                            };
                                        yield %19;
                                    }
                                    ()java.type:"boolean" -> {
                                        %22 : java.type:"boolean" = pattern.match %13
                                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Byte>" -> {
                                                %23 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Byte>" = pattern.type;
                                                yield %23;
                                            }
                                            (%24 : java.type:"java.lang.Byte")java.type:"void" -> {
                                                var.store %10 %24;
                                                yield;
                                            };
                                        yield %22;
                                    }
                                    ()java.type:"boolean" -> {
                                        %25 : java.type:"boolean" = pattern.match %13
                                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Short>" -> {
                                                %26 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Short>" = pattern.type;
                                                yield %26;
                                            }
                                            (%27 : java.type:"java.lang.Short")java.type:"void" -> {
                                                var.store %12 %27;
                                                yield;
                                            };
                                        yield %25;
                                    };
                                yield %15;
                            }
                            ()java.type:"boolean" -> {
                                %28 : java.type:"java.lang.Object" = var.load %1;
                                %29 : java.type:"java.lang.Number" = cast %28 @java.type:"java.lang.Number";
                                %30 : java.type:"int" = invoke %29 @java.ref:"java.lang.Number::intValue():int";
                                %31 : java.type:"int" = constant @0;
                                %32 : java.type:"boolean" = gt %30 %31 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                                yield %32;
                            };
                        yield %14;
                    }
                    ()java.type:"void" -> {
                        %33 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %34 : java.type:"java.lang.String" = constant @"integral type";
                            yield %34;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %35 : java.type:"boolean" = constant @true;
                        yield %35;
                    }
                    ()java.type:"void" -> {
                        %36 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %37 : java.type:"java.lang.String" = constant @"non integral type";
                            yield %37;
                        };
                        yield;
                    };
                %38 : java.type:"java.lang.String" = var.load %3;
                return %38;
            };
            """)
    @Reflect
    private static String casePatternGuardedMultiLabel(Object o) {
        String r = "";
        switch (o) {
            case Integer _, Long _, Byte _, Short _ when ((Number)o).intValue() > 0 -> r += "integral type";
            default -> r += "non integral type";
        }
        return r;
    }

    @IR("""
            func @"casePatternWithCaseConstant" (%0 : java.type:"java.lang.Integer")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.Integer"> = var %0 @"a";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.Integer" = var.load %1;
                %5 : java.type:"java.lang.Integer" = constant @null;
                %6 : Var<java.type:"java.lang.Integer"> = var %5 @"i";
                %7 : java.type:"java.lang.Integer" = constant @null;
                %8 : Var<java.type:"java.lang.Integer"> = var %7 @"i";
                java.switch.statement %4
                    (%9 : java.type:"java.lang.Integer")java.type:"boolean" -> {
                        %10 : java.type:"int" = invoke %9 @java.ref:"java.lang.Integer::intValue():int";
                        %11 : java.type:"int" = constant @42;
                        %12 : java.type:"boolean" = eq %10 %11 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %12;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %14 : java.type:"java.lang.String" = constant @"forty two";
                            yield %14;
                        };
                        yield;
                    }
                    (%15 : java.type:"java.lang.Integer")java.type:"boolean" -> {
                        %16 : java.type:"boolean" = java.cand
                            ()java.type:"boolean" -> {
                                %17 : java.type:"boolean" = pattern.match %15
                                    ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" -> {
                                        %18 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" = pattern.type @"i";
                                        yield %18;
                                    }
                                    (%19 : java.type:"java.lang.Integer")java.type:"void" -> {
                                        var.store %6 %19;
                                        yield;
                                    };
                                yield %17;
                            }
                            ()java.type:"boolean" -> {
                                %20 : java.type:"java.lang.Integer" = var.load %6;
                                %21 : java.type:"int" = invoke %20 @java.ref:"java.lang.Integer::intValue():int";
                                %22 : java.type:"int" = constant @0;
                                %23 : java.type:"boolean" = gt %21 %22 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                                yield %23;
                            };
                        yield %16;
                    }
                    ()java.type:"void" -> {
                        %24 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %25 : java.type:"java.lang.String" = constant @"positive int";
                            yield %25;
                        };
                        yield;
                    }
                    (%26 : java.type:"java.lang.Integer")java.type:"boolean" -> {
                        %27 : java.type:"boolean" = java.cand
                            ()java.type:"boolean" -> {
                                %28 : java.type:"boolean" = pattern.match %26
                                    ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" -> {
                                        %29 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Integer>" = pattern.type @"i";
                                        yield %29;
                                    }
                                    (%30 : java.type:"java.lang.Integer")java.type:"void" -> {
                                        var.store %8 %30;
                                        yield;
                                    };
                                yield %28;
                            }
                            ()java.type:"boolean" -> {
                                %31 : java.type:"java.lang.Integer" = var.load %8;
                                %32 : java.type:"int" = invoke %31 @java.ref:"java.lang.Integer::intValue():int";
                                %33 : java.type:"int" = constant @0;
                                %34 : java.type:"boolean" = lt %32 %33 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                                yield %34;
                            };
                        yield %27;
                    }
                    ()java.type:"void" -> {
                        %35 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %36 : java.type:"java.lang.String" = constant @"negative int";
                            yield %36;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %37 : java.type:"boolean" = constant @true;
                        yield %37;
                    }
                    ()java.type:"void" -> {
                        %38 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %39 : java.type:"java.lang.String" = constant @"zero";
                            yield %39;
                        };
                        yield;
                    };
                %40 : java.type:"java.lang.String" = var.load %3;
                return %40;
            };
            """)
    @Reflect
    static String casePatternWithCaseConstant(Integer a) {
        String r = "";
        switch (a) {
            case 42 -> r += "forty two";
            case Integer i when i > 0 -> r += "positive int";
            case Integer i when i < 0 -> r += "negative int";
            default -> r += "zero";
        }
        return r;
    }

    @IR("""
            func @"caseTypePattern" (%0 : java.type:"java.lang.Object")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.Object"> = var %0 @"o";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.Object" = var.load %1;
                %5 : java.type:"java.lang.String" = constant @null;
                %6 : Var<java.type:"java.lang.String"> = var %5;
                %7 : java.type:"java.util.RandomAccess" = constant @null;
                %8 : Var<java.type:"java.util.RandomAccess"> = var %7;
                %9 : java.type:"int[]" = constant @null;
                %10 : Var<java.type:"int[]"> = var %9;
                %11 : java.type:"java.util.Stack[][]" = constant @null;
                %12 : Var<java.type:"java.util.Stack[][]"> = var %11;
                %13 : java.type:"java.util.Collection[][][]" = constant @null;
                %14 : Var<java.type:"java.util.Collection[][][]"> = var %13;
                %15 : java.type:"java.lang.Number" = constant @null;
                %16 : Var<java.type:"java.lang.Number"> = var %15 @"n";
                java.switch.statement %4
                    (%17 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %18 : java.type:"boolean" = pattern.match %17
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.String>" -> {
                                %19 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.String>" = pattern.type;
                                yield %19;
                            }
                            (%20 : java.type:"java.lang.String")java.type:"void" -> {
                                var.store %6 %20;
                                yield;
                            };
                        yield %18;
                    }
                    ()java.type:"void" -> {
                        %21 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %22 : java.type:"java.lang.String" = constant @"String";
                            yield %22;
                        };
                        yield;
                    }
                    (%23 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %24 : java.type:"boolean" = pattern.match %23
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.util.RandomAccess>" -> {
                                %25 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.util.RandomAccess>" = pattern.type;
                                yield %25;
                            }
                            (%26 : java.type:"java.util.RandomAccess")java.type:"void" -> {
                                var.store %8 %26;
                                yield;
                            };
                        yield %24;
                    }
                    ()java.type:"void" -> {
                        %27 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %28 : java.type:"java.lang.String" = constant @"RandomAccess";
                            yield %28;
                        };
                        yield;
                    }
                    (%29 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %30 : java.type:"boolean" = pattern.match %29
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<int[]>" -> {
                                %31 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<int[]>" = pattern.type;
                                yield %31;
                            }
                            (%32 : java.type:"int[]")java.type:"void" -> {
                                var.store %10 %32;
                                yield;
                            };
                        yield %30;
                    }
                    ()java.type:"void" -> {
                        %33 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %34 : java.type:"java.lang.String" = constant @"int[]";
                            yield %34;
                        };
                        yield;
                    }
                    (%35 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %36 : java.type:"boolean" = pattern.match %35
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.util.Stack[][]>" -> {
                                %37 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.util.Stack[][]>" = pattern.type;
                                yield %37;
                            }
                            (%38 : java.type:"java.util.Stack[][]")java.type:"void" -> {
                                var.store %12 %38;
                                yield;
                            };
                        yield %36;
                    }
                    ()java.type:"void" -> {
                        %39 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %40 : java.type:"java.lang.String" = constant @"Stack[][]";
                            yield %40;
                        };
                        yield;
                    }
                    (%41 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %42 : java.type:"boolean" = pattern.match %41
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.util.Collection[][][]>" -> {
                                %43 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.util.Collection[][][]>" = pattern.type;
                                yield %43;
                            }
                            (%44 : java.type:"java.util.Collection[][][]")java.type:"void" -> {
                                var.store %14 %44;
                                yield;
                            };
                        yield %42;
                    }
                    ()java.type:"void" -> {
                        %45 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %46 : java.type:"java.lang.String" = constant @"Collection[][][]";
                            yield %46;
                        };
                        yield;
                    }
                    (%47 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %48 : java.type:"boolean" = pattern.match %47
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Number>" -> {
                                %49 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Number>" = pattern.type @"n";
                                yield %49;
                            }
                            (%50 : java.type:"java.lang.Number")java.type:"void" -> {
                                var.store %16 %50;
                                yield;
                            };
                        yield %48;
                    }
                    ()java.type:"void" -> {
                        %51 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %52 : java.type:"java.lang.String" = constant @"Number";
                            yield %52;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %53 : java.type:"boolean" = constant @true;
                        yield %53;
                    }
                    ()java.type:"void" -> {
                        %54 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %55 : java.type:"java.lang.String" = constant @"something else";
                            yield %55;
                        };
                        yield;
                    };
                %56 : java.type:"java.lang.String" = var.load %3;
                return %56;
            };
            """)
    @Reflect
    static String caseTypePattern(Object o) {
        String r = "";
        switch (o) {
            case String _ -> r+= "String"; // class
            case RandomAccess _ -> r+= "RandomAccess"; // interface
            case int[] _ -> r+= "int[]"; // array primitive
            case Stack[][] _ -> r+= "Stack[][]"; // array class
            case Collection[][][] _ -> r+= "Collection[][][]"; // array interface
            case final Number n -> r+= "Number"; // final modifier
            default -> r+= "something else";
        }
        return r;
    }

    record R(Number n) {}
    @IR("""
            func @"caseRecordPattern" (%0 : java.type:"java.lang.Object")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.Object"> = var %0 @"o";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.Object" = var.load %1;
                %5 : java.type:"java.lang.Number" = constant @null;
                %6 : Var<java.type:"java.lang.Number"> = var %5 @"n";
                java.switch.statement %4
                    (%7 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %8 : java.type:"boolean" = pattern.match %7
                            ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Record<SwitchStatementTest$R>" -> {
                                %9 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Number>" = pattern.type @"n";
                                %10 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Record<SwitchStatementTest$R>" = pattern.record %9 @java.ref:"(java.lang.Number n)SwitchStatementTest$R";
                                yield %10;
                            }
                            (%11 : java.type:"java.lang.Number")java.type:"void" -> {
                                var.store %6 %11;
                                yield;
                            };
                        yield %8;
                    }
                    ()java.type:"void" -> {
                        %12 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %13 : java.type:"java.lang.String" = constant @"R(_)";
                            yield %13;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %14 : java.type:"boolean" = constant @true;
                        yield %14;
                    }
                    ()java.type:"void" -> {
                        %15 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %16 : java.type:"java.lang.String" = constant @"else";
                            yield %16;
                        };
                        yield;
                    };
                %17 : java.type:"java.lang.String" = var.load %3;
                return %17;
            };
            """)
    @Reflect
    static String caseRecordPattern(Object o) {
        String r = "";
        switch (o) {
            case R(Number n) -> r += "R(_)";
            default -> r+= "else";
        }
        return r;
    }

    @IR("""
            func @"casePatternGuard" (%0 : java.type:"java.lang.Object")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.Object"> = var %0 @"obj";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.Object" = var.load %1;
                %5 : java.type:"java.lang.String" = constant @null;
                %6 : Var<java.type:"java.lang.String"> = var %5 @"s";
                %7 : java.type:"java.lang.Number" = constant @null;
                %8 : Var<java.type:"java.lang.Number"> = var %7 @"n";
                java.switch.statement %4
                    (%9 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %10 : java.type:"boolean" = java.cand
                            ()java.type:"boolean" -> {
                                %11 : java.type:"boolean" = pattern.match %9
                                    ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.String>" -> {
                                        %12 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.String>" = pattern.type @"s";
                                        yield %12;
                                    }
                                    (%13 : java.type:"java.lang.String")java.type:"void" -> {
                                        var.store %6 %13;
                                        yield;
                                    };
                                yield %11;
                            }
                            ()java.type:"boolean" -> {
                                %14 : java.type:"java.lang.String" = var.load %6;
                                %15 : java.type:"int" = invoke %14 @java.ref:"java.lang.String::length():int";
                                %16 : java.type:"int" = constant @3;
                                %17 : java.type:"boolean" = gt %15 %16 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                                yield %17;
                            };
                        yield %10;
                    }
                    ()java.type:"void" -> {
                        %18 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %19 : java.type:"java.lang.String" = constant @"str with length > %d";
                            %20 : java.type:"java.lang.String" = var.load %6;
                            %21 : java.type:"int" = invoke %20 @java.ref:"java.lang.String::length():int";
                            %22 : java.type:"java.lang.Integer" = invoke %21 @java.ref:"java.lang.Integer::valueOf(int):java.lang.Integer";
                            %23 : java.type:"java.lang.String" = invoke %19 %22 @java.ref:"java.lang.String::formatted(java.lang.Object[]):java.lang.String" @invoke.kind="INSTANCE" @invoke.varargs=true;
                            yield %23;
                        };
                        yield;
                    }
                    (%24 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %25 : java.type:"boolean" = java.cand
                            ()java.type:"boolean" -> {
                                %26 : java.type:"boolean" = pattern.match %24
                                    ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Record<SwitchStatementTest$R>" -> {
                                        %27 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Number>" = pattern.type @"n";
                                        %28 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Record<SwitchStatementTest$R>" = pattern.record %27 @java.ref:"(java.lang.Number n)SwitchStatementTest$R";
                                        yield %28;
                                    }
                                    (%29 : java.type:"java.lang.Number")java.type:"void" -> {
                                        var.store %8 %29;
                                        yield;
                                    };
                                yield %26;
                            }
                            ()java.type:"boolean" -> {
                                %30 : java.type:"java.lang.Number" = var.load %8;
                                %31 : java.type:"java.lang.Class<?>" = invoke %30 @java.ref:"java.lang.Object::getClass():java.lang.Class";
                                %32 : java.type:"java.lang.Class" = constant @java.type:"java.lang.Double";
                                %33 : java.type:"boolean" = invoke %31 %32 @java.ref:"java.lang.Object::equals(java.lang.Object):boolean";
                                yield %33;
                            };
                        yield %25;
                    }
                    ()java.type:"void" -> {
                        %34 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %35 : java.type:"java.lang.String" = constant @"R(Double)";
                            yield %35;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %36 : java.type:"boolean" = constant @true;
                        yield %36;
                    }
                    ()java.type:"void" -> {
                        %37 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %38 : java.type:"java.lang.String" = constant @"else";
                            yield %38;
                        };
                        yield;
                    };
                %39 : java.type:"java.lang.String" = var.load %3;
                return %39;
            };
            """)
    @Reflect
    static String casePatternGuard(Object obj) {
        String r = "";
        switch (obj) {
            case String s when s.length() > 3 -> r += "str with length > %d".formatted(s.length());
            case R(Number n) when n.getClass().equals(Double.class) -> r += "R(Double)";
            default -> r += "else";
        }
        return r;
    }

    @IR("""
            func @"defaultCaseNotTheLast" (%0 : java.type:"java.lang.String")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.String"> = var %0 @"s";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.String" = var.load %1;
                java.switch.statement %4
                    ()java.type:"boolean" -> {
                        %5 : java.type:"boolean" = constant @true;
                        yield %5;
                    }
                    ()java.type:"void" -> {
                        %6 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %7 : java.type:"java.lang.String" = constant @"else";
                            yield %7;
                        };
                        yield;
                    }
                    (%8 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %9 : java.type:"java.lang.String" = constant @"M";
                        %10 : java.type:"boolean" = invoke %8 %9 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %10;
                    }
                    ()java.type:"void" -> {
                        %11 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %12 : java.type:"java.lang.String" = constant @"Mow";
                            yield %12;
                        };
                        yield;
                    }
                    (%13 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %14 : java.type:"java.lang.String" = constant @"A";
                        %15 : java.type:"boolean" = invoke %13 %14 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %15;
                    }
                    ()java.type:"void" -> {
                        %16 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %17 : java.type:"java.lang.String" = constant @"Aow";
                            yield %17;
                        };
                        yield;
                    };
                %18 : java.type:"java.lang.String" = var.load %3;
                return %18;
            };
            """)
    @Reflect
    static String defaultCaseNotTheLast(String s) {
        String r = "";
        switch (s) {
            default -> r += "else";
            case "M" -> r += "Mow";
            case "A" -> r += "Aow";
        }
        return r;
    }

    @IR("""
            func @"f" (%0 : java.type:"int")java.type:"int" -> {
                  %1 : Var<java.type:"int"> = var %0 @"i";
                  %2 : java.type:"int" = var.load %1;
                  java.switch.statement %2
                      (%3 : java.type:"int")java.type:"boolean" -> {
                          %4 : java.type:"int" = constant @0;
                          %5 : java.type:"boolean" = eq %3 %4;
                          yield %5;
                      }
                      ()java.type:"void" -> {
                          %6 : java.type:"int" = constant @0;
                          return %6;
                      };
                  %7 : java.type:"int" = constant @0;
                  return %7;
              };
            """)
    @Reflect
    static int f(int i) {
        switch (i) {
            case 0 -> {
                return 0;
            }
        }
        return 0;
    }

    @IR("""
            func @"outOfOrderFallThrought" (%0 : java.type:"int")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"int"> = var %0 @"i";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"ret";
                %4 : java.type:"int" = var.load %1;
                java.switch.statement %4
                    ()java.type:"boolean" -> {
                        %5 : java.type:"boolean" = constant @true;
                        yield %5;
                    }
                    ()java.type:"void" -> {
                        %6 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %7 : java.type:"java.lang.String" = constant @"? ";
                            yield %7;
                        };
                        java.switch.fallthrough;
                    }
                    (%8 : java.type:"int")java.type:"boolean" -> {
                        %9 : java.type:"int" = constant @4;
                        %10 : java.type:"boolean" = eq %8 %9 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %10;
                    }
                    ()java.type:"void" -> {
                        %11 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %12 : java.type:"java.lang.String" = constant @"four ";
                            yield %12;
                        };
                        java.switch.fallthrough;
                    }
                    (%13 : java.type:"int")java.type:"boolean" -> {
                        %14 : java.type:"int" = constant @2;
                        %15 : java.type:"boolean" = eq %13 %14 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %15;
                    }
                    ()java.type:"void" -> {
                        %16 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %17 : java.type:"java.lang.String" = constant @"two ";
                            yield %17;
                        };
                        java.switch.fallthrough;
                    }
                    (%18 : java.type:"int")java.type:"boolean" -> {
                        %19 : java.type:"int" = constant @3;
                        %20 : java.type:"boolean" = eq %18 %19 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %20;
                    }
                    ()java.type:"void" -> {
                        %21 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %22 : java.type:"java.lang.String" = constant @"three ";
                            yield %22;
                        };
                        java.switch.fallthrough;
                    }
                    (%23 : java.type:"int")java.type:"boolean" -> {
                        %24 : java.type:"int" = constant @1;
                        %25 : java.type:"boolean" = eq %23 %24 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %25;
                    }
                    ()java.type:"void" -> {
                        %26 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %27 : java.type:"java.lang.String" = constant @"one";
                            yield %27;
                        };
                        yield;
                    };
                %28 : java.type:"java.lang.String" = var.load %3;
                return %28;
            };
            """)
    @Reflect
    static String outOfOrderFallThrought(int i) {
        String ret = "";
        switch (i) {
            default:
                ret += "? ";
            case 4:
                ret += "four ";
            case 2:
                ret += "two ";
            case 3:
                ret += "three ";
            case 1:
                ret += "one";
        }
        return ret;
    }

    @IR("""
            func @"caseConstantPrimitiveWrapperSelector" (%0 : java.type:"java.lang.Integer")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.Integer"> = var %0 @"i";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.Integer" = var.load %1;
                java.switch.statement %4
                    (%5 : java.type:"java.lang.Integer")java.type:"boolean" -> {
                        %6 : java.type:"int" = invoke %5 @java.ref:"java.lang.Integer::intValue():int";
                        %7 : java.type:"int" = constant @1;
                        %8 : java.type:"boolean" = eq %6 %7 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %8;
                    }
                    ()java.type:"void" -> {
                        %9 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %10 : java.type:"java.lang.String" = constant @"one";
                            yield %10;
                        };
                        yield;
                    }
                    (%11 : java.type:"java.lang.Integer")java.type:"boolean" -> {
                        %12 : java.type:"boolean" = java.cor
                            ()java.type:"boolean" -> {
                                %13 : java.type:"int" = invoke %11 @java.ref:"java.lang.Integer::intValue():int";
                                %14 : java.type:"int" = constant @2;
                                %15 : java.type:"boolean" = eq %13 %14 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                                yield %15;
                            }
                            ()java.type:"boolean" -> {
                                %16 : java.type:"int" = invoke %11 @java.ref:"java.lang.Integer::intValue():int";
                                %17 : java.type:"int" = constant @3;
                                %18 : java.type:"boolean" = eq %16 %17 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                                yield %18;
                            };
                        yield %12;
                    }
                    ()java.type:"void" -> {
                        %19 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %20 : java.type:"java.lang.String" = constant @"two or three";
                            yield %20;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %21 : java.type:"boolean" = constant @true;
                        yield %21;
                    }
                    ()java.type:"void" -> {
                        %22 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %23 : java.type:"java.lang.String" = constant @"else";
                            yield %23;
                        };
                        yield;
                    };
                %24 : java.type:"java.lang.String" = var.load %3;
                return %24;
            };
            """)
    @Reflect
    static String caseConstantPrimitiveWrapperSelector(Integer i) {
        String r = "";
        switch (i) {
            case 1 -> r += "one";
            case 2, 3 -> r += "two or three";
            default -> r += "else";
        };
        return r;
    }

    @IR("""
            func @"constantLabelCasted" (%0 : java.type:"int")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"int"> = var %0 @"i";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"int" = var.load %1;
                java.switch.statement %4
                    (%5 : java.type:"int")java.type:"boolean" -> {
                        %6 : java.type:"int" = constant @1;
                        %7 : java.type:"byte" = conv %6;
                        %8 : java.type:"int" = conv %7;
                        %9 : java.type:"boolean" = eq %5 %8 @func<java.type:"boolean", java.type:"int", java.type:"int">;
                        yield %9;
                    }
                    ()java.type:"void" -> {
                        %10 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %11 : java.type:"java.lang.String" = constant @"one";
                            yield %11;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %12 : java.type:"boolean" = constant @true;
                        yield %12;
                    }
                    ()java.type:"void" -> {
                        %13 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %14 : java.type:"java.lang.String" = constant @"not one";
                            yield %14;
                        };
                        yield;
                    };
                %15 : java.type:"java.lang.String" = var.load %3;
                return %15;
            };
            """)
    @Reflect
    static String constantLabelCasted(int i) {
        String r = "";
        switch (i) {
            case (byte) 1 -> r += "one";
            default -> r += "not one";
        };
        return r;
    }

    @IR("""
            func @"caseConstantStringLiteral" (%0 : java.type:"java.lang.String")java.type:"java.lang.String" -> {
                %1 : Var<java.type:"java.lang.String"> = var %0 @"s";
                %2 : java.type:"java.lang.String" = constant @"";
                %3 : Var<java.type:"java.lang.String"> = var %2 @"r";
                %4 : java.type:"java.lang.String" = var.load %1;
                java.switch.statement %4
                    (%5 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %6 : java.type:"java.lang.String" = constant @"1";
                        %7 : java.type:"boolean" = invoke %5 %6 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                        yield %7;
                    }
                    ()java.type:"void" -> {
                        %8 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %9 : java.type:"java.lang.String" = constant @"one";
                            yield %9;
                        };
                        yield;
                    }
                    (%10 : java.type:"java.lang.String")java.type:"boolean" -> {
                        %11 : java.type:"boolean" = java.cor
                            ()java.type:"boolean" -> {
                                %12 : java.type:"java.lang.String" = constant @"2";
                                %13 : java.type:"boolean" = invoke %10 %12 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                                yield %13;
                            }
                            ()java.type:"boolean" -> {
                                %14 : java.type:"java.lang.String" = constant @"3";
                                %15 : java.type:"boolean" = invoke %10 %14 @java.ref:"java.util.Objects::equals(java.lang.Object, java.lang.Object):boolean";
                                yield %15;
                            };
                        yield %11;
                    }
                    ()java.type:"void" -> {
                        %16 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %17 : java.type:"java.lang.String" = constant @"two or three";
                            yield %17;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %18 : java.type:"boolean" = constant @true;
                        yield %18;
                    }
                    ()java.type:"void" -> {
                        %19 : java.type:"java.lang.String" = var.compound.assign %3 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %20 : java.type:"java.lang.String" = constant @"else";
                            yield %20;
                        };
                        yield;
                    };
                %21 : java.type:"java.lang.String" = var.load %3;
                return %21;
            };
            """)
    @Reflect
    static String caseConstantStringLiteral(String s) {
        String r = "";
        switch (s) {
            case "1" -> r += "one";
            case "2", "3" -> r+= "two or three";
            default -> r += "else";
        };
        return r;
    }

    @IR("""
            func @"caseBoxedGuard" (%0 : java.type:"java.lang.Object", %1 : java.type:"java.lang.Boolean")java.type:"java.lang.String" -> {
                %2 : Var<java.type:"java.lang.Object"> = var %0 @"o";
                %3 : Var<java.type:"java.lang.Boolean"> = var %1 @"B";
                %4 : java.type:"java.lang.String" = constant @"";
                %5 : Var<java.type:"java.lang.String"> = var %4 @"r";
                %6 : java.type:"java.lang.Object" = var.load %2;
                %7 : java.type:"java.lang.Object" = constant @null;
                %8 : Var<java.type:"java.lang.Object"> = var %7;
                java.switch.statement %6
                    (%9 : java.type:"java.lang.Object")java.type:"boolean" -> {
                        %10 : java.type:"boolean" = java.cand
                            ()java.type:"boolean" -> {
                                %11 : java.type:"boolean" = pattern.match %9
                                    ()java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Object>" -> {
                                        %12 : java.type:"jdk.incubator.code.dialect.java.JavaOp$Pattern$Type<java.lang.Object>" = pattern.type;
                                        yield %12;
                                    }
                                    (%13 : java.type:"java.lang.Object")java.type:"void" -> {
                                        var.store %8 %13;
                                        yield;
                                    };
                                yield %11;
                            }
                            ()java.type:"boolean" -> {
                                %14 : java.type:"java.lang.Boolean" = var.load %3;
                                %15 : java.type:"boolean" = invoke %14 @java.ref:"java.lang.Boolean::booleanValue():boolean";
                                yield %15;
                            };
                        yield %10;
                    }
                    ()java.type:"void" -> {
                        %16 : java.type:"java.lang.String" = var.compound.assign %5 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %17 : java.type:"java.lang.String" = constant @"match";
                            yield %17;
                        };
                        yield;
                    }
                    ()java.type:"boolean" -> {
                        %18 : java.type:"boolean" = constant @true;
                        yield %18;
                    }
                    ()java.type:"void" -> {
                        %19 : java.type:"java.lang.String" = var.compound.assign %5 @operator.type=func<java.type:"java.lang.String", java.type:"java.lang.String", java.type:"java.lang.String"> @compound.kind="CONCAT" ()java.type:"java.lang.String" -> {
                            %20 : java.type:"java.lang.String" = constant @"no match";
                            yield %20;
                        };
                        yield;
                    };
                %21 : java.type:"java.lang.String" = var.load %5;
                return %21;
            };
            """)
    @Reflect
    private static String caseBoxedGuard(Object o, Boolean B) {
        String r = "";
        switch (o) {
            case Object _ when B -> r += "match";
            default -> r += "no match";
        }
        return r;
    }
}
