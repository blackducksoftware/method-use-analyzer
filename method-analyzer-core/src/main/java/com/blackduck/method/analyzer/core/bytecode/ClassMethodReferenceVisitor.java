/*
 * method-use-analyzer
 *
 * Copyright (C) 2020 Black Duck Software, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.method.analyzer.core.bytecode;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.method.analyzer.core.model.MethodUse;
import com.blackducksoftware.method.analyzer.core.model.ReferencedMethod;
import com.google.common.collect.Multimap;

/**
 * Represents handling for traversing ASM's byte code model to accumulate method reference information within a project
 *
 * <p>
 * It is intended that one instance of this visitor be passed to ASM for evaluating all class files within a given
 * project context
 *
 * @author romeara
 */
public class ClassMethodReferenceVisitor extends ClassVisitor {

    private static final int DEFAULT_ASM_API = Opcodes.ASM9;

    private final MethodReferenceRegistry referenceRegistry;

    private String currentClassName;

    public ClassMethodReferenceVisitor() {
        this(DEFAULT_ASM_API, null);
    }

    public ClassMethodReferenceVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);

        this.referenceRegistry = new MethodReferenceRegistry();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        currentClassName = formatQualifiedName(name);

        // Exclude any further registered method calls to this class, and clear any existing references to the current
        // class which were added before exclusion
        referenceRegistry.registerExclusion(currentClassName);

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor delegateMethodVisitor = super.visitMethod(access, name, desc, signature, exceptions);

        return new MethodReferenceVisitor(super.api, delegateMethodVisitor, referenceRegistry, currentClassName, name);
    }

    @Override
    public void visitEnd() {
        currentClassName = null;

        super.visitEnd();
    }

    /**
     * @return A mapping of external method references to one or more use locations
     */
    public Multimap<ReferencedMethod, MethodUse> getReferences() {
        return referenceRegistry.getReferences();
    }

    /**
     * Converts a path-style class name to a Java qualified name
     *
     * @param internalName
     *            Path-style name to convert
     * @return A dot-separate Java qualified name for the class
     */
    private static String formatQualifiedName(String internalName) {
        Objects.requireNonNull(internalName);

        return internalName.replace('/', '.');
    }

    /**
     * Handles extracting data about specific method calls from the byte code analysis model provided by the ASM library
     *
     * @author romeara
     */
    private static class MethodReferenceVisitor extends MethodVisitor {

        /** Logger reference to output information to the application log files */
        private final Logger logger = LoggerFactory.getLogger(getClass());

        private final MethodReferenceRegistry referenceRegistry;

        private final String currentClassName;

        private final String currentMethodName;

        @Nullable
        private Integer currentLine;

        public MethodReferenceVisitor(int api, MethodVisitor methodVisitor, MethodReferenceRegistry referenceRegistry, String currentClassName,
                String currentMethodName) {
            super(api, methodVisitor);

            this.referenceRegistry = Objects.requireNonNull(referenceRegistry);
            this.currentClassName = Objects.requireNonNull(currentClassName);
            this.currentMethodName = Objects.requireNonNull(currentMethodName);
        }

        @Override
        public void visitLineNumber(final int line, final Label start) {
            currentLine = line;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            register(owner, name, descriptor);

            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitInvokeDynamicInsn(
                final String name,
                final String descriptor,
                final Handle bootstrapMethodHandle,
                final Object... bootstrapMethodArguments) {
            register(bootstrapMethodHandle.getOwner(), name, bootstrapMethodHandle.getDesc());

            for (Object methodArgument : bootstrapMethodArguments) {
                registerMethodArgument(methodArgument);
            }
        }

        @Override
        public void visitEnd() {
            currentLine = null;

            super.visitEnd();
        }

        private void registerMethodArgument(Object methodArgument) {
            if (methodArgument instanceof Handle) {
                Handle handleArgument = (Handle) methodArgument;

                register(handleArgument.getOwner(), handleArgument.getName(), handleArgument.getDesc());
            } else if (methodArgument instanceof ConstantDynamic) {
                ConstantDynamic constantDyanmicArgument = (ConstantDynamic) methodArgument;

                register(constantDyanmicArgument.getBootstrapMethod().getOwner(), constantDyanmicArgument.getBootstrapMethod().getName(),
                        constantDyanmicArgument.getBootstrapMethod().getDesc());

                for (int i = 0; i < constantDyanmicArgument.getBootstrapMethodArgumentCount(); i++) {
                    registerMethodArgument(constantDyanmicArgument.getBootstrapMethodArgument(i));
                }
            }
        }

        private void register(String owner, String name, String descriptor) {
            // Note: getClassName is specifically used in the argument/return instances as we want "." formatted in that
            // string

            // Treat all arrays as "object", instead of a unique array "class"
            String effectiveOwner = (owner.startsWith("[") ? "java/lang/Object" : owner);

            Type returnType = getReturnType(descriptor);
            Type[] arguments = getArgumentTypes(descriptor);

            List<String> argumentList = Stream.of(arguments)
                    .map(Type::getClassName)
                    .collect(Collectors.toList());

            String useReference = currentClassName + "." + currentMethodName;

            referenceRegistry.registerReference(formatQualifiedName(effectiveOwner), name, argumentList, returnType.getClassName(), useReference, currentLine);
        }

        private Type getReturnType(String descriptor) {
            try {
                return Type.getReturnType(descriptor);
            } catch (StringIndexOutOfBoundsException e) {
                // IDETECT-3909 This can occur for malformed signatures which are just the type, not the method, for an
                // unknown reason
                logger.warn("Malformed method descriptor, attempting type-only processing: {}:{} ({})", currentClassName, currentMethodName, descriptor);

                try {
                    return Type.getType(descriptor);
                } catch (StringIndexOutOfBoundsException e2) {
                    // IDETECT-3909 This can occur for malformed signatures which are just the type, not the method, for
                    // an unknown reason
                    logger.warn("Malformed method descriptor, skipping reference processing: {}:{} ({})", currentClassName, currentMethodName, descriptor);

                    return Type.getType(descriptor);
                }
            }
        }

        private Type[] getArgumentTypes(String descriptor) {
            try {
                return Type.getArgumentTypes(descriptor);
            } catch (StringIndexOutOfBoundsException e) {
                // IDETECT-3909 This can occur for malformed signatures which are just the type, not the method, for an
                // unknown reason
                logger.warn("Malformed method descriptor, skipping arguments: {}:{} ({})", currentClassName, currentMethodName, descriptor);

                return new Type[] {};
            }
        }

    }

}
