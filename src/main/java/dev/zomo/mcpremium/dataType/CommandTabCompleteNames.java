package dev.zomo.mcpremium.dataType;

import java.util.ArrayList;

import dev.zomo.MCLang.LangTemplate;
import dev.zomo.mcpremium.MCPNick;
import dev.zomo.mcpremium.MCPPlayer;

public class CommandTabCompleteNames {
    
    public static ArrayList<String> run() {

        ArrayList<String> names = new ArrayList<String>();

        for (String mode : MCPPlayer.getArgModes())
            names.add(mode);

        for (String n : MCPNick.getNicknames()) {
            if (n.contains(" "))
                n = "\"" + n + "\"";
            names.add(LangTemplate.escapeColors(n, true));
        }

        for (String n : MCPPlayer.MapNameUuids.keySet())
            names.add(n);

        return names;
    }

}
