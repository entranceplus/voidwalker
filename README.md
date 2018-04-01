# Voidwalker
<img src="http://i.imgur.com/yfHO418.jpg" align="top" height=250 />

Voidwalker is a simple cms.

## Prerequisites

Install `boot` and `shadow-cljs`

## Setup

* Install postgresql
* create your profiles.edn (todo write spec for config)
* secreate db with those credentials
* run resources/migration.sql

## Running

Running clj

```shell
boot dev
```

Compiling cljs
```shell
shadow-cljs watch app
```

## License
