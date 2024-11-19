FROM openjdk:23

COPY target/electronic-dance-music-discovery.war /usr/app/

WORKDIR /usr/app

ENTRYPOINT ["java", "-jar", "electronic-dance-music-discovery.war"]