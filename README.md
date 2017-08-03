# Voidwalker
<img src="http://i.imgur.com/yfHO418.jpg" align="top" height=250 />

Voidwalker is a simple cms. Write, publish and edit markdown.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Setup

Install [leningen](https://leiningen.org/)

Install mysql

create profiles.clj with following contents

```clojure
;; WARNING
;; The profiles.clj file is used for local environment variables, such as database credentials.
;; This file is listed in .gitignore and will be excluded from version control by Git.

{:profiles/dev  {:env
                 {:database-url
                  "jdbc:mysql://localhost:3306/voidwalker?user=root&password="}}

 :profiles/test {:env
                 {:database-url
                  "jdbc:mysql://localhost/voidwalker?user=root&password="}}}
```

Make sure you have the correct user and password entered there.

Create a database voidwalker

Now run database migrations

```shell
lein migratus migrate
```

## Running

To start a web server for the application, run:

    lein run

## License
