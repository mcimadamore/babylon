/*
 * @test /nodynamiccopyright/
 * @modules jdk.incubator.code
 * @compile/fail/ref=TestNoCodeReflectionInInnerClasses.out -Xlint:-incubating -XDrawDiagnostics TestNoCodeReflectionInInnerClasses.java
 */

import jdk.incubator.code.*;

class TestNoCodeReflectionInInnerClasses {
    class Inner {
        @Reflect
        public void test1() { }

        @Reflect
        interface ReflectableRunnable extends Runnable { }

        void test2() {
            ReflectableRunnable q = (@Reflect ReflectableRunnable) () -> { };
        }

        void test3() {
            ReflectableRunnable q = (@Reflect ReflectableRunnable) this::test2;
        }
    }
}
