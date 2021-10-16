(defproject UndeadReborn "4.1"
  :java-source-paths ["java-src"]
  :javac-options ["-d" "classes/" "-source" "16" "-target" "16"]
  :repositories [["papermc" "https://papermc.io/repo/repository/maven-public/"]
                 ["sk89q-repo" "https://maven.enginehub.org/repo/"]]
  :dependencies [[org.clojure/clojure "1.10.2"]
                 [org.clojure/core.match "1.0.0"]]
  :profiles {
             :provided {:dependencies [[io.papermc.paper/paper-api "1.17.1-R0.1-SNAPSHOT"]
                                       [com.sk89q.worldguard/worldguard-bukkit "7.0.6"]
                                       [com.sk89q.worldedit/worldedit-bukkit "7.2.6"]]}
             :uberjar  {:uberjar-name "UndeadReborn.jar" :aot :all}})