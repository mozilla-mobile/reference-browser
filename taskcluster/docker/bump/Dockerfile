# %ARG DOCKER_IMAGE_PARENT
FROM $DOCKER_IMAGE_PARENT

MAINTAINER Johan Lorenzo <jlorenzo+tc@mozilla.com>

ENV HUB_ARCHIVE='hub.tgz' \
  HUB_ROOT='/builds/worker/hub' \
  HUB_SHA256='734733c9d807715a4ec26ccce0f9987bd19f1c3f84dd35e56451711766930ef0' \
  HUB_VERSION='2.14.1'

RUN $CURL --output hub.tgz "https://github.com/github/hub/releases/download/v$HUB_VERSION/hub-linux-amd64-$HUB_VERSION.tgz" \
  && echo "$HUB_SHA256  $HUB_ARCHIVE" | sha256sum --check \
  && mkdir "$HUB_ROOT" \
  && tar xzvf "$HUB_ARCHIVE" --strip-components=1 --directory="$HUB_ROOT" \
  && rm "$HUB_ARCHIVE"

ENV PATH="$HUB_ROOT/bin:$PATH"
