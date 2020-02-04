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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.google.common.base.Preconditions;

// TODO romeara doc
public class MethodUseAnalyzer {

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
        Objects.requireNonNull(sourceDirectory);
        Objects.requireNonNull(outputDirectory);
        Objects.requireNonNull(outputFileName);

        Preconditions.checkArgument(Files.exists(sourceDirectory), "The source path provided (%s) does not exist", sourceDirectory.toString());
        Preconditions.checkArgument(Files.isDirectory(sourceDirectory), "The source path provided (%s) is not a directory", sourceDirectory.toString());

        // TODO Auto-generated function stub
        throw new UnsupportedOperationException("MethodUseAnalyzer.analyze is not yet implemented");
    }

}
