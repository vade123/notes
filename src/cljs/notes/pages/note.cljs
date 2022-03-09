(ns notes.pages.note
  (:require
   [reagent.core :as reagent :refer [atom]]
   [notes.router :refer [path-for]]
   [notes.rest :as rest]
   [reagent.session :as session]))


(defn textarea [text editing]
  (fn []
    [:div [:textarea {:class (str "input " (if @editing "editing" "default"))
                      :type "text"
                      :value @text
                      :on-change (fn [e] (reset! text (-> e .-target .-value)))}]]))


(defn save-edited [notes note edited-text editing]
  (swap! editing not)
  (rest/put! notes (assoc note :note edited-text)))


(defn note-page [notes]
  (let [editing (atom false)
        notetext (atom "")]
    (fn []
      (let [routing-data (session/get :route)
            id (get-in routing-data [:route-params :id])
            note (get @notes (keyword id))]
        (reset! notetext (:note note))
        [:span.main
         [:div.row-container
          [:a.back {:href (path-for :index)} "<"]
          [:h1 (str id)]
          [:div.spacer]
          (if (= @editing true)
            [:span.row-container
             [:a {:on-click #(save-edited notes note @notetext editing)} "Save"]
             [:a {:on-click #(swap! editing not)} "Cancel"]]
            [:span.row-container
             [:a {:on-click #(swap! editing not)} "Edit"]
             [:a {:href (path-for :index) :on-click #(rest/delete! notes id)} "Delete"]])]
         [textarea notetext editing]]))))