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

"$JAVA" -XX:+UseParallelGC -XX:ParallelGCThreads=2 -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -Xms256M -Xmx2048M -jar interproscan-5.jar $@ -u $USER_DIR

#end 
