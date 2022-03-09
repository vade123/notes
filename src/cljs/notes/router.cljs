(ns notes.router
  (:require [reitit.frontend :as reitit]))

(def router
  (reitit/router
   [["/" :index]
    ["/notes"
     ["" :items]
     ["/:id" :note]]
    ["/about" :about]
    ["/new" :new]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

