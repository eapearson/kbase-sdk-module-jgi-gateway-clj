# Clojure Project


This is a java project, but I'm going to try to wedge a clojure based service in here...

## Clojure

## Packaging


## running

This runs the Dockerfile, which builds the container and runs ./run.sh
It maps the port 3001 from inside to outside, and provides the PORT env variable
which the app inside picks up and uses.

```
docker run -p 3001:3001 -e PORT=3001 eapearson-ui-test
```

## Mysteries

### input file format

so what is the format of the input json? It is json after all, but the sdk
wraps it up.

id
method
version
context
params
