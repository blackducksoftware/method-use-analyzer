/*
 * method-use-analyzer
 *
 * Copyright (C) 2022 Synopsys Inc.
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
package com.synopsys.method.analyzer.core.report;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;

/**
 * Represents JSON data for a file which failed to parse within an analyzed project
 *
 * <p>
 * Fields within correspond to a defined file format - alterations require evaluation for backwards compatibility and
 * required version control
 *
 * @author romeara
 */
public class BrokenFileJson {

    private final String path;

    private final String error;

    public BrokenFileJson(String path, @Nullable String error) {
        this.path = Objects.requireNonNull(path);
        this.error = error;
    }

    public String getPath() {
        return path;
    }

    @Nullable
    public String getError() {
        return error;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(),
                getError());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;

        if (obj instanceof BrokenFileJson) {
            BrokenFileJson compare = (BrokenFileJson) obj;

            result = Objects.equals(compare.getPath(), getPath())
                    && Objects.equals(compare.getError(), getError());
        }

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).omitNullValues()
                .add("path", getPath())
                .add("error", getError())
                .toString();
    }

}
