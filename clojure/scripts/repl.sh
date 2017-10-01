./lein with-profile deploy run ./templates/deploy.template.cfg ./devdata/config.properties ./devdata/deploy.cfg
export KB_DEPLOYMENT_CONFIG=`pwd`/devdata/deploy.cfg
export KB_SERVICE_NAME=jgi_gateway_eap
./lein repl
