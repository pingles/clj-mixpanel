(ns clj-mixpanel.data
  (:import [org.apache.commons.codec.digest DigestUtils]
           [java.util Date UUID])
  (:require [clj-http.client :as http]
            [clojure.string :as s]
            [clojure.data.json :refer [json-str read-json]])
  (:refer-clojure :exclude [get]))

(def url "http://mixpanel.com/api/2.0/")
(def export-url "http://data.mixpanel.com/api/2.0/export")

(defn- now
  []
  (/ (.getTime (Date. )) 1000))

;; request signing

(defn- sort-args [args]
  (sort-by #(name (first %)) args))

(defn- concat-args [args]
  (s/join (map (fn [[k v]] (str (name k) "=" v)) args)))

(defn- signature-from-args-and-secret [args secret]
  (DigestUtils/md5Hex (str (-> args sort-args concat-args) secret)))

(defn sign [params secret]
  (let [{:keys [api_key expire]} params
        signature (signature-from-args-and-secret params secret)]
    (merge params {:sig signature})))

(defn sign-params [params key secret duration]
  (-> params
      (merge {:api_key key
              :expire (str (int (+ duration (now))))})
      (sign secret)))

(defn new-client [api-key api-secret options]
  (merge options {:api-key api-key :api-secret api-secret}))

(defn get [client path params]
  (http/get (str url path) {:query-params (sign-params params (:api-key client) (:api-secret client) (or (:duration client) 60))}))

;;; specific endpoints

(defn export [client params]
  (http/get export-url {:query-params (sign-params params (:api-key client) (:api-secret client) (or (:duration client) 3600))}))
