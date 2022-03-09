(ns notes.pages.home
  (:require
   [reagent.core :as reagent :refer [atom]]
   [notes.rest :as rest]
   [notes.router :refer [path-for]]))

(defn map-as-sorted-list [m]
  (sort-by :done (map #(val %) m)))

(defn get-done-ids [notes]
  (map (fn [n] (:id n)) (filter (fn [n] (= (:done n) true)) (map-as-sorted-list @notes))))

(defn checkbox-on-change [e notes note done-ids]
  (let [is-checked (.. e -target -checked)
        id (:id note)]
    (rest/put! notes (assoc note :done is-checked))
    (if (= is-checked true)
      (swap! done-ids conj id)
      (swap! done-ids #(remove #{id} %)))))

(defn delete-done-on-click [notes done-ids]
  (let [ids (get-done-ids notes)]
    (rest/delete-multiple! notes ids)
    (reset! done-ids [])))

(defn home-page [notes]
  (let [done-ids (atom [])]
    (rest/fetch-all! notes)
    (reset! done-ids (get-done-ids notes))
    (fn []
      [:span.main
       [:div.row-container
        [:h1 "notes"]
        [:div.spacer]
        [:a {:class (if (= (count @done-ids) 0) "hidden" "")
             :on-click #(delete-done-on-click notes done-ids)} "Delete done"]]
       [:ul (map (fn [note]
                   [:li {:key (str (:id note))}
                    [:input {:type "checkbox"
                             :checked (:done note)
                             :on-change #(checkbox-on-change % notes note done-ids)}]
                    [:a {:class (if (:done note) "done" "")
                         :href (path-for :note {:id (:id note)})}
                     [:b (str (:id note) ": ")] (:note note)]])
                 (map-as-sorted-list @notes))]])))