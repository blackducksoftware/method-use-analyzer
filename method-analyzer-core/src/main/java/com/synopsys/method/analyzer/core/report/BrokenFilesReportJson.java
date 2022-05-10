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

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;

/**
 * Represents full JSON contents for a report on broken files which failed to parse within an analyzed project
 *
 * <p>
 * Fields within correspond to a defined file format - alterations require evaluation for backwards compatibility and
 * required version control
 *
 * @author romeara
 */
public final class BrokenFilesReportJson {

    private final List<BrokenFileJson> brokenFiles;

    public BrokenFilesReportJson(List<BrokenFileJson> brokenFiles) {
        this.brokenFiles = Objects.requireNonNull(brokenFiles);
    }

    public List<BrokenFileJson> getBrokenFiles() {
        return brokenFiles;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBrokenFiles());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;

        if (obj instanceof BrokenFilesReportJson) {
            BrokenFilesReportJson compare = (BrokenFilesReportJson) obj;

            result = Objects.equals(compare.getBrokenFiles(), getBrokenFiles());
        }

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).omitNullValues()
                .add("brokenFiles", getBrokenFiles())
                .toString();
    }

}
