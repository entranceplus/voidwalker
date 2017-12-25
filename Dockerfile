FROM clojure:lein
MAINTAINER Akash Shakdwipeea <ashakdwipeea@gmail.com>

RUN mkdir /usr/src/voidwalker
WORKDIR /usr/src/voidwalker

# prod
COPY target/uberjar/voidwalker.jar .
CMD ["java", "-jar", "voidwalker.jar"]

# dev
# COPY . /usr/src/voidwalker
# CMD ["lein", "repl", ":headless", ":host", "0.0.0.0", ":port", "7888"]
