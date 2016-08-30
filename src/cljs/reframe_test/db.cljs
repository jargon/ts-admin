(ns reframe-test.db
  (:require
    [schema.core :as sc :include-macros true]))

(def schema
  {:initialized sc/Bool
   (sc/optional-key :current-group-id) sc/Str
   (sc/optional-key :users) [{:name sc/Str :phone sc/Num sc/Keyword sc/Any}]
   (sc/optional-key :groups) {sc/Str {:id sc/Str :name sc/Str :subgroups sc/Num :parent-id sc/Str sc/Keyword sc/Any}}
   sc/Keyword sc/Any})

(defn valid-schema?
  [db]
  (let [res (sc/check schema db)]
    (if (some? res)
      (.error js/console (str "schema problem: " res)))))

(def default-db
  {:name "re-frame", :initialized false, :groups-initialized false})

(def dummy-users
  [{:name "Søren Boisen", :id "76fd0625-f84d-4d14-9045-386098f04645", :phone 20780172},
   {:name "Bo Visfeldt",  :id "fa586cca-e0ef-480f-99de-c248792662ea", :phone 27282970}])

(def dummy-groups
  [{:name "Root",            :id "80fd0625-f84d-4d14-9045-386098f04645", :subgroups 3, :parent-id ""}
   {:name "HivePeople",      :id "81fd0625-f84d-4d14-9045-386098f04645", :subgroups 0, :parent-id "80fd0625-f84d-4d14-9045-386098f04645"}
   {:name "Herlev Hospital", :id "82fd0625-f84d-4d14-9045-386098f04645", :subgroups 1, :parent-id "80fd0625-f84d-4d14-9045-386098f04645"}
   {:name "Akutmodtagelsen", :id "83fd0625-f84d-4d14-9045-386098f04645", :subgroups 2, :parent-id "82fd0625-f84d-4d14-9045-386098f04645"}
   {:name "Sekretariatet",   :id "84fd0625-f84d-4d14-9045-386098f04645", :subgroups 0, :parent-id "83fd0625-f84d-4d14-9045-386098f04645"}
   {:name "Sandkassen",      :id "85fd0625-f84d-4d14-9045-386098f04645", :subgroups 0, :parent-id "83fd0625-f84d-4d14-9045-386098f04645"}
   {:name "KK Vikarkorps",   :id "86fd0625-f84d-4d14-9045-386098f04645", :subgroups 10, :parent-id "80fd0625-f84d-4d14-9045-386098f04645"}])

(defn build-subgroups [parent groups]
  (let [child? (fn [group] (= (:parent-id group) (:id parent)))
        subgroups (filterv child? groups)]
    (mapv :id subgroups)))

(defn build-group-hierarchy [root level expanded groups]
  (let [root-id (:id root)
        child? (fn [group] (= (:parent-id group) root-id))
        children (filterv child? groups)
        loaded (or (= (:subgroups root) 0) (not (empty? children)))
        node {:id root-id :level level :loaded loaded :expanded expanded}]
    (if (seq children)
      (into [node] (mapv #(build-group-hierarchy %1 (+ level 1) false groups) children))
      node)))

(build-group-hierarchy (first dummy-groups) 0 true dummy-groups)
(seq (filterv integer? [1 2]))

(defn build-group-map [groups]
  (let [kvps (mapv #(vector (:id %1) %1) groups)]
    (into (hash-map) kvps)))

(build-subgroups (first dummy-groups) dummy-groups)
(build-group-map dummy-groups)

(def tree ["Root" "HivePeople" ["Herlev Hospital" ["Akutmodtagelsen" "Sekretariatet" "Sandkassen"]] ["KK Vikarkorps" "Jepdepdoo"] ["Arkham" ["Batcave" "Batmobile"] "Asylum"]])

