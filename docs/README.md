# server

A Clojure library designed to ... well, that part is up to you.

## Usage

Develop using lein ring:

```
% lein with-profile dev ring server-headless
```

or 

```
lein with-profile server run 3000
```

> Note, though: We need the deploy.cfg in order for interaction with kbase
> services to work. So we need to wedge this in there for interactive 
> development.
> To do this we should steal the config from local_test.
> For this service, we also need the special jgi-token as well

so, yes, something like this:

```
export KB_DEPLOYMENT_CONFIG=`pwd`/deploy.cfg
export KB_SERVICE_NAME=jgi_gateway_clj
lein with-profile service ring server-headless
```

also, you will need to hand-craft the deploy.cfg file in clojure/server for now while doing live testing.

Deployment development like

```
lein with-profile deploy run ./test/deploy.template.cfg ./test/config.properties ./test/deploy.cfg
```

### Make the server

```
% lein with-profile service,deploy uberjar
```

This makes the jar with the server, invoked via server.main, and the deploy tool, invoked via deploy.main

### Copy assets into the distribution 

Here is the manual process:

- the target directory is dist/module
- cp clojure/server/target/server-0.1.0-SNAPSHOT.jar dist/module/server.jar
- cp clojure/server/scripts/Makefile dist/module/Makefile
<!-- - copy clojure/server/scripts/run.sh dist/module/run.sh -->

> TODO: wirite a script for this


### Make a docker image

```
docker build -t jgi-gateway-clj .
```

Run it

```
docker run -rm -d -p 3000:3000 jgi-gateway-clj
```


Run it directly:

For server mode:

```
java -cp server.jar server.core ${PORT}
```

For async mode:

```
java -cp server.jar server.core in.json out.json token
```

where

- in.json is the input parameters in json format
- out.json is the ouput in json format
- token is the auth token string provided directly

So now, for testing in async mode, we need to do this:

- build the docker container
- create directories with sample input and prepared sample output
- run the dockerized app against each of the test work dirs
- compare the real output to the expected.

```
docker build -t jgi-gateway-clj .
docker run --rm jgi-gateway-clj -v `pwd`/tests/work1:/kb/module/work async
```

## Module registration

module is built into an image during registration using the top level Dockerfile

> explain more

## Module lifecycle in server mode

When a module app is run in async mode or a module is run in service mode and accessed for the first time, the docker container wrapping it is run, invoking the entrypoint script.

This script is set in the Dockerfile used to create the image, and by convension is called ```./entrypoint.sh```. We will continue to use that since it is a well known convention.

### Deployment 

The first responsibility of the entrypoint script is to create the deployment configuration file. This is the heart of the reconfigurablity of modules; the configuration may be modified and the service restarted to rebuild the configuration file with new values.

The deployment script must create a configuration file understood by the module given a configuration data file in "ini" format.

The configuration data file contains both standard kbase properties, as well as properties specific to the module itself.

> NOTE: it is unclear how module-specific properties are made available and provided to the module, since it is not under control of the module author.

The "ini" format allows for a top-level section token, with properties within the section being flat (no nested properties.)

In the config data file, the section name is "global".

There are three ways of providing configuration to the deployment script:

- the config data file - provided as an argument to the deployment script
- environment variables:
  - KBASE_ENDPOINT
  - AUTH_SERVICE_URL
  - AUTH_SERVICE_URL_ALLOW_INSECURE
  - KBASE_SECURE_CONFIG_PARAM_<param>

#### The config data file

The config data file may be provided as the second argument to the deployment script. It is an ini file with the top level property "global". It contains required and optional configuration properties. The rules for what is required and optional is dependent upon which environment variables have been set.

This file, if present, will be named ```./work/config.properties```.

> Note: Although this is putatively a java "properties" file, the fact that at least the python ConfigParser is used, and it will throw an error if it consumes a file without a section (as ini files have), I can only surmise that this is not a true properties file, which cannot have a section.

If the file has been provided at all, it is used to set the following service urls, unless the corresponding KBASE_SECURE_CONFIG_PARAM_<param> has been set, then all is well. However, if it has not been set, then that service url will not be set and a service which relies upon it being set may fail.

Oh, and it needs to have these propertes set under the ```[global``` section:

The following properties may be set:

- kbase_endpoint
- job_service_url
- workspace_url
- shock_url
- handle_url
- srv_wiz_url
- njsw_url
- auth_service_url_allow_insecure
- auth_service_url

> Note that the requirement for a specific service setting depends upon whether the module requires that service url setting. If it is not required it should not be required in the configuration file template, nor in the codebase itself. 

> Note that depending upon the language and tools used to build the deploy config file, an error may or may not be generated if a required configuration key is present.

For instance, if shock_url is not provided, and KBASE_SECURE_CONFIG_PARAM_SHOCK_URL environment variable will be used in its stead.

#### The config data file not provided

If the config data file is not provided, the configration will be built from environment variables and certain pre-determined service paths:

- KBASE_ENDPOINT must be provided, as it will be used to construct the service endpoints

- The following service endpoints will be constructed:
    - job_service_url - KBASE_ENDPOINT/userandjobstate
    - workspace_url - KBASE_ENDPOINT/ws
    - shock_url - KBASE_ENDPOINT/shock-api
    - handle_url - KBASE_ENDPOINT/handle_service
    - srv_wiz_url - KBASE_ENDPOINT/service_wizard
    - njsw_url - KBASE_ENDPOINT/njs_wrapper

- If AUTH_SERVICE_URL is set, then auth_service_url will be set to its value
    - otherwise, what it is it set to?

- if AUTH_SERVICE_URL_ALLOW_INSECURE is set, auth_service_url_allow_insecure is set to its value, otherwise it is set to the string "false"

- Any KBASE_SECURE_CONFIG_PARAM_<param> properties are set, which may override any of the above, supply them if they are missing (i.e. the AUTH_ settings), or set values not specified here (e.g. module-specific values)


### Building config file

The configuration file itself, the data file known to the module, is built by using the values provided above to populate a template file. The template file may be implemented in a templating system well supported by the module language, but usage of a common format is preferred. For instance, Python modules by default use Jinja templates, but this is a Python-only format (although ported to some other languages.) On the other hand, a very-widespread format like Mustache or Handlebars would be preferable.

The template file may also result in a data file format suitable to the language. It is notable that the ini format is not necessarily suitable for data to be consumed by a module. For instance, it only represents values as strings, which puts the burden on the module itself to convert numbers and boolean values. It is probably better to have the deployment script do this interpretation and populate a proper data structure, failing if any values are invalid.

For clojure the template language is mustache (using the stencil library) and the data format is edn, the native data serialization format for Clojure.

The config file is built by applying a deployment script or application to the template, with data applied (as described above). The default script will rename the original template, and write the new one in its place. I prefer, though, to keep the original one as-is, and write a new file.

I prefer this because otherwise the service may inadvertently be started with an invalid config file, which would supply bad values to it. Better to just have the file be missing if it has not been processed yet, or if the config file generation fails.

Again, since this is under control of the module, this should be no problem.


Optional


It is expected to start the service as either a json-rpc service or as a one-shot command line app. 

The different startup modes are

server
async
init
test
bash
report

### server

Server mode is indicated by lack of argument provided to docker run. By convention the ```./start_server.sh``` script is called.

By convension, the environment variable ```KB_DEPLOYMENT_CONFIG``` is set to the location of the config file, which is by convention located in the parent directory to the module (i.e. in /kb).

> The term "by convention" implies that these values are not predetermined by the SDK; rather they are commonly used, and for sdk-generated apps will be set this way. However, the values themselves and their usage within the module are determined the module's deployment script, the entrypoint script, and the module code -- that is, they are under complete control of the module itself.

### async

Async mode is indicated by the string "async" being provided to docker run. In this case the predetermined file ./work/token is made available. By convention the entrypoint script will set the environment variable ```KB_AUTH_TOKEN``` to this value

When the container is started, files are mounted in a common location:

/work/token

## Testing

### Testing deployment


java -cp ./target/server-0.1.0-SNAPSHOT-standalone.jar deploy.core ./test/deploy.template.cfg ./test/config.properties ./test/deploy.cfg

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
