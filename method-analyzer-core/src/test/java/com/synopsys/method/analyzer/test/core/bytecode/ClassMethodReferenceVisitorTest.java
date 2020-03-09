/*
 * method-use-analyzer
 *
 * Copyright (C) 2020 Synopsys Inc.
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
package com.synopsys.method.analyzer.test.core.bytecode;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Multimap;
import com.synopsys.method.analyzer.core.bytecode.ClassMethodReferenceVisitor;
import com.synopsys.method.analyzer.core.model.MethodUse;
import com.synopsys.method.analyzer.core.model.ReferencedMethod;
import com.synopsys.method.analyzer.test.core.TestProperties;

public class ClassMethodReferenceVisitorTest {

    private static final Path TEST_PROJECT_DIRECTORY = Paths.get(System.getProperty(TestProperties.TEST_PROJECT_DIRECTORY));

    private static final String CLASS_FILE_REGEX = ".*\\.class";

    private Multimap<ReferencedMethod, MethodUse> result = null;

    @BeforeClass
    public void analyze() throws Exception {
        try (Stream<Path> files = Files.walk(TEST_PROJECT_DIRECTORY)) {
            List<Path> classFiles = files
                    .filter(Files::isRegularFile)
                    .filter(f -> f.toString().matches(CLASS_FILE_REGEX))
                    .collect(Collectors.toList());

            ClassMethodReferenceVisitor bytecodeAnalyzer = new ClassMethodReferenceVisitor();

            for (Path classFile : classFiles) {
                try (InputStream inputStream = Files.newInputStream(classFile)) {
                    ClassReader reader = new ClassReader(inputStream);
                    reader.accept(bytecodeAnalyzer, 0);
                }
            }

            result = bytecodeAnalyzer.getReferences();
        }
    }

    // This covers exclusion of "self" or local methods
    @Test
    public void overallResults() throws Exception {
        Assert.assertNotNull(result);
        Assert.assertEquals(result.keySet().size(), 10, "Expected 10 method references overall: " + result.keySet());
    }

    // This covers detecting automatic methods like hidden constructors
    @Test
    public void objectConstructorDetection() throws Exception {
        ReferencedMethod objectConstructorReference = new ReferencedMethod("java.lang.Object", "<init>", Collections.emptyList(), "void");

        Assert.assertTrue(result.containsKey(objectConstructorReference));

        Collection<MethodUse> objectConstructorReferences = result.get(objectConstructorReference);
        Assert.assertEquals(objectConstructorReferences.size(), 1, "Found unexpected number of results: " + objectConstructorReferences);
        Assert.assertTrue(objectConstructorReferences.contains(new MethodUse("com.synopsys.method.analyzer.test.project.BasicTestClass.<init>", 28)),
                "Unexpected reference: " + objectConstructorReferences);
    }

    // This covers methods used to initializes constants/fields
    @Test
    public void fieldInitializationReference() throws Exception {
        ReferencedMethod systemPropertyReference = new ReferencedMethod("java.lang.System", "getProperty", Arrays.asList("java.lang.String"),
                "java.lang.String");

        Collection<MethodUse> systemPropertyReferences = result.get(systemPropertyReference);
        Assert.assertEquals(systemPropertyReferences.size(), 1, "Found unexpected number of results: " + systemPropertyReferences);
        Assert.assertTrue(systemPropertyReferences.contains(new MethodUse("com.synopsys.method.analyzer.test.project.BasicTestClass.<init>", 30)),
                "Unexpected reference: " + systemPropertyReferences);
    }

    // This covers methods invoked normally within an implementation
    @Test
    public void withinMethodReference() throws Exception {
        ReferencedMethod stringFormatReference = new ReferencedMethod("java.lang.String", "format", Arrays.asList("java.lang.String", "java.lang.Object[]"),
                "java.lang.String");
        Collection<MethodUse> stringFormatReferences = result.get(stringFormatReference);
        Assert.assertEquals(stringFormatReferences.size(), 1, "Found unexpected number of results: " + stringFormatReferences);
        Assert.assertTrue(stringFormatReferences.contains(new MethodUse("com.synopsys.method.analyzer.test.project.BasicTestClass.getThing", 35)),
                "Unexpected reference: " + stringFormatReferences);
    }

    // This covers methods invoked as a lamdba
    @Test
    public void withinLambdaReference() throws Exception {
        ReferencedMethod stringToUpperCaseReference = new ReferencedMethod("java.lang.String", "toUpperCase", Collections.emptyList(),
                "java.lang.String");
        Collection<MethodUse> stringToUpperCaseReferences = result.get(stringToUpperCaseReference);

        // This validation is weird, because compiling with Eclipse vs with Gradle resulted in different behavior in
        // terms of the line number associated with the dynamic invocation of the method. This difference is all the way
        // at the bytecode level
        Assert.assertFalse(stringToUpperCaseReferences.isEmpty());
        Assert.assertTrue(
                stringToUpperCaseReferences.stream()
                        .allMatch(s -> s.getQualifiedMethodName().startsWith("com.synopsys.method.analyzer.test.project.BasicTestClass.getDynamic")),
                "Unexpected reference: " + stringToUpperCaseReferences);
    }

}
