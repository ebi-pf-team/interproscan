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
    printf 'Java not found. Please install Java 1.8 or above\n'
    printf 'and place it on your path,\n'
    printf 'or edit the interproscan.sh script to refer to your Java installation.\n'.
    exit 1
fi

# Check Java version is supported

JAVA_VERSION=$("$JAVA" -Xms32M -Xmx32M -version 2>&1 | { read X; printf '%s' "${X#*\"}"; } )
JAVA_MAJOR_VERSION=${JAVA_VERSION%%.*}
JAVA_MINOR_VERSION=${JAVA_VERSION#*.}
JAVA_MINOR_VERSION=${JAVA_MINOR_VERSION%%.*}
if [[ "${JAVA_MAJOR_VERSION}" == "1" && "${JAVA_MINOR_VERSION}" -lt "8" ]];
then
    printf 'Java version 1.8 or above is required to run InterProScan.\n'
    printf 'Detected version %s.%s\n' "${JAVA_MAJOR_VERSION}" "${JAVA_MINOR_VERSION}"
    printf 'Please install the correct version.\n'
    exit 1
fi

# Check Python is installed

#PYTHON=$(type -p python)
#if [[ "$PYTHON" == "" ]]; then
#    printf 'Python not found. Please install Python 2.7\n'
#    printf 'and place it on your path,\n'
#    printf 'or edit the interproscan.sh script to refer to your Python installation.\n'.
#    exit 1
#fi

# Check Python version is supported

#PYTHON_VERSION=$("$PYTHON" -c 'import sys; print(".".join(map(str, sys.version_info[:2])))')
#PYTHON_MAJOR_VERSION=${PYTHON_VERSION%%.*}
#PYTHON_MINOR_VERSION=${PYTHON_VERSION#*.}
#if [[ "${PYTHON_MAJOR_VERSION}" != "2" || "${PYTHON_MINOR_VERSION}" != "7" ]];
#then
#    printf 'Python version 2.7 is required to run InterProScan.\n'
#    printf 'Detected version %s.%s\n' "${PYTHON_MAJOR_VERSION}" "${PYTHON_MINOR_VERSION}"
#    printf 'Please install the correct version.\n'
#    exit 1
#fi

"$JAVA" -Xdebug -Xrunjdwp:transport=dt_socket,server=n,address=localhost:5005,suspend=y \
-XX:+UseParallelGC -XX:ParallelGCThreads=4 -XX:+AggressiveOpts \
-XX:+UseFastAccessorMethods -Xms128M -Xmx2048M \
-jar interproscan-5.jar "$@" -u $USER_DIR

#end
