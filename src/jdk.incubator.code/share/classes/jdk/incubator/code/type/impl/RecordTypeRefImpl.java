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

package jdk.incubator.code.type.impl;

import jdk.incubator.code.type.MethodRef;
import jdk.incubator.code.type.RecordTypeRef;
import jdk.incubator.code.TypeElement;
import java.util.List;

import static java.util.stream.Collectors.joining;

public final class RecordTypeRefImpl implements RecordTypeRef {
    final TypeElement recordType;
    final List<ComponentRef> components;

    public RecordTypeRefImpl(TypeElement recordType, List<ComponentRef> components) {
        this.recordType = recordType;
        this.components = List.copyOf(components);
    }

    @Override
    public TypeElement recordType() {
        return recordType;
    }

    @Override
    public List<ComponentRef> components() {
        return components;
    }

    @Override
    public MethodRef methodForComponent(int i) {
        if (i < 0 || i >= components.size()) {
            throw new IndexOutOfBoundsException();
        }

        ComponentRef c = components.get(i);
        return MethodRef.method(recordType, c.name(), c.type());
    }

    @Override
    public String toString() {
        return components.stream()
                .map(c -> c.type().externalize() + " " + c.name())
                .collect(joining(", ", "(", ")")) +
                recordType.externalize();
    }

}
