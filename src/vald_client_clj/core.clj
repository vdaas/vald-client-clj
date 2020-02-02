(ns vald-client-clj.core
  (:refer-clojure :exclude [update remove])
  (:import
   [org.vdaas.vald ValdGrpc]
   [org.vdaas.vald.agent AgentGrpc]
   [org.vdaas.vald.payload Object$ID Object$Vector]
   [org.vdaas.vald.payload Search$Request Search$IDRequest Search$Config]
   [io.grpc ManagedChannelBuilder]))

(defn ->search-config [config]
  (-> (Search$Config/newBuilder)
      (.setNum (or (:num config) 0))
      (.setRadius (or (:radius config) 0))
      (.setEpsilon (or (:epsilon config) 0))
      (.setTimeout (or (:timeout config) 0))
      (.build)))

(defn object-id->map [id]
  {:id (.getId id)})

(defn object-distance->map [dist]
  {:id (.getId dist)
   :distance (.getDistance dist)})

(defn meta-vector->map [meta-vec]
  {:uuid (.getUuid meta-vec)
   :meta (.getMeta meta-vec)
   :vector (.getVectorList meta-vec)
   :ips (.getIpsList meta-vec)})

(defn parse-search-response [res]
  (-> res
      (.getResultsList)
      (->> (mapv object-distance->map))))

(defprotocol IClient
  (close [this])
  (exists [this id])
  (search [this vector config])
  (search-by-id [this id config])
  (insert [this id vector])
  (update [this id vector])
  (remove-id [this id])
  (get-object [this id]))

(defrecord Client [channel stub]
  IClient
  (close [this]
    (-> channel
        (.shutdown)))
  (exists [this id]
    (let [id (-> (Object$ID/newBuilder)
                 (.setId id)
                 (.build))]
      (-> stub
          (.exists id)
          (object-id->map))))
  (search [this vector config]
    (let [req (-> (Search$Request/newBuilder)
                  (.addAllVector (seq (map float vector)))
                  (.setConfig (->search-config config))
                  (.build))]
      (-> stub
          (.search req)
          (parse-search-response))))
  (search-by-id [this id config]
    (let [req (-> (Search$IDRequest/newBuilder)
                  (.setId id)
                  (.setConfig (->search-config config))
                  (.build))]
      (-> stub
          (.searchByID req)
          (parse-search-response))))
  (insert [this id vector]
    (let [req (-> (Object$Vector/newBuilder)
                  (.setId id)
                  (.addAllVector (seq (map float vector)))
                  (.build))]
      (-> stub
          (.insert req))))
  (update [this id vector]
    (let [req (-> (Object$Vector/newBuilder)
                  (.setId id)
                  (.addAllVector (seq (map float vector)))
                  (.build))]
      (-> stub
          (.update req))))
  (remove-id [this id]
    (let [id (-> (Object$ID/newBuilder)
                 (.setId id)
                 (.build))]
      (-> stub
          (.remove id))))
  (get-object [this id]
    (let [id (-> (Object$ID/newBuilder)
                 (.setId id)
                 (.build))]
      (-> stub
          (.getObject id)
          (meta-vector->map)))))

(defn grpc-channel [host port]
  (-> (ManagedChannelBuilder/forAddress host port)
      (.usePlaintext)
      (.build)))

(defn blocking-stub [channel]
  (ValdGrpc/newBlockingStub channel))

(defn async-stub [channel]
  (ValdGrpc/newStub channel))

(defn agent-blocking-stub [channel]
  (AgentGrpc/newBlockingStub channel))

(defn agent-async-stub [channel]
  (AgentGrpc/newStub channel))

(defn vald-client [host port]
  (let [channel (grpc-channel host port)
        stub (blocking-stub channel)]
    (->Client channel stub)))

(defn agent-client [host port]
  (let [channel (grpc-channel host port)
        stub (agent-blocking-stub channel)]
    (->Client channel stub)))

(comment
  (def client (vald-client "localhost" 8081))
  (def client (agent-client "localhost" 8081))
  (close client)

  (defn rand-vec []
    (take 4096 (repeatedly #(float (rand)))))

  (-> client
      (exists "test"))
  (-> client
      (search (rand-vec) {:num 10}))
  (-> client
      (search-by-id "test" {:num 10}))

  (-> client
      (insert "test" (rand-vec)))

  (->> (take 100 (range))
       (map
        (fn [i]
          (-> client
              (insert (str "x" i) (rand-vec))))))

  (-> client
      (get-object "test"))
)
