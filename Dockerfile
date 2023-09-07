FROM openjdk:17

ADD target/h2s-0.0.1-SNAPSHOT.jar h2s-0.0.1-SNAPSHOT.jar

ENTRYPOINT [ "java", "-jar", "h2s-0.0.1-SNAPSHOT.jar" ]