package jdk.incubator.code.dialect.java;

import jdk.incubator.code.dialect.core.FunctionType;

/**
 * A symbolic reference to a Java constructor or method. The type of an executable reference
 * is always a function type.
 */
public sealed interface ExecutableRef extends MemberRef permits ConstructorRef, MethodRef {
    @Override
    FunctionType type();
}
