#!/bin/bash

# This is a test version of the entrypoint script, meant to be
# run locally with fakes, which should be noted below.

#
# Entry point
# This script is the interface to the world of SDK driven apps
# TODO: could there be a better term for this? The essential nature is not
# SDK as such, but rather the application running conventions.
#

# PREPARE DEPLOY CONFIG
export KBASE_WORK_DIR=../fake/work
export KBASE_DEPLOY_DIR=../fake/kb-deployment

. $KBASE_DEPLOY_DIR/user-env.sh

java -cp service.jar deploy.core deploy.template.cfg $KBASE_WORK_DIR/config.properties deploy.cfg

# python ./scripts/prepare_deploy_cfg.py ./deploy.cfg ./work/config.properties

# ls ./work

# PREPARE ENVIRONMENT VARIABLES

#
# Place auth token in the environment variable
# Note this does not apply for server mode, but is somewhat harmless
# to run anyway.
#
if [ -f $KBASE_WORK_DIR/token ] ; then
  export KB_AUTH_TOKEN=$(<$KBASE_WORK_DIR/token)
fi


# Server Mode:
# In this mode the server is started up and listens for connections
# This is the preferred mode to run tasks which complete in some limited
# amount of time
# TODO: what are the guidelines for this? several minutes? less than an hour?
if [ $# -eq 0 ] ; then
  echo "Running in server mode"
  sh ./start_server.sh

# Test Mode:
elif [ "${1}" = "test" ] ; then
  echo "Run Tests"
  make test

# Job Mode:
# I find the term "async" here, as in many other terms at KBase, to be confusing.
# It is already a common term with specific meaning.
# In this context it means that the calling code treats this like other code
# contexts would treat an asynchronous task -- fire, forget, receive notification
# when it is complete.
# In this context it really means to just run the app, using the conventions
# env variables, input file, and output file.
elif [ "${1}" = "async" ] ; then
  echo "Running in async mode"
  sh ./run_async.sh

# ??
elif [ "${1}" = "init" ] ; then
  echo "Initialize module"

# Simply provide a bash cli into the docker container.
elif [ "${1}" = "bash" ] ; then
  bash

# ??
elif [ "${1}" = "report" ] ; then
  # export KB_SDK_COMPILE_REPORT_FILE=./work/compile_report.json
  export KB_SDK_COMPILE_REPORT_FILE=$KBASE_WORK_DIR/compile_report.json
  make compile

# ??
else
  echo Unknown
fi
