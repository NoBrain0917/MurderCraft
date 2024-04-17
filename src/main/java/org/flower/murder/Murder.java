package org.flower.murder;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.flower.murder.Controller.Listeners.*;
import org.flower.murder.Controller.MurderCommand;
import org.flower.murder.Controller.Packet.InvisiblePlayer;
import org.flower.murder.Controller.Packet.Spectator;
import org.flower.murder.Map.MapSetting;
import org.flower.murder.Map.MurderMap;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Murder extends JavaPlugin {
    public static Murder Instance;
    public World MainWorld;
    public ProtocolManager Protocol;
    private Team hideNameTeam;

    //public boolean hasSkinRestore;
    public SecureRandom Random;

    public void HideName(Player p){
            hideNameTeam.addEntry(p.getName());
    }

    public void ShowName(Player p){
            hideNameTeam.removeEntry(p.getName());
    }

    @Override
    public void onEnable() {
        MapSetting.Maps.put("로비", new MurderMap("로비"));

        Scoreboard score = Bukkit.getScoreboardManager().getMainScoreboard();

        hideNameTeam = score.getTeam("hide-name");
        if (hideNameTeam == null) {
            hideNameTeam = score.registerNewTeam("hide-name");
            hideNameTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            hideNameTeam.setCanSeeFriendlyInvisibles(false);
        }

        try {
            Random = SecureRandom.getInstanceStrong();
            System.out.println("using SecureRandom!");
        } catch (Exception e) {
            Random = new SecureRandom();
            System.out.println("using SecureRandom default");
        }

        Instance = this;
        MainWorld = Bukkit.getWorld("world");

        Bukkit.getPluginManager().registerEvents(new CommonSkillListener(), this);
        Bukkit.getPluginManager().registerEvents(new MurderControlListener(), this);
        Bukkit.getPluginManager().registerEvents(new DetectiveControlListener(), this);
        Bukkit.getPluginManager().registerEvents(new ToyGunListener(), this);
        Bukkit.getPluginManager().registerEvents(new GameControlListener(), this);

        MurderCommand m = new MurderCommand();
        Objects.requireNonNull(Bukkit.getPluginCommand("머더")).setExecutor(m);
        Objects.requireNonNull(Bukkit.getPluginCommand("머더")).setTabCompleter(m);

        Objects.requireNonNull(Bukkit.getPluginCommand("test")).setExecutor(new TEST());

        MainWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        MainWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        MainWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        MainWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        MainWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);

        Protocol = ProtocolLibrary.getProtocolManager();
        Protocol.removePacketListeners(this);

        /*
        for(Plugin p : Bukkit.getPluginManager().getPlugins()){
            if(p.getName().equalsIgnoreCase("SkinsRestorer")) {
                hasSkinRestore = true;
                break;
            }
        }*/

        MapSetting.Init();
        MurderSetting.Init();

        Spectator.Init();
        InvisiblePlayer.Init();
        // Plugin startup logic


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
