(ns undeadreborn.core
  (:gen-class :name undeadreborn.core.Main)
  (:import (com.pokkedoll.undeadreborn Main)
           (org.bukkit Bukkit)
           (undeadreborn URListener)
           (java.io File)))

(defn on-enable
  "onEnable"
  [#^Main this]
  (-> this .getLogger (.info "Plugin Enabled"))
  (-> (Bukkit/getPluginManager) (.registerEvents (URListener. this) this))
  (when-not (-> this .getDataFolder .exists) (-> this .getDataFolder .mkdir) (-> (File. (.getDataFolder this) "/config.yml") .createNewFile))
  (.saveDefaultConfig this))

(defn on-disable
  "onDisable"
  [#^Main this]
  (-> this .getLogger (.info "Plugin Disabled")))

