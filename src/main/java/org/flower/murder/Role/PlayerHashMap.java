package org.flower.murder.Role;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;

public class PlayerHashMap {
    private final HashMap<String, PlayerData> playerMap = new HashMap<>();

    public void SetPlayer(Player p, PlayerData d) {
        playerMap.put(p.getUniqueId().toString(),d);
    }

    public Collection<PlayerData> Values(){
        return playerMap.values();
    }

    public PlayerData GetPlayer(Player p){
        return playerMap.get(p.getUniqueId().toString());
    }

    public boolean IsEmpty() {
        return playerMap.isEmpty();
    }

    public int Size(){
        return playerMap.size();
    }

    public void Clear(){
        playerMap.clear();
    }

    public boolean IsHave(Player p) {
        return playerMap.get(p.getUniqueId().toString())!=null;
    }
}
