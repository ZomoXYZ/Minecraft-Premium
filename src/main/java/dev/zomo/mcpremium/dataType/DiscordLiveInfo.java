package dev.zomo.mcpremium.dataType;

public class DiscordLiveInfo {
    
    public boolean isLive = false;
    public String liveLink = "";
    public String liveTitle = "";

    public DiscordLiveInfo(String setLivelink, String setLivetitle) {
        liveLink = setLivelink;
        liveTitle = setLivetitle;
    }

    public DiscordLiveInfo(Boolean setisLive) {
        isLive = setisLive;
    }

}
