FROM java:openjdk-8-jdk-alpine
MAINTAINER Erik Pearson

# Developer image for clojure development
# The primary responsibility of this container is to host ...

# First let us just host the app.
# Then we'll work on the developer workflow.

COPY ./contents /kb/module
RUN mkdir -p /kb/module/work \
    && chmod 777 /kb/module \
    && chmod +x /kb/module/entrypoint.sh \
    && apk add --no-cache make \
    && apk add --no-cache bash

WORKDIR /kb/module

RUN make install

ENTRYPOINT [ "./entrypoint.sh" ]

CMD [ ]
