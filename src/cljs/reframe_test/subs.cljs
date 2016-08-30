(ns reframe-test.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]
              [clojure.zip :as zip]))


(defn- end [zipper]
  (loop [loc zipper]
    (if (zip/end? loc)
      loc
      (recur (zip/next loc)))))

(defn- skip-subtree [zipper]
  (or (zip/right zipper)
      (loop [loc zipper]
        (if (zip/up loc)
          (or (zip/right (zip/up loc))
              (recur (zip/up loc)))
          (end loc)))))


(re-frame/register-sub
 :name
 (fn [db]
   (reaction (:name @db))))

(re-frame/register-sub
  :initialized
  (fn [db]
    (let [users-init (reaction (:initialized @db))
          groups-init (reaction (:groups-initialized @db))]
      (reaction (and @users-init @groups-init)))))

(re-frame/register-sub
  :users
  (fn [db]
    (reaction (:users @db))))



(defn- zip-walk [zipper]
  (loop [loc zipper
         leafs []]
    (if (zip/end? loc)
      leafs
      (if (zip/branch? loc)
        (let [leaf (zip/node (zip/down loc))]
          (if (:expanded leaf)
            (recur (zip/next loc) leafs)
            (recur (skip-subtree loc) (conj leafs leaf))))
        (recur (zip/next loc) (conj leafs (zip/node loc)))))))

(def zipper (zip/vector-zip [{:name "root" :expanded true} [{:name "Herlev" :expanded true} [{:name "Akut" :expanded false} {:name "Sandkas" :expanded false}]] {:name "hive" :expanded false}]))
(zip-walk zipper)


(defn- group-handler [groups hierarchy]
  (let [enrich (fn [node] (merge node (groups (:id node))))]
    (map enrich (zip-walk (zip/vector-zip hierarchy)))))

(let [groups {"grp1" {:id "grp1" :name "fluff" :subgroups 1 :parent-id ""}}
      hierarchy [{:id "grp1" :level 0}]]
  (group-handler groups hierarchy))

(re-frame/register-sub
  :groups
  (fn [db]
    (let [groups (reaction (:groups @db))
          hierarchy (reaction (:group-hierarchy @db))]
      (reaction (group-handler @groups @hierarchy)))))

(re-frame/register-sub
  :current-group
  (fn [db]
    (let [id (reaction (:current-group-id @db))
          groups (reaction (:groups @db))]
      (reaction (@id @groups)))))
