(defproject vald-client-clj "0.1.0-SNAPSHOT"
  :description "A client library for Vald."
  :url "https://github.com/rinx/vald-client-clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [io.grpc/grpc-netty-shaded "1.26.0"]
                 [io.grpc/grpc-protobuf "1.26.0"]
                 [io.grpc/grpc-stub "1.26.0"]
                 [org.vdaas.vald/vald-client-java "0.0.2"]]
  :repl-options {:init-ns vald-client-clj.core}
  :profiles {:cmd
             {:source-paths ["cmd"]
              :dependencies [[org.clojure/tools.cli "0.4.2"]
                             [metosin/jsonista "0.2.2"]
                             [camel-snake-kebab "0.4.0"]
                             [io.quarkus/quarkus-grpc "1.26.0"]]
              :plugins [[io.taylorwood/lein-native-image "0.3.1"]]
              :aot :all
              :main vald-client-clj.cmd
              :native-image
              {:name "valdcli"
               :opts ["-H:+ReportExceptionStackTraces"
                      "-H:Log=registerResource:"
                      "--enable-url-protocols=http,https"
                      "--enable-all-security-services"
                      "--no-fallback"
                      "--no-server"
                      "--report-unsupported-elements-at-runtime"
                      "--initialize-at-build-time"
                      "--initialize-at-run-time=io.grpc.netty.shaded.io.netty.buffer.PoolChunk"
                      "--initialize-at-run-time=io.grpc.netty.shaded.io.netty.buffer.UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeDirectByteBuf"
                      "--allow-incomplete-classpath"
                      "--verbose"
                      "-J-Xms1g"
                      "-J-Xmx6g"]
               :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
             :static
             {:source-paths ["cmd"]
              :dependencies [[org.clojure/tools.cli "0.4.2"]
                             [metosin/jsonista "0.2.2"]
                             [camel-snake-kebab "0.4.0"]
                             [io.quarkus/quarkus-grpc "1.26.0"]]
              :plugins [[io.taylorwood/lein-native-image "0.3.1"]]
              :aot :all
              :main vald-client-clj.cmd
              :native-image
              {:name "valdcli"
               :opts ["-H:+ReportExceptionStackTraces"
                      "-H:Log=registerResource:"
                      "--enable-url-protocols=http,https"
                      "--enable-all-security-services"
                      "--no-fallback"
                      "--no-server"
                      "--report-unsupported-elements-at-runtime"
                      "--initialize-at-build-time"
                      "--initialize-at-run-time=io.grpc.netty.shaded.io.netty.buffer.PoolChunk"
                      "--initialize-at-run-time=io.grpc.netty.shaded.io.netty.buffer.UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeDirectByteBuf"
                      "--allow-incomplete-classpath"
                      "--verbose"
                      "--static"
                      "-J-Xms1g"
                      "-J-Xmx6g"]
               :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}})
