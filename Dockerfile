FROM clojure:boot
MAINTAINER Akash Shakdwipeea <ashakdwipeea@gmail.com>

RUN mkdir /voidwalker
WORKDIR /voidwalker


COPY . /voidwalker/

RUN apt-get install -y curl \
  && curl -sL https://deb.nodesource.com/setup_9.x | bash - \
  && apt-get install -y nodejs \
  && curl -L https://www.npmjs.com/install.sh | sh

RUN apt-get install -y curl && curl -sL https://deb.nodesource.com/setup_9.x | bash  && apt-get install -y nodejs && curl -L https://www.npmjs.com/install.sh | sh

RUN npm i -g shadow-cljs

RUN boot publish

CMD ["cat"]
