#!/usr/bin/env bash

function get_bin_directory {
    sed -n '/bin.directory=/p' "$1" | awk -F '=' '{print $2}'
}

USER_DIR=$PWD
INSTALL_DIR="${BASH_SOURCE[0]}"

while [ -h "$INSTALL_DIR" ]; do
  cd "$(dirname "$INSTALL_DIR")"
  INSTALL_DIR="$(readlink "$(basename "$INSTALL_DIR")")"
done

cd "$(dirname "$INSTALL_DIR")"

BIN_DIR=$(get_bin_directory ./interproscan.properties)

if [ -n "$INTERPROSCAN_CONF" ] && [ -f "$INTERPROSCAN_CONF" ]; then
    PROPERTIES="$INTERPROSCAN_CONF"
    PROPERTY="-Dsystem.interproscan.properties=${PROPERTIES}"
    BIN_DIR="$(get_bin_directory "${PROPERTIES}")"
else
    PROPERTIES="./interproscan.properties"
    PROPERTY=""
fi

# Set environment variables for getorf
export EMBOSS_ACDROOT="$BIN_DIR"/nucleotide
export EMBOSS_DATA="$BIN_DIR"/nucleotide


# Check Java is installed
JAVA=$(type -p java)
if [[ "$JAVA" == "" ]]; then
    printf 'Java not found. Please install Java 11 and place it on your path,\n'
    printf 'or edit the interproscan.sh script to refer to your Java installation.\n'.
    exit 1
fi

# Check Java version is supported
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

exit 0

python3 initial_setup.py

"$JAVA" \
 -XX:ParallelGCThreads=8 \
 -Xms2028M -Xmx4000M \
 "$PROPERTY"
 -jar interproscan-5.jar $@ -u $USER_DIR
