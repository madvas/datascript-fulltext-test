(ns datascript-fulltext.server
  (:require
    [cljs.nodejs :as nodejs]
    [datascript.core :as d]
    [print.foo :include-macros true]))

(def lunr (nodejs/require "lunr"))

(nodejs/enable-util-print!)

(def documents (clj->js [{:id 1
                          :text "A JavaScript library for building user interfaces bright."}
                         {:id 2
                          :text "A modern JavaScript utility library delivering modularity, performance & extras bright."}
                         {:id 3
                          :text "Like Solr, but much smaller, and not as bright."}]))

(def db-data [{:project/name "React"
               :project/description-id 1}
              {:project/name "Lodash"
               :project/description-id 2}
              {:project/name "Lunr"
               :project/description-id 3}])

(defn add-documents! []
  (this-as this
    (.field this "text")

    (doseq [doc documents]
      (.add this doc))))

(def schema {:project/name {:db/cardinality :db.cardinality/one}
             :project/description-id {:db/cardinality :db.cardinality/one}})

(defn calculate-scores [idx keyword]
  (->> (js->clj (.search idx keyword) :keywordize-keys true)
    (reduce #(conj %1 [(js/parseInt (:ref %2)) (:score %2)]) [])))

(defn create-db! []
  (let [conn (d/create-conn schema)]
    (d/transact! conn (map #(assoc % :db/id (:project/description-id %)) db-data))
    conn))

(defn sort-by-desc [key-fn coll]
  (sort-by key-fn #(compare %2 %1) coll))

(defn on-jsload []
  (let [keyword "bright"
        idx (lunr add-documents!)
        scores (calculate-scores idx keyword)
        conn (create-db!)
        results (->> (d/q '[:find ?n ?score
                            :in $ [[?d ?score]]
                            :where
                            [?e :project/name ?n]
                            [?e :project/description-id ?d]]
                          @conn scores)
                  (sort-by-desc second))]
    (print.foo/look results)))

(defn -main [& _]
  (on-jsload))

(set! *main-cli-fn* -main)