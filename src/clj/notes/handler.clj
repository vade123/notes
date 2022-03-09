(ns notes.handler
  (:require
   [reitit.ring :as reitit-ring]
   [notes.middleware :refer [middleware]]
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]
   [notes.logic :as n]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [muuntaja.core :as m]
   [reitit.ring.coercion :as rrc]
   [reitit.coercion.spec :as rcs]))

(def mount-target
  [:div#app
   [:h2 "Welcome to notes"]
   [:p "please wait while Figwheel/shadow-cljs is waking up ..."]
   [:p "(Check the js console for hints if nothing exciting happens.)"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    (include-js "/js/app.js")
    [:script "notes.core.init_BANG_()"]]))

(defn index-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

(defn notes-get-handler
  [_]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (n/get-all)})

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/" {:get {:handler index-handler}}]
     ["/notes"
      ["" {:get {:handler index-handler}}]
      ["/:id" {:get {:handler index-handler
                     :parameters {:path {:id int?}}}}]]
     ["/about" {:get {:handler index-handler}}]
     ["/new" {:get {:handler index-handler}}]
     ["/api"

      ["/notes" {:get {:handler notes-get-handler}
                 :post {:parameters {:body {:note string? :done boolean?}}
                        :handler (fn [{{body :body} :parameters}]
                                   {:status 201
                                    :headers {"Content-Type" "application/json"}
                                    :body (n/add-note body)})}}]

      ["/purge" {:post {:parameters {:body {:ids [int?]}}
                        :handler (fn [{{{ids :ids} :body} :parameters}]
                                   {:status 200
                                    :headers {"Content-Type" "application/json"}
                                    :body (n/delete-multiple ids)})}}]

      ["/notes/:id" {:get {:parameters {:path {:id int?}}
                           :handler (fn [{{{id :id} :path} :parameters}]
                                      {:status 200
                                       :headers {"Content-Type" "application/json"}
                                       :body (n/get-by-id id)})}

                     :put {:parameters {:body {:id int? :note string? :done boolean?} :path {:id int?}}
                           :handler (fn [{{body :body {id :id} :path} :parameters}]
                                      {:status 200
                                       :headers {"Content-Type" "application/json"}
                                       :body (n/update-note body id)})}

                     :delete {:parameters {:path {:id int?}}
                              :handler (fn [{{{id :id} :path} :parameters}]
                                         {:status 200
                                          :headers {"Content-Type" "application/json"}
                                          :body (n/delete-note id)})}}]]]
    {:data {:muuntaja m/instance
            :coercion rcs/coercion
            :middleware [muuntaja/format-middleware
                         rrc/coerce-exceptions-middleware
                         rrc/coerce-request-middleware
                         rrc/coerce-response-middleware]}})
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler)) {:middleware middleware}))
