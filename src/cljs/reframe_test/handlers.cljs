(ns reframe-test.handlers
    (:require [re-frame.core :as re-frame]
              [reframe-test.db :as db]
              [clojure.zip :as zip]))

(defn- edit-group-node [zipper id edit-fn & args]
  (let [match? (fn [loc] (and (not (zip/branch? loc))
                              (= (:id (zip/node loc)) id)))]
    (loop [loc zipper]
      (when (not (zip/end? loc))
        (if (match? loc)
          (zip/root (apply zip/edit loc edit-fn args))
          (recur (zip/next loc)))))))



(def common-middlewares (if ^boolean goog.DEBUG (comp re-frame/debug (re-frame/after db/valid-schema?)) []))

(re-frame/register-handler
  :initialize-db
  common-middlewares
  (fn  [_ _]
    db/default-db))

(re-frame/register-handler
  :load-users
  common-middlewares
  (fn [appdb _]
    (merge appdb {:initialized true, :users db/dummy-users})))

(re-frame/register-handler
  :load-groups
  common-middlewares
  (fn [appdb _]
    (let [root (first db/dummy-groups)
          groups (db/build-group-map db/dummy-groups)
          hierarchy (db/build-group-hierarchy root 0 true db/dummy-groups)]
      (merge appdb {:groups-initialized true
                    :groups groups
                    :group-hierarchy hierarchy}))))

(re-frame/register-handler
  :toggle-group-row
  common-middlewares
  (fn [appdb [_ group-id expand]]
    (let [groups (zip/vector-zip (:group-hierarchy appdb))]
      (if-let [new-groups (edit-group-node groups group-id assoc :expanded expand)]
        (assoc appdb :group-hierarchy new-groups)
        appdb))))



(def zipper1 (zip/vector-zip [{:id 1 :name "root" :expanded true} [{:id 2 :name "Herlev" :expanded true} [{:id 3 :name "Akut" :expanded false} {:id 4 :name "Sandkas" :expanded false}]] {:id 5 :name "hive" :expanded false}]))
