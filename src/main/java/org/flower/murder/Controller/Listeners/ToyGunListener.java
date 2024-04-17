package org.flower.murder.Controller.Listeners;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.flower.murder.Controller.GameManager;
import org.flower.murder.Enums.GameState;
import org.flower.murder.Utils;

public class ToyGunListener implements Listener {

    @EventHandler
    public void OnGunUse(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if(p.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

        ItemStack handItem = p.getInventory().getItemInMainHand();
        if (p.getInventory().getItemInMainHand().getType() == Material.STONE_HOE) {
            if (GameManager.LastGame != null) {
                if (GameManager.LastGame.PlayerMap.GetPlayer(p) != null) {
                    if (GameManager.LastGame.PlayerMap.GetPlayer(p).IsDeath) {
                        return;
                    }
                }
            }

            GunUse(p);

            int amount = handItem.getAmount();
            if(amount-1 <= 0)
                p.getInventory().remove(Material.STONE_HOE);
            else
                handItem.setAmount(amount-1);
        }
    }


    private void GunUse(Player p) {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            //pl.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
            pl.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1f);
        }


        Location loc = p.getEyeLocation();
        for (int n = 0; n < 128; n++) {
            loc.add(p.getLocation().getDirection().multiply(1));

            if (!Utils.IsPassable(loc)) break;
            for (Player pl : Bukkit.getOnlinePlayers()) {
                //pl.spawnParticle(Particle.FIREWORKS_SPARK, loc, 1, 0, 0, 0, 0);
                Utils.SpawnParticleLikeCMD(pl, Particle.FIREWORKS_SPARK, loc, 0,0,0,0,5);
            }

            if (GameManager.LastGame != null) {
                if(GameManager.LastGame.State == GameState.END) return;
            }

            if(Utils.NearPlayer(loc, 0.5f, p)) {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1f);
                break;
            }

        }
    }


}
