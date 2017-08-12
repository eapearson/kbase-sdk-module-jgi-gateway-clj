FROM java:openjdk-8-jdk-alpine
# FROM anapsix/alpine-java:jdk8
# FROM kbase/kbase:sdkbase.latest
MAINTAINER Erik Pearson
# -----------------------------------------

# Insert apt-get instructions here to install
# any required dependencies for your module.

# RUN apt-get update
# RUN apk add --no-cache openjdk

# -----------------------------------------

COPY ./dist/module /kb/module
RUN mkdir -p /kb/module/work \
    && chmod 777 /kb/module

WORKDIR /kb/module

RUN apk add --no-cache make \
    && apk add --no-cache bash \
    && make install

ENTRYPOINT [ "./entrypoint.sh" ]

CMD [ ]
