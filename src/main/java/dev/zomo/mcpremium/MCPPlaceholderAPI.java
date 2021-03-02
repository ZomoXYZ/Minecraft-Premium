package dev.zomo.mcpremium;

import java.util.List;

import org.bukkit.OfflinePlayer;

import dev.zomo.MCLang.LangTemplate;
import dev.zomo.mcpremium.dataType.DiscordLiveInfo;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

/**
 * This class will automatically register as a placeholder expansion when a jar
 * including this class is added to the directory
 * {@code /plugins/PlaceholderAPI/expansions} on your server. <br>
 * <br>
 * If you create such a class inside your own plugin, you have to register it
 * manually in your plugins {@code onEnable()} by using
 * {@code new YourExpansionClass().register();}
 */
public class MCPPlaceholderAPI extends PlaceholderExpansion {

    /**
     * Because this is an internal class, you must override this method to let
     * PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * This method should always return true unless we have a dependency we need to
     * make sure is on the server for our placeholders to work!
     *
     * @return always true since we do not have any dependencies.
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * 
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor() {
        List<String> authors = MCP.plugin.getDescription().getAuthors();
        String authorsStr = "";
        for (String author : authors) {
            if (authorsStr.length() > 0)
                authorsStr+= " / ";
            authorsStr+= author;
        }
        return authorsStr;
    }

    /**
     * The placeholder identifier should go here. <br>
     * This is what tells PlaceholderAPI to call our onRequest method to obtain a
     * value if a placeholder starts with our identifier. <br>
     * The identifier has to be lowercase and can't contain _ or %
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier() {
        return "mcp";
    }

    /**
     * This is the version of this expansion. <br>
     * You don't have to use numbers, since it is set as a String.
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion() {
        return MCP.plugin.getDescription().getVersion();
    }

    /**
     * This is the method called when a placeholder with our identifier is found and
     * needs a value. <br>
     * We specify the value identifier in this method. <br>
     * Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param player     A {@link org.bukkit.OfflinePlayer OfflinePlayer}.
     * @param identifier A String containing the identifier/value.
     *
     * @return Possibly-null String of the requested identifier.
     */
    @Override
    public String onRequest(OfflinePlayer player, String identifier) {

        if (identifier.equals("afk")) {
            if (MCPAfk.isAFK(player.getUniqueId()))
                return " " + MCP.lang.string("afk.isAfk");
            return "";
        } else if (identifier.equals("displayname")) {
            String nick = MCPNick.getNick(player.getUniqueId());
            if (nick.length() > 0)
                return LangTemplate.escapeColors(nick);
            return player.getName();
        } else if (identifier.equals("live")) {
            DiscordLiveInfo liveInfo = MCPDiscord.liveInfo(MCPDiscord.getMember(player.getUniqueId()));
            if (liveInfo.isLive)
                return MCP.lang.string("placeholderapi.live") + " ";
            return "";
        }

        // We return null if an invalid placeholder (f.e. %example_placeholder3%)
        // was provided
        return null;
    }
}