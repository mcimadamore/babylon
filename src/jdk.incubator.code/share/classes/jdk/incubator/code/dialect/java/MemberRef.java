package jdk.incubator.code.dialect.java;

import jdk.incubator.code.TypeElement;

/**
 * A symbolic reference to a Java class member (a constructor, a method or a field).
 * A member reference has an {@linkplain #refType() owner type}
 * and a {@linkplain #type() type}.
 */
public sealed interface MemberRef extends JavaRef permits ExecutableRef, FieldRef {
    /**
     * {@return the reference owner type}
     */
    TypeElement refType();
    /**
     * {@return the reference type}
     */
    TypeElement type();
}
