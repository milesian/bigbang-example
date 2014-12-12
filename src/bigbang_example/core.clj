(ns bigbang-example.core
  (:require [com.stuartsierra.component :as component]
            [milesian.bigbang :as bigbang]
            [milesian.aop :as aop]
            [milesian.aop.matchers :as aop-matchers]
            [milesian.aop.utils  :refer (logging-function-invocation)]
            [milesian.identity :as identity]
            [defrecord-wrapper.aop :refer (new-simple-protocol-matcher)]
            [tangrammer.component.co-dependency :as co-dep]
            [milesian.system-examples :refer (new-system-map listening talking Listen Talk)]))


(defn ^{:bigbang/phase :after-start} assoc-atom-system
  [c* system]
  (assert (not (nil? (:bigbang/key (meta c*))))
          "this fn needs your components meta tag with :api-component/key")
  (swap! system assoc (:bigbang/key (meta c*)) c*)
  c*
)

(def system-map (new-system-map))


(let [system-atom (atom system-map)]
  (-> (bigbang/expand system-map
                     {:before-start [[identity/add-meta-key system-map]
                                     [co-dep/assoc-co-dependencies system-atom]
                                     [identity/assoc-meta-who-to-deps]]
                      :after-start  [[assoc-atom-system system-atom]
                                     [aop/wrap logging-function-invocation]]})
     :c
     talking))
