(ns notes.rest
  (:require [ajax.core :refer [GET POST PUT DELETE]]))

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

(defn post! [notes note]
  (POST (str "/api/notes")
    {:response-format :json
     :keywords? true
     :format :json
     :params note
     :handler #(reset! notes %)
     :error-handler (fn [{:keys [status status-text]}]
                      (js/console.log status status-text))}))

(defn delete! [notes id]
  (DELETE (str "/api/notes/" id)
    {:response-format :json
     :keywords? true
     :handler #(reset! notes %)
     :error-handler (fn [{:keys [status status-text]}]
                      (js/console.log status status-text))}))

(defn delete-multiple! [notes ids]
  (POST (str "/api/purge")
    {:response-format :json
     :keywords? true
     :format :json
     :params {:ids ids}
     :handler #(reset! notes %)
     :error-handler (fn [{:keys [status status-text]}]
                      (js/console.log status status-text))}))

(defn put! [notes note]
  (PUT (str "/api/notes/" (:id note))
    {:response-format :json
     :keywords? true
     :format :json
     :params note
     :handler #(reset! notes %)
     :error-handler (fn [{:keys [status status-text]}]
                      (js/console.log status status-text))}))