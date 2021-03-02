package dev.zomo.mcpremium;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import dev.zomo.MCLang.LangTemplate;
import dev.zomo.mcpremium.tasks.MaintenanceTask;

public class MCPMaintenance {

    private static final String[] TIMETYPES = {"s", "m", "h", "d"};
    private static final int[] TIMELENGTHS = {20, 1200, 72000, 1728000};

    public static final HashMap<String, Integer> TimeLengths = new HashMap<String, Integer>();

    private static boolean running = false;

    private static BukkitTask task = null;

    public static void enable() {
        for (int i = 0; i < TIMETYPES.length && i < TIMELENGTHS.length; i++)
            TimeLengths.put(TIMETYPES[i], TIMELENGTHS[i]);
    }

    public static boolean isRunning() {
        return running;
    }
    
    public static int timeToTick(String time) {

        String timeType = String.valueOf(time.charAt(time.length() - 1));

        //ArrayList<String> timeArr = new ArrayList<String>(Arrays.asList(time.split("")));

        int timeMultiplier = TimeLengths.get("s");

        if (TimeLengths.containsKey(timeType))
            timeMultiplier = TimeLengths.get(timeType);

        int tickCount = 0;

        for (String ch : Arrays.asList(time.split(""))) {
            try {
                tickCount = Integer.parseInt(ch) + (tickCount * 10);
            } catch (NumberFormatException ex) {}
        }

        return tickCount*timeMultiplier;
    }

    public static String tickToTime(int tick) {

        int days = tick / TimeLengths.get("d");
        tick %= TimeLengths.get("d");
        int hours = tick / TimeLengths.get("h");
        tick %= TimeLengths.get("h");
        int minutes = tick / TimeLengths.get("m");
        tick %= TimeLengths.get("m");
        int seconds = tick / TimeLengths.get("s");

        String time = "";

        if (days > 0)
            time += days + "d ";
        if (hours > 0)
            time += hours + "h ";
        if (minutes > 0)
            time += minutes + "m ";
        time += seconds + "s";

        return time.trim();
    }

    public static void sendCommandMessage(CommandSender sender) {
        LangTemplate template = new LangTemplate()
            .add("running", isRunning())
            .add("command", isRunning() ? "/maintenance c" : "/maintenance y <time> [<interval>]");
        sender.sendMessage(LangTemplate.escapeColors(MCP.lang.string("commands.maintenance.caution", template)));
    }

    public static boolean begin(int time, int interval) {

        if (isRunning() == true)
            return false;
        
        running = true;

        task = new MaintenanceTask(time, interval).runTaskTimer(MCP.plugin, 0, 20);

        return true;
    }

    public static boolean cancel() {

        if (task == null || task.isCancelled())
            return false;
        
        running = false;
        task.cancel();
        MaintenanceTask.clear();

        return true;
    }

}
