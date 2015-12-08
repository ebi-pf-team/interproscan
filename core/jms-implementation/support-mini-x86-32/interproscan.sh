#!/bin/bash
#
# This is InterProScan v5. Welcome.
# Edit this script to suit your installation needs.
#

USER_DIR=$PWD

cd $(dirname "$0")

# set environment variables for getorf
export EMBOSS_ACDROOT=bin/nucleotide
export EMBOSS_DATA=bin/nucleotide

JAVA=$(type -p java)

if [[ "$JAVA" == "" ]]; then
    printf 'Java not found. Please install java\n'
    printf 'and place it on your path,\n'
    printf 'or edit the interproscan.sh script to refer to your java installation.\n'.
    exit 1
fi

VERSION=$("$JAVA" -Xms32M -Xmx32M -version 2>&1 | { read X; printf '%s' "${X#*\"}"; } )
MAJOR_VERSION=${VERSION%%.*}
MINOR_VERSION=${VERSION#*.}
MINOR_VERSION=${MINOR_VERSION%%.*}

if [[ "${MAJOR_VERSION}" == "1" && ("${MINOR_VERSION}" -lt "6" || "${MINOR_VERSION}" -gt "7") ]];
then
    printf 'Java version 1.6 or 1.7 required\n'
    printf 'Detected version %s.%s\n' "${MAJOR_VERSION}" "${MINOR_VERSION}"
    printf 'Install the correct version \n'
    printf 'or edit the interproscan.sh script to disable version check.\n'
    exit 1
fi

"$JAVA" \
-XX:+UseParallelGC -XX:ParallelGCThreads=2 -XX:+AggressiveOpts \
-XX:+UseFastAccessorMethods -Xms128M -Xmx2048M \
-jar  interproscan-5.jar $@ -u $USER_DIR

#end 
