(ns clj-mixpanel.core
  (:import [org.apache.commons.codec.binary Base64]
           [java.util Date UUID])
  (:require [clj-http.client :as http]
            [clojure.string :as s]
            [clojure.tools.logging :as log])
  (:use [clojure.data.json :only (json-str)])
  (:refer-clojure :exclude [set]))

(def api-url "http://api.mixpanel.com/")
(def track-url (str api-url "track"))
(def engage-url (str api-url "engage"))

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

(defn track-request
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

(defn post
  [url params]
  (log/info "posting to " url params)
  (http/post url
             {:query-params (update-in params [:data] #(-> % json-str base64-encode))}))

(defn engage
  [action token distinct-id properties]
  (post engage-url
        {:data {:$token token
                :$distinct_id distinct-id
                (keyword (str "$" (name action))) properties}}))

;;; public api ;;;

(defn track
  "Sends a tracks an action in the Mixpanel API.

   token: available in the dashboard
   event: string representing the type of event
   m    : a map containing payload information. must contain
          a distinct-id that identifies the user.

   Example:
   (let [user-id (generate-uuid)]
     (track token \"My Event\" {:distinct-id user-id :key \"value})"
  [token event {:keys [distinct-id ip test timestamp] :as payload}]
  {:pre [(contains? payload :distinct-id)]}
  (let [props {:token token
               :distinct-id distinct-id}
        event-data (assoc (dissoc payload :timestamp :distinct-id) :time (or timestamp (now)))
        data (track-request props event event-data)
        params {:data data
                :ip (coerce-bool (nil? ip))
                :test (coerce-bool test)}]
    (post track-url params)))

(defn notify
  "Function for backwards compatibility.  Returns a future representing
the tracking request's response. Deprecated in favor of track."
  [token event options]
  (future (track token event options)))

(def set (partial engage :set))

(def increment (partial engage :add))


