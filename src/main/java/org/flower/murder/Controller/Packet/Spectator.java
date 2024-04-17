package org.flower.murder.Controller.Packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.flower.murder.Murder;

import java.util.*;


public class Spectator {
    private static final Set<PacketType> playerActions = new HashSet<>();
    private static final Set<PacketType> serverActions = new HashSet<>();
    public static HashMap<String,Player> spectatorList = new HashMap<>();


    public static void Init() {
        spectatorList.clear();

        playerActions.add(PacketType.Play.Client.BLOCK_PLACE);
        playerActions.add(PacketType.Play.Client.BLOCK_DIG);
        playerActions.add(PacketType.Play.Client.GROUND);
        playerActions.add(PacketType.Play.Client.ABILITIES);
        playerActions.add(PacketType.Play.Client.LOOK);
        playerActions.add(PacketType.Play.Client.ENTITY_ACTION);
        playerActions.add(PacketType.Play.Client.ARM_ANIMATION);
        playerActions.add(PacketType.Play.Client.USE_ITEM);
        playerActions.add(PacketType.Play.Client.USE_ENTITY);
        playerActions.add(PacketType.Play.Client.CLOSE_WINDOW);


        serverActions.add(PacketType.Play.Server.GAME_STATE_CHANGE);
        serverActions.add(PacketType.Play.Server.ABILITIES);
        serverActions.add(PacketType.Play.Server.PLAYER_INFO);

        Murder.Instance.Protocol.addPacketListener(new PacketAdapter(
                Murder.Instance,
                ListenerPriority.HIGHEST,
                serverActions
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (spectatorList.get(event.getPlayer().getUniqueId().toString())!=null) {


                    if(event.getPacketType() == PacketType.Play.Server.PLAYER_INFO) {
                        String str = event.getPacket().getModifier().getValues().get(0).toString();
                        if(str.equalsIgnoreCase("[UPDATE_GAME_MODE]")) {
                            String str2 = event.getPacket().getModifier().getValues().get(1).toString();
                            boolean isGameMode3 = str2.contains("gameMode=SPECTATOR");

                            if(isGameMode3)
                                event.setCancelled(true);
                        }
                    } else {
                        if(event.getPacketType() == PacketType.Play.Server.GAME_STATE_CHANGE) {
                            boolean isGameMode3 = event.getPacket().getFloat().getValues().get(0)==3;
                            if(isGameMode3)
                                event.setCancelled(true);
                        }else {
                            event.setCancelled(true);
                        }
                    }

                }
            }
        });



        Murder.Instance.Protocol.addPacketListener(new PacketAdapter(Murder.Instance, ListenerPriority.HIGHEST,
                playerActions) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                try {
                    Player receiver = event.getPlayer();
                    if (spectatorList.get(receiver.getUniqueId().toString())!=null) {
                        event.setCancelled(true);
                    }
                } catch (Exception e) {
                    System.out.println(event.getPacket().getType()+"에러\n"+e);
                }

            }
        });
    }


    /*
    private static void UpdateSpectatorPosition(){
        if(bukkitTast == null) {
            bukkitTast = new BukkitRunnable() {
                public void run() {
                    for (Player p: spectators) {
                        p.teleport()
                    }
                }
            };
        }
    }*/


    public static void DisableSpectator(Player p) {
        if(!HasPlayer(p)) return;

        spectatorList.remove(p.getUniqueId().toString());

        p.setAllowFlight(false);
    }


    public static boolean HasPlayer(Player p) {
        return spectatorList.get(p.getUniqueId().toString())!=null;
    }

    public static void EnableSpectator(Player p) {
        if(HasPlayer(p)) return;
        //if(!GameManager.IsStart()) return;

        p.setGameMode(GameMode.ADVENTURE);
        p.setAllowFlight(true);
        p.setHealth(p.getMaxHealth());

        spectatorList.put(p.getUniqueId().toString(),p);

        p.setGameMode(GameMode.SPECTATOR);

    }
}
