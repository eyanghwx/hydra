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

package org.apache.hydra.controler;

import org.apache.hydra.controller.AppDetailsController;
import org.apache.hydra.model.AppEntry;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

public class AppDetailsControllerTest {

    private AppDetailsController controller;

    @Before
    public void setUp() throws Exception {
        this.controller = new AppDetailsController();

    }

    @Test
    public void testGetDetails() throws Exception {
        final AppEntry result = this.controller.getDetails("application 1");
        assertNotNull(result);
    }

    @Test
    public void testPathAnnotation() throws Exception {
        assertNotNull(this.controller.getClass()
                                    .getAnnotations());
        assertThat("The controller has the annotation Path", this.controller.getClass()
                                                                           .isAnnotationPresent(Path.class));

        final Path path = this.controller.getClass()
                                        .getAnnotation(Path.class);
        assertThat("The path is /app_details", path.value(), is("/app_details"));
    }

}
