FROM eclipse-temurin:21

RUN adduser --system --group user
USER user

EXPOSE 8003

VOLUME /tmp
COPY build/libs/*.jar writeserver.jar
ENTRYPOINT ["java","-jar","/writeserver.jar"]