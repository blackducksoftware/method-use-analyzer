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
package com.synopsys.method.analyzer.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.synopsys.method.analyzer.core.bytecode.ClassMethodReferenceVisitor;
import com.synopsys.method.analyzer.core.model.ReferencedMethod;
import com.synopsys.method.analyzer.core.report.ReportGenerator;

/**
 * Represents functionality to analyze and report on the external method calls made within a Java project
 *
 * <p>
 * This class is intended as the primary use interface for method analysis operations. Both {@link #analyze(Path, Path)}
 * and {@link #analyze(Path, Path, String)} may be used to evaluate and report on a target directory's use of methods
 * which are defined within the given directory
 *
 * @author romeara
 */
public class MethodUseAnalyzer {

    /**
     * Regular expression intended to match files with the ".class" extension
     */
    private static final String CLASS_FILE_REGEX = ".*\\.class";

    /**
     * Analyzes Java *.class files with the provided {@code sourceDirectory} for method calls made to classes not
     * defined within the provided class file set
     *
     * <p>
     * Output will be saved with a default file name - to specify a file name, use {@link #analyze(Path, Path, String)}
     *
     * @param sourceDirectory
     *            The directory containing the *.class files to evaluate for external method calls
     * @param outputDirectory
     *            The directory to output the generated report of method calls to
     * @return The full path to the generated report on the file system
     * @throws IOException
     *             If there is an error reading from input files, or saving the output report
     */
    public Path analyze(Path sourceDirectory, Path outputDirectory) throws IOException {
        return analyze(sourceDirectory, outputDirectory, "external-method-uses");
    }

    /**
     * Analyzes Java *.class files with the provided {@code sourceDirectory} for method calls made to classes not
     * defined within the provided class file set
     *
     * @param sourceDirectory
     *            The directory containing the *.class files to evaluate for external method calls
     * @param outputDirectory
     *            The directory to output the generated report of method calls to
     * @param outputFileName
     *            The file name (without extension) to same the report as
     * @return The full path to the generated report on the file system
     * @throws IOException
     *             If there is an error reading from input files, or saving the output report
     */
    public Path analyze(Path sourceDirectory, Path outputDirectory, String outputFileName) throws IOException {
        Objects.requireNonNull(sourceDirectory, "The sourceDirectory parameter is required, and may not be null");
        Objects.requireNonNull(outputDirectory, "The outputDirectory parameter is required, and may not be null");
        Objects.requireNonNull(outputFileName, "The outputFileName parameter is required, and may not be null");

        Preconditions.checkArgument(Files.exists(sourceDirectory), "The source path provided (%s) does not exist", sourceDirectory.toString());
        Preconditions.checkArgument(Files.isDirectory(sourceDirectory), "The source path provided (%s) is not a directory", sourceDirectory.toString());

        Multimap<ReferencedMethod, String> references = null;
        ReportGenerator reportGenerator = new ReportGenerator();

        try (Stream<Path> files = Files.walk(sourceDirectory)) {
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

            references = bytecodeAnalyzer.getReferences();
        }

        // TODO romeara logging (info/debug) of what was found

        return reportGenerator.generateReport(references, outputDirectory, outputFileName);
    }

}
