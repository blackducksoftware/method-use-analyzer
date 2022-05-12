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
package com.synopsys.method.analyzer.core.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.synopsys.method.analyzer.core.model.MethodUse;
import com.synopsys.method.analyzer.core.model.ReferencedMethod;

/**
 * Represents functionality for converting in-memory representations of method references to a stored file
 * representation
 *
 * @author romeara
 */
public class ReportGenerator {

    private static final String EXTENSION = ".bdmu";

    private static final String REFERENCE_METHOD_LABEL = "referenced-methods";

    private static final String REFERENCE_METHOD_USE_LABEL = "referenced-method-uses";

    private static final String BROKEN_FILE_LABEL = "broken-files";

    // TODO romeara - In the future, should we investigate dynamic call-through to make this an actual handshake, and
    // not a manual constant? Aka ask the service what is allowed?
    // This is determined in conjunction with what the KB cloud services will allow - DO NOT adjust this number without
    // consultation with the maximum allowed by that service
    private static final int REFERENCE_MAX_CHUNK_SIZE = 1000;

    private static final int BROKEN_FILE_MAX_CHUNK_SIZE = 1000;

    private static final Gson GSON = new Gson();

    /** Logger reference to output information to the application log files */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MetaDataReportJson metaDataReport;

    /**
     * @param hostName
     *            The name of the host the analysis was performed on. Used in report meta-data
     * @param analyzedDirectory
     *            The directory which was analyzed. Used in report meta-data
     * @param codeLocationName
     *            A name to associate with the analyzed source. May be null. Used in report meta-data
     */
    public ReportGenerator(String hostName, String analyzedDirectory, @Nullable String codeLocationName) {
        this.metaDataReport = new MetaDataReportJson(hostName, analyzedDirectory, codeLocationName);
    }

    /**
     * Generates a report of the provided method references within a project
     *
     * @param references
     *            The references to describe in a report
     * @param outputDirectory
     *            The directory to output the report to
     * @param outputFileName
     *            The file name to use for the report (without extension)
     * @return The path of the generated report on the file system
     * @throws IOException
     *             If there is an error writing the report to the file system
     * @deprecated Use {@link #generateReport(Multimap, Map, Path, String)} instead
     */
    @Deprecated
    public Path generateReport(Multimap<ReferencedMethod, MethodUse> references, Path outputDirectory, String outputFileName) throws IOException {
        return generateReport(references, Collections.emptyMap(), outputDirectory, outputFileName);
    }

    /**
     * Generates a report of the provided method references within a project
     *
     * @param references
     *            The references to describe in a report
     * @param brokenFiles
     *            Paths of any files for which analysis was attempted, by failed due to parser incompatibly of broken
     *            file formatting (mapped to a message indicating the specific error)
     * @param outputDirectory
     *            The directory to output the report to
     * @param outputFileName
     *            The file name to use for the report (without extension)
     * @return The path of the generated report on the file system
     * @throws IOException
     *             If there is an error writing the report to the file system
     */
    public Path generateReport(Multimap<ReferencedMethod, MethodUse> references, Map<Path, String> brokenFiles, Path outputDirectory, String outputFileName)
            throws IOException {
        Objects.requireNonNull(references);
        Objects.requireNonNull(brokenFiles);
        Objects.requireNonNull(outputDirectory);
        Objects.requireNonNull(outputFileName);

        Path destinationFile = outputDirectory.resolve(outputFileName + EXTENSION);
        List<MethodIdJson> uniqueMethodKeys = new LinkedList<>();
        List<ReferencedMethodUsesJson> methodUses = new LinkedList<>();

        for (Entry<ReferencedMethod, Collection<MethodUse>> entry : references.asMap().entrySet()) {
            // Generation unique, opaque ID to match method uses against
            MethodIdJson id = new MethodIdJson(entry.getKey());

            // Add to segmented method use files
            uniqueMethodKeys.add(id);

            // Add to referenced uses file
            methodUses.add(new ReferencedMethodUsesJson(id.getSignature(), entry.getKey(), entry.getValue()));
        }

        List<BrokenFileJson> brokenFileRecords = brokenFiles.entrySet().stream()
                .map(entry -> new BrokenFileJson(entry.getKey().toString(), entry.getValue()))
                .collect(Collectors.toList());

        return writeReport(destinationFile, uniqueMethodKeys, methodUses, brokenFileRecords);
    }

    /**
     * Generates multiple report files describing the method information provided
     *
     * @param destinationFile
     *            The location to output the completed report file
     * @param uniqueMethodKeys
     *            List of unique, opaque keys referencing discovered methods
     * @param methodUses
     *            Detailed description(s) of the method uses discovered
     * @param brokenFiles
     *            Description of files which failed to parse
     * @return The path the report was written to
     * @throws IOException
     *             If there is an error writing the report
     */
    private Path writeReport(Path destinationFile, List<MethodIdJson> uniqueMethodKeys, List<ReferencedMethodUsesJson> methodUses,
            List<BrokenFileJson> brokenFiles) throws IOException {
        Objects.requireNonNull(destinationFile);
        Objects.requireNonNull(uniqueMethodKeys);
        Objects.requireNonNull(methodUses);
        Objects.requireNonNull(brokenFiles);

        Map<Path, Path> outputFileMapping = new HashMap<>();

        Path workingDirectory = Files.createTempDirectory("blackduck-method-uses");

        // Generate a meta-data file with various report information
        Path metaDataFile = workingDirectory.resolve("metaData.json");

        try (BufferedWriter writer = Files.newBufferedWriter(metaDataFile)) {
            GSON.toJson(metaDataReport, writer);
            outputFileMapping.put(metaDataFile, metaDataFile.getFileName());
        }

        // Separate uses into chunks for more efficient processing within the application (without requiring opening and
        // manipulating the file)

        List<List<MethodIdJson>> methodIdPartitions = Lists.partition(uniqueMethodKeys, REFERENCE_MAX_CHUNK_SIZE);

        for (int index = 0; index < methodIdPartitions.size(); index++) {
            List<MethodIdJson> idPartition = methodIdPartitions.get(index);

            Path referencedMethodsFile = workingDirectory.resolve(REFERENCE_METHOD_LABEL + "-" + index + ".json");
            try (BufferedWriter writer = Files.newBufferedWriter(referencedMethodsFile)) {
                GSON.toJson(new MethodIdsReportJson(idPartition), writer);
                outputFileMapping.put(referencedMethodsFile, Paths.get(REFERENCE_METHOD_LABEL).resolve(referencedMethodsFile.getFileName()));
            }
        }

        List<List<ReferencedMethodUsesJson>> methodUsePartitions = Lists.partition(methodUses, REFERENCE_MAX_CHUNK_SIZE);

        for (int index = 0; index < methodUsePartitions.size(); index++) {
            List<ReferencedMethodUsesJson> methodUsePartition = methodUsePartitions.get(index);

            Path referencedMethodUsesFile = workingDirectory.resolve(REFERENCE_METHOD_USE_LABEL + "-" + index + ".json");
            try (BufferedWriter writer = Files.newBufferedWriter(referencedMethodUsesFile)) {
                GSON.toJson(new MethodReferencesReportJson(methodUsePartition), writer);
                outputFileMapping.put(referencedMethodUsesFile, Paths.get(REFERENCE_METHOD_USE_LABEL).resolve(referencedMethodUsesFile.getFileName()));
            }
        }

        List<List<BrokenFileJson>> brokenFilePartitions = Lists.partition(brokenFiles, BROKEN_FILE_MAX_CHUNK_SIZE);

        for (int index = 0; index < brokenFilePartitions.size(); index++) {
            List<BrokenFileJson> brokenFilePartition = brokenFilePartitions.get(index);

            Path brokenFilesFile = workingDirectory.resolve(BROKEN_FILE_LABEL + "-" + index + ".json");
            try (BufferedWriter writer = Files.newBufferedWriter(brokenFilesFile)) {
                GSON.toJson(new BrokenFilesReportJson(brokenFilePartition), writer);
                outputFileMapping.put(brokenFilesFile, Paths.get(BROKEN_FILE_LABEL).resolve(brokenFilesFile.getFileName()));
            }
        }

        writeZipFile(destinationFile, outputFileMapping);

        // GH-22: Explicitly clean up temporary files once use of them is complete
        try {
            try (Stream<Path> walk = Files.walk(workingDirectory)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            logger.warn("Error cleaning up temporary report files", e);
        }

        return destinationFile;
    }

    /**
     * Writes multiple files into a compressed archive
     *
     * @param destination
     *            The location to output the archive to
     * @param filesToZip
     *            Mapping of file system paths to archive, to their relative paths within the compressed archive
     * @throws IOException
     *             If there is an error writing the files to the compressed archive
     */
    private void writeZipFile(Path destination, Map<Path, Path> filesToZip) throws IOException {
        Objects.requireNonNull(destination);
        Objects.requireNonNull(filesToZip);

        try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(destination.toFile()))) {
            for (Entry<Path, Path> entry : filesToZip.entrySet()) {
                try (FileInputStream inputStream = new FileInputStream(entry.getKey().toFile())) {
                    ZipEntry zipEntry = new ZipEntry(entry.getValue().toString());
                    outputStream.putNextEntry(zipEntry);

                    try {
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = inputStream.read(bytes)) >= 0) {
                            outputStream.write(bytes, 0, length);
                        }
                    } finally {
                        outputStream.closeEntry();
                    }
                }
            }
        }
    }
}
