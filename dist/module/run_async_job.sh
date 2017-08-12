#!/bin/bash

echo "Running async job with ${1}, ${2}, ${3}"
find .
# app in-file out-file token
java -cp server.jar server.core ${1} ${2} ${3}
