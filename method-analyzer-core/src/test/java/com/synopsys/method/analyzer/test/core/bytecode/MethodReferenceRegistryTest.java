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
package com.synopsys.method.analyzer.test.core.bytecode;

import java.util.Collection;
import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Multimap;
import com.synopsys.method.analyzer.core.bytecode.MethodReferenceRegistry;
import com.synopsys.method.analyzer.core.model.ReferencedMethod;

public class MethodReferenceRegistryTest {

    @Test(expectedExceptions = NullPointerException.class)
    public void registerReferenceNullMethodOwner() throws Exception {
        new MethodReferenceRegistry().registerReference(null, "methodName", Collections.emptyList(), "output", "whereUsed");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void registerReferenceNullMethodName() throws Exception {
        new MethodReferenceRegistry().registerReference("methodOwner", null, Collections.emptyList(), "output", "whereUsed");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void registerReferenceNullInputs() throws Exception {
        new MethodReferenceRegistry().registerReference("methodOwner", "methodName", null, "output", "whereUsed");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void registerReferenceNullOutput() throws Exception {
        new MethodReferenceRegistry().registerReference("methodOwner", "methodName", Collections.emptyList(), null, "whereUsed");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void registerReferenceNullWhereUsed() throws Exception {
        new MethodReferenceRegistry().registerReference("methodOwner", "methodName", Collections.emptyList(), "output", null);
    }

    @Test
    public void registerReference() throws Exception {
        ReferencedMethod expectedKey = new ReferencedMethod("methodOwner", "methodName", Collections.emptyList(), "output");

        MethodReferenceRegistry methodReferenceRegistry = new MethodReferenceRegistry();

        methodReferenceRegistry.registerReference("methodOwner", "methodName", Collections.emptyList(), "output", "whereUsed");

        Multimap<ReferencedMethod, String> result = methodReferenceRegistry.getReferences();

        Assert.assertNotNull(result);
        Assert.assertEquals(result.keySet().size(), 1);

        Assert.assertTrue(result.containsKey(expectedKey));

        Collection<String> whereUsedResult = result.get(expectedKey);
        Assert.assertNotNull(whereUsedResult);
        Assert.assertEquals(whereUsedResult.size(), 1);
        Assert.assertTrue(whereUsedResult.contains("whereUsed"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void registerExclusionNullExcludedMethodOwner() throws Exception {
        new MethodReferenceRegistry().registerExclusion(null);
    }

    @Test
    public void registerReferenceExcludedBefore() throws Exception {
        String excludedMethodOwner = "excludedMethodOwner";
        ReferencedMethod expectedKey = new ReferencedMethod("methodOwner", "methodName", Collections.emptyList(), "output");

        MethodReferenceRegistry methodReferenceRegistry = new MethodReferenceRegistry();

        methodReferenceRegistry.registerExclusion(excludedMethodOwner);
        methodReferenceRegistry.registerReference("methodOwner", "methodName", Collections.emptyList(), "output", "whereUsed");
        methodReferenceRegistry.registerReference(excludedMethodOwner, "methodName", Collections.emptyList(), "output", "whereUsed");

        Multimap<ReferencedMethod, String> result = methodReferenceRegistry.getReferences();

        Assert.assertNotNull(result);
        Assert.assertEquals(result.keySet().size(), 1);

        Assert.assertTrue(result.containsKey(expectedKey));

        Collection<String> whereUsedResult = result.get(expectedKey);
        Assert.assertNotNull(whereUsedResult);
        Assert.assertEquals(whereUsedResult.size(), 1);
        Assert.assertTrue(whereUsedResult.contains("whereUsed"));
    }

    @Test
    public void registerReferenceExcludedAfter() throws Exception {
        String excludedMethodOwner = "excludedMethodOwner";
        ReferencedMethod expectedKey = new ReferencedMethod("methodOwner", "methodName", Collections.emptyList(), "output");

        MethodReferenceRegistry methodReferenceRegistry = new MethodReferenceRegistry();

        methodReferenceRegistry.registerReference("methodOwner", "methodName", Collections.emptyList(), "output", "whereUsed");
        methodReferenceRegistry.registerReference(excludedMethodOwner, "methodName", Collections.emptyList(), "output", "whereUsed");
        methodReferenceRegistry.registerExclusion(excludedMethodOwner);

        Multimap<ReferencedMethod, String> result = methodReferenceRegistry.getReferences();

        Assert.assertNotNull(result);
        Assert.assertEquals(result.keySet().size(), 1);

        Assert.assertTrue(result.containsKey(expectedKey));

        Collection<String> whereUsedResult = result.get(expectedKey);
        Assert.assertNotNull(whereUsedResult);
        Assert.assertEquals(whereUsedResult.size(), 1);
        Assert.assertTrue(whereUsedResult.contains("whereUsed"));
    }

}
