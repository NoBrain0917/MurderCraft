package org.flower.murder.Map;

import org.flower.murder.MiniVector;

import java.util.ArrayList;

public class MurderMap {
    public String Name;
    public ArrayList<MiniVector> SpawnPositions = new ArrayList<>();

    public MurderMap(String n) {
        Name = n;
    }
}
