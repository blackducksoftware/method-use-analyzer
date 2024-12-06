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
package com.blackduck.method.analyzer.core.model;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;

/**
 * Represents a method referenced within an analyzed set of class files
 *
 * @author romeara
 */
public class ReferencedMethod {

    private final String methodOwner;

    private final String methodName;

    private final List<String> inputs;

    private final String output;

    /**
     * @param methodOwner
     *            The class the method is defined in
     * @param methodName
     *            The name of the method
     * @param inputs
     *            An order list of the classes which comprise the parameters of the method
     * @param output
     *            The class the method returns, or "void"
     */
    public ReferencedMethod(String methodOwner, String methodName, List<String> inputs, String output) {
        this.methodOwner = Objects.requireNonNull(methodOwner);
        this.methodName = Objects.requireNonNull(methodName);
        this.inputs = Objects.requireNonNull(inputs);
        this.output = Objects.requireNonNull(output);
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
        return Objects.hash(getMethodOwner(),
                getMethodName(),
                getInputs(),
                getOutput());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;

        if (obj instanceof ReferencedMethod) {
            ReferencedMethod compare = (ReferencedMethod) obj;

            result = Objects.equals(compare.getMethodOwner(), getMethodOwner())
                    && Objects.equals(compare.getMethodName(), getMethodName())
                    && Objects.equals(compare.getInputs(), getInputs())
                    && Objects.equals(compare.getOutput(), getOutput());
        }

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).omitNullValues()
                .add("methodOwner", getMethodOwner())
                .add("methodName", getMethodName())
                .add("inputs", getInputs())
                .add("output", getOutput())
                .toString();
    }

}
