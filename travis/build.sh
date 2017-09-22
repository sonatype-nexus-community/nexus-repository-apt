#! /bin/bash
set -e

mvn
docker build -t hypertino/nexus3-apt .

if [[ "$TRAVIS_PULL_REQUEST" == "false" && "$TRAVIS_BRANCH" == "master" ]]; then
	docker login -u="$DOCKER_USERNAME" -p="$DOCKER_PASSWORD"
	docker push hypertino/nexus3-apt
fi
