# %ARG DOCKER_IMAGE_PARENT
FROM $DOCKER_IMAGE_PARENT

MAINTAINER Johan Lorenzo <jlorenzo+tc@mozilla.com>

RUN apt-get install -y openjdk-8-jdk \
    && apt-get clean

USER worker:worker

COPY install-sdk.sh ./

RUN "$SHELL" -c "source install-sdk.sh"

# run-task expects to run as root
USER root
