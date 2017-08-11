/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hydra.application;

import org.apache.hydra.application.Hydra;
import org.apache.hydra.controller.AppDetailsController;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ApplicationPath;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HydraTest {

    private Hydra seedApplication;

    @Before
    public void setUp() throws Exception {
        this.seedApplication = new Hydra();

    }

    @Test
    public void testGetClasses() throws Exception {
        final Set<Class<?>> classes = this.seedApplication.getClasses();
        assertNotNull(classes);
        assertEquals(2, classes.size());
        assertThat("contains the the JacksonFeature", classes.contains(JacksonFeature.class));
        assertThat("contains the HomeController", classes.contains(AppDetailsController.class));
    }

    @Test
    public void testAnnotations() throws Exception {


        assertThat("The Class has the annotation ApplicationPath ", this.seedApplication.getClass()
                                                                                        .isAnnotationPresent(
                                                                                                ApplicationPath.class));
        final ApplicationPath annotation = this.seedApplication.getClass()
                                                               .getAnnotation(ApplicationPath.class);
        assertThat("The containing string is 'service'", annotation.value(), is("service"));
    }
}
