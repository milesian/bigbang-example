(ns bigbang-example.core
  (:require [com.stuartsierra.component :as component]
            [milesian.bigbang :as bigbang]
            [milesian.aop :as aop]
            [milesian.aop.matchers :as aop-matchers]
            [milesian.aop.utils  :refer (logging-function-invocation)]
            [milesian.identity :as identity]
            [defrecord-wrapper.aop :refer (new-simple-protocol-matcher)]
            [tangrammer.component.co-dependency :as co-dependency]
            [milesian.system-examples :refer (new-system-map listening talking Listen Talk)])
  (:import [clojure.lang Atom]))



(def system-map (new-system-map))



(let [started-system (bigbang/expand system-map
                                     {:before-start [[identity/add-meta-key system-map]]
                                      :after-start  []})]
  (assert (= :c (-> started-system :c meta :bigbang/key)))
  )


(let [started-system (bigbang/expand system-map
                                     {:before-start [[identity/add-meta-key system-map]
                                                     [identity/assoc-meta-who-to-deps]]
                                      :after-start  []})]
  (assert (= :c (-> started-system :c meta :bigbang/key)))
  (assert (= :c (-> started-system :c :b meta :bigbang/who)))
  )




(let [started-system (bigbang/expand system-map
                                     {:before-start [[identity/add-meta-key system-map]
                                                     [identity/assoc-meta-who-to-deps]]
                                      :after-start  [[aop/wrap logging-function-invocation]]})]

  (assert (= :c (-> started-system :c meta :bigbang/key)))
  (assert (= :c (-> started-system :c :b meta :bigbang/who)))
  (-> started-system :c talking)
  ;; check the repl to see the logging function invocation
  ;; REPL->c: talking
  ;; c->b: listening

  )






(let [system-map  (assoc system-map :b  (-> (:b system-map)
                                            (co-dependency/co-using [:c])))
      system-atom (atom system-map)
      started-system (bigbang/expand system-map
                      {:before-start [[identity/add-meta-key system-map]
                                      [co-dependency/assoc-co-dependencies system-atom]
                                      [identity/assoc-meta-who-to-deps]]
                       :after-start  [[co-dependency/update-atom-system system-atom]
                                      [aop/wrap logging-function-invocation]]})]
  (assert (= :c (-> started-system :c meta :bigbang/key)))
  (assert (= :c (-> started-system :c :b meta :bigbang/who)))
  (-> started-system :c talking)
    ;; check the repl to see the logging function invocation
  ;; REPL->c: talking
  ;; c->b: listening

  (assert (= (-> started-system :c :state) (-> started-system :b :c deref :state)))

  )
