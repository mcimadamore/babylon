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

/*
 * @test
 * @summary Smoke test for code reflection with Reflectable lambdas.
 * @modules jdk.incubator.code
 * @build ReflectableLambdaTest
 * @build CodeReflectionTester
 * @run main CodeReflectionTester ReflectableLambdaTest
 */

import jdk.incubator.code.Reflect;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;

public class ReflectableLambdaTest {

    @Reflect
    interface ReflectableRunnable extends Runnable { }

    @IR("""
            func @"f" ()java.type:"void" -> {
                %0 : java.type:"ReflectableLambdaTest$ReflectableRunnable" = lambda @lambda.isQuotable=true ()java.type:"void" -> {
                    return;
                };
                return;
            };
            """)
    static final ReflectableRunnable QUOTED_NO_PARAM_VOID = (@Reflect ReflectableRunnable) () -> { };

    @Reflect
    interface ReflectableIntSupplier extends IntSupplier { }

    @IR("""
            func @"f" ()java.type:"void" -> {
                %0 : java.type:"ReflectableLambdaTest$ReflectableIntSupplier" = lambda @lambda.isQuotable=true ()java.type:"int" -> {
                    %1 : java.type:"int" = constant @1;
                    return %1;
                };
                return;
            };
            """)
    static final ReflectableIntSupplier QUOTED_NO_PARAM_CONST = (@Reflect ReflectableIntSupplier) () -> 1;

    @Reflect
    interface ReflectableIntUnaryOperator extends IntUnaryOperator { }

    @IR("""
            func @"f" ()java.type:"void" -> {
                %0 : java.type:"ReflectableLambdaTest$ReflectableIntUnaryOperator" = lambda @lambda.isQuotable=true (%1 : java.type:"int")java.type:"int" -> {
                    %2 : Var<java.type:"int"> = var %1 @"x";
                    %3 : java.type:"int" = var.load %2;
                    return %3;
                };
                return;
            };
            """)
    static final ReflectableIntUnaryOperator QUOTED_ID = (@Reflect ReflectableIntUnaryOperator) x -> x;

    @Reflect
    interface ReflectableIntBinaryOperator extends IntBinaryOperator { }

    @IR("""
            func @"f" ()java.type:"void" -> {
                %0 : java.type:"ReflectableLambdaTest$ReflectableIntBinaryOperator" = lambda @lambda.isQuotable=true (%1 : java.type:"int", %2 : java.type:"int")java.type:"int" -> {
                    %3 : Var<java.type:"int"> = var %1 @"x";
                    %4 : Var<java.type:"int"> = var %2 @"y";
                    %5 : java.type:"int" = var.load %3;
                    %6 : java.type:"int" = var.load %4;
                    %7 : java.type:"int" = add %5 %6;
                    return %7;
                };
                return;
            };
            """)
    static final ReflectableIntBinaryOperator QUOTED_PLUS = (@Reflect ReflectableIntBinaryOperator) (x, y) -> x + y;
    @IR("""
            func @"f" ()java.type:"void" -> {
                %0 : java.type:"ReflectableLambdaTest$ReflectableRunnable" = lambda @lambda.isQuotable=true ()java.type:"void" -> {
                    %1 : java.type:"java.lang.AssertionError" = new @java.ref:"java.lang.AssertionError::()";
                    throw %1;
                };
                return;
            };
            """)
    static final ReflectableRunnable QUOTED_THROW_NO_PARAM = (@Reflect ReflectableRunnable) () -> { throw new AssertionError(); };

    @IR("""
            func @"f" (%0 : Var<java.type:"int">)java.type:"void" -> {
                %1 : java.type:"ReflectableLambdaTest$ReflectableIntUnaryOperator" = lambda @lambda.isQuotable=true (%2 : java.type:"int")java.type:"int" -> {
                    %3 : Var<java.type:"int"> = var %2 @"y";
                    %4 : java.type:"int" = var.load %0;
                    %5 : java.type:"int" = var.load %3;
                    %6 : java.type:"int" = add %4 %5;
                    return %6;
                };
                return;
            };
            """)
    static final ReflectableIntUnaryOperator QUOTED_CAPTURE_PARAM = new Object() {
        ReflectableIntUnaryOperator captureContext(int x) {
            return (@Reflect ReflectableIntUnaryOperator) y -> x + y;
        }
    }.captureContext(42);

    static class Context {
        int x, y;

        ReflectableIntUnaryOperator capture() {
            return (@Reflect ReflectableIntUnaryOperator) z -> x + y + z;
        }
    }

    @IR("""
            func @"f" (%0 : java.type:"ReflectableLambdaTest$Context")java.type:"void" -> {
                %1 : java.type:"ReflectableLambdaTest$ReflectableIntUnaryOperator" = lambda @lambda.isQuotable=true (%2 : java.type:"int")java.type:"int" -> {
                    %3 : Var<java.type:"int"> = var %2 @"z";
                    %4 : java.type:"int" = field.load %0 @java.ref:"ReflectableLambdaTest$Context::x:int";
                    %5 : java.type:"int" = field.load %0 @java.ref:"ReflectableLambdaTest$Context::y:int";
                    %6 : java.type:"int" = add %4 %5;
                    %7 : java.type:"int" = var.load %3;
                    %8 : java.type:"int" = add %6 %7;
                    return %8;
                };
                return;
            };
            """)
    static final ReflectableIntUnaryOperator QUOTED_CAPTURE_FIELD = new Context().capture();

    @Reflect
    @IR("""
            func @"captureParam" (%0 : java.type:"int")java.type:"void" -> {
                %1 : Var<java.type:"int"> = var %0 @"x";
                %2 : java.type:"ReflectableLambdaTest$ReflectableIntUnaryOperator" = lambda @lambda.isQuotable=true (%3 : java.type:"int")java.type:"int" -> {
                    %4 : Var<java.type:"int"> = var %3 @"y";
                    %5 : java.type:"int" = var.load %1;
                    %6 : java.type:"int" = var.load %4;
                    %7 : java.type:"int" = add %5 %6;
                    return %7;
                };
                %8 : Var<java.type:"ReflectableLambdaTest$ReflectableIntUnaryOperator"> = var %2 @"op";
                return;
            };
            """)
    static void captureParam(int x) {
        ReflectableIntUnaryOperator op = (@Reflect ReflectableIntUnaryOperator) y -> x + y;
    }

    int x, y;

    @Reflect
    @IR("""
            func @"captureField" (%0 : java.type:"ReflectableLambdaTest")java.type:"void" -> {
                %1 : java.type:"ReflectableLambdaTest$ReflectableIntUnaryOperator" = lambda @lambda.isQuotable=true (%2 : java.type:"int")java.type:"int" -> {
                    %3 : Var<java.type:"int"> = var %2 @"z";
                    %4 : java.type:"int" = field.load %0 @java.ref:"ReflectableLambdaTest::x:int";
                    %5 : java.type:"int" = field.load %0 @java.ref:"ReflectableLambdaTest::y:int";
                    %6 : java.type:"int" = add %4 %5;
                    %7 : java.type:"int" = var.load %3;
                    %8 : java.type:"int" = add %6 %7;
                    return %8;
                };
                %9 : Var<java.type:"ReflectableLambdaTest$ReflectableIntUnaryOperator"> = var %1 @"op";
                return;
            };
            """)
    void captureField() {
        ReflectableIntUnaryOperator op = (@Reflect ReflectableIntUnaryOperator) z -> x + y + z;
    }

    static void m() { }

    @IR("""
            func @"f" ()java.type:"void" -> {
                %0 : java.type:"ReflectableLambdaTest$ReflectableRunnable" = lambda @lambda.isQuotable=true ()java.type:"void" -> {
                    invoke @java.ref:"ReflectableLambdaTest::m():void";
                    return;
                };
                return;
            };
            """)
    static final ReflectableRunnable QUOTED_NO_PARAM_VOID_REF = (@Reflect ReflectableRunnable) ReflectableLambdaTest::m;

    static int g(int i) { return i; }

    @IR("""
            func @"f" ()java.type:"void" -> {
                %0 : java.type:"ReflectableLambdaTest$ReflectableIntUnaryOperator" = lambda @lambda.isQuotable=true (%1 : java.type:"int")java.type:"int" -> {
                    %2 : Var<java.type:"int"> = var %1 @"x$0";
                    %3 : java.type:"int" = var.load %2;
                    %4 : java.type:"int" = invoke %3 @java.ref:"ReflectableLambdaTest::g(int):int";
                    return %4;
                };
                return;
            };
            """)
    static final ReflectableIntUnaryOperator QUOTED_INT_PARAM_INT_RET_REF = (@Reflect ReflectableIntUnaryOperator) ReflectableLambdaTest::g;

    @Reflect
    interface ReflectableIntFunction<A> extends IntFunction<A> { }

    @IR("""
            func @"f" ()java.type:"void" -> {
                %0 : java.type:"ReflectableLambdaTest$ReflectableIntFunction<int[]>" = lambda @lambda.isQuotable=true (%1 : java.type:"int")java.type:"int[]" -> {
                    %2 : Var<java.type:"int"> = var %1 @"x$0";
                    %3 : java.type:"int" = var.load %2;
                    %4 : java.type:"int[]" = new %3 @java.ref:"int[]::(int)";
                    return %4;
                };
                return;
            };
            """)
    static final ReflectableIntFunction<int[]> QUOTED_INT_PARAM_ARR_RET_REF = (@Reflect ReflectableIntFunction<int[]>) int[]::new;

    static class ContextRef {
        int g(int i) { return i; }

        ReflectableIntUnaryOperator capture() {
            return (@Reflect ReflectableIntUnaryOperator) this::g;
        }
    }

    @IR("""
            func @"f" (%0 : java.type:"ReflectableLambdaTest$ContextRef")java.type:"void" -> {
                %1 : java.type:"ReflectableLambdaTest$ReflectableIntUnaryOperator" = lambda @lambda.isQuotable=true (%2 : java.type:"int")java.type:"int" -> {
                    %3 : Var<java.type:"int"> = var %2 @"x$0";
                    %4 : java.type:"int" = var.load %3;
                    %5 : java.type:"int" = invoke %0 %4 @java.ref:"ReflectableLambdaTest$ContextRef::g(int):int";
                    return %5;
                };
                return;
            };
            """)
    static final ReflectableIntUnaryOperator QUOTED_CAPTURE_THIS_REF = (@Reflect ReflectableIntUnaryOperator) new ContextRef().capture();

    static final int Z = 42;
    @IR("""
            func @"f" (%0 : Var<java.type:"int">)java.type:"void" -> {
                %1 : java.type:"ReflectableLambdaTest$ReflectableRunnable" = lambda @lambda.isQuotable=true ()java.type:"void" -> {
                    %2 : java.type:"int" = var.load %0;
                    %3 : Var<java.type:"int"> = var %2 @"x";
                    return;
                };
                return;
            };
            """)
    static ReflectableRunnable QUOTED_CAPTURE_FINAL_STATIC_FIELD = (@Reflect ReflectableRunnable) () -> {
        int x = Z;
    };

    @IR("""
            func @"f" ()java.type:"void" -> {
                  %1 : java.type:"ReflectableLambdaTest$ReflectableRunnable" = lambda @lambda.isQuotable=true ()java.type:"void" -> {
                      %2 : java.type:"int" = constant @1;
                      %3 : java.type:"int" = invoke %2 @java.ref:"ReflectableLambdaTest::n(int):int";
                      return;
                  };
                  return;
            };
            """)
    // the lambda model used to contain operation that perform unnecessary type conversion
    static ReflectableRunnable QUOTED_RETURN_VOID = (@Reflect ReflectableRunnable) () -> {
        n(1);
    };
    static int n(int i) {
        return i;
    }

    @IR("""
            func @"f" ()java.type:"void" -> {
                  %1 : java.type:"ReflectableLambdaTest$ReflectableRunnable" = lambda @lambda.isQuotable=true ()java.type:"void" -> {
                      %2 : java.type:"java.lang.Object" = new @java.ref:"java.lang.Object::()";
                      return;
                  };
                  return;
            };
            """)
    // the lambda model used to contain ReturnOp with a value, even though the lambda type is void
    static ReflectableRunnable QUOTED_EXPRESSION_RETURN_VOID = (@Reflect ReflectableRunnable) () -> new Object();

    @IR("""
            func @"f" ()java.type:"void" -> {
                  %1 : java.type:"ReflectableLambdaTest$ReflectableRunnable" = lambda @lambda.isQuotable=true ()java.type:"void" -> {
                      %2 : java.type:"java.lang.Runnable" = lambda @lambda.isQuotable=false ()java.type:"void" -> {
                          return;
                      };
                      %3 : Var<java.type:"java.lang.Runnable"> = var %2 @"r";
                      return;
                  };
                  return;
            };
            """)
    static ReflectableRunnable QUOTED_NESTED_LAMBDA = (@Reflect ReflectableRunnable) () -> {
        Runnable r = () -> {};
    };

    @IR("""
            func @"f" ()java.type:"void" -> {
                  %1 : java.type:"ReflectableLambdaTest$ReflectableRunnable" = lambda @lambda.isQuotable=true ()java.type:"void" -> {
                      %2 : java.type:"ReflectableLambdaTest$ReflectableRunnable" = lambda @lambda.isQuotable=true ()java.type:"void" -> {
                          return;
                      };
                      %3 : Var<java.type:"ReflectableLambdaTest$ReflectableRunnable"> = var %2 @"r";
                      return;
                  };
                  return;
            };
            """)
    // @@@ should this be the excepted behaviour in case we have a nested Reflectable lambda ?
    static ReflectableRunnable QUOTED_NESTED_Reflectable_LAMBDA = (@Reflect ReflectableRunnable) () -> {
        ReflectableRunnable r = (@Reflect ReflectableRunnable) () -> {};
    };
}
