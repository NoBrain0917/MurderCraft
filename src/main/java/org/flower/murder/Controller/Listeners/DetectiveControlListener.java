package org.flower.murder.Controller.Listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.flower.murder.Controller.BukkitTaskManager;
import org.flower.murder.Controller.GameManager;
import org.flower.murder.Enums.GameState;
import org.flower.murder.Murder;
import org.flower.murder.Role.PlayerRole;
import org.flower.murder.Role.RoleSkill;
import org.flower.murder.Utils;


public class DetectiveControlListener implements Listener {

    @EventHandler
    public void OnGunUse(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if(p.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

        if (p.getInventory().getItemInMainHand().getType() == Material.NETHERITE_HOE) {
            if (GameManager.LastGame != null) {
                if (GameManager.LastGame.PlayerMap.GetPlayer(p) != null) {
                    if (GameManager.LastGame.PlayerMap.GetPlayer(p).Role == PlayerRole.MURDERER) {
                        return;
                    }
                    if (GameManager.LastGame.PlayerMap.GetPlayer(p).IsDeath) {
                        return;
                    }
                }
            }


            if (p.hasCooldown(Material.NETHERITE_HOE)) return;

            float timing = 0.85f;

            if (GameManager.LastGame != null) {
                if (GameManager.LastGame.PlayerMap.GetPlayer(p) != null) {
                    if (GameManager.LastGame.PlayerMap.GetPlayer(p).Skill == RoleSkill.FastReload) {
                        timing = 0.65f;
                    }
                }
            }

            p.setCooldown(Material.NETHERITE_HOE, (int)(timing*4*20));

            float finalTiming = timing;
            BukkitRunnable t = new BukkitRunnable() {
                int c = 0;

                @Override
                public void run() {
                    if (p.getInventory().getItemInMainHand().getType() != Material.NETHERITE_HOE) {
                        cancel();
                        BukkitTaskManager.RemoveTask(this);
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.BOLD + "취소됨"));
                        p.resetCooldown();
                        return;

                    }
                    if (c == 3) {
                        cancel();
                        BukkitTaskManager.RemoveTask(this);
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.BOLD + "발사!"));
                        GunUse(p, finalTiming);
                        return;
                    }
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
                    if (c == 0)
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.BOLD + "준비중."));
                    if (c == 1)
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.BOLD + "준비중.."));
                    if (c == 2)
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.BOLD + "준비중..."));


                    c++;
                }
            };
            t.runTaskTimer(Murder.Instance, 0, 2);
            BukkitTaskManager.AddTask(t);
        }
    }


    private void GunUse(Player p, float timing) {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            //pl.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
            pl.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 0.5f);
        }


        Location loc = p.getEyeLocation();
        for (int n = 0; n < 128; n++) {
            loc.add(p.getLocation().getDirection().multiply(1));

            if (!Utils.IsPassable(loc)) break;
            for (Player pl : Bukkit.getOnlinePlayers()) {
                //pl.spawnParticle(Particle.FIREWORKS_SPARK, loc, 1, 0, 0, 0, 0);
                Utils.SpawnParticleLikeCMD(pl, Particle.SMOKE_LARGE, loc, 0,0,0,0,5);
            }

            if (GameManager.LastGame != null) {
                if(GameManager.LastGame.State == GameState.END) return;
            }

            boolean isKnifeCollision = Utils.GiveDamageToNearEntity(loc, 0.3f, p, true,-1);
            if(isKnifeCollision) {
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    pl.playSound(loc, Sound.BLOCK_STONE_BREAK, 1, 0f);
                    pl.spawnParticle(Particle.SONIC_BOOM, loc, 1000, 0, 0, 0, 10);
                }
                break;
            }
        }


        ReloadGun(p, timing);
    }

    private void ReloadGun(Player p, float timing) {

        if(GameManager.LastGame != null) {
            if(GameManager.LastGame.State == GameState.END)
                return;
        }

        BukkitRunnable t = new BukkitRunnable() {
            @Override
            public void run() {
                if(GameManager.LastGame != null) {
                    if(GameManager.LastGame.State == GameState.END) {
                        BukkitTaskManager.RemoveTask(this);
                        return;
                    }
                }
                p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 0.8f, 0.7f);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.BOLD+"장전중..."));

                BukkitTaskManager.RemoveTask(this);
            }
        };
        t.runTaskLater(Murder.Instance, 20);
        BukkitTaskManager.AddTask(t);

        BukkitRunnable t2 = new BukkitRunnable() {
            @Override
            public void run() {
                if(GameManager.LastGame != null) {
                    if(GameManager.LastGame.State == GameState.END) {
                        BukkitTaskManager.RemoveTask(this);
                        return;
                    }
                }
                p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 0.8f, 0.7f);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.BOLD + "장전 완료!"));

                BukkitTaskManager.RemoveTask(this);

            }
        };
        t2.runTaskLater(Murder.Instance, (int) (4 * 20 * timing)-6);
        BukkitTaskManager.AddTask(t2);
    }
}
