package org.flower.murder.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.flower.murder.MiniVector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MapSetting {
    private static final File file = new File("plugins/Murder/mapData.yml");
    private static FileConfiguration Config;

    public static HashMap<String,MurderMap> Maps = new HashMap<>();
    public static MurderMap Lobby;

    private static void loadConfig(){
        Config = YamlConfiguration.loadConfiguration(file);
        try {
            if (!file.exists()) {
                Config.save(file);
            }
            Config.load(file);
        } catch (Exception e) {
            System.out.println("wtf error");
        }
    }

    private static void loadMaps(){
        try {
            List<String> l = Config.getStringList("mapNames");
            for (String n : l) {
                MurderMap m = new MurderMap(n);


                List<String> p = Config.getStringList(n+"-spawns");

                if(!Objects.requireNonNull(p).isEmpty()) {
                    for (String o : p) {
                        MiniVector v = MiniVector.FromString(o);
                        m.SpawnPositions.add(v);
                    }
                }

                Maps.put(n, m);
            }

        } catch (Exception e) {
            System.out.println("wtf cant load");
        }
    }

    public static void SaveMaps(){
        try {
            ArrayList<String> mapNames = new ArrayList<>();
            for(MurderMap m : Maps.values()) {
                mapNames.add(m.Name);

                Config.set(m.Name+"-spawns", VectorToString(m.SpawnPositions));
            }

            Config.set("mapNames", mapNames);

            Config.save(file);
        } catch (Exception e) {
            System.out.println("cant save map");
        }
    }

    public static void RemoveMap(MurderMap map){
        MapSetting.Maps.remove(map.Name);
        Config.set(map.Name+"-spawns", null);
        SaveMaps();
    }


    public static ArrayList<String> GetMapNamesWithLobby(){
        ArrayList<String> mapNames = new ArrayList<>();
        for(MurderMap m : Maps.values())
            mapNames.add(m.Name);

        return mapNames;
    }

    public static ArrayList<String> GetMapNames(){
        ArrayList<String> mapNames = new ArrayList<>();
        for(MurderMap m : Maps.values()) {
            if(!m.Name.equalsIgnoreCase("로비"))
                mapNames.add(m.Name);
        }

        return mapNames;
    }

    public static void SaveMap(MurderMap map){
        try {
            Config.set("mapNames", GetMapNamesWithLobby());
            Config.set(map.Name+"-spawns", VectorToString(map.SpawnPositions));

            Config.save(file);
        } catch (Exception e) {
            System.out.println("cant save map");
        }
    }


    private static ArrayList<String> VectorToString(ArrayList<MiniVector> vectors){
        ArrayList<String> result = new ArrayList<>();
        for(MiniVector v : vectors) {
            result.add(v.ToString());
        }
        return result;
    }


    public static void Init() {
        loadConfig();
        loadMaps();

        Lobby = Maps.get("로비");
    }
}
