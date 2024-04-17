package org.flower.murder.Controller.Listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.flower.murder.Controller.BukkitTaskManager;
import org.flower.murder.Controller.GameManager;
import org.flower.murder.Murder;
import org.flower.murder.Role.PlayerData;
import org.flower.murder.Role.RoleSkill;
import org.flower.murder.Role.ThrowingKnife;


public class MurderControlListener implements Listener {


    @EventHandler
    public void OnKnifeUse(PlayerInteractEvent event){
        Player p = event.getPlayer();

        if(!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        if(p.getInventory().getItemInMainHand().getType() == Material.IRON_SWORD){


            PlayerData pd = null;
            if(GameManager.LastGame != null) {
                if(GameManager.LastGame.PlayerMap.GetPlayer(p)!=null) {
                    pd = GameManager.LastGame.PlayerMap.GetPlayer(p);
                    if (pd.IsDeath)
                        return;
                }
            }

            if(p.hasCooldown(Material.IRON_SWORD)) return;

            for(Player pl : Bukkit.getOnlinePlayers())
                pl.playSound(p.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 1, 1);

            ThrowingKnife knife = new ThrowingKnife(p, pd);
            knife.Spawn();

            BukkitRunnable t = new BukkitRunnable() {
                int count = 0;

                @Override
                public void run() {
                    knife.onTick(20 * 3, count, this);
                    count++;
                }
            };
            t.runTaskTimer(Murder.Instance, 0, 1);
            BukkitTaskManager.AddTask(t);

            if(pd != null) {
                if (pd.Skill == RoleSkill.FastReload) {
                    ReloadSword(p, 0.4f);
                    return;
                }
            }

            ReloadSword(p, 0.6f);

        }
    }

    @EventHandler
    public void CancelArmorStandInteraction(PlayerArmorStandManipulateEvent event){
        if(event.getRightClicked().getCustomName() == null) return;
        if(event.getRightClicked().getCustomName().equalsIgnoreCase("murder1"))
            event.setCancelled(true);
    }

    private void ReloadSword(Player p, float timing){
        p.setCooldown(Material.IRON_SWORD, (int)(timing*4*20));
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.BOLD+"회수중..."));

        BukkitRunnable t = new BukkitRunnable() {
            @Override
            public void run() {
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.8f, 2f);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.BOLD + "회수 완료!"));
                BukkitTaskManager.RemoveTask(this);
            }
        };
        t.runTaskLater(Murder.Instance, (int)(timing*4*20));
        BukkitTaskManager.AddTask(t);
    }


}
