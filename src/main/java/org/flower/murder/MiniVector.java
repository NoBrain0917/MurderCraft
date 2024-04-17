package org.flower.murder;

import org.bukkit.Location;

public class MiniVector {
    public float x;
    public float y;
    public float z;

    public MiniVector(float a, float b, float c) {
        x = a;
        y = b;
        z = c;
    }

    public Location ToLocation() {
        return new Location(Murder.Instance.MainWorld, x, y, z);
    }

    public String ToString() {
        return x + "," + y + "," + z;
    }

    public String ToBeautyString() {
        return x + ", " + y + ", " + z;
    }

    public static MiniVector FromString(String s) {
        String[] strings = s.split(",");
        return new MiniVector(Float.parseFloat(strings[0]), Float.parseFloat(strings[1]), Float.parseFloat(strings[2]));
    }


}
