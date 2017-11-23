(ns user
  (:require [figwheel-sidecar.repl-api]))

(defn start-server! []
  (figwheel-sidecar.repl-api/start-figwheel!
    (assoc-in (figwheel-sidecar.config/fetch-config)
              [:data :figwheel-options :server-port] 4536)
    "dev-server")
  (figwheel-sidecar.repl-api/cljs-repl "dev-server"))

(comment
  (start-server!))

