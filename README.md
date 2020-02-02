# vald-client-clj

A Clojure client library for [Vald](https://github.com/vdaas/vald).

## Usage

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
      [{:id "meta1"
        :vector [0.1 0.2 0.3 0.4 0.5 0.6]}
       {:id "meta2"
        :vector [0.2 0.2 0.2 0.2 0.2 0.2]}]))

(-> client
    (vald/get-object "meta1"))

(-> client
    (vald/stream-search-by-id ["meta1" "meta2"] {:num 2}))

(-> client
    (vald/search [0.1 0.2 0.3 0.3 0.3 0.4] {:num 2}))

(-> agent-client
    (vald/search [0.1 0.2 0.3 0.3 0.3 0.4] {:num 2}))

(vald/close client)
(vald/close agent-client)
```

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
