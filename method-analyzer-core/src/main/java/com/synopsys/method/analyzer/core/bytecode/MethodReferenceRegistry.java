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
package com.synopsys.method.analyzer.core.bytecode;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.synopsys.method.analyzer.core.model.ReferencedMethod;

/**
 * Represents handling for tracking referenced methods throughout a project
 *
 * <p>
 * Development note - making this external to the actual byte code analysis class(s) and applying thread-safe
 * synchronization to its method might allow for parallel processing, if that becomes necessary
 *
 * @author romeara
 */
public class MethodReferenceRegistry {

    // Key'd by owner, signature, value of referenced locations
    private final Table<String, ReferencedMethod, Collection<String>> references;

    private final Set<String> methodOwnerExclusions;

    public MethodReferenceRegistry() {
        references = HashBasedTable.create();
        methodOwnerExclusions = new HashSet<>();
    }

    /**
     * Registers a method used within a class. Handles ignoring based on any exclusion criteria
     *
     * @param methodOwner
     *            The class the method is defined in
     * @param methodName
     *            The name of the method
     * @param inputs
     *            An order list of the classes which comprise the parameters of the method
     * @param output
     *            The class the method returns, or "void"
     * @param whereUsed
     *            A signature of where within the analyzed project the method was referenced
     * @param lineNumber
     *            If available, the line the method was referenced on
     */
    public void registerReference(String methodOwner, String methodName, List<String> inputs, String output, String whereUsed, @Nullable Integer lineNumber) {
        Objects.requireNonNull(methodOwner);
        Objects.requireNonNull(methodName);
        Objects.requireNonNull(inputs);
        Objects.requireNonNull(output);
        Objects.requireNonNull(whereUsed);

        if (!methodOwnerExclusions.contains(methodOwner)) {
            String useReference = whereUsed + ":" + (lineNumber != null ? lineNumber : "?");

            ReferencedMethod referencedMethod = new ReferencedMethod(methodOwner, methodName, inputs, output);

            Collection<String> values = Optional.ofNullable(references.get(methodOwner, referencedMethod))
                    .orElse(new HashSet<>());
            values.add(useReference);

            references.put(methodOwner, referencedMethod, values);
        }
    }

    /**
     * @param excludedMethodOwner
     *            Registers a class to ignore all registered methods for, and to remove all existing registered method
     *            calls for
     */
    public void registerExclusion(String excludedMethodOwner) {
        Objects.requireNonNull(excludedMethodOwner);

        methodOwnerExclusions.add(excludedMethodOwner);

        Set<ReferencedMethod> columnKeys = new HashSet<>(references.row(excludedMethodOwner).keySet());

        for (ReferencedMethod columnKey : columnKeys) {
            references.remove(excludedMethodOwner, columnKey);
        }
    }

    /**
     * @return A mapping of referenced methods to one or more locations use was detected in
     */
    public Multimap<ReferencedMethod, String> getReferences() {
        Multimap<ReferencedMethod, String> result = HashMultimap.create();

        for (Cell<String, ReferencedMethod, Collection<String>> reference : references.cellSet()) {
            result.putAll(reference.getColumnKey(), reference.getValue());
        }

        return result;
    }

}
