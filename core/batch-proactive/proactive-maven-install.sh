#!/bin/bash

# Install ProActive JARs in local repository (temporary arrangement until ProActive provide JARs via Maven repo)
#
# Example: ./proactive-maven-install.sh ~/projects/proactive/scheduler/ProActiveScheduling-1.0.0 1.0.0
#
# Author:  Antony Quinn <aquinn@ebi.ac.uk>
#
# Version: $Id: proactive-maven-install.sh,v 1.1.1.1 2009/05/22 21:30:07 aquinn Exp $

# Required arguments
PA_HOME=$1
PA_VERSION=$2

# Location of ProActive JAR files
PA_LIB=$PA_HOME/dist/lib

MVN_SUFFIX="-Dversion=$PA_VERSION -Dpackaging=jar -DgeneratePom=true -DgroupId=org.ow2.proactive.scheduler"

# ProActive Core
mvn install:install-file -Dfile=$PA_LIB/ProActive.jar           -DartifactId=proactive-core     $MVN_SUFFIX

# ProActive Core dependencies
mvn install:install-file -Dfile=$PA_LIB/bouncycastle.jar        -DartifactId=bouncycastle       $MVN_SUFFIX
mvn install:install-file -Dfile=$PA_LIB/fractal.jar             -DartifactId=fractal            $MVN_SUFFIX
mvn install:install-file -Dfile=$PA_LIB/javassist.jar           -DartifactId=javassist          $MVN_SUFFIX
mvn install:install-file -Dfile=$PA_LIB/jetty-6.1.11.jar        -DartifactId=jetty              $MVN_SUFFIX
mvn install:install-file -Dfile=$PA_LIB/jetty-util-6.1.11.jar   -DartifactId=jetty-util         $MVN_SUFFIX
mvn install:install-file -Dfile=$PA_LIB/saxon8.jar              -DartifactId=saxon8             $MVN_SUFFIX
mvn install:install-file -Dfile=$PA_LIB/saxon8-dom.jar          -DartifactId=saxon8-dom         $MVN_SUFFIX
mvn install:install-file -Dfile=$PA_LIB/servlet-api.jar         -DartifactId=servlet-api        $MVN_SUFFIX
mvn install:install-file -Dfile=$PA_LIB/trilead-ssh2.jar        -DartifactId=trilead-ssh2       $MVN_SUFFIX

# ProActive Scheduler dependencies
mvn install:install-file -Dfile=$PA_LIB/ProActive_Scheduler-core.jar    -DartifactId=proactive-scheduler-core   $MVN_SUFFIX
mvn install:install-file -Dfile=$PA_LIB/ProActive_Scheduler-client.jar  -DartifactId=proactive-scheduler-client $MVN_SUFFIX
mvn install:install-file -Dfile=$PA_LIB/ProActive_ResourceManager.jar   -DartifactId=proactive-resource-manager $MVN_SUFFIX
mvn install:install-file -Dfile=$PA_LIB/ProActive_SRM-common.jar        -DartifactId=proactive-srm-common       $MVN_SUFFIX

# ProActive Scheduler dependencies
mvn install:install-file -Dfile=$PA_LIB/ejb3-persistence.jar      -DartifactId=ejb3-persistence      $MVN_SUFFIX
mvn install:install-file -Dfile=$PA_LIB/hibernate-annotations.jar -DartifactId=hibernate-annotations $MVN_SUFFIX
