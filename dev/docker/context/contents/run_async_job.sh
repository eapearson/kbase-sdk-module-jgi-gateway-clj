#!/bin/bash

echo "Running async job with ${1}, ${2}, ${3}"
# Not yet implemented in this app
# find .
# app in-file out-file token
java -cp server.jar server.core ${1} ${2} ${3}
