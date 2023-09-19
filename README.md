# vald-client-clj / valdcli

[![LICENSE](https://img.shields.io/github/license/vdaas/vald-client-clj?style=flat-square)](https://github.com/vdaas/vald-client-clj/blob/main/LICENSE)
[![release](https://img.shields.io/github/v/release/vdaas/vald-client-clj?style=flat-square)](https://github.com/vdaas/vald-client-clj/releases)
[![Clojars Project](https://img.shields.io/clojars/v/vald-client-clj.svg?style=flat-square)](https://clojars.org/vald-client-clj)
[![Vald version](https://img.shields.io/github/release/vdaas/vald.svg?style=flat-square)](https://github.com/vdaas/vald/releases/latest)
[![clojure](https://img.shields.io/badge/clojure-1.11.1-orange)](https://clojure.github.io/clojure/)

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

(vald/close client)
```

## valdcli

`valdcli` is a CLI tool built from vald-client-clj.

Fast startup time powered by GraalVM.

### Install

Native binaries are available from the [latest release](https://github.com/vdaas/vald-client-clj/releases/latest).

### Usage

```sh
$ valdcli --help
Usage: valdcli [OPTIONS] ACTION

Options:
      --help                  show usage
      --version               show version
  -d, --debug                 debug mode
  -p, --port PORT  8080       Port number
  -h, --host HOST  localhost  Hostname

Actions:
  create-and-save-index Call create-and-save-index command. (only for Agent)
  create-index          Call create-index command. (only for Agent)
  exists                Check whether ID exists or not.
  get-object            Get object info of single ID.
  index-info            Fetch index info. (only for Agent)
  insert                Insert single vector.
  rand-vec              Prints randomized vector.
  rand-vecs             Prints randomized vectors.
  remove                Remove single ID.
  save-index            Call save-index command. (only for Agent)
  search                Search single vector.
  search-by-id          Search vectors using single ID.
  stream-get-object     Get object info of multiple IDs.
  stream-insert         Insert multiple vectors.
  stream-remove         Remove multiple IDs.
  stream-search         Search multiple vectors.
  stream-search-by-id   Search vectors using multiple IDs.
  stream-update         Update multiple vectors.
  update                Update single vector.
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

## License

Copyright (C) 2020 Vdaas.org Vald team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B21465%2Fvald-client-clj.svg?type=large)](https://app.fossa.com/projects/custom%2B21465%2Fvald-client-clj?ref=badge_large)
