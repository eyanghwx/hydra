FROM centos:7

RUN yum -y install tomcat lsof krb5-workstation
ADD http://archive.apache.org/dist/lucene/solr/6.6.0/solr-6.6.0.tgz /
ADD src/main/scripts/setup-image.sh /setup-image.sh
ADD src/main/resources/samples.xml /tmp/samples.xml
ADD src/main/resources/jaas.config /etc/tomcat/jaas.config.template
ADD src/main/scripts/entrypoint.sh /usr/bin/entrypoint.sh
ADD target/hydra-webapp-1.0-SNAPSHOT.war /var/lib/tomcat/webapps/ROOT.war
RUN /setup-image.sh

EXPOSE 8080
EXPOSE 8983
WORKDIR /

ENTRYPOINT ["/usr/bin/entrypoint.sh" ]
