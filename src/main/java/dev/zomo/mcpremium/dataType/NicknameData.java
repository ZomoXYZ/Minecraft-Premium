package dev.zomo.mcpremium.dataType;

public class NicknameData {
    
    public String UUID = "";
    public String nickname = "";

    public NicknameData(String setUUID, String setNickname) {
        UUID = setUUID;
        nickname = setNickname;
    }

    public void setNick(String setNickname) {
        nickname = setNickname;
    }

}
