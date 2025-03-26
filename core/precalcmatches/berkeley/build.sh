#!/usr/bin/env bash

dirname=$(dirname "$0")
java -XX:+UseParallelGC \
     -XX:ParallelGCThreads=8 \
     -XX:+UseCompressedOops \
     -Xms16000M -Xmx32000M \
     -jar "${dirname}/berkeley-db-builder.jar" "$@"
