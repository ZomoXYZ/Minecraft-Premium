package dev.zomo.mcpremium.dataType;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;

public class PlayerLookupData {
    
    public ArrayList<OfflinePlayer> players = new ArrayList<OfflinePlayer>();
    public ArrayList<String> args = new ArrayList<String>();

    public PlayerLookupData() {
    }

    public void addPlayer(OfflinePlayer p) {
        players.add(p);
    }

    public void addPlayers(ArrayList<OfflinePlayer> pp) {
        for (OfflinePlayer p : pp)
            players.add(p);
    }

    public void addPlayers(List<OfflinePlayer> pp) {
        for (OfflinePlayer p : pp)
            players.add(p);
    }

    public void clearPlayers() {
        players = new ArrayList<OfflinePlayer>();
    }

    public void addArg(String a) {
        args.add(a);
    }

    public void addArgs(ArrayList<String> aa) {
        for (String a : aa)
            args.add(a);
    }

    public void addArgs(List<String> aa) {
        for (String a : aa)
            args.add(a);
    }

    public void clearArgs() {
        args = new ArrayList<String>();
    }

}
