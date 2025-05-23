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

package jdk.incubator.code.parser.impl;

import jdk.incubator.code.parser.impl.Tokens.Token;
import jdk.incubator.code.parser.impl.Tokens.TokenKind;
import jdk.incubator.code.type.*;
import jdk.incubator.code.TypeElement;
import jdk.incubator.code.type.RecordTypeRef;
import jdk.incubator.code.type.impl.ConstructorRefImpl;
import jdk.incubator.code.type.impl.FieldRefImpl;
import jdk.incubator.code.type.impl.MethodRefImpl;
import jdk.incubator.code.type.impl.RecordTypeRefImpl;
import java.util.ArrayList;
import java.util.List;

public final class DescParser {
    private DescParser() {}

    /**
     * Parse an externalized type element from its serialized textual form.
     * @param desc the serialized externalized type element
     * @return the externalized type element
     */
    public static TypeElement.ExternalizedTypeElement parseExTypeElem(String desc) {
        Scanner s = Scanner.factory().newScanner(desc);
        s.nextToken();
        return parseExTypeElem(s);
    }

    /**
     * Parse a method reference from its serialized textual form.
     *
     * @param desc the serialized method reference
     * @return the method reference
     */
    public static MethodRef parseMethodRef(String desc) {
        Scanner s = Scanner.factory().newScanner(desc);
        s.nextToken();
        return parseMethodRef(s);
    }

    /**
     * Parse a constructor reference from its serialized textual form.
     *
     * @param desc the serialized constructor reference
     * @return the constructor reference
     */
    public static ConstructorRef parseConstructorRef(String desc) {
        Scanner s = Scanner.factory().newScanner(desc);
        s.nextToken();
        return parseConstructorRef(s);
    }

    /**
     * Parse a field reference from its serialized textual form.
     *
     * @param desc the serialized field reference
     * @return the field reference
     */
    public static FieldRef parseFieldRef(String desc) {
        Scanner s = Scanner.factory().newScanner(desc);
        s.nextToken();
        return parseFieldRef(s);
    }

    /**
     * Parse a record type reference from its serialized textual form.
     *
     * @param desc the serialized record type reference
     * @return the record type reference
     */
    public static RecordTypeRef parseRecordTypeRef(String desc) {
        Scanner s = Scanner.factory().newScanner(desc);
        s.nextToken();
        return parseRecordTypeRef(s);
    }

    public static TypeElement.ExternalizedTypeElement parseExTypeElem(Lexer l) {
        StringBuilder identifier = new StringBuilder();
        if (l.token().kind == TokenKind.HASH || l.token().kind == TokenKind.AMP) {
            // Quoted identifier
            Token t = l.token();
            while (t.kind != TokenKind.LT) {
                identifier.append(t.kind == TokenKind.IDENTIFIER ? t.name() : t.kind.name);
                l.nextToken();
                t = l.token();
            }
        } else {
            // type element identifier
            Tokens.Token t = l.accept(TokenKind.IDENTIFIER,
                    TokenKind.PLUS, TokenKind.SUB, TokenKind.DOT);
            identifier.append(t.kind == TokenKind.IDENTIFIER ? t.name() : t.kind.name);
            if (t.kind == TokenKind.IDENTIFIER) {
                // keep going if we see "."
                while (l.acceptIf(Tokens.TokenKind.DOT)) {
                    identifier.append(Tokens.TokenKind.DOT.name);
                    t = l.accept(Tokens.TokenKind.IDENTIFIER);
                    identifier.append(t.name());
                }
            }
        }

        // Type parameters
        List<TypeElement.ExternalizedTypeElement> args;
        if (l.token().kind == Tokens.TokenKind.LT) {
            args = new ArrayList<>();
            do {
                l.nextToken();
                TypeElement.ExternalizedTypeElement arg = parseExTypeElem(l);
                args.add(arg);
            } while (l.token().kind == Tokens.TokenKind.COMMA);
            l.accept(Tokens.TokenKind.GT);
        } else {
            args = List.of();
        }

        // Parse array-like syntax []+
        int dims = 0;
        while (l.acceptIf(Tokens.TokenKind.LBRACKET)) {
            l.accept(Tokens.TokenKind.RBRACKET);
            dims++;
        }

        TypeElement.ExternalizedTypeElement td = new TypeElement.ExternalizedTypeElement(identifier.toString(), args);
        if (dims > 0) {
            // If array-like then type definition becomes a child with identifier [+
            return new TypeElement.ExternalizedTypeElement("[".repeat(dims), List.of(td));
        } else {
            return td;
        }
    }

    static TypeElement parseTypeElement(Lexer l) {
        TypeElement.ExternalizedTypeElement typeDesc = parseExTypeElem(l);
        return CoreTypeFactory.CORE_TYPE_FACTORY.constructType(typeDesc);
    }

    static List<TypeElement> parseParameterTypes(Lexer l) {
        List<TypeElement> ptypes = new ArrayList<>();
        l.accept(Tokens.TokenKind.LPAREN);
        if (l.token().kind != Tokens.TokenKind.RPAREN) {
            ptypes.add(parseTypeElement(l));
            while (l.acceptIf(Tokens.TokenKind.COMMA)) {
                ptypes.add(parseTypeElement(l));
            }
        }
        l.accept(Tokens.TokenKind.RPAREN);
        return ptypes;
    }

    // (T, T, T, T)R
    static FunctionType parseMethodType(Lexer l) {
        List<TypeElement> ptypes = parseParameterTypes(l);
        TypeElement rtype = parseTypeElement(l);
        return FunctionType.functionType(rtype, ptypes);
    }

    static MethodRef parseMethodRef(Lexer l) {
        TypeElement refType = parseTypeElement(l);

        l.accept(Tokens.TokenKind.COLCOL);

        String methodName = l.accept(Tokens.TokenKind.IDENTIFIER).name();

        FunctionType mtype = parseMethodType(l);

        return new MethodRefImpl(refType, methodName, mtype);
    }

    static ConstructorRef parseConstructorRef(Lexer l) {
        TypeElement refType = parseTypeElement(l);

        l.accept(Tokens.TokenKind.COLCOL);

        // Constructor reference has the special name "<new>"
        l.accept(Tokens.TokenKind.LT);
        Tokens.Token t = l.accept(Tokens.TokenKind.IDENTIFIER);
        if (!t.name().equals("new")) {
            throw new IllegalArgumentException("Invalid name for constructor reference: " + t.name());
        }
        l.accept(Tokens.TokenKind.GT);

        List<TypeElement> ptypes = parseParameterTypes(l);
        return new ConstructorRefImpl(FunctionType.functionType(refType, ptypes));
    }

    static FieldRef parseFieldRef(Lexer l) {
        TypeElement refType = parseTypeElement(l);

        l.accept(Tokens.TokenKind.COLCOL);

        String fieldName = l.accept(Tokens.TokenKind.IDENTIFIER).name();

        FunctionType mtype = parseMethodType(l);
        if (!mtype.parameterTypes().isEmpty()) {
            throw new IllegalArgumentException();
        }
        return new FieldRefImpl(refType, fieldName, mtype.returnType());
    }

    static RecordTypeRef parseRecordTypeRef(Lexer l) {
        List<RecordTypeRef.ComponentRef> components = new ArrayList<>();
        l.accept(Tokens.TokenKind.LPAREN);
        if (l.token().kind != Tokens.TokenKind.RPAREN) {
            do {
                TypeElement componentType = parseTypeElement(l);
                String componentName = l.accept(Tokens.TokenKind.IDENTIFIER).name();

                components.add(new RecordTypeRef.ComponentRef(componentType, componentName));
            } while(l.acceptIf(Tokens.TokenKind.COMMA));
        }
        l.accept(Tokens.TokenKind.RPAREN);
        TypeElement recordType = parseTypeElement(l);
        return new RecordTypeRefImpl(recordType, components);
    }
}
