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
package com.synopsys.method.analyzer.test.core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Functions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.synopsys.method.analyzer.core.MethodUseAnalyzer;
import com.synopsys.method.analyzer.core.report.MethodIdsReportJson;
import com.synopsys.method.analyzer.core.report.MethodReferencesReportJson;
import com.synopsys.method.analyzer.core.report.ReferencedMethodUsesJson;

public class MethodUseAnalyzerTest {

    private static final Path TEST_PROJECT_DIRECTORY = Paths.get(System.getProperty(TestProperties.TEST_PROJECT_DIRECTORY));

    private static final Gson GSON = new Gson();

    // Not an exhaustive list of every use in the output report, jsut a known set
    private static final Multimap<String, String> EXPECTED_USES = HashMultimap.create();

    static {
        EXPECTED_USES.put("java.lang.Object.<init>():void", "com.synopsys.method.analyzer.test.project.BasicTestClass.<init>:28");
        EXPECTED_USES.put("java.lang.System.getProperty(java.lang.String):java.lang.String",
                "com.synopsys.method.analyzer.test.project.BasicTestClass.<init>:30");
    }

    @Test
    public void analyze() throws Exception {
        MethodUseAnalyzer analyzer = new MethodUseAnalyzer();

        Path resultFile = analyzer.analyze(TEST_PROJECT_DIRECTORY, Files.createTempDirectory("blackduck-method-uses-analyzer-test"), null);

        Assert.assertNotNull(resultFile);
        Assert.assertTrue(Files.exists(resultFile));

        Path resultExpandedDirectory = unzip(resultFile);

        Path expectedReferencedMethod = resultExpandedDirectory.resolve("referenced-methods").resolve("referenced-methods-0.json");
        Path expectedReferencedMethodUses = resultExpandedDirectory.resolve("referenced-method-uses").resolve("referenced-method-uses-0.json");

        Assert.assertTrue(Files.exists(expectedReferencedMethod));
        Assert.assertTrue(Files.exists(expectedReferencedMethodUses));

        MethodIdsReportJson methodIdsReport = null;
        MethodReferencesReportJson methodReferencesReport = null;

        try (BufferedReader reader = Files.newBufferedReader(expectedReferencedMethod)) {
            methodIdsReport = GSON.fromJson(reader, MethodIdsReportJson.class);
        }

        try (BufferedReader reader = Files.newBufferedReader(expectedReferencedMethodUses)) {
            methodReferencesReport = GSON.fromJson(reader, MethodReferencesReportJson.class);
        }

        Assert.assertNotNull(methodIdsReport);
        Assert.assertNotNull(methodReferencesReport);

        Map<String, ReferencedMethodUsesJson> usesById = methodReferencesReport.getMethodUses().stream()
                .collect(Collectors.toMap(use -> use.getMethod().getId(), Functions.identity()));

        Assert.assertEquals(methodIdsReport.getMethodIds().size(), usesById.size());
        Assert.assertTrue(methodIdsReport.getMethodIds().containsAll(usesById.keySet()));

        // Expect specific number of external uses within results
        Assert.assertEquals(methodIdsReport.getMethodIds().size(), 10);

        // Check that specific known uses are detected
        Multimap<String, String> foundUses = HashMultimap.create();

        for (ReferencedMethodUsesJson value : methodReferencesReport.getMethodUses()) {
            String formattedInput = value.getMethod().getInputs().stream()
                    .collect(Collectors.joining(","));
            String formattedMethod = value.getMethod().getMethodOwner() + "." + value.getMethod().getMethodName() + "(" + formattedInput + "):"
                    + value.getMethod().getOutput();

            foundUses.putAll(formattedMethod, value.getUses());
        }

        for (Entry<String, String> expectedEntry : EXPECTED_USES.entries()) {
            Assert.assertTrue(foundUses.containsEntry(expectedEntry.getKey(), expectedEntry.getValue()),
                    "Did not find " + expectedEntry.getKey() + " (" + expectedEntry.getValue() + ")");
        }
    }

    // TODO utility? shared with report generator test code
    private Path unzip(Path zipArchive) throws IOException {
        Path outputDirectory = Files.createTempDirectory("blackduck-method-uses-test-output");

        byte[] buffer = new byte[1024];

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipArchive.toFile()))) {
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                try {
                    Path newPath = outputDirectory.resolve(zipEntry.getName());

                    Files.createDirectories(newPath.getParent());
                    Files.createFile(newPath);

                    try (FileOutputStream fos = new FileOutputStream(newPath.toFile())) {
                        int len;

                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                } finally {
                    zis.closeEntry();
                }

                zipEntry = zis.getNextEntry();
            }
        }

        return outputDirectory;
    }

}
