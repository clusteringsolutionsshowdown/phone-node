#!/usr/bin/env bash

set -e

VERSION=$1

docker build -t clusteringsolutionsshowdown/phone-node:${VERSION} .

docker push clusteringsolutionsshowdown/phone-node:${VERSION}

kubectl set image deployment/phone-node phone-node=clusteringsolutionsshowdown/phone-node:${VERSION}