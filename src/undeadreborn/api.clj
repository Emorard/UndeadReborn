(ns undeadreborn.api
  (:import (com.pokkedoll.undeadreborn Main)
           (org.bukkit.entity Player)
           (org.bukkit.inventory ItemStack)
           (org.bukkit Material NamespacedKey)
           (org.bukkit.inventory.meta SkullMeta)
           (org.bukkit.persistence PersistentDataType PersistentDataContainer))
  )

(defrecord Configuration [^int config-version worlds])

(def #^Main plugin (delay (Main/getInstance)))

(defrecord PlayerZombie [playerUUID zombieUUID contents])

(def zombies (vector))

(def zombie-key (NamespacedKey. plugin "player-uuid"))

(def namespaced-keys {:contents (NamespacedKey. plugin "contents")
                      :armors   (NamespacedKey. plugin "armors")
                      :extras   (NamespacedKey. plugin "extras")})

(defn getPlayerHead
  "doc"
  [#^Player player]
  (let [#^ItemStack item (ItemStack. Material/PLAYER_HEAD 1)]
    (let [#^SkullMeta meta (-> item .getItemMeta)]
      (-> meta (.setOwningPlayer player))
      (-> item (.setItemMeta meta))
      item)))

(defn serialize-items
  [items]
  (map #(-> ^ItemStack % .serializeAsBytes) items))

(defn save
  [^Player player seq]
  (doseq [kv seq]
    (-> player
        .getPersistentDataContainer
        (.set (namespaced-keys (key kv)) PersistentDataType/BYTE_ARRAY (val kv)))))

(defn deserialize-items
  [bytes]
  (map #(ItemStack/deserializeBytes bytes)))

(defn save-contents
  "docstring"
  [^Player player]
  (let [inv (-> player .getInventory)]
    (->> [(-> inv .getContents)
          (-> inv .getArmorContents)
          (-> inv .getExtraContents)]
         ;; [ItemStack[] ItemStack[] ItemStack[]]
         (map seq)
         ;; [byte[][] byte[][] byte[][]]
         (map serialize-items)
         (zipmap (keys namespaced-keys))
         (save player))))

(defn get-contents
  [^PersistentDataContainer container]
  (->> (vals namespaced-keys)
       (map #(-> container (.get % PersistentDataType/BYTE_ARRAY)))
       (map seq)))