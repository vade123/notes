(ns notes.pages.new
  (:require
   [reagent.core :as reagent :refer [atom]]
   [notes.rest :as rest]
   [accountant.core :as accountant]
   [notes.router :refer [path-for]]))

(defn new-page [notes]
  (let [val (atom "")]
    (fn []
      [:span.main
       [:h1 "Add new"]
       [:textarea.input {:type "text"
                         :value @val
                         :on-change #(reset! val (-> % .-target .-value))
                         :on-key-press #(when (= 13 (.-charCode %))
                                          (rest/post! notes {:note @val :done false})
                                          (accountant/navigate! (path-for :index)))}]
       [:div.save-cont
        (if (not= @val "")
          [:a.save {:href (path-for :index) :on-click #(rest/post! notes {:note @val :done false})} "Save"]
          [:a.save-disabled "Save"])]])))