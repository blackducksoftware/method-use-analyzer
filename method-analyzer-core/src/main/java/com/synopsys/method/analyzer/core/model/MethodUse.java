/*
 * method-use-analyzer
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.method.analyzer.core.model;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;

/**
 * Represents a specific instance of a reference to an external method (class and line number)
 *
 * @author romeara
 */
public class MethodUse {

    private final String qualifiedMethodName;

    private final Integer lineNumber;

    /**
     * @param className
     *            The qualified name of the source class and method name using the method
     * @param lineNumber
     *            The line number the method use is on, if available
     */
    public MethodUse(String className, @Nullable Integer lineNumber) {
        this.qualifiedMethodName = Objects.requireNonNull(className);
        this.lineNumber = lineNumber;
    }

    public String getQualifiedMethodName() {
        return qualifiedMethodName;
    }

    public Optional<Integer> getLineNumber() {
        return Optional.ofNullable(lineNumber);
    }

    public String toSignature() {
        return getQualifiedMethodName() + ":" + getLineNumber()
                .map(Object::toString)
                .orElse("?");
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQualifiedMethodName(),
                getLineNumber());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;

        if (obj instanceof MethodUse) {
            MethodUse compare = (MethodUse) obj;

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
