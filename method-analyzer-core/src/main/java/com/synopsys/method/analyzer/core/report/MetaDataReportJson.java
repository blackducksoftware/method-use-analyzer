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
package com.synopsys.method.analyzer.core.report;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;

/**
 * Represents full JSON contents for a report of meta-data associated with an analysis
 *
 * <p>
 * Fields within correspond to a defined file format - alterations require evaluation for backwards compatibility and
 * required version control
 *
 * @author romeara
 */
public class MetaDataReportJson {

    private final String hostName;

    private final String analyzedDirectory;

    @Nullable
    private final String projectName;

    public MetaDataReportJson(String hostName, String analyzedDirectory, @Nullable String projectName) {
        this.hostName = Objects.requireNonNull(hostName);
        this.analyzedDirectory = Objects.requireNonNull(analyzedDirectory);
        this.projectName = projectName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getAnalyzedDirectory() {
        return analyzedDirectory;
    }

    @Nullable
    public String getProjectName() {
        return projectName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHostName(),
                getAnalyzedDirectory(),
                getProjectName());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;

        if (obj instanceof MetaDataReportJson) {
            MetaDataReportJson compare = (MetaDataReportJson) obj;

            result = Objects.equals(compare.getHostName(), getHostName())
                    && Objects.equals(compare.getAnalyzedDirectory(), getAnalyzedDirectory())
                    && Objects.equals(compare.getProjectName(), getProjectName());
        }

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).omitNullValues()
                .add("hostName", getHostName())
                .add("analyzedDirectory", getAnalyzedDirectory())
                .add("projectName", getProjectName())
                .toString();
    }

}
