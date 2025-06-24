#!/bin/bash

MVN_HOME="X:\.m2\repository"
SC_CONTRACT_DOCKER_VERSION=4.2.0

STUB_GROUP="${STUB_GROUP:-com.tn.service}"
STUB_ARTIFACT="${STUB_ARTIFACT:-tn-user-service}"
STUB_VERSION="${STUB_VERSION:-1.0.00-SNAPSHOT}"
STUB_PORT="9876"

if [[ -z $1 ]]; then
  STUB_RUNNER_MODE="${STUB_RUNNER_MODE:-REMOTE}"
else
  STUB_RUNNER_MODE=$1
fi

STUBRUNNER_REPOSITORY_ROOT="" #TODO add the URL to the GitHub TN repo.
STUBRUNNER_PORT="${STUBRUNNER_PORT:-8083}"
STUBRUNNER_IDS="${STUB_GROUP}:${STUB_ARTIFACT}:${STUB_VERSION}:stubs:${STUB_PORT}"

if [[ $STUB_RUNNER_MODE = "LOCAL" ]]; then
  echo "Running in LOCAL mode."
  docker run -rm \
    -v "${MVN_HOME}:/home/scc/,m2" \
    -e "STUB_RUNNER_MODE=LOCAL" \
    -e "STUBRUNNER_IDS=${STUBRUNNER_IDS}" \
    -e "SERVER_PORT=${STUBRUNNER_PORT}" \
    -p "${STUBRUNNER_PORT}:${STUBRUNNER_PORT}" \
    -p "${STUB_PORT}:${STUB_PORT}"
    springcloud/spring-cloud-contract-stub-runner:"${SC_CONTRACT_DOCKER_VERSION}"
elif [[ $STUB_RUNNER_MODE = "REMOTE" ]]; then
  echo "Running in REMOTE mode."
  docker run -rm \
    -e "STUBRUNNER_REPOSITORY_ROOT=${STUBRUNNER_REPOSITORY_ROOT}" \
    -e "STUBRUNNER_IDS=${STUBRUNNER_IDS}" \
    -e "SERVER_PORT=${STUBRUNNER_PORT}" \
    -p "${STUBRUNNER_PORT}:${STUBRUNNER_PORT}" \
    -p "${STUB_PORT}:${STUB_PORT}"
    springcloud/spring-cloud-contract-stub-runner:"${SC_CONTRACT_DOCKER_VERSION}"
else
  echo "Unknown mode: ${STUB_RUNNER_MODE}."
fi