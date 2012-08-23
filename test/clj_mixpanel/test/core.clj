(ns clj-mixpanel.test.core
  (:refer-clojure :exclude [set])
  (:use [clj-mixpanel.core])
  (:use [midje.sweet]))

(let [response {:status 200 :body "hi"}
      token "123"
      distinct-id "456"
      event "login"
      timestamp "12345"]

  (fact (track token event {:distinct-id distinct-id :timestamp timestamp}) => response
    (provided
      (post track-url
       {:data {:event event
               :properties {:token token :distinct_id distinct-id :time timestamp}}
        :ip "1" :test "0"})
      => response))

  (fact (set token distinct-id {:hams :clams}) => response
    (provided
      (post engage-url
            {:data {:$token token
                    :$distinct_id distinct-id
                    :$set {:hams :clams}}})
      => response))

  (fact (increment token distinct-id {:hams :clams}) => response
    (provided
      (post engage-url
            {:data {:$token token
                    :$distinct_id distinct-id
                    :$add {:hams :clams}}})
      => response)))

