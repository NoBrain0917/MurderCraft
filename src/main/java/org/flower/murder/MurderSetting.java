package org.flower.murder;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.Field;

public class MurderSetting {
    public static int SkillPercentage = 20;
    public static boolean AllowEntityDamageInLobby = true;
    public static int MurdererCount = 1;
    public static int DetectiveCount = 1;
    public static int TotalTime = 180;

    private static final File file = new File("plugins/Murder/setting.yml");
    private static FileConfiguration Config;

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

    private static void loadValue(){
        try {
            for (Field f : MurderSetting.class.getFields()) {
                String v = Config.getString(f.getName());
                if(v==null) continue;
                if(v.trim().isEmpty()) continue;
                Utils.SetFieldValueAuto(f,v);
            }

        } catch (Exception e) {
            System.out.println("cant load");
        }
    }

    public static void Init(){
        loadConfig();
        loadValue();
    }

    public static void Save() {
        try {
            for (Field f : MurderSetting.class.getFields()) {
                Config.set(f.getName(), f.get(null).toString());
            }

            Config.save(file);
        } catch (Exception e) {
            System.out.println("cant save");
        }
    }

}
