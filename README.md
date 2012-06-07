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

## License

Copyright &copy; 2012 Paul Ingles

Distributed under the Eclipse Public License, the same as Clojure.
