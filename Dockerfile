FROM openjdk:15-jdk
COPY /target/kthg-dproject2021-0.0.1-jar-with-dependencies.jar /app.jar
EXPOSE 8080
ENV DB_DRIVER=""
ENV DB_CONN_URL=""
ENV DB_USERNAME=""
ENV DB_PASSWORD=""
ENV LOGLEVEL=""
ENV JAVA_OPTS=""
RUN touch /app.jar
ENTRYPOINT ["sh", "-cp", "java ${JAVA_OPTS} -DdependencyTrack.logging.level=$LOGGING_LEVEL -jar /app.jar ${0} ${@}"]