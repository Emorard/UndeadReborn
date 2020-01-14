(defproject UndeadReborn "1.3A-SNAPSHOT"
     :java-source-paths ["java-src"]
     :javac-options ["-d" "classes/" "-source" "1.8" "-target" "1.8"]
     :repositories [ ["spigot-repo" "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"] ]
     :dependencies [ [org.clojure/clojure "1.10.1"] ]
     :profiles {
                :provided {:dependencies [[org.spigotmc/spigot-api "1.12-R0.1-SNAPSHOT"]]}
                :uberjar {:uberjar-name "UndeadReborn.jar" :aot :all}
                }
     )