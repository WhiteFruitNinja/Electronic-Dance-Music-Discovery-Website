FROM openjdk:23

COPY target/*.war electronic-dance-music-discovery-website.war

ENTRYPOINT ["java","-jar","/electronic-dance-music-discovery-website.war"]