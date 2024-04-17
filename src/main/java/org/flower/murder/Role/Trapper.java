package org.flower.murder.Role;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.flower.murder.Murder;
import org.flower.murder.Utils;

import java.util.ArrayList;
import java.util.List;

public class Trapper {
    public Player owner;
    public ArmorStand trapPosition;
    public boolean activated;

    public Trapper(Player p) {
        owner = p;
    }

    public void Spawn(Location l){
        Location loc = Utils.GetMinLocationY(l);
        loc.setY(l.getY()-0.9f);

        trapPosition = loc.getWorld().spawn(loc, ArmorStand.class);
        trapPosition.setMarker(true);
        trapPosition.setGravity(false);
        trapPosition.customName(Component.text("murder_trap"));
        trapPosition.setHelmet(new ItemStack(Material.WITHER_SKELETON_SKULL));
        trapPosition.setVisible(false);
        activated = true;

    }

    public void Destroy(){
        if(!trapPosition.isDead())
            trapPosition.remove();

        activated = false;
    }

    public void GiveTrapDamage(Player damagedPlayer) {
        if(!activated) return;

        Destroy();

        damagedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 5, 10, true, false));
        damagedPlayer.playSound(damagedPlayer.getLocation(), Sound.ENTITY_GENERIC_SMALL_FALL, 1f, 0.1f);
        owner.playSound(owner.getLocation(), Sound.ENTITY_GENERIC_SMALL_FALL, 1f, 0f);
    }

}
