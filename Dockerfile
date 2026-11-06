FROM eclipse-temurin:25-jdk-alpine

ARG IMAGE_DIR

RUN apk add --no-cache bash &&\
    addgroup tn &&\
    adduser --system tn-user-service tn &&\
    mkdir -p /opt/tn/tn-user-service

COPY ${IMAGE_DIR} /opt/tn/tn-user-service

USER tn-user-service

ENTRYPOINT [ "bash", "/opt/tn/tn-user-service/bin/start.sh" ]
