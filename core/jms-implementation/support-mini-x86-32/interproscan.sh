#!/bin/bash
#
# This is InterProScan v5. Welcome.
# Edit this script to suit your installation needs.
#

USER_DIR=$PWD

INSTALL_DIR="${BASH_SOURCE[0]}"
while [ -h "$INSTALL_DIR" ]; do
  cd "$(dirname "$INSTALL_DIR")"
  INSTALL_DIR="$(readlink "$(basename "$INSTALL_DIR")")"
done
cd "$(dirname "$INSTALL_DIR")"
INSTALL_DIR="$(pwd)/"

# set environment variables for getorf
export EMBOSS_ACDROOT=bin/nucleotide
export EMBOSS_DATA=bin/nucleotide

# Check Java is installed

JAVA=$(type -p java)
if [[ "$JAVA" == "" ]]; then
    printf 'Java not found. Please install Java 11 and place it on your path,\n'
    printf 'or edit the interproscan.sh script to refer to your Java installation.\n'.
    exit 1
fi

# Check Java version is supported

JAVA_VERSION=$("$JAVA" -Xms32M -Xmx32M -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' )
JAVA_MAJOR_VERSION="$( cut -d ';' -f 1 <<< "$JAVA_VERSION" )"
if [[ "${JAVA_MAJOR_VERSION}" -ne "11" ]];
then
    printf 'Java version 11 is required to run InterProScan.\n'
    printf 'Detected version %s\n' "${JAVA_MAJOR_VERSION}"
    printf 'Please install the correct version.\n'
    exit 1
fi

#"$JAVA" \
#-XX:+UseParallelGC -XX:ParallelGCThreads=4 -XX:+AggressiveOpts \
#-XX:+UseFastAccessorMethods -Xms1028M -Xmx6072M \
#-jar  interproscan-5.jar $@ -u $USER_DIR

#-Dorg.apache.activemq.SERIALIZABLE_PACKAGES="*" \

"$JAVA" \
-XX:+UseParallelGC -XX:ParallelGCThreads=4  \
 -Xms1028M -Xmx2056M \
-jar  interproscan-5.jar $@ -u $USER_DIR

#end
