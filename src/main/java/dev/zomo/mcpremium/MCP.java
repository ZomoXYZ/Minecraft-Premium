package dev.zomo.mcpremium;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.gson.Gson;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import dev.zomo.MCCommands.Command;
import dev.zomo.MCCommands.CommandMain;
import dev.zomo.MCLang.Lang;
import dev.zomo.MCLang.LangTemplate;
import dev.zomo.mcpremium.dataType.PlayerLookupData;
import dev.zomo.mcpremium.dataType.WhoisCommandTabComplete;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class MCP extends JavaPlugin {

    public static final ArrayList<String> debugMessages = new ArrayList<String>();

    public static Translate translate = null;

    public static JavaPlugin plugin = null;
    public static Logger logger = null;
    public static Server server = null;
    public static Lang lang = null;
    public static File pluginFile = null;

    private static final String[] LANGUAGECODES = { "af", "sq", "ar", "hy", "az", "eu", "be", "bn", "bs", "bg", "ca", "ceb",
            "ny", "zh-TW", "hr", "cs", "da", "nl", "en", "eo", "et", "tl", "fi", "fr", "gl", "ka", "de", "el", "gu",
            "ht", "ha", "iw", "hi", "hmn", "hu", "is", "ig", "id", "ga", "it", "ja", "jw", "kn", "kk", "km", "ko", "lo",
            "la", "lv", "lt", "mk", "mg", "ms", "ml", "mt", "mi", "mr", "mn", "my", "ne", "no", "fa", "pl", "pt", "ro",
            "ru", "sr", "st", "si", "sk", "sl", "so", "es", "su", "sw", "sv", "tg", "ta", "te", "th", "tr", "uk", "ur",
            "uz", "vi", "cy", "yi", "yo", "zu" };

    private static final String[] LANGUAGENAMES = { "Afrikaans", "Albanian", "Arabic", "Armenian", "Azeri (Latin)",
            "Basque", "Belarusian", "bn", "bs", "Bulgarian", "Catalan", "ceb", "ny", "Chinese (T)", "Croatian", "Czech",
            "Danish", "Dutch", "English", "Esperanto", "Estonian", "Tagalog", "Finnish", "French", "Galician",
            "Georgian", "German", "Greek", "Gujarati", "ht", "ha", "iw", "Hindi", "hmn", "Hungarian", "Icelandic", "ig",
            "Indonesian", "ga", "Italian", "Japanese", "jw", "Kannada", "Kazakh", "km", "Korean", "lo", "la", "Latvian",
            "Lithuanian", "FYRO Macedonian", "mg", "Malay", "ml", "Maltese", "Maori", "Marathi", "Mongolian", "my",
            "ne", "no", "Farsi", "Polish", "Portuguese", "Romanian", "Russian", "sr", "st", "si", "Slovak", "Slovenian",
            "so", "Spanish", "su", "Swahili", "Swedish", "tg", "Tamil", "Telugu", "Thai", "Turkish", "Ukrainian",
            "Urdu", "Uzbek (Latin)", "Vietnamese", "Welsh", "yi", "yo", "Zulu" };
        
    public static final HashMap<String, String> Languages = new HashMap<String, String>();

    /**
     * Log through PaperMC's java.util.logging.Logger class and removes color codes
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void log(String logMessage) {
        if (logger != null)
            logger.info(LangTemplate.escapeColors(logMessage, true));
    }

    /**
     * Log through PaperMC's java.util.logging.Logger class and removes color codes
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void debug(String loginfo, String logMessage) {

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        String dateISO = df.format(new Date());

        String message = LangTemplate.escapeColors(dateISO + " [" + loginfo + "] " + logMessage, true);

        debugMessages.add(message);

        /* dead code warning is intentional */
        if (MCPConfig.debug() && logger != null)
            logger.info(message);
    }

    /**
     * Log through PaperMC's java.util.logging.Logger class without removing color
     * codes
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void lograw(String str) {
        if (logger != null)
            logger.info(str);
    }

    /**
     * Override PaperMC's JavaPlugin.onEnable() method
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    @Override
    public void onEnable() {

        for (int i = 0; i < LANGUAGECODES.length && i < LANGUAGENAMES.length; i++)
            Languages.put(LANGUAGECODES[i], LANGUAGENAMES[i]);

        plugin = this;
        logger = this.getLogger();
        server = this.getServer();
        pluginFile = this.getFile();
        
        MCPConfig.enable();
        lang = new Lang(this, MCPConfig.lang());

        MCPNick.enable();
        MCPEvents.enable();
        MCPPlayer.enable();
        MCPAfk.enable();
        MCPDiscord.enable();
        MCPLoadPlugin.enable();
        MCPMaintenance.enable();

        if (server.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MCPPlaceholderAPI().register();
        }

        try {
            if (MCPConfig.googlAPIKey().length() > 0) {
                /* I know the "setApiKey" method is deprecated,
                 * but the "official" way to set the Api Key is to set an environmental variable,
                 * and PaperMC throws many warnings about me setting the environmental variable
                 * 
                 * both ways work, and both throw warnings, but I'd rather use this deprecated method
                 * than a giant method that someone on StackOverflow made by hacking together 2 other methods
                 */
                translate = TranslateOptions.newBuilder().setTargetLanguage(MCPConfig.langShort()).setApiKey(
                        MCPConfig.googlAPIKey()).build().getService();
            }
        } catch (Exception ex) {

        }
        
        getServer().getPluginManager().registerEvents(new MCPEvents(), this);
        getServer().getPluginManager().registerEvents(new MCPPlayer(), this);

        new CommandMain(getCommand("afk"), (CommandSender sender, ArrayList<String> args) -> {
                if (sender instanceof Player)
                    MCPAfk.afk((Player) sender);

                return true;
            });

        new CommandMain(getCommand("nick"), (CommandSender sender, ArrayList<String> args) -> {
                if (sender instanceof Player) {
                    
                    Player player = (Player) sender;

                    String name = "";
                    for (String s : args) {
                        if (name.length() > 0)
                            name+= " ";
                        name+= s;
                    }

                    if (stripColors(name).length() == 0) {
                        MCPNick.clearNick(player);
                    } else {
                        MCPNick.setNick(player, name);
                    }
                    
                }

                return true;
            });

        new CommandMain(getCommand("fsleep"), (CommandSender sender, ArrayList<String> args) -> {

            int ticks = -1;

            if (args.size() > 0)
                ticks = Integer.parseInt(args.get(0));

            MCPEvents.sleepCheck(true, ticks);

            return true;
        });

        new CommandMain(getCommand("maintenance"), (CommandSender sender, ArrayList<String> args) -> {

            if (args.size() > 1 && args.get(0).equals("y")) {

                int time = 0;
                if (args.size() > 1)
                    time = MCPMaintenance.timeToTick(args.get(1));
                int interval = 0;
                if (args.size() > 2)
                    interval = MCPMaintenance.timeToTick(args.get(2));

                if (!MCPMaintenance.begin(time, interval))
                    MCPMaintenance.sendCommandMessage(sender);

                //store data outside command and prevent running twice

                //on player join, display message
                //should make creating time remaining message from inside task should be own static function
                // - so join alert message can get it too 

                //interpret args.get(1) i.e. 5m -> ticks, 30s -> ticks, 24h -> ticks
                //this is length until maintenance
                //MCPMaintenance.timeToTick()

                //interpret args.get(2) i.e. 5m -> ticks, 30s -> ticks, 24h -> ticks
                //this is interval between announcements

                //do announcements !!! :)))
                // r/programmerhumor
                // r/restofthefuckingowl
                // r/bruhmoment
                // r/redditirl
                // r/reddit
                // r/red
                // r/decreasinglyverbose
                // r/me_irl
                // twitter.com/meirlbot
                // twitter.com/potus
                // whitehouse.gov

                // r/pareidolia

                // - send number of times to be intervaled to task
                // - each interval display on everyones screen and in chat
                //   - if interval < 1s (20 ticks), display up to 2 decimal places
                //   - "<time> before server restart"
                // - task can restart server

                //restart after announcements

            } else if (args.size() > 0 && args.get(0).equals("c")) {

                if (!MCPMaintenance.cancel())
                    MCPMaintenance.sendCommandMessage(sender);
                else {
                    String title = LangTemplate.escapeColors(MCP.lang.string("commands.maintenance.cancelled"));

                    for (Player player : MCP.plugin.getServer().getOnlinePlayers())
                        player.sendTitle(title, "", 5, 80, 20);

                    MCP.log(MCP.stripColors(title));
                }

            } else
                MCPMaintenance.sendCommandMessage(sender);

            return true;
        });/// <command> y <time>

        new CommandMain(getCommand("gabe"), (CommandSender sender, ArrayList<String> args) -> {
                sendAll("somebody has used the gabe command...");
                return true;
            });

        Command reloadCommand = new Command("reload", (CommandSender sender, ArrayList<String> args) -> {

                //working on making a way to reload the entire plugin
                /*String ranFine = MCPLoadPlugin.reloadThis();

                if (ranFine.length() == 0)
                    sender.sendMessage("Reloaded this plugin");// sender.sendMessage(lang.string("commands.mcp.reload.all"));
                else
                    sender.sendMessage(ranFine);*/

                sender.sendMessage(lang.string("commands.error.invalidArgs"));

                return true;
            }).subcommand("all", (CommandSender sender, ArrayList<String> args) -> {
                MCPConfig.reloadFile();
                MCPDiscord.reloadFile();
                lang.reloadFile();
                MCPNick.reloadFile();

                sender.sendMessage(lang.string("commands.mcp.reload.all"));
                return true;
            }).subcommand("config", (CommandSender sender, ArrayList<String> args) -> {
                MCPConfig.reloadFile();

                sender.sendMessage(lang.string("commands.mcp.reload.config"));
                return true;
            }).subcommand("discord", (CommandSender sender, ArrayList<String> args) -> {
                MCPDiscord.reloadFile();

                sender.sendMessage(lang.string("commands.mcp.reload.discord"));
                return true;
            }).subcommand("lang", (CommandSender sender, ArrayList<String> args) -> {
                lang.reloadFile();

                sender.sendMessage(lang.string("commands.mcp.reload.lang"));
                return true;
            }).subcommand("nickname", (CommandSender sender, ArrayList<String> args) -> {
                MCPNick.reloadFile();

                sender.sendMessage(lang.string("commands.mcp.reload.nickname"));
                return true;
            });

        Command whoisCommand = new Command("whois", (CommandSender sender, ArrayList<String> args) -> {
                if (args.size() == 0)
                    sender.sendMessage(lang.string("commands.error.invalidArgs"));
                else {

                    /*String name = "";

                    Player player = null;

                    if (args.get(0).equals("nick")) {

                        for (int i = 1; i < args.size(); i++) {
                            if (name.length() > 0)
                                name+= " ";
                            name+= args.get(i);
                        }

                        player = MCPNick.getPlayer(name);

                    } else if (args.get(0).equals("user")) {

                        for (int i = 1; i < args.size(); i++) {
                            if (name.length() > 0)
                                name += " ";
                            name += args.get(i);
                        }

                        player = getServer().getPlayer(name);

                    } else if (args.get(0).equals("uuid")) {

                        for (int i = 1; i < args.size(); i++) {
                            if (name.length() > 0)
                                name += " ";
                            name += args.get(i);
                        }

                        player = getServer().getPlayer(UUID.fromString(name));

                    } else {

                        for (int i = 0; i < args.size(); i++) {
                            if (name.length() > 0)
                                name += " ";
                            name += args.get(i);
                        }

                        player = MCPNick.getPlayer(name);

                        if (player == null) 
                            player = getServer().getPlayer(name);
                        
                        try {
                            if (player == null)
                                player = getServer().getPlayer(UUID.fromString(name));
                        } catch (IllegalArgumentException ex) {

                        }

                    }

                    if (player == null)
                        sender.sendMessage(lang.string("commands.mcp.whois.missing"));
                    else {

                        net.dv8tion.jda.api.entities.Member discordMember = MCPDiscord.getMember(player);

                        LangTemplate template = genPlayerTemplate(player, true);
                        if (discordMember != null)
                            template = MCPDiscord.memberTemplate(template, discordMember);
                        // TODO add catch if null
                        // could be null if member is not in the primary server
                        // either player left or player joined from secondary server

                        BaseComponent[] message = genChatMessage(template, "commands.mcp.whois.found");

                        sender.sendMessage(message);

                    }*/

                    //ArrayList<OfflinePlayer> players = MCPPlayer.args2Player(args);
                    PlayerLookupData data = MCPPlayer.playerLookup(args);

                    debug("data.players.size()", String.valueOf(data.players.size()));

                    if (data.players.size() == 0)
                        sender.sendMessage(lang.string("commands.mcp.whois.missing"));

                    for (OfflinePlayer player : data.players) {

                        net.dv8tion.jda.api.entities.Member discordMember = MCPDiscord.getMember(player);

                        LangTemplate template = genPlayerTemplate(player, true);
                        if (discordMember != null)
                            template = MCPDiscord.memberTemplate(template, discordMember);
                        // TODO add catch if null
                        // could be null if member is not in the primary server
                        // either player left or player joined from secondary server

                        BaseComponent[] message = genChatMessage(template, "commands.mcp.whois.found");

                        sender.sendMessage(message);
                    }

                }
                return true;
            }).autocomplete(new WhoisCommandTabComplete());

        new CommandMain(getCommand("mcp"), (CommandSender sender, ArrayList<String> args) -> {
                sender.sendMessage(LangTemplate.escapeColors(lang.string("commands.error.invalidArgs")));
                return true;
            })
            .subcommand(reloadCommand)
            .subcommand(whoisCommand)
            .subcommand("version", (CommandSender sender, ArrayList<String> args) -> {

                PluginDescriptionFile desc = this.getDescription();

                LangTemplate template = new LangTemplate()
                    .add("version", desc.getVersion())
                    .add("mcversion", desc.getAPIVersion());

                /*
                * <template> (for afk.*)
                * version: plugin vesrion
                * mcversion: version of minecraft this plugin was made for
                */

                sender.sendMessage(LangTemplate.escapeColors(lang.string("commands.mcp.version", template)));
                return true;
            })
            .subcommand("load", (CommandSender sender, ArrayList<String> args) -> {

                String ranFine = MCPLoadPlugin.load(String.join(" ", args));

                if (ranFine.length() == 0)
                    sender.sendMessage("loaded plugin");
                else
                    sender.sendMessage(ranFine);

                return true;
            })
            .subcommand("unload", (CommandSender sender, ArrayList<String> args) -> {

                String ranFine = MCPLoadPlugin.unload(String.join(" ", args));

                if (ranFine.length() == 0)
                    sender.sendMessage("unloaded plugin");
                else
                    sender.sendMessage(ranFine);

                return true;
            });

        new CommandMain(getCommand("mcpdump"), (CommandSender sender, ArrayList<String> args) -> {
            String message = "";
            for (int i = Math.max(0, debugMessages.size()-11); i < debugMessages.size(); i++) {
                if (message.length() > 0)
                    message+="\n";
                message+= debugMessages.get(i);
            }

            sender.sendMessage(message);
            return true;
        });

    }

    /**
     * Override PaperMC's JavaPlugin.onDisable() method
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    @Override
    public void onDisable() {
        MCPDiscord.sendStop();

        /*MCPLoadPlugin.disable();
        MCPDiscord.disable();
        MCPAfk.disable();
        MCPEvents.disable();
        MCPNick.disable();
        MCPConfig.disable();

        // reset global variables
        MCP.translate = null;
        MCP.plugin = null;
        MCP.logger = null;
        MCP.server = null;
        MCP.lang = null;*/
    }

    /**
     * Sends a message to a CommandSender
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void send(CommandSender sender, String msg) {
        sender.sendMessage(LangTemplate.escapeColors("&r" + msg + "&r", !(sender instanceof Player)));
    }

    /**
     * Broadcasts a message
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void sendAll(String msg) {
        server.broadcastMessage(LangTemplate.escapeColors("&r" + LangTemplate.escapeColors(msg) + "&r"));
    }

    private static ArrayList<String> chatMessageSplit(LangTemplate template, String formatStrWhole) {
        ArrayList<String> format = new ArrayList<String>();
        List<String> formatStr = Arrays.asList(LangTemplate.escapeColors(lang.string(
                formatStrWhole, template)).split(""));

        String tempStr = "";
        boolean escape = false;

        for (int i = 0; i < formatStr.size(); i++) {
            if (escape) {
                tempStr += formatStr.get(i);
                escape = false;
            } else if (tempStr.equals("\\")) {
                tempStr += formatStr.get(i);
                escape = true;
            } else if (formatStr.get(i).equals("|")) {
                format.add(LangTemplate.escapeColors(tempStr));
                tempStr = "";
            } else
                tempStr += formatStr.get(i);
        }
        format.add(LangTemplate.escapeColors(tempStr));
        tempStr = "";

        return format;
    }

    /**
     * Generates a chat message
     *
     * @author Ashley Zomo
     * @version 1.0.1
     * @since 2021-01-10
     * @return BaseComponent[] able to be sent in PaperMC
     * @param template    an instance of dev.zomo.mcpremium.lang.LangTemplate
     * @param chatFormat  format for the chat message
     * @param hoverFormat format for hovering over the chat message
     * @param clickFormat format for clicking on the chat message
     */
    public static BaseComponent[] genChatMessage(LangTemplate template, String chatFormats) {

        ArrayList<String> chat = chatMessageSplit(template, chatFormats + ".chat");
        ArrayList<String> hover = chatMessageSplit(template, chatFormats + ".hover");
        ArrayList<String> click = chatMessageSplit(template, chatFormats + ".click");

        ComponentBuilder message = new ComponentBuilder("");

        for (int i = 0; i < chat.size(); i++) {

            message = message.append(chat.get(i));

            String hoverEventStr = "";

            if (hover.size() > i)
                hoverEventStr = hover.get(i);

            String clickEventStr = "";

            if (click.size() > i)
                clickEventStr = click.get(i);

            if (hoverEventStr.length() == 0 && clickEventStr.length() == 0)
                message = message.reset();
            else {

                if (hoverEventStr.length() > 0) {

                    HoverEvent.Action action = HoverEvent.Action.SHOW_TEXT;

                    if (hoverEventStr.length() > 2 && String.valueOf(hoverEventStr.charAt(1)).equals(":")) {

                        switch (String.valueOf(hoverEventStr.charAt(1)).toLowerCase()) {
                            case "e":
                                action = HoverEvent.Action.SHOW_ENTITY;
                                break;
                            case "i":
                                action = HoverEvent.Action.SHOW_ITEM;
                                break;
                            case "t":
                                action = HoverEvent.Action.SHOW_TEXT;
                                break;
                        }

                        hoverEventStr = hoverEventStr.substring(2);

                    }

                    message = message.event(new HoverEvent(action, new Text(hoverEventStr)));
                }

                if (clickEventStr.length() > 0) {

                    ClickEvent.Action action = ClickEvent.Action.OPEN_URL;

                    if (clickEventStr.length() > 2 && String.valueOf(clickEventStr.charAt(1)).equals(":")) {

                        switch (String.valueOf(clickEventStr.charAt(1)).toLowerCase()) {
                            case "p":
                                action = ClickEvent.Action.CHANGE_PAGE;
                                break;
                            case "c":
                                action = ClickEvent.Action.COPY_TO_CLIPBOARD;
                                break;
                            case "f":
                                action = ClickEvent.Action.OPEN_FILE;
                                break;
                            case "u":
                                action = ClickEvent.Action.OPEN_URL;
                                break;
                            case "r":
                                action = ClickEvent.Action.RUN_COMMAND;
                                break;
                            case "s":
                                action = ClickEvent.Action.SUGGEST_COMMAND;
                                break;
                        }

                        clickEventStr = clickEventStr.substring(2);

                    }

                    message = message.event(new ClickEvent(action, clickEventStr));
                }
            }

        }
        /* format
         *   abc|def|ghi
         * hover
         *   im hovering over abc|t:im hovering over def
         * click
         *   https://google.com/q?=abc|c:copy text|r:/kill @p
         */

        return message.create();
    }

    /**
     * Generates a template from a Player
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return template including info about the player
     * @param template an instance of dev.zomo.mcpremium.lang.LangTemplate
     * @param player   org.bukkit.entity.Player
     * @param noColor  whether or not color shouldn't be included (true removes
     *                 color)
     */
    public static LangTemplate genPlayerTemplate(LangTemplate template, Player player, boolean noColor) {

        String displayname = "";
        String nickname = MCPNick.getNick(player);
        String name = player.getName();

        displayname = nickname.length() > 0 ? nickname : name;

        if (noColor) {
            displayname = stripColors(displayname);
            nickname = stripColors(nickname);
            name = stripColors(name);
        }/* else {
            displayname = LangTemplate.doubleEscapeColor(displayname);
            nickname = LangTemplate.doubleEscapeColor(nickname);
        }*/
        
        template = template
                .add("displayname", LangTemplate.doubleEscapeAll(displayname))
                .add("nickname", LangTemplate.doubleEscapeAll(nickname))
                .add("name", LangTemplate.doubleEscapeAll(name))
                //.add("prefix", user.getCachedData().getMetaData().getPrefix())
                .add("prefix", "")
                //.add("suffix", user.getCachedData().getMetaData().getSuffix());
                .add("suffix", "");

        /*
         * <template> (for afk.*)
         * displayname: nickname or actual name
         * nickname: nickname or empty
         * name: actual name
         * prefix: prefix
         * suffix: suffix
         */

        return template;
    }

    /**
     * Generates a template from a Player
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return template including info about the player
     * @param player  org.bukkit.entity.Player
     * @param noColor whether or not color shouldn't be included (true removes
     *                color)
     */
    public static LangTemplate genPlayerTemplate(Player player, boolean noColor) {
        return genPlayerTemplate(new LangTemplate(), player, noColor);
    }

    /**
     * Generates a template from a Player
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return template including info about the player
     * @param player org.bukkit.entity.Player
     */
    public static LangTemplate genPlayerTemplate(Player player) {
        return genPlayerTemplate(player, false);
    }

    /**
     * Generates a template from a Player
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return template including info about the player
     * @param template an instance of dev.zomo.mcpremium.lang.LangTemplate
     * @param player   org.bukkit.entity.Player
     * @param noColor  whether or not color shouldn't be included (true removes
     *                 color)
     */
    public static LangTemplate genPlayerTemplate(LangTemplate template, OfflinePlayer player, boolean noColor) {

        String displayname = "";
        String nickname = MCPNick.getNick(player);
        String name = player.getName();
        
        displayname = nickname.length() > 0 ? nickname : name;

        if (noColor) {
            displayname = stripColors(displayname);
            nickname = stripColors(nickname);
            name = stripColors(name);
        }/* else {
            displayname = LangTemplate.doubleEscapeColor(displayname);
            nickname = LangTemplate.doubleEscapeColor(nickname);
        }*/
        
        template = template
                .add("displayname", LangTemplate.doubleEscapeAll(displayname))
                .add("nickname", LangTemplate.doubleEscapeAll(nickname))
                .add("name", LangTemplate.doubleEscapeAll(name));

        /*
         * <template> (for afk.*)
         * displayname: nickname or actual name
         * nickname: nickname or empty
         * name: actual name
         */

        return template;
    }

    /**
     * Generates a template from a Player
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return template including info about the player
     * @param player  org.bukkit.entity.Player
     * @param noColor whether or not color shouldn't be included (true removes
     *                color)
     */
    public static LangTemplate genPlayerTemplate(OfflinePlayer player, boolean noColor) {
        return genPlayerTemplate(new LangTemplate(), player, noColor);
    }

    /**
     * Generates a template from a Player
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return template including info about the player
     * @param player org.bukkit.entity.Player
     */
    public static LangTemplate genPlayerTemplate(OfflinePlayer player) {
        return genPlayerTemplate(player, false);
    }

    /**
     * gets the default world from server.properties
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return the name of the default world
     */
    public static String getDefaultWorld() {

        try {
            
            File serverProperties = new File(server.getWorldContainer(), "server.properties");
            BufferedReader reader = new BufferedReader(new FileReader(serverProperties));
            
            String worldname = reader.readLine();
            while (worldname != null && !worldname.startsWith("level-name"))
                worldname = reader.readLine();
            reader.close();

            worldname = worldname.split("=")[1].trim();

            return worldname;

        } catch (IOException e) {
            //error reading file
        }

        return null;

    }

    /**
     * gets the stats for a specified player
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return String
     * @param player   a Player
     * @param category a category in the stats file
     * @param stat     a stat in the stats file
     */
    public static String getPlayerStats(Player player, String category, String stat) {

        String defualtWorld = getDefaultWorld();

        if (defualtWorld == null)
            return null;
        
        try {
            Gson gson = new Gson();

            File statsFile = new File(server.getWorldContainer(),
                    defualtWorld + "/stats/" + player.getUniqueId().toString() + ".json");

            BufferedReader reader = new BufferedReader(new FileReader(statsFile));

            Map<?, ?> entireJson = gson.fromJson(reader, Map.class);

            reader.close();

            Map<?, ?> stats = (Map<?, ?>) entireJson.get("stats");
            if (stats == null)
                return null;
            Map<?, ?> statCategory = (Map<?, ?>) stats.get("minecraft:" + category);
            if (statCategory == null)
                return null;
            Object statObj = statCategory.get("minecraft:" + stat);
            String statValue = null;
            if (statObj != null)
                statValue = statCategory.get("minecraft:" + stat).toString();

            if (statValue == null || statValue.length() == 0)
                return null;

            return statValue;

        } catch (IOException ex) {
            //error parsing file
        }

        return null;

    }

    /**
     * removes all color and color templates from a string
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2021-01-10
     * @return String
     * @param str string
     */
    public static String stripColors(String str) {
        return ChatColor.stripColor(LangTemplate.escapeColors(str));
    }

}
