package org.flower.murder.Controller;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.flower.murder.Controller.Packet.InvisiblePlayer;
import org.flower.murder.Controller.Packet.Spectator;
import org.flower.murder.Enums.GameState;
import org.flower.murder.Enums.KillCause;
import org.flower.murder.Enums.WinCause;
import org.flower.murder.Map.MapSetting;
import org.flower.murder.Map.MurderMap;
import org.flower.murder.Murder;
import org.flower.murder.MurderSetting;
import org.flower.murder.Role.*;
import org.flower.murder.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GameManager {


    public PlayerHashMap PlayerMap = new PlayerHashMap();
    public GameState State = GameState.NONE;

    public int MaxMurdererCount;
    public int MaxDetectiveCount;
    public int MurdererDeathCount;
    public int OtherDeathCount;

    public BukkitRunnable Timer;
    public BossBar TimerBar;
    public MurderMap CurrentMap;
    public Collection<PlayerData> AllPlayers;
    public List<Trapper> InstalledTraps = new ArrayList<>();

    public ArmorStand DetectiveBowArmorstand;

    public static GameManager LastGame;

    private final static int COUNTDOWN_SEC = 10;


    /***
     * Check game is start
     * @return if it was started
     */
    public static boolean IsStart() {
        if (GameManager.LastGame == null) return false;
        return GameManager.LastGame.State == GameState.READY || GameManager.LastGame.State == GameState.START;
    }


    /***
     * Ignore the existing game and start a new one
     * @param mapName Map name
     * @return Running state after game startup
     */
    public GameState Start(String mapName){

        if(LastGame != null) {
            if(LastGame.TimerBar != null) {
                LastGame.TimerBar.removeAll();
                LastGame.Timer.cancel();
            }

            if(LastGame.DetectiveBowArmorstand != null) {
                if(!LastGame.DetectiveBowArmorstand.isDead())
                    LastGame.DetectiveBowArmorstand.remove();
            }
        }


        LastGame = this;
        InstalledTraps.clear();

        if(Bukkit.getOnlinePlayers().size() < 2)
            return GameState.NOT_ENOUGH_PLAYER;

        if(MapSetting.Lobby == null)
            return GameState.NO_LOBBY_SPAWN;

        if(MapSetting.Lobby.SpawnPositions.isEmpty())
            return GameState.NO_LOBBY_SPAWN;

        MurderMap m = MapSetting.Maps.get(mapName);
        if(m == null)
            return GameState.CANT_FIND_MAP;

        if(m.Name.equalsIgnoreCase("로비"))
            return GameState.CANT_FIND_MAP;

        if(m.SpawnPositions.isEmpty())
            return GameState.NOT_ENOUGH_POSITION;

        clearChat();

        CurrentMap = m;

        Utils.KillArmorStand();
        BukkitTaskManager.KillAll();

        /*
        if(Murder.Instance.hasSkinRestore)
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "sr setskinall steve classic");*/

        for(Player p : Bukkit.getOnlinePlayers()) {
            if(Spectator.HasPlayer(p))
                Spectator.DisableSpectator(p);

            Murder.Instance.HideName(p);
            Utils.MapRandomTeleport(p, m);

            p.getInventory().clear();
            p.resetCooldown();
            p.setHealth(20);
            p.setGameMode(GameMode.ADVENTURE);
            p.setFoodLevel(20);

            Utils.RemoveAllEffect(p);
        }



        Utils.SendAllTitle("",ChatColor.BOLD+"맵: "+m.Name,0,40,10);
        BukkitRunnable t = new BukkitRunnable() {
            @Override
            public void run() {
                selectRole();
                AllPlayers = PlayerMap.Values();
                showYourRole();
                countdown();
                BukkitTaskManager.RemoveTask(this);
            }
        };
        t.runTaskLater(Murder.Instance, 20*3);
        BukkitTaskManager.AddTask(t);

        return GameState.READY;
    }


    /***
     * Show your role as title
     */
    private void showYourRole(){
        for(PlayerData p : AllPlayers) {
            switch (p.Role) {
                case MURDERER:
                    p.Player.sendTitle("",ChatColor.BOLD+"당신은 "+ChatColor.RESET+ChatColor.RED+ChatColor.BOLD+"살인마"+ ChatColor.RESET+ChatColor.BOLD+"입니다",10,60,20);
                    p.Player.playSound(p.Player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 2);
                    break;
                case DETECTIVE:
                    p.Player.sendTitle("",ChatColor.BOLD+"당신은 "+ChatColor.RESET+ChatColor.AQUA+ChatColor.BOLD+"경찰"+ ChatColor.RESET+ChatColor.BOLD+"입니다",10,60,20);
                    p.Player.playSound(p.Player.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1, 1.5f);
                    break;
                default:
                    p.Player.sendTitle("",ChatColor.BOLD+"당신은 "+ChatColor.RESET+ChatColor.GREEN+ChatColor.BOLD+"시민"+ ChatColor.RESET+ChatColor.BOLD+"입니다",10,60,20);
                    p.Player.playSound(p.Player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 2, 2);
                    break;
            }
        }
    }


    /***
     * Show who won
     * @param cause Why we won
     */
    public void Win(WinCause cause){
        State = GameState.END;

        Utils.AllPlaySound(Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f ,2);


        switch (cause) {
            case MUDERER_WIN:
                Utils.SendAllTitle(ChatColor.RED +""+ChatColor.BOLD+"살인마 승리",ChatColor.BOLD+"모든 사람이 죽었습니다",0,60,20);
                break;

            case CITIZEN_WIN:
                Utils.SendAllTitle(ChatColor.GREEN +""+ChatColor.BOLD+"시민 승리",ChatColor.BOLD+"살인마가 죽었습니다",0,60,20);
                break;

            case TIME_OUT:
                Utils.SendAllTitle(ChatColor.GREEN +""+ChatColor.BOLD+"시민 승리",ChatColor.BOLD+"시간이 모두 지났습니다",0,60,20);
                break;

            case BEST_MURDERER:
            case LAST_ONE:
                PlayerData alone = null;
                for(PlayerData pd : AllPlayers) {
                    if(!pd.IsDeath) {
                        alone = pd;
                        break;
                    }
                }
                if(alone == null) {
                    System.out.println("wtf");
                    return;
                }

                if(cause == WinCause.BEST_MURDERER)
                    Utils.SendAllTitle(ChatColor.RED +""+ChatColor.BOLD+"최고의 살인마",ChatColor.BOLD+alone.Player.getName()+" 승리",0,60,20);
                if(cause == WinCause.LAST_ONE)
                    Utils.SendAllTitle(ChatColor.YELLOW +""+ChatColor.BOLD+"최후의 1인",ChatColor.BOLD+alone.Player.getName()+" 승리",0,60,20);
                break;


            case DRAW:
                Utils.SendAllTitle(ChatColor.GRAY +""+ChatColor.BOLD+"무승부",ChatColor.BOLD+"아무일도 일어나지 않았습니다",0,60,20);
                break;

        }

        GameEnd(cause);
    }


    /***
     * Determine who won
     * @return Whether it's okay to make them spectators
     */
    private boolean update(){
        if(!IsStart()) return true;

        int AllPlayerCount = AllPlayers.size();

        if(AllPlayerCount==1 && MurdererDeathCount+OtherDeathCount == 1) {
            Win(WinCause.DRAW);
            return false;
        }

        if(MaxMurdererCount == AllPlayerCount) {
            if(AllPlayerCount-1 == MurdererDeathCount) {
                Win(WinCause.BEST_MURDERER);
                return false;
            }
        } else {

            if (MaxMurdererCount == 0) {
                if (AllPlayerCount - 1 == OtherDeathCount + MurdererDeathCount) {
                    Win(WinCause.LAST_ONE);
                    return false;
                }
            } else {
                if (MurdererDeathCount >= MaxMurdererCount) {
                    Win(WinCause.CITIZEN_WIN);
                    return false;
                }
                if (AllPlayerCount - MaxMurdererCount <= OtherDeathCount) {
                    Win(WinCause.MUDERER_WIN);
                    return false;
                }
            }
        }

        return true;
    }


    /***
     * Sort by name
     * @param players Player Data Array
     * @return Sorted player names
     */
    private String getNames(ArrayList<PlayerData> players){
        StringBuilder result = new StringBuilder();
        if(players.isEmpty()) return "";

        for(PlayerData p : players) {
            result.append(", ");
            result.append(p.Player.getName());
        }
        return result.substring(2);
    }


    /***
     * Sort by name with color
     * @param players Player Data Array
     * @return Sorted player names with color
     */
    private String getNamesWithColor(ArrayList<PlayerData> players){
        StringBuilder result = new StringBuilder();
        if(players.isEmpty()) return "";

        for(PlayerData p : players) {
            result.append(", ");
            result.append(ChatColor.RESET);

            if(p.Role == PlayerRole.MURDERER)
                result.append(ChatColor.RED);
            if(p.Role == PlayerRole.DETECTIVE)
                result.append(ChatColor.AQUA);
            if(p.Role == PlayerRole.CITIZEN)
                result.append(ChatColor.GREEN);


            result.append(ChatColor.BOLD);
            result.append(p.Player.getName());
            result.append(ChatColor.RESET);
            result.append(ChatColor.BOLD);
        }
        return result.substring(2);
    }


    /***
     * Display game results in a newspaper-like format
     * @param cause Why we won
     */
    private void showResultMessageLikeNewspaper(WinCause cause){
        ArrayList<PlayerData> murderers = new ArrayList<>();
        ArrayList<PlayerData> detectives = new ArrayList<>();
        ArrayList<PlayerData> Shooter = new ArrayList<>();
        PlayerData Alone = null;

        boolean isKillerAllDetective = true;
        boolean noMurderer = MaxMurdererCount == 0;

        for(PlayerData pd : AllPlayers) {
            if(!pd.IsDeath)
                Alone = pd;

            if(pd.Role == PlayerRole.MURDERER) {
                murderers.add(pd);
                if(pd.Killer != null) {
                    PlayerRole killerRole = PlayerMap.GetPlayer(pd.Killer).Role;
                    if(Shooter.contains(PlayerMap.GetPlayer(pd.Killer))) continue;

                    if(killerRole!=PlayerRole.MURDERER)
                        Shooter.add(PlayerMap.GetPlayer(pd.Killer));

                    if(isKillerAllDetective) {
                        if (killerRole == PlayerRole.CITIZEN || killerRole == PlayerRole.MURDERER)
                            isKillerAllDetective = false;
                    }
                }
            }

            if(pd.Role == PlayerRole.DETECTIVE)
                detectives.add(pd);
        }

        clearChat();

        Bukkit.broadcastMessage(ChatColor.BOLD+"---------< XX 뉴스 >---------");

        if(OtherDeathCount+MurdererDeathCount==0)
            Bukkit.broadcastMessage(ChatColor.BOLD + "속보입니다. " + ChatColor.YELLOW + ChatColor.BOLD + CurrentMap.Name + ChatColor.RESET + ChatColor.BOLD + "에서 칼부림 예고가 일어났습니다.");
        else
            Bukkit.broadcastMessage(ChatColor.BOLD+"속보입니다. "+ChatColor.YELLOW+ChatColor.BOLD+CurrentMap.Name +ChatColor.RESET+ChatColor.BOLD+"에서 살인사건이 일어났습니다.");

        if(!noMurderer) {
            if(OtherDeathCount+MurdererDeathCount==0)
                Bukkit.broadcastMessage(ChatColor.BOLD+"수사 결과 작성자는 "+ChatColor.RED+ChatColor.BOLD+getNames(murderers)+ChatColor.RESET+ChatColor.BOLD+"로 추정됩니다.");
            else
                Bukkit.broadcastMessage(ChatColor.BOLD+"수사 결과 살인마는 "+ChatColor.RED+ChatColor.BOLD+getNames(murderers)+ChatColor.RESET+ChatColor.BOLD+"로 추정됩니다.");
        }

        if(cause==WinCause.MUDERER_WIN) {
            if(!detectives.isEmpty())
                Bukkit.broadcastMessage(ChatColor.BOLD + "현장에 있던 경찰 " + ChatColor.AQUA + ChatColor.BOLD + getNames(detectives) + ChatColor.RESET + ChatColor.BOLD + "과 " + ChatColor.GREEN + ChatColor.BOLD + "시민" + ChatColor.RESET + ChatColor.BOLD + "들 모두 끔찍한 시체로 발견되었습니다.");
            else
                Bukkit.broadcastMessage(ChatColor.BOLD + "현장에 있던 "+ ChatColor.GREEN + ChatColor.BOLD + "시민" + ChatColor.RESET + ChatColor.BOLD + "들이 모두 끔찍한 시체로 발견되었습니다.");

        }
        if(cause == WinCause.CITIZEN_WIN) {
            if(!Shooter.isEmpty())
                Bukkit.broadcastMessage(ChatColor.BOLD + "현장에 있던 " + getNamesWithColor(Shooter) + ChatColor.RESET + ChatColor.BOLD + "가 살인마를 모두 제압했습니다.");
            else
                Bukkit.broadcastMessage(ChatColor.BOLD + "살인마의 추적을 할려했지만 실패하였습니다.");
        }


        if(cause == WinCause.TIME_OUT) {
            if(!detectives.isEmpty())
                Bukkit.broadcastMessage(ChatColor.BOLD+"현장엔 경찰 "+ChatColor.AQUA+ChatColor.BOLD+getNames(detectives)+ChatColor.RESET+ChatColor.BOLD+"과 "+ChatColor.GREEN+ChatColor.BOLD+"시민"+ChatColor.RESET+ChatColor.BOLD+"들이 있었습니다.");
            else
                Bukkit.broadcastMessage(ChatColor.BOLD+"현장엔 "+ChatColor.GREEN+ChatColor.BOLD+"시민"+ChatColor.RESET+ChatColor.BOLD+"들이 있었습니다.");


            if(OtherDeathCount+MurdererDeathCount==0)
                Bukkit.broadcastMessage(ChatColor.BOLD+"그리고 아무일도 일어나지 않았습니다.");
            else
                Bukkit.broadcastMessage(ChatColor.BOLD+"그리고 몇개의 시체들이 발견되었습니다.");
        }

        if(cause == WinCause.BEST_MURDERER) {
            Bukkit.broadcastMessage(ChatColor.BOLD + "살인마 자기들끼리 싸우다 결국 " + ChatColor.RED + ChatColor.BOLD + (Alone != null ? Alone.Player.getName() : "?") + ChatColor.RESET + ChatColor.BOLD + "만이 살아남았습니다.");
        }

        if(cause == WinCause.LAST_ONE) {
            if (Alone != null && Alone.Role == PlayerRole.DETECTIVE)
                Bukkit.broadcastMessage(ChatColor.BOLD + "시민들이 갑자기 이유없이 자기들끼리 싸웠으며 결국 " + ChatColor.AQUA + ChatColor.BOLD + Alone.Player.getName() +ChatColor.RESET+ChatColor.BOLD+ "만이 살아남았습니다.");
            else
                Bukkit.broadcastMessage(ChatColor.BOLD + "시민들이 갑자기 이유없이 자기들끼리 싸웠으며 결국 " + ChatColor.GREEN + ChatColor.BOLD + (Alone != null ? Alone.Player.getName() : "?") +ChatColor.RESET+ChatColor.BOLD+ "만이 살아남았습니다.");
        }

        if(cause == WinCause.DRAW) {
            if(!detectives.isEmpty())
                Bukkit.broadcastMessage(ChatColor.BOLD+"현장엔 경찰 "+ChatColor.AQUA+ChatColor.BOLD+getNames(detectives)+ChatColor.RESET+ChatColor.BOLD+"과 "+ChatColor.GREEN+ChatColor.BOLD+"시민"+ChatColor.RESET+ChatColor.BOLD+"들이 있었습니다.");
            else
                Bukkit.broadcastMessage(ChatColor.BOLD+"현장엔 "+ChatColor.GREEN+ChatColor.BOLD+"시민"+ChatColor.RESET+ChatColor.BOLD+"들이 있었습니다.");

            Bukkit.broadcastMessage(ChatColor.BOLD+"그리고 아무일도 일어나지 않았습니다.");
        }



        Bukkit.broadcastMessage(ChatColor.BOLD+"---------------------------------");
        //Bukkit.broadcastMessage(ChatColor.BOLD+"수사 결과 살인자는 "+ChatColor.RED+ChatColor.BOLD+getNames(murderers)+ChatColor.RESET+ChatColor.BOLD+"로 추정되며");

    }


    /***
     * Reset everything after the game
     * @param cause Why we won
     */
    public void GameEnd(WinCause cause){

        BukkitTaskManager.KillAll();
        Utils.KillArmorStand();
        InvisiblePlayer.Reset();

        if(Timer != null) {
            Timer.cancel();
            TimerBar.removeAll();
        }

        for(Player p : Bukkit.getOnlinePlayers()) {
            if (Spectator.HasPlayer(p))
                Spectator.DisableSpectator(p);

            Utils.MapRandomTeleport(p, MapSetting.Lobby);

            p.setGameMode(GameMode.ADVENTURE);
            p.resetCooldown();
            p.getInventory().clear();

            Utils.RemoveAllEffect(p);
            Murder.Instance.ShowName(p);

        }

        if(DetectiveBowArmorstand != null) {
            if(!DetectiveBowArmorstand.isDead())
                DetectiveBowArmorstand.remove();

            DetectiveBowArmorstand = null;
        }

        InstalledTraps.clear();

        /*
        if(Murder.Instance.hasSkinRestore) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "sr setskinall steve classic");
        }*/

        showResultMessageLikeNewspaper(cause);

        LastGame = null;
    }


    /***
     * Using an armor stand to drop a gun
     * @param pd Player Data
     */
    private void spawnGunArmorStand(PlayerData pd){

        Location l = Utils.GetMinLocationY(pd.Player.getLocation());
        l.setY(l.getY()+1);

        DetectiveBowArmorstand = pd.Player.getWorld().spawn(l, ArmorStand.class);
        DetectiveBowArmorstand.setInvisible(true);
        DetectiveBowArmorstand.customName(Component.text("murder2"));
        DetectiveBowArmorstand.setMarker(true);
        DetectiveBowArmorstand.setArms(true);
        DetectiveBowArmorstand.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION,1, true,false));
        DetectiveBowArmorstand.setItemInHand(new ItemStack(Material.NETHERITE_HOE));

        Bukkit.broadcastMessage(ChatColor.GREEN+""+ChatColor.BOLD+"총이 맵 어딘가에 떨궈졌습니다.");

    }


    /***
     * Show the purpose of winning for each role
     * @param p Player Data
     */
    private void showYourObject(PlayerData p) {
        switch (p.Role) {
            case MURDERER:
                p.Player.sendMessage(ChatColor.BOLD + "목표: 모든 사람을 죽이세요.");
                break;
            case DETECTIVE:
                p.Player.sendMessage(ChatColor.BOLD + "목표: 살인마를 찾아 죽이세요 다만 선량한 시민을 죽일시 천벌을 받습니다.");
                break;
            default:
                p.Player.sendMessage(ChatColor.BOLD + "목표: 최대한 오랫동안 살아남으세요.");
                break;
        }
    }


    /***
     * Handling events when a player dies
     * @param p Dead Players
     * @param killer Killed players
     * @param cause Reason for death
     */
    public void PlayerKilled(Player p, Player killer, KillCause cause){
        if(!IsStart()) return;
        PlayerData pd = PlayerMap.GetPlayer(p);

        if(pd.Skill == RoleSkill.ARMOR && killer != null) {
            PlayerData kd = PlayerMap.GetPlayer(killer);
            if(kd.Role == PlayerRole.MURDERER)
                kd.Player.setCooldown(Material.IRON_SWORD,3*20);
            if(kd.Role == PlayerRole.DETECTIVE)
                kd.Player.setCooldown(Material.NETHERITE_HOE,3*20);
            if(kd.Role == PlayerRole.CITIZEN && kd.CitizenHasBow)
                kd.Player.setCooldown(Material.NETHERITE_HOE,3*20);
            
            kd.Player.sendMessage(ChatColor.BOLD+"상대의 낡은 갑옷이 당신의 공격을 한번 막았습니다.");
            pd.Player.sendMessage(ChatColor.BOLD+"낡은 갑옷이 상대의 공격을 한번 막았습니다.");

            p.spawnParticle(Particle.BLOCK_CRACK, p.getLocation(), 100, 0.5, 1, 0.5, 0, Material.STONE.createBlockData());
            killer.spawnParticle(Particle.BLOCK_CRACK, p.getLocation(), 100, 0.5, 1, 0.5, 0, Material.STONE.createBlockData());

            p.playSound(p.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 0f);
            killer.playSound(p.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 0f);

            p.setNoDamageTicks(3*20);

            pd.Skill = RoleSkill.None;

            
            return;
        }

        if(MaxMurdererCount !=0) {
            if ((pd.Role != PlayerRole.MURDERER && killer != null) ) {
                PlayerData kd = PlayerMap.GetPlayer(killer);
                if (kd.Role == PlayerRole.DETECTIVE || (kd.Role == PlayerRole.CITIZEN && kd.CitizenHasBow)) {
                    if(kd.Skill == RoleSkill.Citizen_SPY) {
                        kd.Player.sendMessage(ChatColor.BOLD+"시민을 쐈지만 알고보니 살인마의 스파이였습니다.");
                        kd.Skill = RoleSkill.None;
                    } else {
                        PlayerKilled(killer, null, KillCause.GODS_JUDGE);
                    }
                }
            }
        }

        SpawnDeadBody(p);
        Utils.AllPlaySound(Sound.ENTITY_PLAYER_ATTACK_CRIT,1,2);
        ShowDeathTitle(p, cause);
        p.getInventory().clear();


        if(pd.Role == PlayerRole.DETECTIVE)
            spawnGunArmorStand(pd);

        if(pd.Role == PlayerRole.CITIZEN && pd.CitizenHasBow)
            spawnGunArmorStand(pd);


        if(pd.Role == PlayerRole.MURDERER)
            MurdererDeathCount++;
        else
            OtherDeathCount++;

        pd.LastKillCause = cause;
        pd.IsDeath = true;
        pd.Killer = killer;

        if(update()) {
            Spectator.EnableSpectator(p);
        }
    }


    /***
     * 6Show dead players why they died
     * @param p Player
     * @param cause Reason for death
     */
    public void ShowDeathTitle(Player p, KillCause cause){
        String youDie = ChatColor.RED + "" + ChatColor.BOLD+"죽었습니다";
        switch (cause) {
            case THROWING_KNIFE:
                p.sendTitle(youDie, ChatColor.BOLD+"살인마가 던진 칼에 의해 죽었습니다", 10,60,20);
                break;
            case NORMAL_KNIFE:
                p.sendTitle(youDie, ChatColor.BOLD+"살인마가 당신을 죽였습니다", 10,60,20);
                break;
            case SHOT_GUN:
                p.sendTitle(youDie, ChatColor.BOLD+"경찰이 당신을 쐈습니다", 10,60,20);
                break;
            case SUICIDE:
                p.sendTitle(youDie, ChatColor.BOLD+"당신의 뻘짓으로 인해 죽었습니다", 10,60,20);
                break;
            case GODS_JUDGE:
                p.sendTitle(youDie, ChatColor.BOLD+"선량한 시민을 죽였습니다", 10,40,20);
                break;
        }
    }




    /***
     * Grants items that can be used for skills
     * @param pd Player Data
     */
    private void giveSkillItem(PlayerData pd) {
        if (pd.Skill == RoleSkill.FakeGun) {
            pd.Player.getInventory().setItem(2, Utils.ItemWithName("가짜 권총", "진짜랑 똑같지만 사용할 수 없는 가짜 권총.", Material.NETHERITE_HOE));
        }

        if (pd.Skill == RoleSkill.TOYGUN) {
            pd.Player.getInventory().setItem(2, Utils.ItemWithName("장난감 총", "맞추면 아파요.", Material.STONE_HOE, 3));
        }

        if (pd.Skill == RoleSkill.LIE_DETECTION || pd.Skill == RoleSkill.Glowing || pd.Skill == RoleSkill.INVISIBLE || pd.Skill == RoleSkill.FastSpeed || pd.Skill == RoleSkill.CAMOUFLAGE || pd.Skill == RoleSkill.TRAP) {
            pd.Player.getInventory().setItem(2, Utils.ItemWithName("특수능력","우클릭시 특수능력을 사용할 수 있습니다.", Material.GOLD_NUGGET));
        }
    }

    /***
     * Skill Description
     * @param pd Player Data
     */
    private void showSkillDescription(PlayerData pd) {
        if(pd.Skill!=RoleSkill.None)
            pd.Player.sendMessage(ChatColor.BOLD+"---------------------");

        switch(pd.Skill) {
            case Glowing:
                pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 발광\n모든 사람을 3초 동안 발광 상태로 만듭니다.");
                break;
            case INVISIBLE:
                pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 투명화\n5초 동안 투명으로 변합니다.");
                break;
            case FastSpeed:
                pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 재빠른 발\n5초 동안 빠르게 이동할 수 있습니다.");
                break;
            case FastReload:
                pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 빠른 장전\n회수, 장전을 더 빠르게 할 수 있습니다.");
                break;
            case ARMOR:
                pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 낡은 갑옷\n총알과 칼을 1회 막아줍니다.");
                break;
            case LIE_DETECTION:
                pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 거짓말 탐지기\n주변 사람을 심문합니다.");
                break;
            case FakeGun:
                pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 가짜 총\n쏠 수는 없지만 진짜랑 똑같은 총이 주어집니다.");
                break;
            case FastKnife:
                pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 빠른 칼\n칼이 더 빠르게 날라갑니다.");
                break;
            case Citizen_SPY:
                pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 알빠노\n실수로 시민을 쏘더라도 1번은 심판을 받지 않습니다.");
                break;
            case TOYGUN:
                pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 장난감 총\n딱 3번 쏠 수 있는 장난감 총입니다. 총의 맞은 사람은 실명과 구속이 걸립니다.");
                break;
            case VETERAN:
                pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 베테랑\n경찰의 거짓말 탐지기에 걸리지 않습니다.");
                break;
            case CAMOUFLAGE:
                /*
                if(Murder.Instance.hasSkinRestore)
                    pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 위장의 달인\n20초 동안 모든 사람의 스킨이 똑같은 스킨으로 바뀝니다.");
                else*/
                    pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 위장의 달인\n20초 동안 자신의 모습을 변장시킵니다.");
                break;
            case TRAP:
                pd.Player.sendMessage(ChatColor.BOLD+"특수능력: 트랩 설치기사\n밟으면 심한 구속이 걸리는 트랩을 설치합니다. 자신을 제외한 모든 플레이어가 밟을 수 있습니다.");
                break;
        }
    }


    /***
     * Role-specific item grants
     * @param pd Player Data
     */
    private void giveRoleItem(PlayerData pd) {
        if(pd.Role == PlayerRole.MURDERER) {
            pd.Player.getInventory().setHeldItemSlot(0);
            pd.Player.getInventory().setItem(1, Utils.ItemWithName("날카로운 칼","좌클릭으로 공격할 수 있고 우클릭으로 칼을 던질 수 있습니다.", Material.IRON_SWORD));

        }

        if(pd.Role == PlayerRole.DETECTIVE) {
            pd.Player.getInventory().setHeldItemSlot(0);
            pd.Player.getInventory().setItem(1, Utils.ItemWithName("권총","우클릭시 약간의 지연시간과 함께 총을 발사합니다.", Material.NETHERITE_HOE));

        }
    }


    /***
     * Organize your chats
     */
    private void clearChat(){
        for(int i=0; i < 100; i ++)
        {
            Bukkit.broadcast(Component.text(""));
        }
    }


    /***
     * Start the real game after the countdown
     */
    private void countdown(){
        BukkitRunnable t = new BukkitRunnable() {
            int sec = COUNTDOWN_SEC+1;

            public void run() {
                if (sec == 0) {
                    cancel();
                    Bukkit.broadcast(Component.text(ChatColor.YELLOW+""+ChatColor.BOLD + "게임 시작!"));
                    Utils.AllPlaySound(Sound.ENTITY_PLAYER_LEVELUP, 1,1);
                    onCountdownEnd();
                    BukkitTaskManager.RemoveTask(this);
                    return;
                }

                if(sec <= 10) {
                    Bukkit.broadcast(Component.text(ChatColor.YELLOW + "" + ChatColor.BOLD + sec + "초 뒤 게임이 시작됩니다."));
                    Utils.AllPlaySound(Sound.BLOCK_NOTE_BLOCK_HAT,1,1);
                }


                sec--;
            }
        };
        t.runTaskTimer(Murder.Instance, 0, 20);
        BukkitTaskManager.AddTask(t);
    }

    /***
     * Start playing for real
     */
    private void onCountdownEnd(){
        State = GameState.START;

        if(MurderSetting.TotalTime < 30)
            MurderSetting.TotalTime = 30;

        TimerBar = Bukkit.createBossBar(
                ChatColor.BOLD+String.format("남은 시간 %02d:%02d", (MurderSetting.TotalTime / 60) % 60, MurderSetting.TotalTime % 60),
                BarColor.GREEN,
                BarStyle.SOLID);

        for (PlayerData pd : AllPlayers) {

            if(pd.IsDeath) continue;

            showYourObject(pd);
            giveSkillItem(pd);
            giveRoleItem(pd);
            showSkillDescription(pd);

            pd.Player.resetCooldown();

            TimerBar.addPlayer(pd.Player);
        }


        Timer = new BukkitRunnable() {
            int second = MurderSetting.TotalTime;
            @Override
            public void run() {
                second--;
                TimerBar.setProgress(((double)second / (double)MurderSetting.TotalTime));
                TimerBar.setTitle(ChatColor.BOLD+String.format("남은 시간 %02d:%02d", (second / 60) % 60, second % 60));

                if(second == 0) {
                    cancel();
                    Win(WinCause.TIME_OUT);
                    return;
                }

                if(second == 120) {
                    for(Player pl : Bukkit.getOnlinePlayers())
                        pl.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 3*20, 1, true, false));

                    Bukkit.broadcast(Component.text(ChatColor.BOLD+"모두에게 발광이 주어집니다."));
                    Bukkit.broadcast(Component.text(ChatColor.BOLD+"다음 발광: 1분 뒤"));


                }

                if(second == 60) {
                    for(Player pl : Bukkit.getOnlinePlayers())
                        pl.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 3*20, 1, true, false));

                    Bukkit.broadcast(Component.text(ChatColor.BOLD+"모두에게 발광이 주어집니다."));
                }
            }
        };
        Timer.runTaskTimer(Murder.Instance,0,20);
    }


    /***
     * Randomly grant skills to players
     * @param p Player Data
     */
    private void addRandomSkill(PlayerData p) {
        int rand = Murder.Instance.Random.nextInt(100);
        if(rand > MurderSetting.SkillPercentage-1) return;

        RoleSkill[] skills = RoleSkill.values();
        if(p.Role == PlayerRole.MURDERER) {
            p.Skill = skills[PlayerData.murderSkillIndexs[Murder.Instance.Random.nextInt(PlayerData.murderSkillIndexs.length)]];
        }
        if(p.Role == PlayerRole.DETECTIVE) {
            p.Skill = skills[PlayerData.detectiveSkillIndexs[Murder.Instance.Random.nextInt(PlayerData.detectiveSkillIndexs.length)]];

        }
        if(p.Role == PlayerRole.CITIZEN) {
            p.Skill = skills[PlayerData.citizenSkillIndexs[Murder.Instance.Random.nextInt(PlayerData.citizenSkillIndexs.length)]];
        }

    }


    /***
     * Randomly select roles
     */
    private void selectRole(){
        ArrayList<Player> p = new ArrayList<>();

        for(Player pl : Bukkit.getOnlinePlayers()){
            if(pl.getGameMode() == GameMode.SPECTATOR) continue;
            p.add(pl);
        }

        for(int n=0;n< MurderSetting.MurdererCount;n++) {
            if (p.isEmpty()) return;

            Player murder = p.get(Murder.Instance.Random.nextInt(p.size()));
            p.remove(murder);
            PlayerData data = new PlayerData(murder, PlayerRole.MURDERER);
            PlayerMap.SetPlayer(murder, data);
            addRandomSkill(data);

            MaxMurdererCount++;
        }

        for(int n=0;n< MurderSetting.DetectiveCount;n++) {
            if (p.isEmpty()) return;

            Player detective = p.get(Murder.Instance.Random.nextInt(p.size()));
            p.remove(detective);
            PlayerData data2 = new PlayerData(detective, PlayerRole.DETECTIVE);
            PlayerMap.SetPlayer(detective, data2);
            addRandomSkill(data2);

            MaxDetectiveCount++;
        }


        if(p.isEmpty()) return;

        for(Player pl : p) {
            PlayerData data3 = new PlayerData(pl, PlayerRole.CITIZEN);
            PlayerMap.SetPlayer(pl, data3);
            addRandomSkill(data3);
        }

    }


    /***
     * Summoning a corpse using an armor stand
     * @param p Player
     */
    public void SpawnDeadBody(Player p){

        Location l = Utils.GetMinLocationY(p.getLocation());
        l.setY(l.getY()-0.4f);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(p.getName());
        head.setItemMeta(meta);


        ArmorStand armorStand = Murder.Instance.MainWorld.spawn(l, ArmorStand.class);
        armorStand.setGravity(true);
        armorStand.setInvisible(true);
        armorStand.customName(Component.text("murder"));
        armorStand.setMarker(true);

        armorStand.setHelmet(head);
    }



}
