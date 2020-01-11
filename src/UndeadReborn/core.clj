(ns UndeadReborn.core
  (:gen-class :name UndeadReborn.core.Main)
  (:import (hm.moe.pokkedoll.undeadreborn Main)
           (org.bukkit Bukkit)
           (UndeadReborn URListener)
           (java.io File)))

(defn on-enable
  "onEnable"
  [#^Main this]
  (.info (.getLogger this) "Plugin Enabled")
  (.registerEvents (Bukkit/getPluginManager) (new URListener this) this)
  (when-not (.exists (.getDataFolder this)) (.mkdir (.getDataFolder this)) (.createNewFile (File. (.getDataFolder this) "/config.yml")))
  (.saveDefaultConfig this)
  )

(defn on-disable
  "onDisable"
  [#^Main this]
  (.info (.getLogger this) "Plugin Disabled")
  )

