(ns vald-client-clj.core
  (:import
   [org.vdaas.vald ValdGrpc ValdGrpc$ValdStub ValdGrpc$ValdBlockingStub]
   [io.grpc ManagedChannelBuilder]))

(defn grpc-channel [host port]
  (-> (ManagedChannelBuilder/forAddress host port)
      (.usePlaintext)
      (.build)))

(defn blocking-stub [channel]
  (ValdGrpc/newBlockingStub channel))

(defn async-stub [channel]
  (ValdGrpc/newStub channel))

(defn vald-client [host port]
  (-> (grpc-channel host port)
      (blocking-stub)))

(comment
  (-> (vald-client "localhost" 8081)
      (.search nil)))
