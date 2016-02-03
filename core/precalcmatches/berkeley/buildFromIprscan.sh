#!/bin/bash
#
# This is InterProScan v5. Welcome.
# Edit this script to suit your installation needs.
#

cd $(dirname "$0")

JAVA=$(type -p java)

if [[ "$JAVA" == "" ]]; then
    printf 'Java not found. Please install Java\n'
    printf 'and place it on your path,\n'
    printf 'or edit the interproscan.sh script to refer to your java installation.\n'.
    exit 1
fi

VERSION=$("$JAVA" -version 2>&1 | { read X; printf '%s' "${X#*\"}"; } )
MAJOR_VERSION=${VERSION%%.*}
MINOR_VERSION=${VERSION#*.}
MINOR_VERSION=${MINOR_VERSION%%.*}

if [[ "${MAJOR_VERSION}" == "1" && "${MINOR_VERSION}" -lt "8" ]];
then
    printf 'Java version 1.8 or above required\n'
    printf 'Detected version %s.%s\n' "${MAJOR_VERSION}" "${MINOR_VERSION}"
    printf 'Please install the correct version\n'
    exit 1
fi

"$JAVA" -Xmx2048M -jar berkeley-db-builder.jar "$@"

#end