package org.flower.murder;

import com.comphenix.protocol.events.PacketContainer;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.flower.murder.Controller.GameManager;
import org.flower.murder.Map.MurderMap;
import org.flower.murder.Role.PlayerData;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Utils {

    public static void SendAllTitle(String s, String s2, int a, int b, int c){
        for(Player p : Bukkit.getOnlinePlayers())
            p.sendTitle(s, s2, a,b,c);
    }

    public static void AllPlaySound(Sound s, float v, float pi){
        for(Player p : Bukkit.getOnlinePlayers())
            p.playSound(p.getLocation(), s, v, pi);
    }

    public static boolean IsPassable(Location l){
        Block currentBlock = l.getBlock();
        if(currentBlock.getBlockData().getMaterial() != Material.AIR) {
            if(currentBlock.getBlockData().getMaterial() == Material.BARRIER)
                return true;
            return currentBlock.isPassable();
        }
        return true;
    }

    public static void SetFieldValueAuto(Field f, String v) {
        try {
            Type t = f.getGenericType();
            if (t == int.class)
                f.setInt(null, Integer.parseInt(v));
            if (t == float.class)
                f.setFloat(null, Float.parseFloat(v));
            if (t == double.class)
                f.setDouble(null, Double.parseDouble(v));
            if (t == boolean.class)
                f.setBoolean(null, v.equalsIgnoreCase("true"));
        } catch (Exception ignored) {
        }
    }

    public static void SpawnParticleLikeCMD(Player p, Particle particle, Location l, double dx, double dy, double dz, double speed, int count){
        p.spawnParticle(particle, l, count, dx, dy, dz, speed);
    }

    public static void Log(PacketContainer p) {
        StringBuilder s = new StringBuilder();

        s.append(p.getType());
        s.append("패킷 실행 로그\n");

        if(p.getIntegers().size() > 0) {
            s.append("Int: ");
            s.append(p.getIntegers().getValues());
            s.append("\n");
        }

        if(p.getFloat().size() > 0) {
            s.append("Float: ");
            s.append(p.getFloat().getValues());
            s.append("\n");
        }

        if(p.getDoubles().size() > 0) {
            s.append("Double: ");
            s.append(p.getDoubles().getValues());
            s.append("\n");
        }

        if(p.getBytes().size() > 0) {
            s.append("Bytes: ");
            s.append(p.getBytes().getValues());
            s.append("\n");
        }

        if(p.getBlocks().size() > 0) {
            s.append("Blocks: ");
            s.append(p.getBlocks().getValues());
            s.append("\n");
        }

        if(p.getBlockData().size() > 0) {
            s.append("BlockData: ");
            s.append(p.getBlockData().getValues());
            s.append("\n");
        }

        if(p.getBlockPositionModifier().size() > 0) {
            s.append("BlockPositionModifier: ");
            s.append(p.getBlockPositionModifier().getValues());
            s.append("\n");
        }

        if(p.getModifier().size() > 0) {
            s.append("Modifier: ");
            s.append(p.getModifier().getValues());
            s.append("\n");
        }

        if(p.getStructures().size() > 0) {
            s.append("Structures: ");
            s.append(p.getStructures().getValues());
            s.append("\n");
        }

        if(p.getBlockEntityTypeModifier().size() > 0) {
            s.append("BlockEntityTypeModifier: ");
            s.append(p.getBlockEntityTypeModifier().getValues());
            s.append("\n");
        }

        if(p.getMovingBlockPositions().size() > 0) {
            s.append("MovingBlockPositions: ");
            s.append(p.getMovingBlockPositions().getValues());
            s.append("\n");
        }


        System.out.println(s);
    }

    public static boolean NearPlayer(Location l, float round, Player starter){
        Collection<Entity> entitys = l.getNearbyEntities(round,1.62f,round);
        for(Entity e : entitys) {

            if(e instanceof Player) {
                if(starter.getEntityId() == e.getEntityId()) continue;

                if(GameManager.LastGame != null) {
                    PlayerData pd = GameManager.LastGame.PlayerMap.GetPlayer(((Player) e));
                    if(pd != null)
                        if(pd.IsDeath)
                            continue;;
                }

                double diff = l.getY()-e.getLocation().getY();


                if(diff > 2.2) continue;
                if(diff < -0.2) continue;


                ((Player) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW,40,3,true,false));
                ((Player) e).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,40,5,true,false ));
                return true;

            }
        }
        return false;
    }

    public static int FastDistance(Location l1, Location l2){
        return FastDistance(l1,l2,0);
    }

    public static double FastDistanceNo(Location l1, Location l2, float yOffset){
        double dx = ((l1.getX() - l2.getX()));
        double dy = (((l1.getY()+yOffset) - l2.getY()));
        double dz = ((l1.getZ() - l2.getZ()));

        return (dx*dx)+(dy*dy)+(dz*dz);
    }

    public static int FastDistance(Location l1, Location l2, float yOffset){
        int dx = (int) ((l1.getX() - l2.getX())*10);
        int dy = (int) (((l1.getY()+yOffset) - l2.getY())*10);
        int dz = (int) ((l1.getZ() - l2.getZ())*10);

        return (dx*dx)+(dy*dy)+(dz*dz);
    }

    /***
     * Find ground coordinates in case the player is in the air
     * @param current Player's current coordinates
     * @return Minimum coordinates with ground
     */
    public static Location GetMinLocationY(Location current) {
        int y = current.getBlockY();
        for(int n=y;n>=-64;n--) {
            current.setY(n);
            Material type = current.getBlock().getType();

            if(type != Material.AIR && (!current.getBlock().isPassable()|| current.getBlock().getType() == Material.LAVA)) {
                return current;
            }
        }
        current.setY(y);
        return current;
    }

    public static boolean GiveDamageToNearEntity(Location l, float round, Player starter, boolean isGun, int id){
        Collection<Entity> entitys = l.getNearbyEntities(round,1.62f,round);
        for(Entity e : entitys) {

            if(e instanceof LivingEntity) {
                if(starter.getEntityId() == e.getEntityId()) continue;

                double diff = l.getY()-e.getLocation().getY();

                if(isGun) {
                    // 총과 칼 충돌
                    if (IsKnifeCollision(e)) {
                        e.remove();
                        return true;
                    }
                } else {
                    // 칼과 칼 출동
                    if(IsKnifeX2Collision(e,id)) {
                        e.remove();
                        return true;
                    }
                }

                if(diff > 2.2) continue;
                if(diff < -0.2) continue;
                ((LivingEntity) e).damage(10, starter);
                e.setLastDamageCause(new EntityDamageEvent(starter, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 10));
            }
        }
        return false;
    }

    public static void MapRandomTeleport(Player p, MurderMap m) {
        Location l = m.SpawnPositions.get(Murder.Instance.Random.nextInt(m.SpawnPositions.size())).ToLocation();
        Location yawAndPitch = p.getLocation();

        l.setYaw(yawAndPitch.getYaw());
        l.setPitch(yawAndPitch.getPitch());

        p.teleport(l);
    }

    public static void RemoveAllEffect(Player p) {
        for (PotionEffect effect : p.getActivePotionEffects())
            p.removePotionEffect(effect.getType());
    }

    public static boolean IsKnifeX2Collision(Entity e, int owner){
        List<MetadataValue> datas = e.getMetadata("owner");
        if(datas.isEmpty()) return false;
        return datas.get(0).asInt() != owner;
    }

    public static boolean IsKnifeCollision(Entity e){
        if(e.getCustomName() == null) return false;
        return e.getCustomName().equalsIgnoreCase("murder1");
    }

    public static void KillArmorStand(){
        Collection<ArmorStand> entities = Murder.Instance.MainWorld.getEntitiesByClass(ArmorStand.class);
        for(ArmorStand a: entities) {
            if(a.getCustomName()==null) continue;

            if(a.getCustomName().equalsIgnoreCase("murder2"))
                a.remove();

            if(a.getCustomName().equalsIgnoreCase("murder1"))
                a.remove();

            if(a.getCustomName().equalsIgnoreCase("murder"))
                a.remove();

            if(a.getCustomName().equalsIgnoreCase("murder_trap"))
                a.remove();
        }
    }

    public static ItemStack ItemWithName(String name, String des, Material m, int amount) {
        ItemStack item = new ItemStack(m, amount);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(Component.text(ChatColor.BOLD+ name));
        itemMeta.lore(Collections.singletonList(Component.text(ChatColor.RESET+des)));
        item.setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack ItemWithName(String name, String des, Material m) {
        return ItemWithName(name, des, m, 1);
    }


}
