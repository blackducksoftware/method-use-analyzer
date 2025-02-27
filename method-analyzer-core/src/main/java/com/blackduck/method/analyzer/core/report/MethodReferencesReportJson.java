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

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;

/**
 * Represents full JSON contents for a report on multiple methods referenced within an analyzed project with data on
 * where it is used
 *
 * <p>
 * Fields within correspond to a defined file format - alterations require evaluation for backwards compatibility and
 * required version control
 *
 * @author romeara
 */
public final class MethodReferencesReportJson {

    private final List<ReferencedMethodUsesJson> methodUses;

    public MethodReferencesReportJson(List<ReferencedMethodUsesJson> methodUses) {
        this.methodUses = Objects.requireNonNull(methodUses);
    }

    public List<ReferencedMethodUsesJson> getMethodUses() {
        return methodUses;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMethodUses());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;

        if (obj instanceof MethodReferencesReportJson) {
            MethodReferencesReportJson compare = (MethodReferencesReportJson) obj;

            result = Objects.equals(compare.getMethodUses(), getMethodUses());
        }

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).omitNullValues()
                .add("methodUses", getMethodUses())
                .toString();
    }

}
