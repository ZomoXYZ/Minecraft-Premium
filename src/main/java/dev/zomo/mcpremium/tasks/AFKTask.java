package dev.zomo.mcpremium.tasks;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import dev.zomo.mcpremium.MCPAfk;

public class AFKTask extends BukkitRunnable {

    Player player = null;

    public AFKTask(Player setPlayer) {
        player = setPlayer;
    }

    @Override
    public void run() {
        MCPAfk.addAfk(player);
    }

}
