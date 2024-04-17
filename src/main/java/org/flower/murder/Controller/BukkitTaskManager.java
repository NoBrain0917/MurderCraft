package org.flower.murder.Controller;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class BukkitTaskManager {
    private static final ArrayList<BukkitRunnable> Tasks = new ArrayList<>();

    public static void AddTask(BukkitRunnable task) {
        Tasks.add(task);
    }

    public static void RemoveTask(BukkitRunnable task) {
        Tasks.remove(task);
    }

    public static void KillAll() {
        for(BukkitRunnable task : Tasks) {
            task.cancel();
        }
        Tasks.clear();
    }

}
