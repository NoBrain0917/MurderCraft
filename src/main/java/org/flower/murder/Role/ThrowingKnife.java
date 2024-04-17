package org.flower.murder.Role;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.flower.murder.Controller.BukkitTaskManager;
import org.flower.murder.Controller.GameManager;
import org.flower.murder.Enums.GameState;
import org.flower.murder.Murder;
import org.flower.murder.Utils;

public class ThrowingKnife {
    public ArmorStand judegeArmorStand;
    public ArmorStand visualArmorStand;
    public Player player;
    public PlayerData playerData;
    public float speed;

    public ThrowingKnife(Player p, PlayerData pd) {
        player = p;
        playerData = pd;
    }

    public void Spawn(){
        Location l = player.getLocation();
        l.setYaw(l.getYaw()-90);

        Location l2 = player.getLocation();
        l2.setY(l2.getY()+0.7f);

        Location fakeLoc = player.getLocation();
        fakeLoc.add(player.getLocation().getDirection().multiply(-32));
        if(fakeLoc.getY() < -110)
            fakeLoc.setY(-110);

        visualArmorStand = player.getWorld().spawn(fakeLoc, ArmorStand.class);
        visualArmorStand.setInvisible(true);
        visualArmorStand.setArms(true);
        visualArmorStand.setGravity(false);
        visualArmorStand.setMarker(true);
        visualArmorStand.setItem(EquipmentSlot.HAND, new ItemStack(Material.IRON_SWORD));
        visualArmorStand.setMetadata("owner", new FixedMetadataValue(Murder.Instance, player.getEntityId()));
        visualArmorStand.customName(Component.text("murder1"));

        judegeArmorStand= player.getWorld().spawn(fakeLoc, ArmorStand.class);
        judegeArmorStand.setInvisible(true);
        judegeArmorStand.setMarker(true);
        judegeArmorStand.setGravity(false);
        judegeArmorStand.customName(Component.text("murder1"));
        judegeArmorStand.setMetadata("owner", new FixedMetadataValue(Murder.Instance, player.getEntityId()));

        judegeArmorStand.teleport(player.getEyeLocation());
        visualArmorStand.teleport(l2.add(l.getDirection().multiply(0.42f)));

        speed = 1.5f;

        if(playerData !=null) {
            if (playerData.Skill == RoleSkill.FastKnife)
                speed = 2f;
        }
    }

    public void destroyAtTask(BukkitRunnable task){
        task.cancel();
        BukkitTaskManager.RemoveTask(task);

        if(!visualArmorStand.isDead())
            visualArmorStand.remove();

        if(!judegeArmorStand.isDead())
            judegeArmorStand.remove();
    }

    public void onTick(int max, int count, BukkitRunnable task){
        if(count > max || visualArmorStand.isDead() || judegeArmorStand.isDead()) {
            destroyAtTask(task);
            return;
        }

        if(playerData !=null) {
            if (playerData.IsDeath) {
                destroyAtTask(task);
                return;
            }
        }

        Location nextLocation = visualArmorStand.getLocation().add(visualArmorStand.getLocation().getDirection().multiply(speed));
        visualArmorStand.teleport(nextLocation);

        Location judgeBeforeLocation = judegeArmorStand.getLocation().add(judegeArmorStand.getLocation().getDirection().multiply(speed*0.5f));
        Location judgeLocation = judegeArmorStand.getLocation().add(judegeArmorStand.getLocation().getDirection().multiply(speed));
        judegeArmorStand.teleport(judgeLocation);

        if (GameManager.LastGame != null) {
            if(GameManager.LastGame.State == GameState.END)
                return;
        }

        boolean coll1 = Utils.GiveDamageToNearEntity(judgeBeforeLocation, 0.5f, player, false, player.getEntityId());
        boolean coll2 = Utils.GiveDamageToNearEntity(judgeLocation, 0.5f, player, false, player.getEntityId());

        if(coll1 || coll2) {
            destroyAtTask(task);

            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.playSound(judgeLocation, Sound.BLOCK_STONE_BREAK, 1, 0f);
                pl.spawnParticle(Particle.SONIC_BOOM, judgeLocation, 1000, 0, 0, 0, 10);
            }
            return;
        }


        if(!Utils.IsPassable(judgeLocation) || !Utils.IsPassable(judgeBeforeLocation)) {
            destroyAtTask(task);
        }


    }
}
