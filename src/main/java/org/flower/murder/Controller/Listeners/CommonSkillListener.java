package org.flower.murder.Controller.Listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.flower.murder.Controller.BukkitTaskManager;
import org.flower.murder.Controller.GameManager;
import org.flower.murder.Controller.Packet.InvisiblePlayer;
import org.flower.murder.Enums.GameState;
import org.flower.murder.Murder;
import org.flower.murder.Role.PlayerData;
import org.flower.murder.Role.PlayerRole;
import org.flower.murder.Role.RoleSkill;
import org.flower.murder.Role.Trapper;
import org.flower.murder.Utils;

import java.util.ArrayList;
import java.util.Collection;

public class CommonSkillListener implements Listener {

    private Player cancelMovePlayer1,cancelMovePlayer2;


    @EventHandler
    public void CancelJumping(PlayerJumpEvent event) {
        Player p = event.getPlayer();
        if(p == cancelMovePlayer1)
            event.setCancelled(true);
        if(p == cancelMovePlayer2)
            event.setCancelled(true);
    }

    @EventHandler
    public void OnTrapStep(PlayerMoveEvent event) {
        if(!GameManager.IsStart()) return;

        Player p = event.getPlayer();
        PlayerData pd = GameManager.LastGame.PlayerMap.GetPlayer(p);

        if(pd == null) return;
        if(pd.IsDeath) return;

        for(Trapper trap : GameManager.LastGame.InstalledTraps) {
            if(!trap.activated) continue;
            if(Utils.FastDistance(trap.trapPosition.getLocation(), p.getLocation(), 1.9f) <= 225) { // <- 1.5^2 * 10^2 = 255
                if (!trap.owner.getUniqueId().toString().equalsIgnoreCase(p.getUniqueId().toString()))
                    trap.GiveTrapDamage(p);
            }

        }
    }

    @EventHandler
    public void OnSpecficItemUse(PlayerInteractEvent event) {
        if(!GameManager.IsStart())return;

        Player p = event.getPlayer();

        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (p.getInventory().getItemInMainHand().getType() != Material.GOLD_NUGGET) return;

        if (p.hasCooldown(Material.GOLD_NUGGET)) return;
        if(GameManager.LastGame.State != GameState.START) return;

        PlayerData pd = GameManager.LastGame.PlayerMap.GetPlayer(p);
        if(pd == null) return;

        if(pd.Skill == RoleSkill.FastSpeed)
            fastSpeed(p);
        if(pd.Skill == RoleSkill.Glowing)
            allGlowing(p);
        if(pd.Skill == RoleSkill.INVISIBLE)
            invisible(p);
        if(pd.Skill == RoleSkill.LIE_DETECTION)
            interrogate(p);
        if(pd.Skill == RoleSkill.CAMOUFLAGE)
            camouflage(p);
        if(pd.Skill == RoleSkill.TRAP)
            summonTrap(p);
    }

    private void summonTrap(Player p) {
        if(GameManager.LastGame == null) return;

        Trapper lastInstalled = null;
        for(Trapper t : GameManager.LastGame.InstalledTraps) {
            if(t.owner.getUniqueId().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
                lastInstalled = t;
                break;
            }
        }

        if(lastInstalled != null ) {
            lastInstalled.Destroy();
            GameManager.LastGame.InstalledTraps.remove(lastInstalled);
        }

        Trapper newTrap = new Trapper(p);
        newTrap.Spawn(p.getLocation());

        GameManager.LastGame.InstalledTraps.add(newTrap);

        waitAndCooldownReset(p, 30);
    }


    private void camouflage(Player p){

        /*
        if(Murder.Instance.hasSkinRestore) {
            ArrayList<Player> list = new ArrayList<>(Bukkit.getOnlinePlayers());
                Player randomPlayer = list.get(Murder.Instance.Random.nextInt(list.size()));
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "sr setskinall "+randomPlayer.getName()+" classic");

                p.getInventory().remove(Material.GOLD_NUGGET);

            BukkitRunnable t = new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "sr setskinall steve classic");
                }
            };
            t.runTaskLater(Murder.Instance,20*20);
            BukkitTaskManager.AddTask(t);

            BukkitRunnable t2 = new BukkitRunnable() {
                @Override
                public void run() {
                    p.getInventory().setItem(2, Utils.ItemWithName("특수능력","우클릭시 특수능력을 사용할 수 있습니다.", Material.GOLD_NUGGET));
                }
            };
            t2.runTaskLater(Murder.Instance,20*40);
            BukkitTaskManager.AddTask(t2);
        } else {*/
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 20, 1, true, false));

            ItemStack[] armor = new ItemStack[4];
            armor[0] = new ItemStack(Material.LEATHER_BOOTS, 1);
            armor[1] = new ItemStack(Material.LEATHER_LEGGINGS, 1);
            armor[2] = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
            armor[3] = new ItemStack(Material.PLAYER_HEAD, 1);

            p.getInventory().setArmorContents(armor);

            BukkitRunnable t = new BukkitRunnable() {
                @Override
                public void run() {
                    p.getInventory().setArmorContents(new ItemStack[4]);
                }
            };
            t.runTaskLater(Murder.Instance, 20 * 20);
            BukkitTaskManager.AddTask(t);

            waitAndCooldownReset(p, 40);
        //}



    }

    private void allGlowing(Player p){

        Bukkit.broadcast(Component.text(ChatColor.BOLD+"살인마가 발광 능력을 썼습니다! 모두 조심하세요."));

        for(Player pl : Bukkit.getOnlinePlayers()) {
           // if(p.getEntityId() == pl.getEntityId()) continue;
            pl.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 3*20, 1, true, false));
        }
        waitAndCooldownReset(p, 20);
    }

    private void invisible(Player p){
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 5*20, 1, true, false));

        InvisiblePlayer.PlayerLastSlot.put(p.getUniqueId().toString(), 2);
        PacketContainer fakeSlot = new PacketContainer(PacketType.Play.Client.HELD_ITEM_SLOT);
        fakeSlot.getIntegers().write(0,0);
        Murder.Instance.Protocol.receiveClientPacket(p, fakeSlot);

        BukkitRunnable t = new BukkitRunnable() {
            @Override
            public void run() {
                Integer lastSlot = InvisiblePlayer.PlayerLastSlot.get(p.getUniqueId().toString());

                if(lastSlot == null) return;
                PacketContainer fakeSlot = new PacketContainer(PacketType.Play.Client.HELD_ITEM_SLOT);
                fakeSlot.getIntegers().write(0, lastSlot);

                InvisiblePlayer.PlayerLastSlot.remove(p.getUniqueId().toString());
                Murder.Instance.Protocol.receiveClientPacket(p, fakeSlot);

                BukkitTaskManager.RemoveTask(this);
            }
        };
        t.runTaskLaterAsynchronously(Murder.Instance,20*20);
        BukkitTaskManager.AddTask(t);

        waitAndCooldownReset(p, 20);
    }



    private void fastSpeed(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5*20, 0, true, false));
        waitAndCooldownReset(p, 15);
    }

    private void interrogate(Player p) {
        Player nearPlayer = getNearPlayer(p, 2);
        if(nearPlayer == null) {
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            p.sendMessage(Component.text(ChatColor.RED+""+ChatColor.BOLD+"주변에 사람이 없습니다."));
            return;
        }

        waitAndCooldownReset(p, 50);

        cancelMovePlayer1 = p;
        cancelMovePlayer2 = nearPlayer;

        nearPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, PotionEffect.INFINITE_DURATION, 255, true,false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, PotionEffect.INFINITE_DURATION, 255, true,false));

        PlayerData p1 = GameManager.LastGame.PlayerMap.GetPlayer(p);
        PlayerData p2 = GameManager.LastGame.PlayerMap.GetPlayer(nearPlayer);

        BukkitRunnable t = new BukkitRunnable() {
            int c = 0;
            @Override
            public void run() {
                if(p1.IsDeath || p2.IsDeath || !GameManager.IsStart()) {
                    p.sendMessage(ChatColor.BOLD+"취소되었습니다.");

                    cancelMovePlayer1 = null;
                    cancelMovePlayer2 = null;

                    nearPlayer.removePotionEffect(PotionEffectType.SLOW);
                    p.removePotionEffect(PotionEffectType.SLOW);

                    cancel();
                    return;
                }


                if(c==1)
                    chatMessage(p, nearPlayer, ChatColor.BOLD+p.getName()+": 잠시만요. "+nearPlayer.getName()+"씨");
                if(c==2)
                    chatMessage(p, nearPlayer, ChatColor.BOLD+nearPlayer.getName()+": 네?");
                if(c==3)
                    chatMessage(p, nearPlayer, ChatColor.BOLD+p.getName()+": 혹시 당신, 선량한 시민이 맞습니까?");
                if(c==4)
                    chatMessage(p, nearPlayer, ChatColor.BOLD+nearPlayer.getName()+": 당연하죠. 저는 사람을 죽이는 살인마가 아니에요");

                if(c==5) {
                    if(GameManager.LastGame.PlayerMap.GetPlayer(nearPlayer).Role == PlayerRole.MURDERER && GameManager.LastGame.PlayerMap.GetPlayer(nearPlayer).Skill != RoleSkill.VETERAN)
                        p.sendMessage(ChatColor.GREEN+""+ChatColor.BOLD+"( 삐삐삑!! )");
                    else
                        p.sendMessage(ChatColor.GREEN+""+ChatColor.BOLD+"( 거짓말 탐지기의 반응이 없다 )");
                }

                if(c==6) {
                    cancel();

                    cancelMovePlayer1 = null;
                    cancelMovePlayer2 = null;

                    nearPlayer.removePotionEffect(PotionEffectType.SLOW);
                    p.removePotionEffect(PotionEffectType.SLOW);

                }
                c++;
            }
        };
        t.runTaskTimer(Murder.Instance, 0, 40);
        //BukkitTaskManager.AddTask(t);

    }

    private void chatMessage(Player p, Player p2, String msg) {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
        p.sendMessage(Component.text(msg));

        p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
        p2.sendMessage(Component.text(msg));

    }


    private Player getNearPlayer(Player p, double r) {
        Collection<Entity> entities = p.getNearbyEntities(r,r,r);
        Location loc = p.getLocation();

        double maxDistance = 100;
        Entity nearPlayer = null;

        for(Entity entity : entities) {
            if(entity instanceof Player) {
                if(entity.equals(p)) continue;
                double distance = entity.getLocation().distance(loc);
                if(distance < maxDistance) {
                    maxDistance = distance;
                    nearPlayer = entity;
                }
            }
        }

        return (Player)nearPlayer;
    }


    private void waitAndCooldownReset(Player p, float delay){
        p.setCooldown(Material.GOLD_NUGGET, (int)(delay*20));
    }
}



