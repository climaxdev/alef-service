FROM openjdk:17-alpine

ARG VERSION
ARG JAR_FILE

LABEL name="authoring-search-service" \
    vendor="Alef Education" \
    release="MVP" \
    summary="Authoring Search Service" \
    description="Service for managing AAT search"

ENV APP_ROOT=/usr/app
ENV HOME=${APP_ROOT}
ENV SPRING_PROFILES_ACTIVE=""
ENV JAVA_OPTS=""
ENV JAR_FILE=${JAR_FILE}

RUN mkdir ${APP_ROOT} && \
    mkdir ${APP_ROOT}/bin && \
    chmod -R u+x ${APP_ROOT}/bin && \
    chgrp -R 0 ${APP_ROOT} && \
    chmod -R g=u ${APP_ROOT} /etc/passwd
### Containers should NOT run as root as a good practice
#USER 1001
WORKDIR ${APP_ROOT}

ADD $JAR_FILE "$APP_ROOT/$JAR_FILE"

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar $APP_ROOT/$JAR_FILE" ]

EXPOSE 8080

VOLUME $APP_ROOT/tmp
