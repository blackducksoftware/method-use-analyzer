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
package com.blackduck.method.analyzer.test.project;

import java.util.Collections;
import java.util.stream.Collectors;

public class BasicTestClass {

    public final String string = System.getProperty("will.be.null");

    public String getThing() {
        getInternal();

        return String.format("base %s", "insert");
    }

    private String getInternal() {
        return "internal";
    }

    public Long getDynamic() {
        return Collections.singletonList("s").stream()
                .map(String::toUpperCase)
                .collect(Collectors.counting());
    }

}
