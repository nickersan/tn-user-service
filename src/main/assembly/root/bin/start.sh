#!/bin/bash

ROOT="$( cd "$( dirname "${BASH_SOURCE[@]}" )/.." >/dev/null 2>&1 && pwd )"
CLASSPATH=$(JARS=("$ROOT"/lib/*.jar); IFS=:; echo "${JARS[*]}")

java ${JAVA_OPTS} -cp ${CLASSPATH} com.tn.user.Application
