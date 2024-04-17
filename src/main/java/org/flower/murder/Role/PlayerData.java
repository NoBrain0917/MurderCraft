package org.flower.murder.Role;

import org.bukkit.entity.Player;
import org.flower.murder.Enums.KillCause;

import java.util.Random;

public class PlayerData {
    public PlayerRole Role = PlayerRole.CITIZEN;

    public Player Player;
    public Player Killer;
    public boolean IsDeath;
    public boolean CitizenHasBow;
    public KillCause LastKillCause = KillCause.NONE;
    public RoleSkill Skill = RoleSkill.None;

    public static int[] murderSkillIndexs = new int[]{1, 2, 3, 4, 5, 11,12,13 };
    public static int[] detectiveSkillIndexs = new int[]{1, 2,6,7,13};
    public static int[] citizenSkillIndexs = new int[]{1,8,9, 10,13};

    public PlayerData(Player p, PlayerRole r){
        Player = p;
        Role  = r;
    }

}
