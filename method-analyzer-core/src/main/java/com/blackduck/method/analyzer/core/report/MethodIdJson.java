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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.blackduck.method.analyzer.core.model.ReferencedMethod;
import com.google.common.base.MoreObjects;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

/**
 * Represents JSON data for a opaque identifiers of a method referenced within an analyzed project
 *
 * <p>
 * Fields within correspond to a defined file format - alterations require evaluation for backwards compatibility and
 * required version control
 *
 * @author romeara
 */
public class MethodIdJson {

    private final String signature;

    private final String methodOwner;

    private final String methodName;

    private final String inputs;

    private final String outputs;

    public MethodIdJson(ReferencedMethod method) {
        Objects.requireNonNull(method);

        String inputsString = method.getInputs().stream()
                .map(String::trim)
                .collect(Collectors.joining(","));

        String signature = new StringBuilder()
                .append(method.getMethodOwner().trim()).append('.').append(method.getMethodName().trim())
                .append('(').append(inputsString).append(')')
                .append(':').append(method.getOutput().trim())
                .toString();

        this.signature = generateId(signature);
        methodOwner = generateId(method.getMethodOwner());
        methodName = generateId(method.getMethodName());
        inputs = generateId(inputsString);
        outputs = generateId(method.getOutput());
    }

    public String getSignature() {
        return signature;
    }

    public String getMethodOwner() {
        return methodOwner;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getInputs() {
        return inputs;
    }

    public String getOutput() {
        return outputs;
    }

    /**
     * Generates a unique, opaque ID for a given referenced method
     *
     * <p>
     * This generated ID is matched with algorithms on the KnowledgeBase cloud service - do NOT alter this generation
     * without involving the KnowledgeBase team and versioning the report format
     *
     * @param method
     *            The method to identify
     * @return An unique, opaque ID
     */
    private String generateId(String signature) {
        Objects.requireNonNull(signature);

        HashCode sha256 = Hashing.sha256()
                .hashString(signature, StandardCharsets.UTF_8);

        return Base64.getEncoder().encodeToString(sha256.asBytes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSignature(),
                getMethodOwner(),
                getMethodName(),
                getInputs(),
                getOutput());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;

        if (obj instanceof MethodIdJson) {
            MethodIdJson compare = (MethodIdJson) obj;

            result = Objects.equals(compare.getSignature(), getSignature())
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
                .add("signature", getSignature())
                .add("methodOwner", getMethodOwner())
                .add("methodName", getMethodName())
                .add("inputs", getInputs())
                .add("outputs", getOutput())
                .toString();
    }

}
