(ns vald-client-clj.e2e-test
  (:require [clojure.test :as t :refer [deftest testing]]
            [vald-client-clj.core :as vald]
            [jsonista.core :as json]
            [camel-snake-kebab.core :as csk]))

(def host "localhost")
(def port 8081)

(def data (atom nil))
(def client (atom nil))

(defn read-data [filename]
  (let [mapper (json/object-mapper
                 {:encode-key-fn name
                  :decode-key-fn csk/->kebab-case-keyword})]
    (-> (slurp filename)
        (json/read-value mapper))))

(defn once-fixture [f]
  (->> (read-data "wordvecs1000.json")
       (reset! data))
  (f))

(t/use-fixtures :once once-fixture)

(defn set-up []
  (->> (vald/vald-client host port)
       (reset! client)))

(defn tear-down []
  (when @client
    (vald/close @client)
    (reset! client nil)))

(deftest e2e
  (testing :insert
    (testing "Test for Insert operation"
      (set-up)
      (let [cfg {:skip-strict-exist-check true}
            datum (get @data 0)
            id (get datum :id)
            v (get datum :vector)
            result (vald/insert @client cfg id v)]
        (t/is (= (count (:ips result)) 1)))
      (tear-down))
    (testing "Test for MultiInsert operation"
      (set-up)
      (let [cfg {:skip-strict-exist-check true}
            vectors (drop 1 (take 11 @data))
            results (vald/multi-insert @client cfg vectors)]
        (t/is (= (count results) 10))
        (->> results
             (map #(t/is (= (count (:ips %)) 1)))))
      (tear-down))
    (testing "Test for StreamInsert operation"
      (set-up)
      (let [cfg {:skip-strict-exist-check true}
            vectors (drop 11 (take 100 @data))
            validate (fn [result]
                       (t/is (= (count (:ips result)) 1)))
            pm (vald/stream-insert @client validate cfg vectors)]
        (t/is (nil? (:error @pm)))
        (t/is (= (:count @pm) 89)))
      (tear-down)))
  (testing :agent
    (testing "Test for CreateIndex operation"
      (set-up)
      (let [result (vald/create-index @client 10000)]
        (t/is (= {} result)))
      (tear-down))
    (testing "Test for SaveIndex operation"
      (set-up)
      (let [result (vald/save-index @client)]
        (t/is (= {} result)))
      (tear-down))
    (testing "Test for IndexInfo operation"
      (set-up)
      (let [result (vald/index-info @client)]
        (t/is (= (:stored result) 100))
        (t/is (= (:uncommitted result) 0)))
      (tear-down)))
  (testing :exists
    (testing "Test for Exists operation"
      (set-up)
      (let [datum (get @data 0)
            result (vald/exists @client (:id datum))]
        (t/is (some? (:id result))))
      (tear-down)))
  (testing :get-object
    (testing "Test for GetObject operation"
      (set-up)
      (let [datum (get @data 0)
            result (vald/get-object @client (:id datum))]
        (t/is (= (:id datum) (:id result))))
      (tear-down))
    (testing "Test for StreamGetObject operation"
      (set-up)
      (let [data (take 10 @data)
            ids (->> data
                     (mapv #(get % :id)))
            data' (into {} (map (juxt :id :vector) data))
            validate (fn [result]
                       (let [id (:id result)
                             expected (get data' id)]
                         (t/is (some? expected))))
            pm (vald/stream-get-object @client validate ids)]
        (t/is (nil? (:error @pm))))
      (tear-down)))
  (testing :search
    (testing "Test for Search operation"
      (set-up)
      (let [cfg {:num 3
                 :radius -1.0
                 :epsilon 0.1
                 :timoeut 3000000000}
            v (:vector (get @data 0))
            result (vald/search @client cfg v)]
        (t/is (= (count result) 3)))
      (tear-down))
    (testing "Test for MultiSearch operation"
      (set-up)
      (let [cfg {:num 3
                 :radius -1.0
                 :epsilon 0.1
                 :timeout 3000000000}
            vectors (->> (drop 1 (take 11 @data))
                         (map (fn [v]
                                (:vector v)))
                         (filterv some?))
            results (vald/multi-search @client cfg vectors)]
        (t/is (= (count results) 10))
        (->> results
             (map #(t/is (= (count %) 3)))))
      (tear-down))
    (testing "Test for StreamSearch operation"
      (set-up)
      (let [cfg {:num 3
                 :radius -1.0
                 :epsilon 0.1
                 :timeout 3000000000}
            vectors (->> (drop 11 (take 21 @data))
                         (map (fn [v]
                                (:vector v)))
                         (filterv some?))
            validate (fn [result]
                       (t/is (= (count result) 3)))
            pm (vald/stream-search @client validate cfg vectors)]
        (t/is (nil? (:error @pm)))
        (t/is (= (:count @pm) 10)))
      (tear-down)))
  (testing :search-by-id
    (testing "Test for SearchByID operation"
      (set-up)
      (let [cfg {:num 3
                 :radius -1.0
                 :epsilon 0.1
                 :timoeut 3000000000}
            id (:id (get @data 0))
            result (vald/search-by-id @client cfg id)]
        (t/is (= (count result) 3)))
      (tear-down))
    (testing "Test for MultiSearchByID operation"
      (set-up)
      (let [cfg {:num 3
                 :radius -1.0
                 :epsilon 0.1
                 :timeout 3000000000}
            ids (->> (drop 1 (take 11 @data))
                     (map (fn [v]
                            (:id v)))
                     (filterv some?))
            results (vald/multi-search-by-id @client cfg ids)]
        (t/is (= (count results) 10))
        (->> results
             (map #(t/is (= (count %) 3)))))
      (tear-down))
    (testing "Test for StreamSearchByID operation"
      (set-up)
      (let [cfg {:num 3
                 :radius -1.0
                 :epsilon 0.1
                 :timeout 3000000000}
            ids (->> (drop 11 (take 21 @data))
                         (map (fn [v]
                                (:id v)))
                         (filterv some?))
            validate (fn [result]
                       (t/is (= (count result) 3)))
            pm (vald/stream-search-by-id @client validate cfg ids)]
        (t/is (nil? (:error @pm)))
        (t/is (= (:count @pm) 10)))
      (tear-down)))
  (testing :update
    (testing "Test for Update operation"
      (set-up)
      (let [cfg {:skip-strict-exist-check true}
            id (:id (get @data 0))
            v (:vector (get @data 1))
            result (vald/update @client cfg id v)]
        (t/is (= (count (:ips result)) 1)))
      (tear-down))
    (testing "Test for MultiUpdate operation"
      (set-up)
      (let [cfg {:skip-strict-exist-check true}
            vectors (map (fn [i v]
                           {:id (:id i)
                            :vector (:vector v)})
                         (drop 1 (take 11 @data))
                         (drop 2 (take 12 @data)))
            results (vald/multi-update @client cfg vectors)]
        (t/is (= (count results) 10))
        (->> results
             (map #(t/is (= (count (:ips %)) 1)))))
      (tear-down))
    (testing "Test for StreamUpdate operation"
      (set-up)
      (let [cfg {:skip-strict-exist-check true}
            vectors (map (fn [i v]
                           {:id (:id i)
                            :vector (:vector v)})
                         (drop 11 (take 21 @data))
                         (drop 12 (take 22 @data)))
            validate (fn [result]
                       (t/is (= (count (:ips result)) 1)))
            pm (vald/stream-update @client validate cfg vectors)]
        (t/is (nil? (:error @pm)))
        (t/is (= (:count @pm) 10)))
      (tear-down)))
  (testing :upsert
    (testing "Test for Upsert operation"
      (set-up)
      (let [cfg {:skip-strict-exist-check true}
            datum (get @data 0)
            id (get datum :id)
            v (get datum :vector)
            result (vald/upsert @client cfg id v)]
        (t/is (= (count (:ips result)) 1)))
      (tear-down))
    (testing "Test for MultiUpsert operation"
      (set-up)
      (let [cfg {:skip-strict-exist-check true}
            vectors (drop 1 (take 11 @data))
            results (vald/multi-upsert @client cfg vectors)]
        (t/is (= (count results) 10))
        (->> results
             (map #(t/is (= (count (:ips %)) 1)))))
      (tear-down))
    (testing "Test for StreamUpsert operation"
      (set-up)
      (let [cfg {:skip-strict-exist-check true}
            vectors (drop 11 (take 21 @data))
            validate (fn [result]
                       (t/is (= (count (:ips result)) 1)))
            pm (vald/stream-upsert @client validate cfg vectors)]
        (t/is (nil? (:error @pm)))
        (t/is (= (:count @pm) 10)))
      (tear-down)))
  (testing :remove
    (testing "Test for Remove operation"
      (set-up)
      (let [cfg {:skip-strict-exist-check true}
            datum (get @data 0)
            id (get datum :id)
            result (vald/remove-id @client cfg id)]
        (t/is (= (count (:ips result)) 1)))
      (tear-down))
    (testing "Test for MultiRemove operation"
      (set-up)
      (let [cfg {:skip-strict-exist-check true}
            ids (->> (drop 1 (take 11 @data))
                     (mapv #(get % :id)))
            results (vald/multi-remove @client cfg ids)]
        (t/is (= (count results) 10))
        (->> results
             (map #(t/is (= (count (:ips %)) 1)))))
      (tear-down))
    (testing "Test for StreamRemove operation"
      (set-up)
      (let [cfg {:skip-strict-exist-check true}
            ids (->> (drop 11 (take 21 @data))
                     (mapv #(get % :id)))
            validate (fn [result]
                       (t/is (= (count (:ips result)) 1)))
            pm (vald/stream-remove @client validate cfg ids)]
        (t/is (nil? (:error @pm)))
        (t/is (= (:count @pm) 10)))
      (tear-down))))

(comment
  (first (read-data "wordvecs1000.json"))
  (reset! data [{:id "a" :vector [0.1 0.2 0.3]}
                {:id "b" :vector [0.2 0.3 0.4]}])
  (reset! data
          (vec
            (take 100 (repeat
                        {:id "a" :vector [0.1 0.2 0.3]}))))
  )
