FROM autonationdocker.azurecr.io/java11osbaseimage:latest

VOLUME /tmp
#ARG JAR_FILE
ADD target/*.jar app.jar

ENTRYPOINT exec java $JAVA_OPTS -Dspring.profiles.active=$APP_ACTIVE_PROFILE -Dserver.name=$HOSTNAME -jar /app.jar