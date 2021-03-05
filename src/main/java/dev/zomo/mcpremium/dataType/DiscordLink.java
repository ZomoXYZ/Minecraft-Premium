package dev.zomo.mcpremium.dataType;

import java.util.UUID;

import org.bukkit.OfflinePlayer;

import dev.zomo.mcpremium.MCP;
import dev.zomo.mcpremium.MCPConfig;
import dev.zomo.mcpremium.MCPDiscord;
import net.dv8tion.jda.api.entities.Member;

public class DiscordLink {

    public String discordID = "";
    public String uuid = "";
    public String loginCode = "";
    public OfflinePlayer offlinePlayer = null;
    
    public DiscordLink(String newUUID, String newDiscordID, String newLoginCode) {
        discordID = newDiscordID;
        uuid = newUUID;
        offlinePlayer = MCP.plugin.getServer().getOfflinePlayer(UUID.fromString(newUUID));
        loginCode = newLoginCode;
    }

    public Member getMember() {
        if (MCPDiscord.verificationChannel != null)
            return MCPDiscord.verificationChannel.getGuild().getMemberById(discordID);
        return null;
    }

    private String genCode(int depth) {
        if (depth < 20) {
            String code = "";
            for (int i = 0; i < MCPConfig.discordCodeLength(); i++) {
                code+= Integer.toHexString((int) Math.round(Math.random() * 15));
            }
            for (DiscordLink info : MCPDiscord.discordLinkInfo)
                if (code.equals(info.loginCode))
                    return genCode();
                    
            return code.toUpperCase();
        } else
            return null;
    }

    private String genCode() {
        return genCode(0);
    }
    
    public DiscordLink(String setUUID) {
        uuid = setUUID;
        offlinePlayer = MCP.plugin.getServer().getOfflinePlayer(UUID.fromString(setUUID));
        loginCode = genCode();
    }
    
    public void setDiscordID(String newDiscordID) {
        discordID = newDiscordID;
    }

    public void clearCode() {
        loginCode = "";
    }

}
