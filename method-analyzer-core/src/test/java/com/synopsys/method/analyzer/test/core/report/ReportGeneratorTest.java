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
package com.synopsys.method.analyzer.test.core.report;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Functions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.synopsys.method.analyzer.core.model.ReferencedMethod;
import com.synopsys.method.analyzer.core.report.MethodIdsReportJson;
import com.synopsys.method.analyzer.core.report.MethodReferencesReportJson;
import com.synopsys.method.analyzer.core.report.ReferencedMethodUsesJson;
import com.synopsys.method.analyzer.core.report.ReportGenerator;

public class ReportGeneratorTest {

    private static final Gson GSON = new Gson();

    private Path testReportDirectory;

    private ReportGenerator reportGenerator;

    @BeforeClass
    public void setup() throws Exception {
        testReportDirectory = Files.createTempDirectory("blackduck-method-uses-test");
        reportGenerator = new ReportGenerator();
    }

    @Test
    public void simpleReport() throws Exception {
        Multimap<ReferencedMethod, String> references = HashMultimap.create();
        references.put(new ReferencedMethod("methodOwner", "methodName", Collections.singletonList("input"), "output"), "use");

        Path result = reportGenerator.generateReport(references, testReportDirectory, "simpleReport");

        Assert.assertNotNull(result);
        Assert.assertTrue(Files.exists(result));

        Path resultExpandedDirectory = unzip(result);

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

        ReferencedMethodUsesJson resultJson = usesById.values().stream().findFirst().get();

        Assert.assertEquals(usesById.size(), 1);
        Assert.assertEquals(resultJson.getMethod().getMethodName(), "methodName");
        Assert.assertEquals(resultJson.getMethod().getMethodOwner(), "methodOwner");
        Assert.assertEquals(resultJson.getMethod().getOutput(), "output");
        Assert.assertEquals(resultJson.getMethod().getInputs(), Collections.singletonList("input"));
        Assert.assertEquals(resultJson.getUses().size(), 1);
        Assert.assertTrue(resultJson.getUses().contains("use"));
    }

    @Test
    public void partitionedReport() throws Exception {
        Multimap<ReferencedMethod, String> references = HashMultimap.create();

        for (int i = 0; i < 2000; i++) {
            references.put(new ReferencedMethod("methodOwner" + i, "methodName" + i, Collections.singletonList("input" + i), "output" + i), "use" + i);
        }

        Path result = reportGenerator.generateReport(references, testReportDirectory, "simpleReport");

        Assert.assertNotNull(result);
        Assert.assertTrue(Files.exists(result));

        Path resultExpandedDirectory = unzip(result);

        Path expectedReferencedMethod1 = resultExpandedDirectory.resolve("referenced-methods").resolve("referenced-methods-0.json");
        Path expectedReferencedMethodUses1 = resultExpandedDirectory.resolve("referenced-method-uses").resolve("referenced-method-uses-0.json");
        Path expectedReferencedMethod2 = resultExpandedDirectory.resolve("referenced-methods").resolve("referenced-methods-1.json");
        Path expectedReferencedMethodUses2 = resultExpandedDirectory.resolve("referenced-method-uses").resolve("referenced-method-uses-1.json");

        Assert.assertTrue(Files.exists(expectedReferencedMethod1), "Expected " + expectedReferencedMethod1 + " to exist");
        Assert.assertTrue(Files.exists(expectedReferencedMethodUses1), "Expected " + expectedReferencedMethodUses1 + " to exist");
        Assert.assertTrue(Files.exists(expectedReferencedMethod2), "Expected " + expectedReferencedMethod2 + " to exist");
        Assert.assertTrue(Files.exists(expectedReferencedMethodUses2), "Expected " + expectedReferencedMethodUses2 + " to exist");

        MethodIdsReportJson methodIdsReport1 = null;
        MethodReferencesReportJson methodReferencesReport1 = null;
        MethodIdsReportJson methodIdsReport2 = null;
        MethodReferencesReportJson methodReferencesReport2 = null;

        try (BufferedReader reader = Files.newBufferedReader(expectedReferencedMethod1)) {
            methodIdsReport1 = GSON.fromJson(reader, MethodIdsReportJson.class);
        }

        try (BufferedReader reader = Files.newBufferedReader(expectedReferencedMethod2)) {
            methodIdsReport2 = GSON.fromJson(reader, MethodIdsReportJson.class);
        }

        try (BufferedReader reader = Files.newBufferedReader(expectedReferencedMethodUses1)) {
            methodReferencesReport1 = GSON.fromJson(reader, MethodReferencesReportJson.class);
        }

        try (BufferedReader reader = Files.newBufferedReader(expectedReferencedMethodUses2)) {
            methodReferencesReport2 = GSON.fromJson(reader, MethodReferencesReportJson.class);
        }

        Assert.assertNotNull(methodIdsReport1);
        Assert.assertNotNull(methodReferencesReport1);
        Assert.assertNotNull(methodIdsReport2);
        Assert.assertNotNull(methodReferencesReport2);

        // Check partitioning numbers are correct
        Assert.assertEquals(methodIdsReport1.getMethodIds().size(), 1000);
        Assert.assertEquals(methodIdsReport2.getMethodIds().size(), 1000);

        Set<String> allIds = Stream.concat(methodIdsReport1.getMethodIds().stream(), methodIdsReport2.getMethodIds().stream())
                .collect(Collectors.toSet());

        Map<String, ReferencedMethodUsesJson> usesById = methodReferencesReport1.getMethodUses().stream()
                .collect(Collectors.toMap(use -> use.getMethod().getId(), Functions.identity()));
        Map<String, ReferencedMethodUsesJson> usesById2 = methodReferencesReport2.getMethodUses().stream()
                .collect(Collectors.toMap(use -> use.getMethod().getId(), Functions.identity()));

        usesById.putAll(usesById2);

        Assert.assertEquals(allIds.size(), usesById.size());
        Assert.assertTrue(allIds.containsAll(usesById.keySet()));
    }

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
