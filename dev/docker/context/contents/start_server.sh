#!/bin/bash
script_dir=$(dirname "$(readlink -f "$0")")
export KB_DEPLOYMENT_CONFIG=$script_dir/deploy.cfg
java -cp service.jar server.core ${PORT}
