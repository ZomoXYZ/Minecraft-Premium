package dev.zomo.mcpremium;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import dev.zomo.mcpremium.dataType.NicknameData;
import dev.zomo.MCLang.LangTemplate;

public class MCPNick {

    private static ArrayList<NicknameData> nicknames = new ArrayList<NicknameData>();

    private static File nicknameFile = null;
    private static FileConfiguration nicknameCache = null;

    private static File loadFile() {

        File file = new File(MCP.plugin.getDataFolder(), "nicknames.yml");

        if (!file.exists())
            MCP.plugin.saveResource("nicknames.yml", false);

        return file;

    }

    private static void parseFile() {
        int playerCount = nicknameCache.getInt("playerCount");
        String curPath = "";

        for (int i = 0; i < playerCount; i++) {

            curPath = "player" + i;

            String UUID = nicknameCache.getString(curPath + ".uuid");
            String nickname = nicknameCache.getString(curPath + ".nickname");

            nicknames.add(new NicknameData(UUID, nickname));

        }
    }

    public static void enable() {

        nicknameFile = loadFile();
        nicknameCache = YamlConfiguration.loadConfiguration(nicknameFile);

        parseFile();

    }

    public static void disable() {

        nicknameFile = null;
        nicknameCache = null;
        nicknames.clear();

    }

    public static void reloadFile() {

        nicknameFile = loadFile();
        nicknameCache = YamlConfiguration.loadConfiguration(nicknameFile);

        parseFile();

    }

    public static String getNick(String uuid) {

        for (NicknameData nickData : nicknames)
            if (nickData.UUID.equals(uuid))
                return nickData.nickname;
        
        return "";
    }

    public static String getNick(UUID uuid) {
        return getNick(uuid.toString());
    }

    public static String getNick(OfflinePlayer player) {
        return getNick(player.getUniqueId());
    }

    public static void setNick(Player player, String nick) {

        nick = nick.replaceAll("\\|", "\\\\|");

        if (!player.hasPermission("MCP.nick.bypassMaxLength")) {
            String shortNick = "";
            for (int length = 0, i = 0; length < MCPConfig.nickMaxLength() && i < nick.length(); i++) {

                if (Character.compare(nick.charAt(i), "&".charAt(0)) == 0) {
                    if (i + 1 < nick.length()) {
                        shortNick += nick.charAt(i);
                        i++;
                        shortNick += nick.charAt(i);
                    }
                    //colors dont count towards length
                } else {

                    if (Character.compare(nick.charAt(i), "\\".charAt(0)) == 0) {
                        if (i + 1 < nick.length()) {
                            if (Character.compare(nick.charAt(i + 1), "&".charAt(0)) == 0)
                                shortNick += "\\&";
                            else if (Character.compare(nick.charAt(i + 1), "\\".charAt(0)) == 0)
                                shortNick += "\\\\";
                            else
                                shortNick += "\\" + nick.charAt(i + 1);
                            i++;
                        }
                        //escape characters dont count towards length
                    } else {
                        shortNick+= nick.charAt(i);
                        length++;
                    }
                }
                
            }

            nick = shortNick;
        }

        nick = nick + "&r";

        if (!player.hasPermission("MCP.nick.color"))
            nick = LangTemplate.escapeColors(nick, true);

        String curPath = "";
        
        if (LangTemplate.escapeColors(nick, true).trim().replaceAll(" ", "").length() > 0) {

            for (int i = 0; i < nicknames.size(); i++)
                if (nicknames.get(i).UUID.equals(player.getUniqueId().toString())) {
                    curPath = "player" + i;
                    nicknames.get(i).setNick(nick);
                }

            if (curPath.length() == 0) {
                int playerCount = nicknameCache.getInt("playerCount");
                curPath = "player" + playerCount;
                nicknameCache.set("playerCount", playerCount + 1);

                String uuid = player.getUniqueId().toString();

                nicknameCache.set(curPath + ".uuid", uuid);
                nicknames.add(new NicknameData(uuid, nick));
            }
            
            nicknameCache.set(curPath + ".nickname", nick);

            try {
                nicknameCache.save(nicknameFile);
            } catch (IOException e) {
                MCP.logger.severe(MCP.lang.string("error.nicknameCache"));
            }

            player.setDisplayName(LangTemplate.escapeColors(nick));

            LangTemplate template = new LangTemplate()
                .add("nick", nick);

            /*
            * <template>
            * nick: nickname
            */

            MCP.send(player, MCP.lang.string("commands.nick.set", template));

        } else
            clearNick(player);

    }

    public static void clearNick(Player player) {

        player.setDisplayName(player.getName());

        String curPath = "";

        for (int i = 0; i < nicknames.size(); i++)
            if (nicknames.get(i).UUID.equals(player.getUniqueId().toString())) {
                curPath = "player" + i;
                nicknames.get(i).setNick("");
            }

        if (curPath.length() == 0) {
            int playerCount = nicknameCache.getInt("playerCount");
            curPath = "player" + playerCount;
            nicknameCache.set("playerCount", playerCount + 1);

            String uuid = player.getUniqueId().toString();

            nicknameCache.set(curPath + ".uuid", uuid);
            nicknames.add(new NicknameData(uuid, ""));
        }

        nicknameCache.set(curPath + ".nickname", "");

        try {
            nicknameCache.save(nicknameFile);
        } catch (IOException e) {
            MCP.logger.severe(MCP.lang.string("error.nicknameCache"));
        }

        MCP.send(player, MCP.lang.string("commands.nick.clear"));
    }

    public static Player getPlayer(String nick) {

        nick = nick.toLowerCase();

        for (NicknameData data : nicknames)
            if (LangTemplate.escapeColors(data.nickname, true).toLowerCase().equals(nick))
                return MCP.plugin.getServer().getPlayer(UUID.fromString(data.UUID));
        
        return null;
    }

    public static ArrayList<Player> getPlayers(String nick) {

        nick = nick.toLowerCase();

        ArrayList<Player> players = new ArrayList<Player>();

        for (NicknameData data : nicknames)
            if (LangTemplate.escapeColors(data.nickname, true).toLowerCase().equals(nick))
                players.add(MCP.plugin.getServer().getPlayer(UUID.fromString(data.UUID)));
        
        return players;
    }

    public static OfflinePlayer getOfflinePlayer(String nick) {

        nick = nick.toLowerCase();

        for (NicknameData data : nicknames)
            if (LangTemplate.escapeColors(data.nickname, true).toLowerCase().equals(nick)) {
                if (MCP.plugin.getServer().getPlayer(UUID.fromString(data.UUID)) != null)
                    return MCP.plugin.getServer().getPlayer(UUID.fromString(data.UUID));
                else
                    return MCP.plugin.getServer().getOfflinePlayer(UUID.fromString(data.UUID));
            }
        
        return null;
    }

    public static ArrayList<OfflinePlayer> getOfflinePlayers(String nick) {

        nick = nick.toLowerCase();

        ArrayList<OfflinePlayer> players = new ArrayList<OfflinePlayer>();

        for (NicknameData data : nicknames)
            if (LangTemplate.escapeColors(data.nickname, true).toLowerCase().equals(nick)) {
                if (MCP.plugin.getServer().getPlayer(UUID.fromString(data.UUID)) != null)
                    players.add(MCP.plugin.getServer().getPlayer(UUID.fromString(data.UUID)));
                else
                    players.add(MCP.plugin.getServer().getOfflinePlayer(UUID.fromString(data.UUID)));
            }
                
        
        return players;
    }

    public static ArrayList<String> getNicknames() {
        ArrayList<String> nicks = new ArrayList<String>();

        for (NicknameData data : nicknames)
            nicks.add(data.nickname);

        return nicks;
    }

}
