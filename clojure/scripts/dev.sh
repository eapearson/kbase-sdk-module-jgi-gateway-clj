./lein with-profile deploy run ./templates/deploy.template.cfg ./devdata/config.properties ./devdata/deploy.cfg
export KB_DEPLOYMENT_CONFIG=`pwd`/deploy.cfg
export KB_SERVICE_NAME=jgi_gateway_clj
lein with-profile dev ring server-headless