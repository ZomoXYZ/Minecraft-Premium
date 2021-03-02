package dev.zomo.mcpremium.dataType;

import java.util.ArrayList;

import dev.zomo.MCCommands.InterfaceTab;

public class WhoisCommandTabComplete implements InterfaceTab {

    /*TODO add support to see current args typed in, then change tab completion based on arg number
    */
    
    public ArrayList<String> run() {

        return CommandTabCompleteNames.run();
    }

}
