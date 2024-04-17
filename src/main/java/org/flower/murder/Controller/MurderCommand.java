package org.flower.murder.Controller;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.flower.murder.Enums.GameState;
import org.flower.murder.Enums.WinCause;
import org.flower.murder.Map.MapSetting;
import org.flower.murder.Map.MurderMap;
import org.flower.murder.MiniVector;
import org.flower.murder.MurderSetting;
import org.flower.murder.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

public class MurderCommand implements TabExecutor, CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!commandSender.isOp()) {
            Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "권한이 없습니다.");
            return false;
        }

        if (ArgEqual(strings, "강제시작", 0))
            MurderStart(strings, commandSender, true);

        if (ArgEqual(strings, "시작", 0))
            MurderStart(strings, commandSender, false);

        if (ArgEqual(strings, "맵", 0)) {
            if (ArgEqual(strings, "추가", 1)) {
                AddMap(strings, commandSender);
            }

            if (ArgEqual(strings, "스폰", 1)) {
                AddMapSpawns(strings, commandSender);
            }

            if (ArgEqual(strings, "삭제", 1)) {
                RemoveMap(strings, commandSender);
            }
        }

        if (ArgEqual(strings, "설정", 0)) {
            SetSetting(strings, commandSender);
        }

        if (ArgEqual(strings, "강종", 0)) {
            ForceStopGame(strings, commandSender);
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (ArgEqual(strings, "시작", 0) || ArgEqual(strings, "강제시작", 0)) {
            if (strings.length == 2)
                return MapSetting.GetMapNames();
            return null;
        }



        if (ArgEqual(strings, "설정", 0)) {
            ArrayList<String> l = new ArrayList<>();
            if (strings.length == 2) {
                Field[] fs = MurderSetting.class.getFields();
                for (Field f : fs)
                    l.add(f.getName());

                return l;
            }
            if (strings.length == 3) {
                try {
                    Field f = MurderSetting.class.getField(strings[1].trim());
                    if (f.getGenericType() == boolean.class)
                        return Arrays.asList("true", "false");
                } catch (NoSuchFieldException e) {
                    return null;
                }

                return l;
            }
            return null;
        }

        if (ArgEqual(strings, "맵", 0)) {
            Player p = (Player) commandSender;
            if (ArgEqual(strings, "스폰", 1)) {
                if (strings.length == 4)
                    return Collections.singletonList(String.format("%.2f", p.getLocation().getX()));
                if (strings.length == 5)
                    return Collections.singletonList(String.format("%.2f", p.getLocation().getY()));
                if (strings.length == 6)
                    return Collections.singletonList(String.format("%.2f", p.getLocation().getZ()));
                if (strings.length > 6)
                    return null;
                return MapSetting.GetMapNamesWithLobby();
            }

            if (ArgEqual(strings, "삭제", 1)) {
                if (strings.length > 3)
                    return null;
                return MapSetting.GetMapNamesWithLobby();
            }

            if (ArgEqual(strings, "추가", 1)) {
                return null;
            }

            return Arrays.asList("추가", "스폰", "삭제");
        }

        if (strings.length == 1 || strings.length == 0) {
            return Arrays.asList("강제시작","시작", "맵", "설정", "강종");
        }
        return null;
    }

    private void Send(CommandSender s, String msg) {
        s.sendMessage(msg);
        if (!(s instanceof Player))
            Bukkit.broadcast(Component.text(msg));
    }


    private boolean ArgEqual(String[] strings, String s, int argIndex) {
        if (strings.length <= argIndex)
            return false;
        return strings[argIndex].equalsIgnoreCase(s);
    }

    private void SetSetting(String[] strings, CommandSender commandSender) {
        if (strings.length >= 2) {
            try {
                String fieldName = strings[1].trim();
                Field field = MurderSetting.class.getField(fieldName);

                if (strings.length >= 3) {
                    String v = strings[2].trim();
                    String prev = field.get(null).toString();
                    try {
                        Utils.SetFieldValueAuto(field, v);

                        MurderSetting.Save();
                        Send(commandSender, ChatColor.GREEN + "" + ChatColor.BOLD + "설정이 변경되었습니다. ( " + prev + " -> " + v + " )");

                    } catch (Exception e) {
                        Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "잘못된 설정 값입니다.");
                    }
                } else {
                    String prev = field.get(null).toString();
                    Send(commandSender, ChatColor.GREEN + "" + ChatColor.BOLD + "현재 값: " + prev);
                }

            } catch (Exception e) {
                System.out.println(e);
                Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "설정 이름을 찾을 수 없습니다.");
            }

        } else {
            Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "설정 이름이 비어있습니다.");
        }
    }

    private void ForceStopGame(String[] strings, CommandSender commandSender) {
        if(!GameManager.IsStart()) {
            Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "게임이 시작하지 않았습니다.");
            return;
        }
       GameManager.LastGame.Win(WinCause.DRAW);
    }
    private void MurderStart(String[] strings, CommandSender commandSender, boolean force) {
        if(!force) {
            if(GameManager.LastGame != null) {
                if(GameManager.LastGame.State == GameState.START || GameManager.LastGame.State == GameState.READY) {
                    Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "이미 게임이 진행 중 입니다.");
                    return;
                }
            }
        }

        if (strings.length >= 2) {
            String mapName = strings[1].trim();
            GameState state = new GameManager().Start(mapName);
            GameManager.LastGame.State = state;

            if(state != GameState.READY)
                showStateMessage(state, commandSender);
        }

        if (strings.length == 1) {
            if (MapSetting.Maps.size() <= 1) {
                Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "맵이 하나도 없어 시작할 수 없습니다.");
                return;
            }

            ArrayList<String> maps = MapSetting.GetMapNames();
            int rand = new Random().nextInt(maps.size());
            GameState state = new GameManager().Start(maps.get(rand));
            GameManager.LastGame.State = state;

            if(state != GameState.READY)
                showStateMessage(state, commandSender);
        }
    }

    private void showStateMessage(GameState state, CommandSender commandSender) {
        if(state == GameState.NO_LOBBY_SPAWN)
            Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "로비 스폰 설정이 되어있지 않습니다.");
        if(state == GameState.CANT_FIND_MAP)
            Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "맵을 찾을 수 없습니다.");
        if(state == GameState.NOT_ENOUGH_POSITION)
            Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "맵에 스폰장소가 부족합니다.");
        if(state == GameState.NOT_ENOUGH_PLAYER)
            Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "플레이어 수가 부족합니다.");


    }

    private void AddMap(String[] strings, CommandSender commandSender) {
        if (strings.length >= 3) {
            if (MapSetting.GetMapNamesWithLobby().contains(strings[2].trim())) {
                Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "이미 같은 이름의 맵이 있습니다.");
                return;
            }
            Send(commandSender, ChatColor.GREEN + "" + ChatColor.BOLD + strings[2].trim() + "맵이 추가되었습니다.");
            MurderMap m = new MurderMap(strings[2].trim());
            MapSetting.Maps.put(m.Name, m);
            MapSetting.SaveMap(m);
        } else
            Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "맵 이름이 비어있습니다.");
    }

    private void RemoveMap(String[] strings, CommandSender commandSender) {
        if (strings.length >= 3) {
            MurderMap m = MapSetting.Maps.get(strings[2].trim());
            if (m == null) {
                Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "맵을 찾을 수 없습니다.");
            } else {
                if(m.Name.equalsIgnoreCase("로비")) {
                    Send(commandSender, ChatColor.GREEN + "" + ChatColor.BOLD + "로비 스폰이 초기화되었습니다.");
                    m.SpawnPositions.clear();
                    MapSetting.SaveMap(m);
                    return;
                }
                Send(commandSender, ChatColor.GREEN + "" + ChatColor.BOLD + strings[2].trim() + "맵이 삭제되었습니다.");
                MapSetting.RemoveMap(m);
            }
        } else
            Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "맵 이름이 비어있습니다.");
    }

    private void AddMapSpawns(String[] strings, CommandSender commandSender) {
        if (strings.length >= 3) {
            MurderMap m = MapSetting.Maps.get(strings[2].trim());
            if (m == null) {
                Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "맵을 찾을 수 없습니다.");
            } else {
                try {
                    if (strings.length == 3 && commandSender instanceof Player) {
                        Player p = (Player)commandSender;
                        String x = String.format("%.2f", p.getLocation().getX());
                        String y = String.format("%.2f", p.getLocation().getY());
                        String z = String.format("%.2f", p.getLocation().getZ());

                        MiniVector v = new MiniVector(Float.parseFloat(x), Float.parseFloat(y), Float.parseFloat(z));
                        m.SpawnPositions.add(v);
                        MapSetting.SaveMap(m);
                        Send(commandSender, ChatColor.GREEN + "" + ChatColor.BOLD + strings[2].trim() + "에 스폰 좌표 " + v.ToBeautyString() + "가 추가되었습니다.");

                    } else if (strings.length >= 6) {

                        MiniVector v = new MiniVector(Float.parseFloat(strings[3]), Float.parseFloat(strings[4]), Float.parseFloat(strings[5]));
                        m.SpawnPositions.add(v);
                        MapSetting.SaveMap(m);
                        Send(commandSender, ChatColor.GREEN + "" + ChatColor.BOLD + strings[2].trim() + "에 스폰 좌표 " + v.ToBeautyString() + "가 추가되었습니다.");


                    } else {
                        Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "좌표가 없습니다.");
                    }
                } catch (Exception e) {
                    Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "잘못된 좌표입니다.");
                }
            }
        } else
            Send(commandSender, ChatColor.RED + "" + ChatColor.BOLD + "맵 이름이 비어있습니다.");
    }
}