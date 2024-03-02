FROM eclipse-temurin:21

RUN adduser --system --group user
USER user

EXPOSE 8002

VOLUME /tmp
COPY build/libs/*.jar webserver.jar
ENTRYPOINT ["java","-jar","/webserver.jar"]