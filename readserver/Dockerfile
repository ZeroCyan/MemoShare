FROM eclipse-temurin:21

RUN adduser --system --group user
USER user

EXPOSE 8004

VOLUME /tmp
COPY build/libs/*.jar readserver.jar
ENTRYPOINT ["java","-jar","/readserver.jar"]