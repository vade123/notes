(ns notes.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]
   [ajax.core :refer [GET POST PUT DELETE]]))


(defonce notes (atom {}))

(defn map-as-list [m]
  (map #(val %) m))

;; -------------------------
;; Requests

(defn fetch-all! [notes]
  (GET "/api/notes"
    {:response-format :json
     :keywords? true
     :handler #(reset! notes %)
     :error-handler (fn [{:keys [status status-text]}]
                      (js/console.log status status-text))}))

(defn fetch-one! [id note]
  (GET (str "/api/notes/" id)
    {:response-format :json
     :keywords? true
     :handler #(reset! note %)
     :error-handler (fn [{:keys [status status-text]}]
                      (js/console.log status status-text))}))

(defn post! [note]
  (POST (str "/api/notes")
    {:response-format :json
     :keywords? true
     :format :json
     :params note
     :handler #(reset! notes %)
     :error-handler (fn [{:keys [status status-text]}]
                      (js/console.log status status-text))}))

(defn delete! [id]
  (DELETE (str "/api/notes/" id)
    {:response-format :json
     :keywords? true
     :handler #(reset! notes %)
     :error-handler (fn [{:keys [status status-text]}]
                      (js/console.log status status-text))}))

(defn put! [note]
  (PUT (str "/api/notes/" (:id note))
    {:response-format :json
     :keywords? true
     :format :json
     :params note
     :handler #(reset! notes %)
     :error-handler (fn [{:keys [status status-text]}]
                      (js/console.log status status-text))}))

;; -------------------------
;; Routes

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

;; -------------------------
;; Page components

(defn home-page []
  (fetch-all! notes)
  (fn []
    [:span.main
     [:h1 "notes"]
     [:ul (map (fn [note]
                 [:li {:key (str (:id note))}
                  [:a {:href (path-for :note {:id (:id note)})} [:b (str (:id note) ": ")] (:note note)]]) (map-as-list @notes))]]))

(defn save-edited [note edited-text editing]
  (swap! editing not)
  (put! (assoc note :note edited-text)))

(defn textarea [text editing]
  (fn []
    [:div [:textarea {:class (str "input " (if @editing "editing" "default"))
                      :type "text"
                      :value @text
                      :on-change (fn [e] (reset! text (-> e .-target .-value)))}]]))

(defn note-page []
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
             [:a {:on-click #(save-edited note @notetext editing)} "Save"]
             [:a {:on-click #(swap! editing not)} "Cancel"]]
            [:span.row-container
             [:a {:on-click #(swap! editing not)} "Edit"]
             [:a {:href (path-for :index) :on-click #(delete! id)} "Delete"]])]
         [textarea notetext editing]]))))

(defn about-page []
  (fn [] [:span.main
          [:h1 "About notes"]
          [:p "Crudi kikkare, clojure/script prac"]]))

(defn new-page []
  (let [val (atom "")]
    (fn []
      [:span.main
       [:h1 "Add new"]
       [:textarea.input {:type "text" :value @val :on-change #(reset! val (-> % .-target .-value))}]
       [:div.save-cont
        (if (not= @val "")
          [:a.save {:href (path-for :index) :on-click #(post! {:note @val :done false})} "Save"]
          [:a.save-disabled "Save"])]])))

;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index #'home-page
    :about #'about-page
    :note #'note-page
    :new #'new-page))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [:header
        [:p [:a {:href (path-for :index)} "Home"] " | "
         [:a {:href (path-for :about)} "About notes"] " | "
         [:a {:href (path-for :new)} "Add new"]]]
       [page]
       [:footer
        [:hr.divider]
        [:p "notes was generated by the "
         [:a {:href "https://github.com/reagent-project/reagent-template"} "Reagent Template"] "."]]])))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rdom/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (reagent/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))