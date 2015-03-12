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

VERSION=$("$JAVA" -version 2>&1 | { read X; printf '%s' "${X#*\"}"; } )
MAJOR_VERSION=${VERSION%%.*}
MINOR_VERSION=${VERSION#*.}
MINOR_VERSION=${MINOR_VERSION%%.*}

if [[ "${MAJOR_VERSION}" == "1" && "${MINOR_VERSION}" -lt "6" ]];
then
    printf 'Java version 1.6 or above required\n'
    printf 'Detected version %s.%s\n' "${MAJOR_VERSION}" "${MINOR_VERSION}"
    printf 'Install the correct version \n'
    printf 'or edit the interproscan.sh script to disable version check.\n'
    exit 1
fi

"$JAVA" -Xdebug -Xrunjdwp:transport=dt_socket,server=n,address=localhost:5005,suspend=y -jar -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -Xms512M -Xmx2048M interproscan-5.jar "$@" -u $USER_DIR

#end
