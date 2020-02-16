# vald-client-clj / valdcli

[![LICENSE](https://img.shields.io/github/license/rinx/vald-client-clj?style=flat-square)](https://github.com/rinx/vald-client-clj/blob/master/LICENSE)
[![release](https://img.shields.io/github/v/release/rinx/vald-client-clj?style=flat-square)](https://github.com/rinx/vald-client-clj/releases)
[![Clojars Project](https://img.shields.io/clojars/v/vald-client-clj.svg?style=flat-square)](https://clojars.org/vald-client-clj)
[![Docker Pulls](https://img.shields.io/docker/pulls/rinx/valdcli.svg?style=flat-square)](https://hub.docker.com/r/rinx/valdcli)

[![GitHub Actions: Build Native image](https://github.com/rinx/vald-client-clj/workflows/Build%20native%20image/badge.svg)](https://github.com/rinx/vald-client-clj/actions)

A Clojure gRPC client library for [Vald](https://github.com/vdaas/vald).

## Usage

To use this library, one of the following libraries is required.

- `io.grpc/grpc-okhttp`
- `io.grpc/grpc-netty`
- `io.grpc/grpc-netty-shaded`

```clojure
(require '[vald-client-clj.core :as vald])

;; vald gateway
(def client
  (vald/vald-client "localhost" 8081))

;; vald agent
(def agent-client
  (vald/agent-client "localhost" 8081))
;; NOTE:
;;   gateway and agent have same interfaces.
;;   These methods can also be used for agent client.

(-> client
    (vald/stream-insert
      println
      [{:id "meta1"
        :vector [0.1 0.2 0.3 0.4 0.5 0.6]}
       {:id "meta2"
        :vector [0.2 0.2 0.2 0.2 0.2 0.2]}])
    (deref))

(-> client
    (vald/get-object "meta1"))

(-> client
    (vald/stream-search-by-id println {:num 2} ["meta1" "meta2"])
    (deref))

(-> client
    (vald/search {:num 2} [0.1 0.2 0.3 0.3 0.3 0.4]))

(-> agent-client
    (vald/search {:num 2} [0.1 0.2 0.3 0.3 0.3 0.4]))

(vald/close client)
(vald/close agent-client)
```

## valdcli

`valdcli` is a CLI tool built from vald-client-clj.

Fast startup time powered by GraalVM.

### Install

Native binaries are available from the [latest release](https://github.com/rinx/vald-client-clj/releases/latest).

#### Docker image

[Docker image](https://hub.docker.com/r/rinx/valdcli) is also available.

```sh
$ docker run -it rinx/valdcli --help
```

### Usage

```sh
$ valdcli --help
Usage: valdcli [OPTIONS] ACTION

Options:
      --help                  show usage
  -d, --debug                 debug mode
  -p, --port PORT  8080       Port number
  -h, --host HOST  localhost  Hostname
  -a, --agent                 connect as an agent client

Actions:
  exists               Check whether ID exists or not.
  insert               Insert single vector.
  search               Search single vector.
  search-by-id         Search vectors using single ID.
  update               Update single vector.
  remove               Remove single ID.
  get-object           Get object info of single ID.
  stream-insert        Insert multiple vectors.
  stream-search        Search multiple vectors.
  stream-search-by-id  Search vectors using multiple IDs.
  stream-update        Update multiple vectors.
  stream-remove        Remove multiple IDs.
  stream-get-object    Get object info of multiple IDs.
  rand-vec             Prints randomized vector.
  rand-vecs            Prints randomized vectors.
```

It supports both EDN and JSON format data.

#### insert

```sh
## insert EDN formatted vector
$ valdcli insert abc "[0.1 0.2 0.3 0.4 0.5 0.6]"

## it supports to read stdin
$ echo "[0.1 0.2 0.3 0.4 0.5 0.6]" | valdcli -p 8081 insert abc

## by adding '--json' flag, it reads JSON formatted vector
$ valdcli -h vald.vdaas.org -p 8081 insert --json abc "[0.1, 0.2, 0.3, 0.4, 0.5, 0.6]"
```

#### search, search-by-id

```sh
$ valdcli search '[0.1 0.2 0.3 0.4 0.5 0.6]'

## using options
$ valdcli search --num 100 --epsilon 0.02 '[0.1 0.2 0.3 0.4 0.5 0.6]'

## search id 'xyz'
$ valdcli search-by-id --num 100 xyz
```

#### stream-insert, stream-search

```sh
$ valdcli stream-insert '[{:id "abc" :vector [0.1 0.2 0.3 0.4 0.5 0.6]} {:id "def" :vector [0.1 0.2 0.3 0.4 0.5 0.6]}]'

$ valdcli stream-search -n 5 '[[0.1 0.2 0.3 0.4 0.5 0.6] [0.1 0.2 0.3 0.4 0.5 0.6] [0.1 0.2 0.3 0.4 0.5 0.6]]'

$ valdcli stream-search-by-id -n 5 '["abc" "def" "xyz"]'
```

#### tips

usages of each commands available by running:

```sh
$ valdcli exists --help
```

To use as an agent client, it is recommended to have an alias like:

```sh
alias agentcli='valdcli --agent'
```

## TODO

- [ ] `vald-client-clj.core` documentation
- [x] publish to Clojars
- [x] generete randomized vector
- [ ] REPL, socket REPL and scripting
- [x] e2e testing

## License

Copyright © 2020 rinx

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
