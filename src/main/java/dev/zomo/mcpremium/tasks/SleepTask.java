package dev.zomo.mcpremium.tasks;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import dev.zomo.mcpremium.MCP;

public class SleepTask extends BukkitRunnable {

    World world = null;
    int tick = 0;
    long initialTime = 0;
    long timeDifference = 0;

    int ticks = 0;

    public SleepTask(World setWorld, int setTicks) {
        world = setWorld;
        ticks = setTicks;
        initialTime = world.getFullTime();
        
        timeDifference = 24000 - (initialTime%24000);

        MCP.debug("SleepTask", "Time til morning: " + timeDifference);

        if (world.hasStorm() || world.isThundering()) {
            
            long stormEnd = initialTime + world.getWeatherDuration();
            MCP.debug("SleepTask", "(is storming) Time til storm end: " + world.getWeatherDuration());

            if (stormEnd % 24000 < 18000) {
                MCP.debug("SleepTask", "(is storming) Storm ends during the day (" + stormEnd % 24000 + ")");
                timeDifference = world.getWeatherDuration();
            }

        }
    }

    @Override
    public void run() {
        if (tick < ticks) {
            double percent = tick / (ticks * 1.0);
            if (percent < 0.5)
                percent = 4*percent*percent*percent;
            else
                percent = (1-percent)*(2*percent-2)*(2*percent-2)+1;
            world.setTime(initialTime + Math.round(timeDifference*percent));
        } else {
            world.setTime(initialTime + timeDifference);
            world.setThundering(false);
            world.setStorm(false);
            this.cancel();
        }
        tick++;
    }

}
