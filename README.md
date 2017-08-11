# Apache HYDRA

## Introduction

HYDRA is application catalog for deploying docker enabled cloud
application on Hadoop.

check it out:

```bash
git clone https://github.com/apache/hydra.git
```

## Prerequisites
* Firefox or Chrome
* [npm](https://www.npmjs.org)
* [nodejs](http://nodejs.org)
* [JDK](http://www.oracle.com/technetwork/java/javaee/downloads/index.html)
* [IDE](http://www.jetbrains.com/)
* [bower](http://bower.io)
* [PhantomJs](http://phantomjs.org) or `brew install phantomjs`
* [Docker](http://docker.io)

## Installation

```bash
mvn package
```

When running this command a couple of things happen:
* Bower install will be run
* JSLint will be run in src/main/javascript sources
* Javascript will be minified
* All the other standard maven phases.

## Status of the project

See Apache [JIRA](http://issues.apache.org/jira/browse/HYDRA)

