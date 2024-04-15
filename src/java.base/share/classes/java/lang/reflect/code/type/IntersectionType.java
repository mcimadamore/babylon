/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package java.lang.reflect.code.type;

import java.util.List;

/**
 * An intersection type.
 */
public final class IntersectionType implements JavaType {

    final List<JavaType> components;

    IntersectionType(List<JavaType> components) {
        this.components = components;
    }

    /**
     * {@return the type-variable name}
     */
    public List<JavaType> components() {
        return components;
    }

    @Override
    public TypeDefinition toTypeDefinition() {
        return new TypeDefinition("&",
                components.stream()
                        .map(JavaType::toTypeDefinition).toList());
    }

    @Override
    public String toString() {
        return toTypeDefinition().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof IntersectionType that &&
                components.equals(that.components);
    }

    @Override
    public int hashCode() {
        return components.hashCode();
    }

    @Override
    public JavaType toBasicType() {
        throw new UnsupportedOperationException("Interesection type");
    }

    @Override
    public String toNominalDescriptorString() {
        throw new UnsupportedOperationException("Intersection type");
    }

    @Override
    public boolean isClass() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }
}
