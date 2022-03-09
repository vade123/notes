(ns notes.logic
  (:require
   [clojure.data.json :as json]))

(def notes-collection (atom {}))

(defn get-all []
  (str (json/write-str @notes-collection)));; as array: (map #(val %) @notes-collection))))

(defn get-id []
  (if (= (count @notes-collection) 0)
    1
    (inc (:id (val (apply max-key #(:id (val %)) @notes-collection))))))

(defn get-by-id [id]
  (str (json/write-str (get @notes-collection id))))

(defn add-note [note]
  (str (json/write-str (swap! notes-collection conj {(get-id) (assoc note :id (get-id))}))))

(defn update-note [note id]
  (str (json/write-str (swap! notes-collection update-in [id] merge note))))

(defn delete-note [id]
  (str (json/write-str (swap! notes-collection dissoc id))))

(defn delete-multiple [ids]
  (str (json/write-str (reset! notes-collection (apply dissoc @notes-collection ids)))))

;; ---- init some notes------
(add-note {:note "next level liiba laaaba" :done false})
(add-note {:note "next level liiba laaaba v 2" :done false})
;; --------------------------