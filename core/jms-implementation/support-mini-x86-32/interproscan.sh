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

#JAVA_VERSION=$("$JAVA" -Xms32M -Xmx32M -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' )
JAVA_VERSION=$("$JAVA" -Xms32M -Xmx32M -version 2>&1 | sed -n '/version/p' | awk -F '"' '{print $2}' )
JAVA_MAJOR_VERSION_FULL="$( cut -d ';' -f 1 <<< "$JAVA_VERSION" )"
JAVA_MAJOR_VERSION="${JAVA_MAJOR_VERSION_FULL%%.*}"
if [[ "${JAVA_MAJOR_VERSION}" -lt "11" ]];
then
    printf 'Java version 11 is required to run InterProScan.\n'
    printf 'Detected version %s\n' "${JAVA_VERSION}"
    printf 'Please install the correct version.\n'
    exit 1
fi

python3 initial_setup.py

"$JAVA" \
 -XX:ParallelGCThreads=8 \
 -Xms2028M -Xmx9216M \
 -jar  interproscan-5.jar $@ -u $USER_DIR

#end
