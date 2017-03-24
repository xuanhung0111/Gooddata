#!/bin/bash
set -x
USERID=${HOST_USERID:=$(id -u ${USER})}
GROUPID=${HOST_GROUPID:=$(id -g ${USER})}

docker run \
       --rm -u ${USERID}:${GROUPID} \
       -e HOME=/graphene-tests \
       -v `pwd`:/graphene-tests \
       -v $HOME/.m2:/home/$USER/.m2 \
       -w /graphene-tests \
       -i $IMAGE_ID "$@"
