package org.flower.murder.Controller.Listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.flower.murder.Controller.BukkitTaskManager;
import org.flower.murder.Controller.GameManager;
import org.flower.murder.Controller.Packet.Spectator;
import org.flower.murder.Enums.GameState;
import org.flower.murder.Enums.KillCause;
import org.flower.murder.Enums.WinCause;
import org.flower.murder.Map.MapSetting;
import org.flower.murder.Map.MurderMap;
import org.flower.murder.Murder;
import org.flower.murder.MurderSetting;
import org.flower.murder.Role.PlayerData;
import org.flower.murder.Role.PlayerRole;
import org.flower.murder.Utils;

import java.util.List;
import java.util.Objects;

public class GameControlListener implements Listener {

    @EventHandler
    public void OnPlayerDamage(EntityDamageEvent event){
        if(!MurderSetting.AllowEntityDamageInLobby) {
            event.setCancelled(true);
            return;
        }

        if(!GameManager.IsStart()) return;
        if(!(event.getEntity() instanceof Player)) return;

        event.setCancelled(true);
    }


    @EventHandler
    public void TakeDropedGun(PlayerMoveEvent event) {
        if(!GameManager.IsStart()) return;

        Player p = event.getPlayer();
        PlayerData pd = GameManager.LastGame.PlayerMap.GetPlayer(p);

        if(pd == null) return;
        if(pd.IsDeath) return;
        if(pd.Role != PlayerRole.CITIZEN) return;
        if(pd.CitizenHasBow) return;

        if(GameManager.LastGame != null) {
            if(GameManager.LastGame.DetectiveBowArmorstand != null) {
                ArmorStand a = GameManager.LastGame.DetectiveBowArmorstand;
                if(Utils.FastDistance(a.getLocation(), p.getLocation()) <= 100) { // <-- 1*1 = 1 -> 1*10^2 = 100
                    //if (Objects.requireNonNull(a.getCustomName()).equalsIgnoreCase("murder2")) {
                    if (a.isDead()) return;

                    a.remove();
                    pd.CitizenHasBow = true;
                    pd.Player.getInventory().setItem(1, Utils.ItemWithName("권총", "우클릭시 약간의 지연시간과 함께 총을 발사합니다.", Material.NETHERITE_HOE));
                    pd.Player.getInventory().setHeldItemSlot(0);

                    GameManager.LastGame.DetectiveBowArmorstand = null;

                    Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "누군가 경찰의 의지를 이었습니다.");
                    //}
                }
            }
        }

        /*
        List<Entity> armorStandCollection = p.getNearbyEntities(1,1,1);
        for(Entity e : armorStandCollection) {
            if(e instanceof ArmorStand) {
                if(e.isDead()) continue;

                ArmorStand a = (ArmorStand) e;
                 if(Objects.requireNonNull(a.getCustomName()).equalsIgnoreCase("murder2")) {

                     a.remove();
                     pd.CitizenHasBow = true;
                     pd.Player.getInventory().setItem(1, Utils.ItemWithName("권총","우클릭시 약간의 지연시간과 함께 총을 발사합니다.", Material.NETHERITE_HOE));
                     pd.Player.getInventory().setHeldItemSlot(0);

                     Bukkit.broadcastMessage(ChatColor.GREEN+""+ChatColor.BOLD+"누군가 경찰의 의지를 이었습니다.");

                 }

            }
        }*/

    }

    @EventHandler
    public void DeadlyBlockDetect(PlayerMoveEvent event) {
        if(!GameManager.IsStart()) return;
        //if(GameManager.LastGame.State != GameState.START) return;

        Player p = event.getPlayer();
        PlayerData pd = GameManager.LastGame.PlayerMap.GetPlayer(p);

        if(pd == null) return;
        if(pd.IsDeath) return;

        Location nowLocation = p.getLocation();
        nowLocation.setY(nowLocation.getY()-2);

        Block underBlock = nowLocation.getBlock();
        Block nowBlock = p.getLocation().getBlock();

        if(underBlock.getType() == Material.RED_GLAZED_TERRACOTTA)
            GameManager.LastGame.PlayerKilled(p, null, KillCause.SUICIDE);
        else if(nowBlock.getType() == Material.LAVA)
            GameManager.LastGame.PlayerKilled(p, null, KillCause.SUICIDE);
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void OnPlayerKilled(EntityDamageByEntityEvent event){
        if(!GameManager.IsStart()) return;

        Entity vic = event.getEntity();
        Entity atk = event.getDamager();

        if((vic instanceof Player) && (atk instanceof Player)) {
            Player vicPlayer = (Player)vic;
            Player atkPlayer = (Player)atk;

            PlayerData vicData = GameManager.LastGame.PlayerMap.GetPlayer(vicPlayer);

            if(vicData==null) return;
            if(vicData.IsDeath) return;

            PlayerData atkData = GameManager.LastGame.PlayerMap.GetPlayer(atkPlayer);
            if(atkData == null) return;
            if(atkData.IsDeath) return;

            boolean isThrow = event.getDamage() == 10;
            boolean isHand = atkPlayer.getInventory().getItemInMainHand().getType() == Material.IRON_SWORD;


            if(atkData.Role == PlayerRole.MURDERER) {
                if(isThrow)
                    GameManager.LastGame.PlayerKilled(vicPlayer, atkPlayer, KillCause.THROWING_KNIFE);
                else if(isHand && !atkPlayer.hasCooldown(Material.IRON_SWORD))
                    GameManager.LastGame.PlayerKilled(vicPlayer, atkPlayer, KillCause.NORMAL_KNIFE);

            }

            if(atkData.Role == PlayerRole.DETECTIVE && isThrow)
                GameManager.LastGame.PlayerKilled(vicPlayer, atkPlayer, KillCause.SHOT_GUN);

            if(atkData.Role == PlayerRole.CITIZEN && isThrow && atkData.CitizenHasBow)
                GameManager.LastGame.PlayerKilled(vicPlayer, atkPlayer, KillCause.SHOT_GUN);
        }
    }


    @EventHandler
    public void CancelInteractBlock(PlayerInteractEvent event){
        if(!GameManager.IsStart()) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block Block = event.getClickedBlock();

            if(Block == null) return;

            if(Block.getType().toString().contains("CHEST"))
                event.setCancelled(true);

            if(Block.getType().toString().contains("SHULKER_BOX"))
                event.setCancelled(true);

            if(Block.getBlockData() instanceof ShulkerBox)
                event.setCancelled(true);

            if(Block.getBlockData() instanceof TrapDoor)
                event.setCancelled(true);

        }
    }

    @EventHandler
    public void OnJoinAtGameRunning(PlayerJoinEvent event){
        Player p = event.getPlayer();

        if(!GameManager.IsStart()) {
            if(MapSetting.Lobby!=null) {
                if(!MapSetting.Lobby.SpawnPositions.isEmpty()) {
                    Utils.MapRandomTeleport(p,MapSetting.Lobby);
                    p.setGameMode(GameMode.ADVENTURE);
                    return;
                }
            }
            return;
        }

        if(GameManager.LastGame.State == GameState.START)
            GameManager.LastGame.TimerBar.addPlayer(p);

        boolean gamingButQuit = GameManager.LastGame.PlayerMap.GetPlayer(p)!=null;

        MurderMap m = GameManager.LastGame.CurrentMap;
        Utils.MapRandomTeleport(p, m);
        p.setGameMode(GameMode.ADVENTURE);

        BukkitRunnable t = new BukkitRunnable() {
            @Override
            public void run() {
                if(!Spectator.HasPlayer(p))
                    Spectator.EnableSpectator(p);

                if(gamingButQuit) {
                    if(GameManager.LastGame.PlayerMap.GetPlayer(p).LastKillCause == KillCause.NONE)
                        GameManager.LastGame.ShowDeathTitle(p, KillCause.SUICIDE);
                    else
                        GameManager.LastGame.ShowDeathTitle(p, GameManager.LastGame.PlayerMap.GetPlayer(p).LastKillCause);
                }

                Utils.RemoveAllEffect(p);

                BukkitTaskManager.RemoveTask(this);

            }
        };
        t.runTask(Murder.Instance);
        BukkitTaskManager.AddTask(t);


    }

    @EventHandler
    public void OnLeaveAtGameRunning(PlayerQuitEvent event){
        if(!GameManager.IsStart()) return;

        Player p = event.getPlayer();

        if(GameManager.LastGame.PlayerMap.GetPlayer(p)==null) {
            if(Spectator.HasPlayer(p))
                Spectator.DisableSpectator(p);

            p.setGameMode(GameMode.ADVENTURE);

            int playerCount = 0;
            for(Player pl : Bukkit.getOnlinePlayers()) {
                if(pl.getEntityId() == p.getEntityId()) continue;
                if(pl.getGameMode() == GameMode.ADVENTURE)
                    playerCount++;
            }


            if(playerCount <= 1)
                GameManager.LastGame.Win(WinCause.DRAW);
            return;
        }


        if(!GameManager.LastGame.PlayerMap.GetPlayer(p).IsDeath)
            GameManager.LastGame.PlayerKilled(p,null,KillCause.SUICIDE);

        if(Spectator.HasPlayer(p))
            Spectator.DisableSpectator(p);


        Utils.RemoveAllEffect(p);

        p.setGameMode(GameMode.ADVENTURE);

    }

    @EventHandler
    public void LobbyRandomSpawn(PlayerRespawnEvent event){
        if(GameManager.IsStart()) return;

        Player p = event.getPlayer();

        if(MapSetting.Lobby!=null) {
            if(!MapSetting.Lobby.SpawnPositions.isEmpty()) {
                Location l = MapSetting.Lobby.SpawnPositions.get(Murder.Instance.Random.nextInt(MapSetting.Lobby.SpawnPositions.size())).ToLocation();
                event.setRespawnLocation(l);
                p.setGameMode(GameMode.ADVENTURE);
            }
        }
    }

    @EventHandler
    public void CancelSwapItem(InventoryClickEvent event){
        if(!GameManager.IsStart()) return;

        event.setCancelled(true);
    }


    @EventHandler
    public void CancelDropItem(PlayerDropItemEvent event){
        if(!GameManager.IsStart()) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void CancelArmorStandInteraction(PlayerArmorStandManipulateEvent event){
        if(!GameManager.IsStart()) return;

        event.setCancelled(true);
    }


    @EventHandler
    public void CancelSpectatorChat(AsyncChatEvent event) {
        if(!GameManager.IsStart()) return;

        PlayerData pd = GameManager.LastGame.PlayerMap.GetPlayer(event.getPlayer());
        if(pd == null) return;

        if(pd.IsDeath)
            event.setCancelled(true);
    }

    @EventHandler
    public void CancelItemFrameInteraction(PlayerItemFrameChangeEvent event){
        if(!GameManager.IsStart()) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void CancelOtherInteraction(HangingBreakEvent event){
        if(!GameManager.IsStart()) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void CancelSwapItem(PlayerSwapHandItemsEvent event) {
        if(!GameManager.IsStart())return;

        event.setCancelled(true);
    }

    @EventHandler
    public void CancelHunger(FoodLevelChangeEvent event) {
        if(event.getFoodLevel() < 20)
            event.setCancelled(true);
    }
}
