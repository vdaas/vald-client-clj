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
      (.setNum (or (:num config) 10))
      (.setRadius (or (:radius config) 1.0))
      (.setEpsilon (or (:epsilon config) 0.1))
      (.setTimeout (or (:timeout config) 100000))
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

(defn object-vector->map [obj-vec]
  {:id (.getId obj-vec)
   :vector (seq (.getVectorList obj-vec))})

(defn parse-search-response [res]
  (-> res
      (.getResultsList)
      (->> (mapv object-distance->map))))

(defprotocol IClient
  (close [this])
  (exists [this id])
  (search [this config vector])
  (search-by-id [this config id])
  (stream-search [this f config vectors])
  (stream-search-by-id [this f config ids])
  (insert [this id vector])
  (stream-insert [this f vectors])
  (multi-insert [this vectors])
  (update [this id vector])
  (stream-update [this f vectors])
  (multi-update [this vectors])
  (remove-id [this id])
  (stream-remove [this f ids])
  (multi-remove [this ids])
  (get-object [this id])
  (stream-get-object [this f ids]))

(defrecord Client [type channel stub async-stub]
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
  (search [this config vector]
    (when (false? (.isShutdown channel))
      (let [req (-> (Search$Request/newBuilder)
                    (.addAllVector (seq (map float vector)))
                    (.setConfig (->search-config config))
                    (.build))]
        (-> stub
            (.search req)
            (parse-search-response)))))
  (search-by-id [this config id]
    (when (false? (.isShutdown channel))
      (let [req (-> (Search$IDRequest/newBuilder)
                    (.setId id)
                    (.setConfig (->search-config config))
                    (.build))]
        (-> stub
            (.searchByID req)
            (parse-search-response)))))
  (stream-search [this f config vectors]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            cnt (atom 0)
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
                              (swap! cnt inc)
                              (-> (parse-search-response res)
                                  (f)))
                            (onError [this throwable]
                              (deliver pm {:status :error
                                           :error throwable
                                           :count @cnt}))
                            (onCompleted [this]
                              (deliver pm {:status :done
                                           :count @cnt})))))]
        (->> reqs
             (map #(-> ^StreamObserver observer
                       (.onNext %)))
             (doall))
        (-> ^StreamObserver observer
            (.onCompleted))
        pm)))
  (stream-search-by-id [this f config ids]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            cnt (atom 0)
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
                              (swap! cnt inc)
                              (-> (parse-search-response res)
                                  (f)))
                            (onError [this throwable]
                              (deliver pm {:status :error
                                           :error throwable
                                           :count @cnt}))
                            (onCompleted [this]
                              (deliver pm {:status :done
                                           :count @cnt})))))]
        (->> reqs
             (map #(-> ^StreamObserver observer
                       (.onNext %)))
             (doall))
        (-> ^StreamObserver observer
            (.onCompleted))
        pm)))
  (insert [this id vector]
    (when (false? (.isShutdown channel))
      (let [req (-> (Object$Vector/newBuilder)
                    (.setId id)
                    (.addAllVector (seq (map float vector)))
                    (.build))]
        (-> stub
            (.insert req)))))
  (stream-insert [this f vectors]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            cnt (atom 0)
            reqs (->> vectors
                      (mapv #(-> (Object$Vector/newBuilder)
                                 (.setId (:id %))
                                 (.addAllVector (seq (map float (:vector %))))
                                 (.build))))
            observer (-> async-stub
                         (.streamInsert
                          (reify StreamObserver
                            (onNext [this res]
                              (swap! cnt inc)
                              (f res))
                            (onError [this throwable]
                              (deliver pm {:status :error
                                           :error throwable
                                           :count @cnt}))
                            (onCompleted [this]
                              (deliver pm {:status :done
                                           :count @cnt})))))]
        (->> reqs
             (map #(-> ^StreamObserver observer
                       (.onNext %)))
             (doall))
        (-> ^StreamObserver observer
            (.onCompleted))
        pm)))
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
  (stream-update [this f vectors]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            cnt (atom 0)
            reqs (->> vectors
                      (mapv #(-> (Object$Vector/newBuilder)
                                 (.setId (:id %))
                                 (.addAllVector (seq (map float (:vector %))))
                                 (.build))))
            observer (-> async-stub
                         (.streamUpdate
                          (reify StreamObserver
                            (onNext [this res]
                              (swap! cnt inc)
                              (f res))
                            (onError [this throwable]
                              (deliver pm {:status :error
                                           :error throwable
                                           :count @cnt}))
                            (onCompleted [this]
                              (deliver pm {:status :done
                                           :count @cnt})))))]
        (->> reqs
             (map #(-> ^StreamObserver observer
                       (.onNext %)))
             (doall))
        (-> ^StreamObserver observer
            (.onCompleted))
        pm)))
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
  (stream-remove [this f ids]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            cnt (atom 0)
            reqs (->> ids
                      (mapv #(-> (Object$ID/newBuilder)
                                 (.setId %)
                                 (.build))))
            observer (-> async-stub
                         (.streamRemove
                          (reify StreamObserver
                            (onNext [this res]
                              (swap! cnt inc)
                              (f res))
                            (onError [this throwable]
                              (deliver pm {:status :error
                                           :error throwable
                                           :count @cnt}))
                            (onCompleted [this]
                              (deliver pm {:status :done
                                           :count @cnt})))))]
        (->> reqs
             (map #(-> ^StreamObserver observer
                       (.onNext %)))
             (doall))
        (-> ^StreamObserver observer
            (.onCompleted))
        pm)))
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
                   (.build))
            mapper (case type
                     :vald meta-vector->map
                     :agent object-vector->map)]
        (-> stub
            (.getObject id)
            (mapper)))))
  (stream-get-object [this f ids]
    (when (false? (.isShutdown channel))
      (let [pm (promise)
            cnt (atom 0)
            mapper (case type
                     :vald meta-vector->map
                     :agent object-vector->map)
            reqs (->> ids
                      (mapv #(-> (Object$ID/newBuilder)
                                 (.setId %)
                                 (.build))))
            observer (-> async-stub
                         (.streamGetObject
                          (reify StreamObserver
                            (onNext [this res]
                              (swap! cnt inc)
                              (-> (mapper res)
                                  (f)))
                            (onError [this throwable]
                              (deliver pm {:status :error
                                           :error throwable
                                           :count @cnt}))
                            (onCompleted [this]
                              (deliver pm {:status :done
                                           :count @cnt})))))]
        (->> reqs
             (map #(-> ^StreamObserver observer
                       (.onNext %)))
             (doall))
        (-> ^StreamObserver observer
            (.onCompleted))
        pm))))

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
    (->Client :vald channel stub async-stub)))

(defn agent-client [host port]
  (let [channel (grpc-channel host port)
        stub (agent-blocking-stub channel)
        async-stub (agent-async-stub channel)]
    (->Client :agent channel stub async-stub)))

(comment
  (def client (vald-client "localhost" 8081))
  (def client (agent-client "localhost" 8081))
  (close client)

  (defn rand-vec []
    (take 4096 (repeatedly #(float (rand)))))

  (-> client
      (exists "test"))
  (-> client
      (search {:num 10} (rand-vec)))
  (-> client
      (search-by-id {:num 10} "test"))

  (deref
   (stream-search client println {:num 10} (take 10 (repeatedly #(rand-vec)))))
  (deref
   (stream-search-by-id client println {:num 3} ["test" "abc"]))
  (deref
   (stream-get-object client println ["zz1" "zz3"]))

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
    @(stream-insert client println vectors))

  (-> client
      (get-object "zz3")))
