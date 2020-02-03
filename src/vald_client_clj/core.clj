(ns vald-client-clj.core
  (:refer-clojure :exclude [update remove])
  (:import
   [org.vdaas.vald ValdGrpc]
   [org.vdaas.vald.agent AgentGrpc]
   [org.vdaas.vald.payload Object$ID Object$IDs Object$Vector Object$Vectors]
   [org.vdaas.vald.payload Search$Request Search$IDRequest Search$Config]
   [io.grpc ManagedChannelBuilder]
   [io.grpc.stub StreamObserver]))

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
   :vector (seq (.getVectorList meta-vec))
   :ips (seq (.getIpsList meta-vec))})

(defn parse-search-response [res]
  (-> res
      (.getResultsList)
      (->> (mapv object-distance->map))))

(defprotocol IClient
  (close [this])
  (exists [this id])
  (search [this vector config])
  (search-by-id [this id config])
  (stream-search [this vectors config])
  (stream-search-by-id [this ids config])
  (insert [this id vector])
  (stream-insert [this vectors])
  (multi-insert [this vectors])
  (update [this id vector])
  (stream-update [this vectors])
  (multi-update [this vectors])
  (remove-id [this id])
  (stream-remove [this ids])
  (multi-remove [this ids])
  (get-object [this id])
  (stream-get-object [this ids]))

(defrecord Client [channel stub async-stub]
  IClient
  (close [this]
    (when (false? (.isShutdown channel))
      (-> channel
          (.shutdown))))
  (exists [this id]
    (when (false? (.isShutdown channel))
      (let [id (-> (Object$ID/newBuilder)
                   (.setId id)
                   (.build))]
        (-> stub
            (.exists id)
            (object-id->map)))))
  (search [this vector config]
    (when (false? (.isShutdown channel))
      (let [req (-> (Search$Request/newBuilder)
                    (.addAllVector (seq (map float vector)))
                    (.setConfig (->search-config config))
                    (.build))]
        (-> stub
            (.search req)
            (parse-search-response)))))
  (search-by-id [this id config]
    (when (false? (.isShutdown channel))
      (let [req (-> (Search$IDRequest/newBuilder)
                    (.setId id)
                    (.setConfig (->search-config config))
                    (.build))]
        (-> stub
            (.searchByID req)
            (parse-search-response)))))
  (stream-search [this vectors config]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            am (atom [])
            config (->search-config config)
            reqs (->> vectors
                      (mapv #(-> (Search$Request/newBuilder)
                                 (.addAllVector (seq (map float %)))
                                 (.setConfig config)
                                 (.build))))
            observer (-> async-stub
                         (.streamSearch
                          (reify StreamObserver
                            (onNext [this res]
                              (->> (parse-search-response res)
                                   (swap! am conj)))
                            (onError [this throwable]
                              (deliver pm @am))
                            (onCompleted [this]
                              (deliver pm @am)))))]
        (->> reqs
             (map #(-> observer
                       (.onNext %)))
             (doall))
        (-> observer
            (.onCompleted))
        @pm)))
  (stream-search-by-id [this ids config]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            am (atom [])
            config (->search-config config)
            reqs (->> ids
                      (mapv #(-> (Search$IDRequest/newBuilder)
                                 (.setId %)
                                 (.setConfig config)
                                 (.build))))
            observer (-> async-stub
                         (.streamSearchByID
                          (reify StreamObserver
                            (onNext [this res]
                              (->> (parse-search-response res)
                                   (swap! am conj)))
                            (onError [this throwable]
                              (deliver pm @am))
                            (onCompleted [this]
                              (deliver pm @am)))))]
        (->> reqs
             (map #(-> observer
                       (.onNext %)))
             (doall))
        (-> observer
            (.onCompleted))
        @pm)))
  (insert [this id vector]
    (when (false? (.isShutdown channel))
      (let [req (-> (Object$Vector/newBuilder)
                    (.setId id)
                    (.addAllVector (seq (map float vector)))
                    (.build))]
        (-> stub
            (.insert req)))))
  (stream-insert [this vectors]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            am (atom [])
            reqs (->> vectors
                      (mapv #(-> (Object$Vector/newBuilder)
                                 (.setId (:id %))
                                 (.addAllVector (seq (map float (:vector %))))
                                 (.build))))
            observer (-> async-stub
                         (.streamInsert
                          (reify StreamObserver
                            (onNext [this res]
                              (swap! am conj res))
                            (onError [this throwable]
                              (deliver pm @am))
                            (onCompleted [this]
                              (deliver pm @am)))))]
        (->> reqs
             (map #(-> observer
                       (.onNext %)))
             (doall))
        (-> observer
            (.onCompleted))
        @pm)))
  (multi-insert [this vectors]
    (when (false? (.isShutdown channel))
      (let [vecs (->> vectors
                      (map #(-> (Object$Vector/newBuilder)
                                (.setId (:id %))
                                (.addAllVector (seq (map float (:vector %))))
                                (.build))))
            req (-> (Object$Vectors/newBuilder)
                    (.addAllVectors vecs))]
        (-> stub
            (.multiInsert req)))))
  (update [this id vector]
    (when (false? (.isShutdown channel))
      (let [req (-> (Object$Vector/newBuilder)
                    (.setId id)
                    (.addAllVector (seq (map float vector)))
                    (.build))]
        (-> stub
            (.update req)))))
  (stream-update [this vectors]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            am (atom [])
            reqs (->> vectors
                      (mapv #(-> (Object$Vector/newBuilder)
                                 (.setId (:id %))
                                 (.addAllVector (seq (map float (:vector %))))
                                 (.build))))
            observer (-> async-stub
                         (.streamUpdate
                          (reify StreamObserver
                            (onNext [this res]
                              (swap! am conj res))
                            (onError [this throwable]
                              (deliver pm @am))
                            (onCompleted [this]
                              (deliver pm @am)))))]
        (->> reqs
             (map #(-> observer
                       (.onNext %)))
             (doall))
        (-> observer
            (.onCompleted))
        @pm)))
  (multi-update [this vectors]
    (when (false? (.isShutdown channel))
      (let [vecs (->> vectors
                      (map #(-> (Object$Vector/newBuilder)
                                (.setId (:id %))
                                (.addAllVector (seq (map float (:vector %))))
                                (.build))))
            req (-> (Object$Vectors/newBuilder)
                    (.addAllVectors vecs))]
        (-> stub
            (.multiUpdate req)))))
  (remove-id [this id]
    (when (false? (.isShutdown channel))
      (let [id (-> (Object$ID/newBuilder)
                   (.setId id)
                   (.build))]
        (-> stub
            (.remove id)))))
  (stream-remove [this ids]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            am (atom [])
            reqs (->> ids
                      (mapv #(-> (Object$ID/newBuilder)
                                 (.setId %)
                                 (.build))))
            observer (-> async-stub
                         (.streamRemove
                          (reify StreamObserver
                            (onNext [this res]
                              (swap! am conj res))
                            (onError [this throwable]
                              (deliver pm @am))
                            (onCompleted [this]
                              (deliver pm @am)))))]
        (->> reqs
             (map #(-> observer
                       (.onNext %)))
             (doall))
        (-> observer
            (.onCompleted))
        @pm)))
  (multi-remove [this ids]
    (when (false? (.isShutdown channel))
      (let [req (-> (Object$IDs/newBuilder)
                    (.addAllID ids))]
        (-> stub
            (.multiRemove req)))))
  (get-object [this id]
    (when (false? (.isShutdown channel))
      (let [id (-> (Object$ID/newBuilder)
                   (.setId id)
                   (.build))]
        (-> stub
            (.getObject id)
            (meta-vector->map)))))
  (stream-get-object [this ids]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            am (atom [])
            reqs (->> ids
                      (mapv #(-> (Object$ID/newBuilder)
                                 (.setId %)
                                 (.build))))
            observer (-> async-stub
                         (.streamGetObject
                          (reify StreamObserver
                            (onNext [this res]
                              (->> (meta-vector->map res)
                                   (swap! am conj)))
                            (onError [this throwable]
                              (deliver pm @am))
                            (onCompleted [this]
                              (deliver pm @am)))))]
        (->> reqs
             (map #(-> observer
                       (.onNext %)))
             (doall))
        (-> observer
            (.onCompleted))
        @pm))))

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
        stub (blocking-stub channel)
        async-stub (async-stub channel)]
    (->Client channel stub async-stub)))

(defn agent-client [host port]
  (let [channel (grpc-channel host port)
        stub (agent-blocking-stub channel)
        async-stub (agent-async-stub channel)]
    (->Client channel stub async-stub)))

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

  (println
   (stream-search client (take 10 (repeatedly #(rand-vec))) {:num 10}))
  (println
   (stream-search-by-id client ["test" "zz1" "zz3"] {:num 3}))
  (println
   (stream-get-object client ["zz1" "zz3"]))

  (-> client
      (insert "test" (rand-vec)))

  (->> (take 300 (range))
       (map
        (fn [i]
          (-> client
              (insert (str "zz" i) (rand-vec))))))

  (let [vectors (->> (take 10 (range))
                     (map
                      (fn [i]
                        {:id (str "a" i)
                         :vector (rand-vec)})))]
    (stream-insert client vectors))

  (-> client
      (get-object "zz3")))
