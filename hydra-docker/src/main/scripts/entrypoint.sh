#!/bin/bash

/solr-6.6.0/bin/solr start -p 8983 -force
/solr-6.6.0/bin/solr create_core -c hydra -force
/solr-6.6.0//bin/post -c hydra /tmp/samples.xml
/usr/libexec/tomcat/server start
