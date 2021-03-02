package dev.zomo.mcpremium;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import dev.zomo.mcpremium.tasks.AFKTask;

public class MCPAfk {

    public static ArrayList<Player> playersAFK = new ArrayList<Player>();
    public static ArrayList<Player> allPlayers = new ArrayList<Player>();
    private static ArrayList<BukkitTask> allPlayersAFKTask = new ArrayList<BukkitTask>();

    private static int afkTimeout = 0;

    /**
     * enables this module
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void enable() {
        afkTimeout = MCPConfig.afkTimeout();
    }

    /**
     * disables this module
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void disable() {
        afkTimeout = 0;
        playersAFK.clear();
        allPlayers.clear();
        allPlayersAFKTask.clear();
    }
    
    /**
     * Toggles the afk status of a player
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param player a player
     */
    public static void afk(Player player) {

        boolean found = false;

        for (int i = 0; i < playersAFK.size() && !found; i++) {
            if (playersAFK.get(i).getUniqueId().equals(player.getUniqueId())) {
                found = true;
                playersAFK.remove(i);
                MCP.sendAll(MCP.lang.string("afk.offAfk", MCP.genPlayerTemplate(player)));
                MCPDiscord.updatePresense();
            }
        }

        if (!found) {
            playersAFK.add(player);
            MCP.sendAll(MCP.lang.string("afk.onAfk", MCP.genPlayerTemplate(player)));
            MCPDiscord.updatePresense();
        }

        MCPEvents.sleepCheck();

    }

    /**
     * Enables the afk status of a player
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param player a player
     */
    public static void addAfk(Player player) {

        boolean found = false;

        for (int i = 0; i < playersAFK.size() && !found; i++) {
            if (playersAFK.get(i).getUniqueId().equals(player.getUniqueId())) {
                found = true;
                //already afk
            }
        }

        if (!found) {
            playersAFK.add(player);
            MCP.sendAll(MCP.lang.string("afk.onAfk", MCP.genPlayerTemplate(player)));
            MCPDiscord.updatePresense();
        }

        MCPEvents.sleepCheck();

    }

    /**
     * Disables the afk status of a player
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param player a player
     */
    public static void removeAfk(Player player) {

        boolean found = false;

        for (int i = 0; i < playersAFK.size() && !found; i++) {
            if (playersAFK.get(i).getUniqueId().toString().equals(player.getUniqueId().toString())) {
                found = true;
                playersAFK.remove(i);
                MCP.sendAll(MCP.lang.string("afk.offAfk", MCP.genPlayerTemplate(player)));
                MCPDiscord.updatePresense();
            }
        }

        MCPEvents.sleepCheck();

    }

    /**
     * Removes a player from the cache
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param player a player
     */
    public static void removePlayer(Player player) {

        boolean found = false;

        removeAfk(player);
        
        for (int i = 0; i < allPlayers.size() && !found; i++) {
            if (allPlayers.get(i).getUniqueId().equals(player.getUniqueId())) {
                allPlayers.remove(i);
                if (allPlayersAFKTask.size() > i &&  allPlayersAFKTask.get(i) != null)
                    allPlayersAFKTask.get(i).cancel();
                allPlayersAFKTask.remove(i);
                found = true;
                //MCPDiscord.updatePresense();
                // this method is only called from PlayerQuitEvent which also calls .updatePresence()
            }
        }

    }

    /**
     * Adds a player to the cache
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param player a player
     */
    public static void addPlayer(Player player) {
        
        boolean found = false;
        for (int i = 0; i < allPlayers.size() && !found; i++)
            if (allPlayers.get(i).getUniqueId().equals(player.getUniqueId()))
                found = true;

        if (!found) {
            allPlayers.add(player);
            allPlayersAFKTask.add(new AFKTask(player).runTaskLater(MCP.plugin, 20 * afkTimeout));
        }

    }

    /**
     * Cancels the task for the player (the task is what runs delayed to enable the
     * afk status for a player after not moving for a bit)
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param player a player
     */
    public static void cancelTask(Player player) {
        
        boolean found = false;
        for (int i = 0; i < allPlayers.size() && !found; i++) {
            if (allPlayers.get(i).getUniqueId().equals(player.getUniqueId())) {
                if (allPlayersAFKTask.get(i) != null)
                    allPlayersAFKTask.get(i).cancel();
                found = true;
            }
        }

    }

    /**
     * Runs the task for the player (the task is what runs delayed to enable the afk
     * status for a player after not moving for a bit)
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param player a player
     */
    public static void runTask(Player player) {

        boolean found = false;
        for (int i = 0; i < allPlayers.size() && !found; i++) {
            if (allPlayers.get(i).getUniqueId().equals(player.getUniqueId())) {
                if (allPlayersAFKTask.get(i) != null)
                    allPlayersAFKTask.get(i).cancel();
                runTask(player, i);
                found = true;
            }
        }

    }

    /**
     * Runs the task for the player (the task is what runs delayed to enable the afk
     * status for a player after not moving for a bit)
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param player a player
     * @param i the index at which the player's task exists
     */
    private static void runTask(Player player, int i) {
        allPlayersAFKTask.set(i, new AFKTask(player).runTaskLater(MCP.plugin, 20 * afkTimeout));
    }

    /**
     * Removes the afk status of a player and then runs the task for them (the task
     * is what runs delayed to enable the afk status for a player after not moving
     * for a bit)
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param player a player
     */
    public static void updateTask(Player player) {
        removeAfk(player);
        runTask(player);
    }

    /**
     * Checks if the player is afk
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return boolean if the player is afk
     * @param uuid a player's uuid
     */
    public static boolean isAFK(String uuid) {
        for (int i = 0; i < playersAFK.size(); i++)
            if (playersAFK.get(i).getUniqueId().toString().equals(uuid))
                return true;

        return false;
    }

    /**
     * Checks if the player is afk
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return boolean if the player is afk
     * @param uuid a player's uuid
     */
    public static boolean isAFK(UUID uuid) {
        return isAFK(uuid.toString());
    }

    /**
     * Checks if the player is afk
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return boolean if the player is afk
     * @param player a player
     */
    public static boolean isAFK(Player player) {
        return isAFK(player.getUniqueId().toString());
    }

}
