(defproject serial "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [overtone "0.9.1"]
                 [leipzig "0.8.1"]
                 [overtone.orchestra "0.1.0-SNAPSHOT"]]
;;  :main serial.twelve-tone
  :jvm-opts ^:replace [
                       "-Xms1g" "-Xmx8g"
                       "-XX:MaxGCPauseMillis=1000" ; Specify a target
                                        ; of 20ms for max gc pauses
                       "-XX:+UseTLAB" ; Uses thread-local object allocation blocks. This
                                        ;  improves concurrency by reducing contention on
                                        ;  the shared heap lock.

                       "-XX:+UseG1GC"
                       ])
