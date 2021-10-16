(ns undeadreborn.api
  (:import (hm.moe.pokkedoll.undeadreborn Main)
           (org.bukkit.entity Player)
           (org.bukkit.inventory ItemStack)
           (org.bukkit Material Bukkit)
           (org.bukkit.inventory.meta SkullMeta)
           (org.bukkit.configuration.file YamlConfiguration))
  )

(defrecord Configuration [^int config-version worlds])

(def plugin (delay (Main/getInstance)))

(defrecord PlayerZombie [playerUUID zombieUUID contents])

(def zombies (vector))

(defn getPlayerHead
  "doc"
  [#^Player player]
  (let [#^ItemStack item (ItemStack. Material/PLAYER_HEAD 1)]
    (let [#^SkullMeta meta (-> item .getItemMeta)]
      (-> meta (.setOwningPlayer player))
      (-> item (.setItemMeta meta))
      item)))

