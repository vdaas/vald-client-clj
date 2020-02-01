(ns vald-client-clj.core
  (:refer-clojure :exclude [update remove])
  (:import
   [org.vdaas.vald ValdGrpc ValdGrpc$ValdStub ValdGrpc$ValdBlockingStub]
   [org.vdaas.vald.payload Object$ID Object$Vector]
   [org.vdaas.vald.payload Search$Request Search$IDRequest Search$Config]
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

(defn ->search-config [config]
  (-> (Search$Config/newBuilder)
      (.setNum (or (:num config) 0))
      (.setRadius (or (:radius config) 0))
      (.setEpsilon (or (:epsilon config) 0))
      (.setTimeout (or (:timeout config) 0))
      (.build)))

(defn parse-object-id [id]
  {:id (.getId id)})

(defn parse-object-distance [dist]
  {:id (.getId dist)
   :distance (.getDistance dist)})

(defn parse-search-response [res]
  (-> res
      (.getResultsList)
      (->> (mapv parse-object-distance))))

(defn parse-meta-vector [meta-vec]
  {:uuid (.getUuid meta-vec)
   :meta (.getMeta meta-vec)
   :vector (.getVectorList meta-vec)
   :ips (.getIpsList meta-vec)})

(defn exists [client id]
  (let [id (-> (Object$ID/newBuilder)
               (.setId id)
               (.build))]
    (-> client
        (.exists id)
        (parse-object-id))))

(defn search [client vector config]
  (let [req (-> (Search$Request/newBuilder)
                (.addAllVector (seq (map float vector)))
                (.setConfig (->search-config config))
                (.build))]
    (-> client
        (.search req)
        (parse-search-response))))

(defn search-by-id [client id config]
  (let [req (-> (Search$IDRequest/newBuilder)
                (.setId id)
                (.setConfig (->search-config config))
                (.build))]
    (-> client
        (.searchByID req)
        (parse-search-response))))

(defn insert [client id vector]
  (let [req (-> (Object$Vector/newBuilder)
                (.setId id)
                (.addAllVector (seq (map float vector)))
                (.build))]
    (-> client
        (.insert req))))

(defn update [client id vector]
  (let [req (-> (Object$Vector/newBuilder)
                (.setId id)
                (.addAllVector (seq (map float vector)))
                (.build))]
    (-> client
        (.update req))))

(defn remove [client id]
  (let [id (-> (Object$ID/newBuilder)
               (.setId id)
               (.build))]
    (-> client
        (.remove id))))

(defn get-object [client id]
  (let [id (-> (Object$ID/newBuilder)
               (.setId id)
               (.build))]
    (-> client
        (.getObject id)
        (parse-meta-vector))))

(comment
  (def client (vald-client "localhost" 8081))

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
