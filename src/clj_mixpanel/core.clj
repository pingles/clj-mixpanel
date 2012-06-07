(ns clj-mixpanel.core
  (:import [org.apache.commons.codec.binary Base64]
           [java.util Date UUID])
  (:require [clj-http.client :as http]
            [clojure.string :as s])
  (:use [clojure.data.json :only (json-str)]
        [clojure.tools.logging :only (info)]))

(def api-url "http://api.mixpanel.com/track/")

(defn generate-uuid
  "Can be used to generate a unique ID suitable for identifying users."
  []
  (s/replace (str (UUID/randomUUID)) "-" ""))

(defn base64-encode
  "Base64 encode suitable for passing data to the Mixpanel API."
  ([^String s]
     (base64-encode s false false))
  ([^String s chunked url-safe]
     (String. (Base64/encodeBase64 (.getBytes s) chunked url-safe))))

(defn base64-decode
  [^String s]
  (String. (Base64/decodeBase64 (.getBytes s))))

(defn mk-request
  "event is a string representing the type of event. props should
   contain the token and distinct_id. m can contain any key values
   that represent the event's data."
  [{:keys [distinct-id token] :as props} event m]
  {:pre [(contains? props :distinct-id)
         (contains? props :token)]}
  {:event event
   :properties (assoc m :distinct_id distinct-id :token token)})

(defn- coerce-bool
  [b]
  (if (true? b)
    "1"
    "0"))

(defn now
  []
  (apply str (drop-last 3 (str (.getTime (Date. ))))))

(defn notify
  "Sends a notification to the Mixpanel API. Returns a future representing
   the notification.

   token: available in the dashboard
   event: string representing the type of event
   m    : a map containing payload information. must contain
          a distinct-id that identifies the user.

   Example:
   (let [user-id (generate-uuid)]
     (notify token \"My Event\" {:distinct-id user-id :key \"value})"
  [token event {:keys [distinct-id ip test timestamp] :as payload}]
  {:pre [(contains? payload :distinct-id)]}
  (let [props {:token token
               :distinct-id distinct-id}
        event-data (assoc (dissoc payload :timestamp :distinct-id) :time (or timestamp (now)))
        data (mk-request props event event-data)
        params {:data data
                :ip (coerce-bool (nil? ip))
                :test (coerce-bool test)}
        query-params (update-in params [:data] #(-> % (json-str) (base64-encode)))]
    (info params)
    (future (http/get api-url {:query-params query-params}))))
