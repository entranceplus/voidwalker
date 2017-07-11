FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/voidwalker.jar /voidwalker/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/voidwalker/app.jar"]
