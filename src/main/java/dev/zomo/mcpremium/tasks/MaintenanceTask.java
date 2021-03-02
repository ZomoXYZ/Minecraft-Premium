package dev.zomo.mcpremium.tasks;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import dev.zomo.MCLang.LangTemplate;
import dev.zomo.mcpremium.MCP;
import dev.zomo.mcpremium.MCPMaintenance;

public class MaintenanceTask extends BukkitRunnable {

    static int time = 0;
    static int interval = 0;

    static int curTick = 0;
    static int timeSinceInterval = 0;

    public MaintenanceTask(int settime, int setinterval) {
        time = settime/20;
        if (setinterval == 0) {
            if (settime > MCPMaintenance.TimeLengths.get("d"))
                interval = MCPMaintenance.TimeLengths.get("d") / 20;
            else if (settime > MCPMaintenance.TimeLengths.get("h"))
                interval = MCPMaintenance.TimeLengths.get("h") / 20;
            else if (settime > MCPMaintenance.TimeLengths.get("m"))
                interval = MCPMaintenance.TimeLengths.get("m") / 20;
            else
                interval = 30;
        } else
            interval = setinterval/20;

        interval = Math.min(interval, time/2);
    }

    public static void sendMessage(Player sendTo) {
        LangTemplate template = new LangTemplate().add("time", MCPMaintenance.tickToTime((time - curTick) * 20));

        String title = LangTemplate.escapeColors(MCP.lang.string("commands.maintenance.title", template));
        String subtitle = LangTemplate.escapeColors(MCP.lang.string("commands.maintenance.subtitle", template));

        if (sendTo == null) {
            for (Player player : MCP.plugin.getServer().getOnlinePlayers())
                player.sendTitle(title, subtitle, 5, 80, 20);

            MCP.log(MCP.stripColors(title + " " + subtitle));
        } else
            sendTo.sendTitle(title, subtitle, 5, 80, 20);
    }

    public static void sendMessage() {
        sendMessage(null);
    }

    public static void clear() {
        time = 0;
        interval = 0;
        curTick = 0;
        timeSinceInterval = 0;
    }

    @Override
    public void run() {

        if (time > 0) {

            if (curTick == time)
                MCP.server.shutdown();
            else if (timeSinceInterval == interval || timeSinceInterval == 0) {
                timeSinceInterval = 0;

                if (time - curTick <= interval)
                    interval/=2;

                sendMessage();

            }

            curTick++;
            timeSinceInterval++;

        }

    }

}
