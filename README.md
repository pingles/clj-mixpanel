# clj-mixpanel

Send events to Mixpanel.

## Usage

Add it to your `project.clj`:

    [clj-mixpanel "0.0.1-SNAPSHOT"]

You'll need the API token that's available in your account dashboard ([https://mixpanel.com/account/](https://mixpanel.com/account/)).

Mixpanel tracks events against users, all events must track users with a `:distinct-id` value; `clj-mixpanel` includes a `generate-uuid` fn that can be used.

To trigger an event named "Signed Up", and pass it data for an additional attribute (named `extra-data` below):

```clojure
(def api-token "<API>")

(notify api-token "Signed Up" {:distinct-id (generate-uuid) :extra-data "hello"})}
```

Note that `notify` wraps sending notifications as a [http://clojuredocs.org/clojure_core/clojure.core/future](future).

The response can be retrieved by dereferencing the result. In the event of an error, the API will return a 200 response with more details in the body.

### Data export

Mixpanel has two slightly different export APIs - one for getting raw
event data and one for "formatted data" that gives you programmatic
access to many of the metrics you can see in the Mixpanel UI.

`clj-mixpanel` has basic support for both:

Raw data access:
```clj
(->>
 (export {:api-key "yourkeyhere
          :api-secret "yoursecrethere
         {:event (json-str ["Your Event Name"])
          :from_date "2014-08-14"
          :to_date "2014-08-17"})
 :body
 s/split-lines
 (map read-json)
 first)
=> {:event "Your Event Name", :properties {:$region "California", :$initial_referring_domain "$direct", :existing_user true, :$initial_referrer "$direct", :mp_country_code "US", :$screen_height 1200, :$city "Mountain View", :$os "Mac OS X", :distinct_id "xxxxxxxx", :$screen_width 1920, :$browser "Chrome"}}
```

Formatted data access:
```clj
(-> (get {:api-key "yourkeyhere"
          :api-secret "yoursecrethere"}
         "events" {:event (json-str ["Your Event Name"])
                   :type "unique"
                   :unit "week"
                   :interval 2})
    :body
    read-json)
=> {:legend_size 1, :data {:series ["2014-08-11" "2014-08-18"], :values {:"Your Event Name" {:2014-08-18 2754, :2014-08-11 4136, :2014-08-04 4101}}}}
```

## License

Copyright &copy; 2012 Paul Ingles

Distributed under the Eclipse Public License, the same as Clojure.
