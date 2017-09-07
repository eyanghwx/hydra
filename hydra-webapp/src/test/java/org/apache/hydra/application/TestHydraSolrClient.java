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

import org.apache.hydra.model.AppStoreEntry;
import org.apache.hydra.model.Application;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.hydra.application.EmbeddedSolrServerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.method;

import static org.junit.Assert.*;

import java.util.List;

public class TestHydraSolrClient {

  static final String CONFIGSET_DIR = "src/test/resources/configsets";
  static SolrClient solrClient;
  static HydraSolrClient spy;


  @Before
  public void setup() throws Exception {
    String targetLocation = EmbeddedSolrServerFactory.class
        .getProtectionDomain().getCodeSource().getLocation().getFile() + "/..";

    String solrHome = targetLocation + "/solr";
    solrClient = EmbeddedSolrServerFactory.create(solrHome, CONFIGSET_DIR, "exampleCollection");
    spy = PowerMockito.spy(new HydraSolrClient());
    when(spy, method(HydraSolrClient.class, "getSolrClient")).withNoArguments().thenReturn(solrClient);
  }

  @After
  public void teardown() throws Exception { 
    try {
      solrClient.close();
    } catch (Exception e) {
    }
  }

  @Test
  public void testRegister() throws Exception {
    Application example = new Application();
    example.setOrganization("jenkins-ci.org");
    example.setName("jenkins");
    example.setDescription("World leading open source automation system.");
    example.setIcon("/css/img/feather.png");
    spy.register(example);
    List<AppStoreEntry> apps = spy.getRecommendedApps();
    assertEquals(1, apps.size());
  }

  @Test
  public void testSearch() throws Exception {
    Application example = new Application();
    example.setOrganization("jenkins-ci.org");
    example.setName("jenkins");
    example.setDescription("World leading open source automation system.");
    example.setIcon("/css/img/feather.png");
    spy.register(example);
    List<AppStoreEntry> results = spy.search("name_s:jenkins");
    int expected = 1;
    int actual = results.size();
    assertEquals(expected, actual);
  }

  @Test
  public void testNotFoundSearch() throws Exception {
    Application example = new Application();
    example.setOrganization("jenkins-ci.org");
    example.setName("jenkins");
    example.setDescription("World leading open source automation system.");
    example.setIcon("/css/img/feather.png");
    spy.register(example);
    List<AppStoreEntry> results = spy.search("name_s:abc");
    int expected = 0;
    int actual = results.size();
    assertEquals(expected, actual);
  }

  @Test
  public void testGetRecommendedApps() throws Exception {
    List<AppStoreEntry> expected = spy.getRecommendedApps();
    List<AppStoreEntry> actual = spy.getRecommendedApps();
    assertEquals(expected, actual);
  }

}
