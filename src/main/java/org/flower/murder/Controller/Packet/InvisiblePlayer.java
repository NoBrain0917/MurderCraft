package org.flower.murder.Controller.Packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.flower.murder.Controller.GameManager;
import org.flower.murder.Murder;
import org.flower.murder.Role.PlayerRole;
import org.flower.murder.Role.RoleSkill;

import java.util.HashMap;

public class InvisiblePlayer {

    public static HashMap<String,Integer> PlayerLastSlot = new HashMap<>();

    public static void Reset(){
        for(Player p : Bukkit.getOnlinePlayers()) {
            Integer i = PlayerLastSlot.get(p.getUniqueId().toString());

            if(i==null) continue;
            
            PacketContainer fakeSlot = new PacketContainer(PacketType.Play.Client.HELD_ITEM_SLOT);
            fakeSlot.getIntegers().write(0, i);

            Murder.Instance.Protocol.receiveClientPacket(p,fakeSlot);
        }

        PlayerLastSlot.clear();
    }

    public static void Init() {

        Murder.Instance.Protocol.addPacketListener(new PacketAdapter(
                Murder.Instance,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.HELD_ITEM_SLOT
        ) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if(!GameManager.IsStart()) return;
                Player p = event.getPlayer();
                if(!p.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

                if (GameManager.LastGame != null) {
                    if (GameManager.LastGame.PlayerMap.GetPlayer(p) != null) {
                        if (GameManager.LastGame.PlayerMap.GetPlayer(p).Role == PlayerRole.MURDERER) {
                            return;
                        }
                    }
                }


                PacketContainer packet = event.getPacket();
                int slot = packet.getIntegers().getValues().get(0);
                boolean handHasItem = slot == 1 || slot == 2;

                PlayerLastSlot.remove(p.getUniqueId().toString());

                if(!handHasItem) return;
                packet.getIntegers().write(0,0);
                event.setPacket(packet);

            }
        });

    }
}
