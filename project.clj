(defproject vald-client-clj "0.1.0-SNAPSHOT"
  :description "A client library for Vald."
  :url "https://github.com/rinx/vald-client-clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "0.7.559"]
                 [io.grpc/grpc-netty-shaded "1.26.0"]
                 [io.grpc/grpc-protobuf "1.26.0"]
                 [io.grpc/grpc-stub "1.26.0"]
                 [org.vdaas.vald/vald-client-java "0.0.2"]]
  :repl-options {:init-ns vald-client-clj.core})
