FROM openjdk:8u222-jdk-stretch
VOLUME /tmp
EXPOSE 8888
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.fleetmonitor.integration.ConfigServerApplication"]