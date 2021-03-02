package dev.zomo.mcpremium;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import dev.zomo.mcpremium.dataType.PlayerLookupData;

public class MCPPlayer implements Listener {
    
    public static HashMap<String, ArrayList<String>> MapNameUuids = new HashMap<String, ArrayList<String>>();
    public static HashMap<String, ArrayList<String>> MapUuidNames = new HashMap<String, ArrayList<String>>();

    private static File nameFile = null;
    private static FileConfiguration nameCache = null;

    private static File loadFile() {

        File file = new File(MCP.plugin.getDataFolder(), "namecache.yml");

        if (!file.exists())
            MCP.plugin.saveResource("namecache.yml", false);

        return file;

    }

    private static void parseFile() {
        int playerCount = nameCache.getInt("playerCount");
        String curPath = "";

        for (int i = 0; i < playerCount; i++) {

            curPath = "player" + i;

            String uuid = nameCache.getString(curPath + ".uuid");
            ArrayList<String> names = new ArrayList<>(nameCache.getStringList(curPath + ".names"));

            MapUuidNames.put(uuid, names);

            for (String name : names) {
                ArrayList<String> uuids = new ArrayList<>();
                if (MapNameUuids.containsKey(name)) {
                    uuids = new ArrayList<>(MapNameUuids.get(name));
                }

                uuids.add(uuid);

                MapNameUuids.put(name, uuids);

            }

        }
    }

    public static void enable() {

        nameFile = loadFile();
        nameCache = YamlConfiguration.loadConfiguration(nameFile);

        parseFile();

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        String uuid = event.getPlayer().getUniqueId().toString();
        String name = event.getPlayer().getName();

        if (!MapUuidNames.containsKey(uuid) || !MapUuidNames.get(uuid).contains(name)) {

            int playerCount = -1;

            ArrayList<String> names = new ArrayList<String>();

            if (MapUuidNames.containsKey(uuid)) {
                for (int i = 0; i < nameCache.getInt("playerCount"); i++) {
                    if (nameCache.getString("player" + i + ".uuid").equals(uuid)) {
                        playerCount = i;
                        names = new ArrayList<>(nameCache.getStringList("player" + i + ".names"));
                        break;
                    }
                }
            }

            if (playerCount == -1) {
                playerCount = nameCache.getInt("playerCount");
                nameCache.set("playerCount", playerCount + 1);
            }

            String curPath = "player" + playerCount;
            
            names.add(name);

            nameCache.set(curPath + ".uuid", uuid);
            nameCache.set(curPath + ".names", names);

            try {
                nameCache.save(nameFile);
            } catch (IOException e) {
                MCP.logger.severe(MCP.lang.string("error.nameCache"));
            }

            //add player to arrays

            if (MapUuidNames.containsKey(uuid)) {

                ArrayList<String> mapnames = new ArrayList<>(MapUuidNames.get(uuid));

                mapnames.add(name);

                MapUuidNames.replace(uuid, mapnames);

            } else {

                ArrayList<String> mapnames = new ArrayList<>();

                mapnames.add(name);

                MapUuidNames.put(uuid, mapnames);

            }

            if (MapNameUuids.containsKey(name)) {

                ArrayList<String> mapuuid = new ArrayList<>(MapNameUuids.get(name));

                mapuuid.add(uuid);

                MapNameUuids.replace(name, mapuuid);

            } else {

                ArrayList<String> mapuuid = new ArrayList<>();

                mapuuid.add(uuid);

                MapNameUuids.put(name, mapuuid);

            }

        }

    }

    private static ArrayList<OfflinePlayer> filterNull(ArrayList<OfflinePlayer> args) {
        ArrayList<OfflinePlayer> nonNull = new ArrayList<OfflinePlayer>();
        for (OfflinePlayer item : args) {
            if (item != null) {
                nonNull.add(item);
            }
        }
        return nonNull;
    }

    public static String[] getArgModes() {
        String[] modes = { "uuid", "name", "nick", "discord" };
        return modes;
    }

    public static boolean isArgMode(String check) {
        return Arrays.asList(getArgModes()).contains(check);
    }

    public static ArrayList<OfflinePlayer> args2Player(ArrayList<String> args) {

        if (args.size() == 0)
            return null;

        String[] modes = getArgModes();

        int mode = -1;

        for (int i = 0; i < modes.length; i++) {
            if (args.get(0).equals(modes[i])) {
                mode = i;
                args = new ArrayList<>(args.subList(1, args.size()));
                break;
            }
        }

        String query = String.join(" ", args);

        ArrayList<OfflinePlayer> players = new ArrayList<OfflinePlayer>();
        
        switch(mode) {
            case -1:
                players = uuidlookup(query);

                if (players.size() == 0)
                    players = namelookup(query);

                if (players.size() == 0)
                    players = nicklookup(query);
                
                break;
            case 0:
                players = uuidlookup(query);
                break;
            case 1:
                players = namelookup(query);
                break;
            case 2:
                players = nicklookup(query);
                break;
            case 3:
                players = discordlookup(query);
        }

        return players;
    }
    public static ArrayList<OfflinePlayer> args2Player(String args) {
        ArrayList<String> argslist = new ArrayList<String>(Arrays.asList(args.split(" ")));
        return args2Player(argslist);
    }

    private static ArrayList<OfflinePlayer> uuidlookup(String query) {
        ArrayList<OfflinePlayer> players = new ArrayList<OfflinePlayer>();

        try {
            if (MCP.plugin.getServer().getPlayer(UUID.fromString(query)) != null)
                players.add(MCP.plugin.getServer().getPlayer(UUID.fromString(query)));
            else
                players.add(MCP.plugin.getServer().getOfflinePlayer(UUID.fromString(query)));
        } catch (IllegalArgumentException ex) {
        }

        return filterNull(players);
    }

    private static ArrayList<OfflinePlayer> namelookup(String query) {
        ArrayList<OfflinePlayer> players = new ArrayList<OfflinePlayer>();

        if (MapNameUuids.containsKey(query)) {
            ArrayList<String> uuids = MapNameUuids.get(query);
            for (String uuid : uuids) {
                if (MCP.plugin.getServer().getPlayer(UUID.fromString(uuid)) != null)
                    players.add(MCP.plugin.getServer().getPlayer(UUID.fromString(uuid)));
                else
                    players.add(MCP.plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)));
            }
        }
        
        return filterNull(players);
    }

    private static ArrayList<OfflinePlayer> nicklookup(String query) {
        return filterNull(MCPNick.getOfflinePlayers(query));
    }

    private static ArrayList<OfflinePlayer> discordlookup(String query) {
        return filterNull(MCPDiscord.getPlayers(query));
    }

    private static ArrayList<String> getNameLookup(ArrayList<String> args) {

        ArrayList<String> ret = new ArrayList<String>();

        ArrayList<String> nameLookup = new ArrayList<String>();

        if (isArgMode(args.get(0)) || args.get(0).startsWith("\"")) {
            
            if (args.get(0).startsWith("\"") || args.get(1).startsWith("\"")) {
                int i = 0;
                if (!args.get(0).startsWith("\"")) {
                    nameLookup.add(args.get(0));
                    i = 1;
                }

                boolean finished = false;
                for (; i < args.size(); i++) {
                    if (finished) {
                        ret.add(args.get(i));
                    } else if (args.get(i).endsWith("\"")) {
                        finished = true;
                        nameLookup.add(args.get(i).substring(0, args.get(i).length() - 1));
                        ret.add(String.join(" ", nameLookup));
                    } else {
                        String curArg = args.get(i);
                        if (curArg.startsWith("\""))
                            curArg = curArg.substring(1);
                        nameLookup.add(curArg);
                    }
                }
                
            } else {
                ret.add(args.get(0) + " " + args.get(1));
                
                for (int i = 2; i < args.size(); i++)
                    ret.add(args.get(i));
            }

        } else //first and only first arg is the name, so the given arraylist works
            return args;
        

        return ret;
            
    }

    public static PlayerLookupData playerLookup(ArrayList<String> args) {
        // TODO make sure commands in this plugin are using this method too
        PlayerLookupData data = new PlayerLookupData();

        ArrayList<String> playerLookup = new ArrayList<String>();

        if (args.size() > 0) {
            
            /*playerLookup.add(args.get(0));
            args.remove(0);
            if (Arrays.asList(getArgModes()).contains(playerLookup.get(0))) {
                
                if (args.get(0).startsWith("\"")) {

                    String curArg = args.get(0).substring(1, args.get(0).length());

                    while (args.size() >= 0 && !curArg.endsWith("\"")) {
                        playerLookup.add(args.get(0));
                        args.remove(0);
                        curArg = args.get(0);
                    }

                    if (args.size() >= 0) {
                        playerLookup.add(curArg.substring(1, curArg.length()));
                        args.remove(0);
                    } else {
                        //no ending quote
                        return data;
                    }

                } else {
                    playerLookup.add(args.get(0));
                    args.remove(0);
                }
            }*/

            playerLookup = getNameLookup(args);

            if (playerLookup.size() > 0) {

                data.addArgs(playerLookup.subList(1, playerLookup.size()));
            
                ArrayList<OfflinePlayer> players = args2Player(playerLookup.get(0));

                data.addPlayers(players);

            }

        }

        return data;
    }

}
