# Apache HYDRA Docker Image

## Introduction

HYDRA Docker image is pre-packaged docker container for Hadoop Application Catalog.

check it out:

```
git clone https://github.com/apache/hydra.git
```

## Compile

```
mvn package
```

## Run

```
docker run -d -p 8080:8080 -p 8983:8983 hortonworks/hydra-docker:1.0-SNAPSHOT
```

When running this command a couple of things happens:
* Solr server will create hydra collection for hosting application catalog
* Sample applications are registered in Embedded Solr
* Tomcat will run hydra-webapp on port 8080

User can browse port 8080 to deploy application on a Hadoop cluster.
