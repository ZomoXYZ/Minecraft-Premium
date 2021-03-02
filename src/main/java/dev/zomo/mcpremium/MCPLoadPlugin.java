package dev.zomo.mcpremium;

import java.io.File;
import java.util.HashMap;

import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MCPLoadPlugin {

    private static boolean disabled = true;

    private static HashMap<String, Plugin> plugins = new HashMap<String, Plugin>();

    public static PluginManager manager = null;

    public static void enable() {
        disabled = false;
        manager = MCP.server.getPluginManager();
    }
    public static void disable() {
        disabled = true;
        plugins.clear();
        manager = null;
    }

    public static String load(String pluginName) {

        if (plugins.containsKey(pluginName))
            return "plugin already loaded";

        //File pluginFile = new File(MCP.plugin.getDataFolder().getParent(), "plugins/" + pluginName + ".jar");
        File pluginFile = new File(MCP.plugin.getDataFolder().getParent(), pluginName + ".jar");

        if (!pluginFile.exists())
            return "File doesnt exist";

        try {
            Plugin loadPlugin = manager.loadPlugin(pluginFile);
            manager.enablePlugin(loadPlugin);
            plugins.put(pluginName, loadPlugin);
            return "";
        } catch (InvalidPluginException ex) {
            return "Invalid plugin";
        } catch (InvalidDescriptionException ex) {
            return "Invalid plugin description";
        }

    }

    public static String unload(String pluginName) {

        if (!plugins.containsKey(pluginName))
            return "plugin not loaded";

        manager.disablePlugin(plugins.get(pluginName));
        plugins.remove(pluginName);
        
        return "";
    }

    public static String reloadThis() {

        if (disabled) return "Already disabled";

        PluginManager tempmanager = manager;
        JavaPlugin tempplugin = MCP.plugin;

        tempmanager.disablePlugin(tempplugin);

        try {
            Plugin loadPlugin = tempmanager.loadPlugin(MCP.pluginFile);
            tempmanager.enablePlugin(loadPlugin);

            return "";
        } catch (InvalidPluginException ex) {
            tempmanager.enablePlugin(tempplugin);
            return "Invalid plugin, loaded original back";
        } catch (InvalidDescriptionException ex) {
            tempmanager.enablePlugin(tempplugin);
            return "Invalid plugin description, loaded original back";
        }

    }

}
