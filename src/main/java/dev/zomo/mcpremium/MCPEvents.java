package dev.zomo.mcpremium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.google.cloud.translate.Translation;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jsoup.Jsoup;

import dev.zomo.mcpremium.dataType.DiscordLiveInfo;
import dev.zomo.mcpremium.tasks.MaintenanceTask;
import dev.zomo.mcpremium.tasks.SleepTask;
import dev.zomo.MCLang.LangTemplate;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;

public class MCPEvents implements Listener {

    static ArrayList<ArrayList<Player>> sleeping = new ArrayList<ArrayList<Player>>();
    static ArrayList<BukkitTask> sleepingTask = new ArrayList<BukkitTask>();

    private static ArrayList<Integer> lastSleepCount = new ArrayList<Integer>();
    private static ArrayList<Integer> lastOnlineCount = new ArrayList<Integer>();
    static List<World> worlds = null;

    public static void enable() {
        worlds = MCP.server.getWorlds();
        for (int i = 0; i < worlds.size(); i++) {
            sleeping.add(new ArrayList<Player>());
            sleepingTask.add(null);
            lastSleepCount.add(0);
            lastOnlineCount.add(0);
        }
    }

    public static void disable() {
        worlds = null;
        sleeping.clear();
        sleepingTask.clear();
        lastSleepCount.clear();
        lastOnlineCount.clear();
    }

    private static void send(Player player, String msg) {
        player.sendMessage(LangTemplate.escapeColors("&r" + msg + "&r"));
    }

    private static void sendAll(String msg, World world) {
        for (Player player : MCP.plugin.getServer().getOnlinePlayers())
            if (player.getWorld().getUID().equals(world.getUID()))
                send(player, msg);
    }

    public static void sleepCheck(boolean doForce, int ticks) {

        ticks = Math.max(ticks, 20);

        double sleepPercent = MCPConfig.sleepPercent()/100.0;

        for (int worldnum = 0; worldnum < worlds.size(); worldnum++) {

            ArrayList<Player> sleepingInfo = sleeping.get(worldnum);
            World world = worlds.get(worldnum);

            int onlineCount = 0;
            for (Player player : MCP.plugin.getServer().getOnlinePlayers()) {
                GameMode playergm = player.getGameMode();
                Boolean isSurvival = playergm.equals(GameMode.SURVIVAL) || playergm.equals(GameMode.ADVENTURE);

                if (!MCPAfk.isAFK(player) && player.getWorld().getUID().equals(world.getUID()) && isSurvival)
                    onlineCount++;
            }

            if (doForce || lastSleepCount.get(worldnum) != sleepingInfo.size() || lastOnlineCount.get(worldnum) != onlineCount) {

                lastSleepCount.set(worldnum, sleepingInfo.size());
                lastOnlineCount.set(worldnum, onlineCount);

                if (doForce || sleepingInfo.size() > 0) {

                    if (doForce || sleepingInfo.size() >= onlineCount * sleepPercent) {

                        while (sleeping.get(worldnum).size() > 0)
                            sleeping.get(worldnum).remove(0);
                        
                        if (sleepingTask.get(worldnum) != null) {
                            sleepingTask.get(worldnum).cancel();
                            sleepingTask.set(worldnum, null);
                        }

                        sendAll(MCP.lang.string("sleep.sleepPass"), world);

                        sleepingTask.set(worldnum, new SleepTask(world, ticks).runTaskTimer(MCP.plugin, 20, 1));

                    } else {
                        LangTemplate template = new LangTemplate()
                            .add("sleeping", sleepingInfo.size())
                            .add("required", (int) Math.ceil(onlineCount * sleepPercent))
                            .add("online", onlineCount)
                            .add("percent", (int) (sleepingInfo.size() / ((double) onlineCount)))
                            .add("world", world.getName());

                        /*
                        * <template>
                        * sleeping: number of players asleep
                        * required: required number of players asleep
                        * online: number of players in world
                        * percent: percent of players asleep
                        * world: name of world
                        */

                        sendAll(MCP.lang.string("sleep.sleepCheck", template), world);
                    }

                }

            }

        }

    }
    public static void sleepCheck() {
        sleepCheck(false, -1);
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {

        if (!event.isCancelled()) {

            int worldnum = -1;

            for (int i = 0; i < worlds.size(); i++) {
                if (event.getPlayer().getWorld().getUID().equals(worlds.get(i).getUID())) {
                    worldnum = i;
                }
            }

            if (worldnum == -1) {
                //cant find world
            } else {
                
                boolean alreadyIn = false;

                for (int i = 0; i < sleeping.get(worldnum).size(); i++)
                    if (sleeping.get(worldnum).get(i).getUniqueId().equals(event.getPlayer().getUniqueId()))
                        alreadyIn = true;
                
                if (!alreadyIn)
                    sleeping.get(worldnum).add(event.getPlayer());
                    
            }

        }

        sleepCheck();

        MCPAfk.cancelTask(event.getPlayer());

    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {

        int worldnum = -1;

        for (int i = 0; i < worlds.size(); i++) {
            if (event.getPlayer().getWorld().getUID().equals(worlds.get(i).getUID())) {
                worldnum = i;
            }
        }

        if (worldnum == -1) {
            // cant find world
        } else {

            boolean found = false;

            for (int i = 0; i < sleeping.get(worldnum).size() && !found; i++)
                if (sleeping.get(worldnum).get(i).getUniqueId().equals(event.getPlayer().getUniqueId())) {
                    sleeping.get(worldnum).remove(i);
                    found = true;
                }

        }   

        sleepCheck();

        MCPAfk.runTask(event.getPlayer());
        
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        MCPDiscord.sendLeave(event.getPlayer());

        String quitMessage = MCP.lang.string("event.leave", MCP.genPlayerTemplate(event.getPlayer(), true));
        quitMessage = LangTemplate.escapeColors(quitMessage);
        event.setQuitMessage(quitMessage);

        int worldnum = -1;

        for (int i = 0; i < worlds.size(); i++) {
            if (event.getPlayer().getWorld().getUID().equals(worlds.get(i).getUID())) {
                worldnum = i;
            }
        }

        if (worldnum == -1) {
            // cant find world
        } else {

            boolean found = false;

            for (int i = 0; i < sleeping.get(worldnum).size() && !found; i++) {
                if (sleeping.get(worldnum).get(i).getUniqueId().equals(event.getPlayer().getUniqueId())) {
                    sleeping.get(worldnum).remove(i);
                    found = true;
                }
            }

        }

        sleepCheck();


        MCPAfk.removePlayer(event.getPlayer());
        MCPDiscord.updatePresense();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        MCPAfk.addPlayer(event.getPlayer());
        String nickname = MCPNick.getNick(event.getPlayer());
        if (nickname.length() > 0)
            event.getPlayer().setDisplayName(LangTemplate.escapeColors(nickname));

        MCPDiscord.sendJoin(event.getPlayer());

        String joinMessage = MCP.lang.string("event.join", MCP.genPlayerTemplate(event.getPlayer(), true));
        joinMessage = LangTemplate.escapeColors(joinMessage);
        event.setJoinMessage(joinMessage);

        MCPDiscord.updatePresense();

        if (MCPMaintenance.isRunning())
            MaintenanceTask.sendMessage(event.getPlayer());
        
    }

    public static HashMap<String, Integer> deathCounts = new HashMap<String, Integer>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        String uuid = event.getEntity().getUniqueId().toString();

        int deathCount = 0;

        if (deathCounts.containsKey(uuid)) {
            deathCount = deathCounts.get(uuid);
        } else {
            String deathCountStr = MCP.getPlayerStats(event.getEntity(), "custom", "deaths");

            if (deathCountStr != null)
                deathCount = (int) Float.valueOf(deathCountStr).floatValue();
            else
                deathCount = 0;
        }

        deathCount++;

        deathCounts.put(uuid, deathCount);
        
        String playerName = event.getEntity().getName();

        MCPDiscord.sendDeath(event.getEntity(), event.getDeathMessage());

        LangTemplate descriptionTemplate = new LangTemplate()
            .add("name", playerName)
            .add("deaths", deathCount);

        /*
         * <template>
         * name: name of player
         * deaths: number of deaths
         */

        List<Content> hoverMessage = new ArrayList<Content>();
        hoverMessage.add(new Text(ChatColor.GREEN + MCP.lang.string("deathAchievement.title")));
        hoverMessage.add(new Text("\n" + ChatColor.GREEN + MCP.lang.string("deathAchievement.description", descriptionTemplate)));

        BaseComponent[] message = new ComponentBuilder(playerName + " has made the advancement ")
            .append( "[" + MCP.lang.string("deathAchievement.title") + "]" )
            .color( net.md_5.bungee.api.ChatColor.GREEN ).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage))
            .create();

        MCP.server.broadcastMessage(event.getDeathMessage());
        event.getEntity().playSound(event.getEntity().getLocation(), "ui.toast.challenge_complete", 1, 1);
        event.setDeathMessage("");

        MCP.server.broadcast(message);

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        MCPAfk.updateTask(event.getPlayer());
    }

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        MCPAfk.updateTask(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        MCPAfk.updateTask(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        MCPAfk.updateTask(event.getPlayer());
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        MCPAfk.updateTask(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        MCPAfk.updateTask(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        MCPAfk.updateTask(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        MCPAfk.updateTask(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        MCPAfk.updateTask(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {

        if (MCPConfig.enableDiscordVerification()) {

            if (MCPDiscord.loginError) {
                // kick / error logging in
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, LangTemplate.escapeColors(MCP.lang.string("discord.kickReason.loginError")));
            } else if (!MCPDiscord.loggedIn) {
                // kick / not logged in
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, LangTemplate.escapeColors(MCP.lang.string("discord.kickReason.notLoggedin")));
            } else if (!MCPDiscord.canJoin(event.getPlayer().getUniqueId())) {
                
                String code = MCPDiscord.addUser(event.getPlayer().getUniqueId());

                //if code is null, error
                if (code == null) {
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                            LangTemplate.escapeColors(MCP.lang.string("discord.kickReason.codegenError")));
                    //error generating code
                } else {
                    LangTemplate template = MCPDiscord.clientTemplate();
                    template = MCPDiscord.verificationChannelTemplate(template);
                    template = template
                        .add("logincode", code)
                        .add("command", MCP.lang.string("discord.command"));
                    /*
                    * <template> (addition to discord.*)
                    * code: code to connect discord and minecraft
                    */

                    // kick / not allowed
                    event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST,
                        LangTemplate.escapeColors(MCP.lang.string("discord.kickReason.notAllowed", template)));
                }

            } else if (!MCPDiscord.isInServer(event.getPlayer())) {
                LangTemplate template = MCPDiscord.clientTemplate();
                /*
                 * <template> (addition to discord.*)
                 */

                // kick / not allowed
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST,
                        LangTemplate.escapeColors(MCP.lang.string("discord.kickReason.notInServer", template)));
            }

        }
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        MCPDiscord.sendAdvancement(event.getPlayer(), event.getAdvancement());
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {

        try {

            event.setCancelled(true);

            DiscordLiveInfo liveInfo = MCPDiscord.liveInfo(MCPDiscord.getMember(event.getPlayer()));

            String message = event.getMessage().replaceAll("\\|", "\\\\|");

            String messageTranslatedStr = "";
            String messageTranslatedStrRaw = "";

            if (MCP.translate != null) {

                Translation messageTranslated = MCP.translate.translate(message);

                messageTranslatedStrRaw = Jsoup.parse(messageTranslated.getTranslatedText()).text();

                if (!messageTranslated.getSourceLanguage().equals(MCPConfig.langShort()) && !messageTranslatedStrRaw.toLowerCase().equals(message.toLowerCase())) {

                    String sourceLang = messageTranslated.getSourceLanguage();
                    String endLang = MCPConfig.langShort();

                    sourceLang = MCP.Languages.get(sourceLang);
                    if (sourceLang == null)
                        sourceLang = messageTranslated.getSourceLanguage();
                    endLang = MCP.Languages.get(endLang);
                    if (endLang == null)
                        endLang = MCPConfig.langShort();

                    LangTemplate transTemplate = new LangTemplate()
                        .add("from", sourceLang)
                        .add("to", endLang)
                        .add("message", messageTranslatedStrRaw);

                    /*
                     * <template>
                     * from: input language
                     * to: output language
                     * message: translated message content
                     */

                    messageTranslatedStr = MCP.lang.string("translate.response", transTemplate);
                }

            }

            LangTemplate template = MCP.genPlayerTemplate(event.getPlayer())
                .add("live", liveInfo.isLive)
                .add("livetitle", liveInfo.liveTitle)
                .add("livelink", liveInfo.liveLink)
                .add("message", message)
                .add("translation", messageTranslatedStr)
                .add("translationraw", messageTranslatedStr);

            /*
             * <template>
             * name: name of player
             * displayname: number of deaths
             * live: is live
             * livelink: link of live (blank if empty)
             * message: message content
             */

            BaseComponent[] finalMessage = MCP.genChatMessage(template, "chat");

            for (Player recipient : event.getRecipients())
                recipient.sendMessage(finalMessage);

            MCPDiscord.sendChat(event.getPlayer(), event.getMessage());

            if (MCPConfig.logChat())
                MCP.log(MCP.stripColors(MCP.lang.string("chat.chat", template)).replaceAll("\\|", ""));
        
        } catch (Exception ex) {
            MCP.logger.severe(ex.getMessage());
            MCP.sendAll(MCP.lang.string("error.unknownChatError"));
            event.setCancelled(false);
        }

    }

}
