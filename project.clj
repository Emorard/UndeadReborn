(defproject UndeadReborn "4.0-SNAPSHOT"
     :java-source-paths ["java-src"]
     :javac-options ["-d" "classes/" "-source" "1.8" "-target" "1.8"]
     :repositories [ ["spigot-repo" "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"]
                     ["sk89q-repo" "https://maven.enginehub.org/repo/"]]
     :dependencies [ [org.clojure/clojure "1.10.2"]
                     [org.clojure/core.match "1.0.0"]]
     :profiles {
                :provided {:dependencies [[org.spigotmc/spigot-api "1.16.5-R0.1-SNAPSHOT"]
                                          [com.sk89q.worldguard/worldguard-bukkit "7.0.0"]
                                          [com.sk89q.worldedit/worldedit-bukkit "7.0.0"]]}
                :uberjar {:uberjar-name "UndeadReborn.jar" :aot :all}
                }
     )