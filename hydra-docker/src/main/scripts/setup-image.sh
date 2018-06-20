#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

mkdir -p /etc/hadoop
tar xvf solr-6.6.0.tgz
chmod -R 777 /solr-6.6.0/server/logs /var/log/tomcat /var/cache/tomcat /var/lib/tomcat/webapps /solr-6.6.0/server/solr
chmod 777 /etc/tomcat
#groupadd -g 1001 hadoop
#useradd -u 1013 -g 1001 hbase
