(ns reframe-test.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:require [re-frame.core :as re-frame]
            [secretary.core :as secretary]
            [reframe-test.config :as config]
            [goog.events :as events])
  (:import [goog.history Html5History EventType]))


;; goog.history setup taken from http://www.lispcast.com/mastering-client-side-routing-with-secretary-and-goog-history
(defn- make-history []
  (doto (Html5History.)
    (.setPathPrefix (str js/window.location.protocol "//" js/window.location.host))
    (.setUseFragment true)))

(defn- handle-url-change [e]
  (when config/debug?
    (js/console.log e))
  (let [token (.-token e)
        is-nav (.-isNavigation e)]
    (secretary/dispatch! token)))

(defn- hook-browser-navigation! []
  (doto (make-history)
    (events/listen
      EventType.NAVIGATE
      (fn [event]
        (handle-url-change event)))
    (.setEnabled true)))


(defn- init-app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/groups" []
    (re-frame/dispatch [:navigate-to :groups]))

  (defroute group-path "/groups/:id" [id]
    (re-frame/dispatch [:navigate-to :group id]))

  (defroute create-user "/users/create" []
    (re-frame/dispatch [:navigate-to :createuser]))

  (hook-browser-navigation!))
