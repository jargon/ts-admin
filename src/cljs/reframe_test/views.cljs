(ns reframe-test.views
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [cljsjs.react-bootstrap]
              [reframe-test.routes :as routes]))

(defn- componentize [c]
  (map #(-> [c %1])))

(def Glyphicon (reagent/adapt-react-class (aget js/ReactBootstrap "Glyphicon")))
(def Button (reagent/adapt-react-class (aget js/ReactBootstrap "Button")))
(def Table (reagent/adapt-react-class (aget js/ReactBootstrap "Table")))

;; Forms
(def FormGroup (reagent/adapt-react-class (aget js/ReactBootstrap "FormGroup")))
(def FormControl (reagent/adapt-react-class (aget js/ReactBootstrap "FormControl")))
(def ControlLabel (reagent/adapt-react-class (aget js/ReactBootstrap "ControlLabel")))
(def Checkbox (reagent/adapt-react-class (aget js/ReactBootstrap "Checkbox")))
(def Radio (reagent/adapt-react-class (aget js/ReactBootstrap "Radio")))


(defn create-user-panel []
  (fn []
    [:form
     [FormGroup {:controlId "Name"}
      [ControlLabel "Name"]
      [FormControl {:type "text" :placeholder "Enter name of user here"}]]]))

(defn user-item [{:keys [name phone]}]
  [:tr [:td name] [:td phone]])

(defn users-panel []
  (let [users (re-frame/subscribe [:users])]
    (fn []
      [Table {:bordered true :striped true}
       (into [:tbody] (componentize user-item) @users)])))


(defn group-panel []
  (let [group (re-frame/subscribe [:current-group])]
    (fn []
      [:div])))

(defn group-link [{:keys [id name]}]
  [:a {:href (routes/group-path {:id id})} name])

(defn group-row [{:keys [id name level expanded subgroups] :as group}]
  (let [td-style {:padding-left (str (+ (* level 15) 10) "px")}
        prefix (when (> level 0) "└ ")
        toggle-icon (if expanded "minus" "plus")
        on-click #(re-frame/dispatch [:toggle-group-row id (not expanded)])]
    [:tr
     [:td {:style td-style :on-click on-click} prefix [group-link group]]
     [:td {:style {:width "20px" :color "#0000EE"}} (when (> subgroups 0) [Glyphicon {:glyph toggle-icon}])]]))

(defn group-list-panel []
  (let [groups (re-frame/subscribe [:groups])]
    (fn []
      [Table {:bordered true :striped true :hover true}
       (into [:tbody] (componentize group-row) @groups)])))

(defn main-panel []
  (let [initialized (re-frame/subscribe [:initialized])]
    (fn []
      [:div.container-fluid
        [:div.row
          [:div.col-md-2]
          [:div.col-md-8
            (if-not @initialized
              [:div "Loading..."]
              [group-list-panel])]
          [:div.col-md-2]]])))

(user-item {:name "hans" :phone 1234})
(into [:ul] (componentize :user-item) '({:name "Hansi" :phone 1} {:name "Gunther" :phone 2}))

