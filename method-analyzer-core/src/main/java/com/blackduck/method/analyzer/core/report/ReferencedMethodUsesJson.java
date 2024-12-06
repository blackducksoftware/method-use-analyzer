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
package com.blackduck.method.analyzer.core.report;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.blackduck.method.analyzer.core.model.MethodUse;
import com.blackduck.method.analyzer.core.model.ReferencedMethod;
import com.google.common.base.MoreObjects;

/**
 * Represents JSON data for a method referenced within an analyzed project with data on where it is used
 *
 * <p>
 * Fields within correspond to a defined file format - alterations require evaluation for backwards compatibility and
 * required version control
 *
 * @author romeara
 */
public class ReferencedMethodUsesJson {

    private final ReferencedMethodJson method;

    private final Collection<MethodUseJson> uses;

    public ReferencedMethodUsesJson(String id, ReferencedMethod method, Collection<MethodUse> uses) {
        Objects.requireNonNull(uses);

        this.method = new ReferencedMethodJson(id, method.getMethodOwner(), method.getMethodName(), method.getInputs(), method.getOutput());
        this.uses = uses.stream()
                .map(use -> new MethodUseJson(use.getQualifiedMethodName(), use.getLineNumber().orElse(null)))
                .collect(Collectors.toList());
    }

    public ReferencedMethodJson getMethod() {
        return method;
    }

    public Collection<MethodUseJson> getUses() {
        return uses;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMethod(),
                getUses());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;

        if (obj instanceof ReferencedMethodUsesJson) {
            ReferencedMethodUsesJson compare = (ReferencedMethodUsesJson) obj;

            result = Objects.equals(compare.getMethod(), getMethod())
                    && Objects.equals(compare.getUses(), getUses());
        }

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).omitNullValues()
                .add("method", getMethod())
                .add("uses", getUses())
                .toString();
    }

    /**
     * Represents JSON data for the method referenced within an analyzed project
     *
     * @author romeara
     */
    public static final class ReferencedMethodJson {

        private final String id;

        private final String methodOwner;

        private final String methodName;

        private final List<String> inputs;

        private final String output;

        public ReferencedMethodJson(String id, String methodOwner, String methodName, List<String> inputs, String output) {
            this.id = Objects.requireNonNull(id);
            this.methodOwner = Objects.requireNonNull(methodOwner);
            this.methodName = Objects.requireNonNull(methodName);
            this.inputs = Objects.requireNonNull(inputs);
            this.output = Objects.requireNonNull(output);
        }

        public String getId() {
            return id;
        }

        public String getMethodOwner() {
            return methodOwner;
        }

        public String getMethodName() {
            return methodName;
        }

        public List<String> getInputs() {
            return inputs;
        }

        public String getOutput() {
            return output;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId(),
                    getMethodOwner(),
                    getMethodName(),
                    getInputs(),
                    getOutput());
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            boolean result = false;

            if (obj instanceof ReferencedMethodJson) {
                ReferencedMethodJson compare = (ReferencedMethodJson) obj;

                result = Objects.equals(compare.getId(), getId())
                        && Objects.equals(compare.getMethodOwner(), getMethodOwner())
                        && Objects.equals(compare.getMethodName(), getMethodName())
                        && Objects.equals(compare.getInputs(), getInputs())
                        && Objects.equals(compare.getOutput(), getOutput());
            }

            return result;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass()).omitNullValues()
                    .add("id", getId())
                    .add("methodOwner", getMethodOwner())
                    .add("methodName", getMethodName())
                    .add("inputs", getInputs())
                    .add("output", getOutput())
                    .toString();
        }

    }

    /**
     * Represents JSON data for a specific use of a method within an analyzed project
     *
     * @author romeara
     */
    public static final class MethodUseJson {

        private final String qualifiedMethodName;

        @Nullable
        private final Integer lineNumber;

        public MethodUseJson(String qualifiedMethodName, Integer lineNumber) {
            this.qualifiedMethodName = Objects.requireNonNull(qualifiedMethodName);
            this.lineNumber = lineNumber;
        }

        public String getQualifiedMethodName() {
            return qualifiedMethodName;
        }

        @Nullable
        public Integer getLineNumber() {
            return lineNumber;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getQualifiedMethodName(),
                    getLineNumber());
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            boolean result = false;

            if (obj instanceof ReferencedMethodUsesJson.MethodUseJson) {
                ReferencedMethodUsesJson.MethodUseJson compare = (ReferencedMethodUsesJson.MethodUseJson) obj;

                result = Objects.equals(compare.getQualifiedMethodName(), getQualifiedMethodName())
                        && Objects.equals(compare.getLineNumber(), getLineNumber());
            }

            return result;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass()).omitNullValues()
                    .add("qualifiedMethodName", getQualifiedMethodName())
                    .add("lineNumber", getLineNumber())
                    .toString();
        }

    }

}
