package dev.zomo.mcpremium;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.advancement.Advancement;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import dev.zomo.MCLang.LangTemplate;
import dev.zomo.mcpremium.dataType.DiscordLink;
import dev.zomo.mcpremium.dataType.DiscordLiveInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;

public class MCPDiscord {

    public static boolean loggedIn = false;
    public static boolean loginError = false;

    private static JDA jda = null;
    private static Presence presence = null;
    public static SelfUser client = null;
    public static TextChannel verificationChannel = null;
    public static TextChannel chatChannel = null;
    public static Role verifiedRole = null;

    private static File discordFile = null;
    private static FileConfiguration discordCache = null;

    public static ArrayList<DiscordLink> discordLinkInfo = new ArrayList<DiscordLink>();

    private static String webhookURL = "";

    private static void sendMessage(TextChannel chatChannel, String msg) {

        msg = cleanMessage(msg);

        chatChannel.sendMessage(msg).queue();
    }

    private static String cleanMessage(String messageContent) {
        messageContent = messageContent.replaceAll("(?i)@everyone", "**everyone**");
        messageContent = messageContent.replaceAll("(?i)@here", "**here**");
        return messageContent;
    }

    /**
     * Loads the cache file
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return the cache file
     */
    private static File loadFile() {

        File file = new File(MCP.plugin.getDataFolder(), "discordLink.yml");

        if (!file.exists())
            MCP.plugin.saveResource("discordLink.yml", false);

        return file;

    }

    /**
     * Creates a template based on the bot client
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return a template
     * @param template a template instance
     */
    public static LangTemplate clientTemplate(LangTemplate template) {
        return template.add("clientname", client.getName()).add("clientdiscriminator", client.getDiscriminator())
                .add("clientid", client.getId()).add("clientmention", client.getId());

        /*
         * <template> (for discord.*) name: client username discriminator: client
         * discriminator id: client id clientmention: mention client
         */
    }

    /**
     * Creates a template based on the bot client
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return a template
     */
    public static LangTemplate clientTemplate() {
        return clientTemplate(new LangTemplate());
    }

    /**
     * Creates a template based on the verification channel
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return a template
     * @param template a template instance
     */
    public static LangTemplate verificationChannelTemplate(LangTemplate template) {
        if (verificationChannel != null)
            return template.add("channelname", verificationChannel.getName()).add("channelid", verificationChannel.getId())
                .add("channelmention", "<#" + verificationChannel.getId() + ">");
        else
            return template;

        /*
         * <template> (for discord.*) name: channel name id: channel id mention: mention
         * channel
         */
    }

    /**
     * Creates a template based on the verification channel
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return a template
     */
    public static LangTemplate verificationChannelTemplate() {
        return verificationChannelTemplate(new LangTemplate());
    }

    /**
     * Creates a template based on a given user
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return a template
     * @param template    a template instance
     * @param discordUser a discord user
     */
    public static LangTemplate userTemplate(LangTemplate template, User discordUser) {

        if (discordUser == null)
            return template.add("username", "").add("userdiscriminator", "").add("userid", "").add("usermention", "");

        return template.add("username", discordUser.getName()).add("userdiscriminator", discordUser.getDiscriminator())
                .add("userid", discordUser.getId()).add("usermention", "<@" + discordUser.getId() + ">");

        /*
         * <template> (for discord.*) username: discord username userdiscriminator:
         * discord discriminator userid: discord id usermention: mention user
         */
    }

    /**
     * Creates a template based on a given user
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return a template
     * @param discordUser a discord user
     */
    public static LangTemplate userTemplate(User discordUser) {
        return userTemplate(new LangTemplate(), discordUser);
    }

    /**
     * Creates a template based on a given user
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return a template
     * @param discordID a discord user's ID
     */
    public static LangTemplate userTemplate(String discordID) {
        User discordUser = jda.getUserById(discordID);
        return userTemplate(discordUser);
    }

    /**
     * Creates a template based on a given member
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return a template
     * @param template      a template instance
     * @param discordMember a discord member
     */
    public static LangTemplate memberTemplate(LangTemplate template, Member discordMember) {

        if (discordMember == null)
            return template.add("username", "").add("userdiscriminator", "").add("userid", "").add("usermention", "")
                    .add("userdisplayname", "").add("usernickname", "");

        template = userTemplate(template, discordMember.getUser());

        String nickname = discordMember.getNickname();

        return template.add("userdisplayname", discordMember.getEffectiveName()).add("usernickname",
                nickname == null ? "" : nickname);

        /*
         * <template> (from discord.*) userdisplayname: discord nickname or username
         * usernickname: discord nickname or empty
         */
    }

    /**
     * Creates a template based on a given member
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return a template
     * @param discordMember a discord member
     */
    public static LangTemplate memberTemplate(Member discordMember) {
        return memberTemplate(new LangTemplate(), discordMember);
    }

    /**
     * Enables this module
     *
     * @author Ashley Zomo
     * @version 1.0.1
     * @since 2020-12-15
     */
    public static void enable() {

        String token = MCPConfig.discordToken();
        String channelID = MCPConfig.discordVerificationChannel();

        if (token.length() > 0) {

            try {

                jda = JDABuilder.createDefault(token).addEventListeners(new MCPDiscordEvents())
                        .enableIntents(GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL)
                        .enableIntents(GatewayIntent.GUILD_PRESENCES).enableCache(CacheFlag.ACTIVITY).build();
                jda.awaitReady();

                presence = jda.getPresence();

                client = jda.getSelfUser();

                MCP.log(MCP.lang.string("discord.login", clientTemplate()));

                loggedIn = true;

                if (MCPConfig.discordVerificationChannel().length() > 0)
                    verificationChannel = jda.getTextChannelById(MCPConfig.discordVerificationChannel());
                if (MCPConfig.discordChatChannel().length() > 0)
                    chatChannel = jda.getTextChannelById(MCPConfig.discordChatChannel());
                if (MCPConfig.discordVerifiedRole().length() > 0)
                    verifiedRole = jda.getRoleById(MCPConfig.discordVerifiedRole());

                if (verificationChannel == null && MCPConfig.enableDiscordVerification()) {
                    MCP.logger.severe(MCP.lang.string("error.discord.badChannel"));
                    loginError = true;
                } else {
                    updatePresense();
                    sendStart();
                }

            } catch (LoginException e) {
                MCP.logger.severe(MCP.lang.string("error.discord.badLogin"));
                loginError = true;
            } catch (InterruptedException e) {
                MCP.logger.severe(MCP.lang.string("error.discord.badLogin"));
                loginError = true;
            }

        } else if (token.length() == 0) {
            MCP.logger.severe(MCP.lang.string("error.discord.missingToken"));
            loginError = true;
        } else if (channelID.length() == 0) {
            if (MCPConfig.enableDiscordVerification()) {
                MCP.logger.severe(MCP.lang.string("error.discord.missingChannel"));
                loginError = true;
            }
        }

        if (!loginError) {

            if (MCPConfig.enableDiscordVerification()) {

                discordLinkInfo = new ArrayList<DiscordLink>();

                discordFile = loadFile();
                discordCache = YamlConfiguration.loadConfiguration(discordFile);

                int playerCount = discordCache.getInt("playerCount");
                String curPath = "";

                for (int i = 0; i < playerCount; i++) {

                    curPath = "player" + i;

                    String uuid = discordCache.getString(curPath + ".uuid");
                    String id = discordCache.getString(curPath + ".discordid");
                    String logincode = discordCache.getString(curPath + ".logincode");

                    discordLinkInfo.add(new DiscordLink(uuid, id, logincode));

                }

            }

            loadWebhookData();

        }

    }
    
    /**
     * on configuration file reload
     *
     * @author Ashley Zomo
     * @version 1.0.1
     * @since 2020-12-15
     */
    public static void configreload() {
        loadWebhookData();
    }

    /**
     * Load webhook data from the chat channel
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-15
     */
    private static void loadWebhookData() {

        if (MCPConfig.discordUseWebhook() && chatChannel != null) {

            try {

                List<Webhook> webhooks = chatChannel.retrieveWebhooks().complete();

                for (Webhook webhook : webhooks) {
                    if (webhook.getName().equals(client.getName())) {
                        if (MCPConfig.discordWebhookProxy().length() > 0) {
                            String webhookID = webhook.getId();
                            String webhookToken = webhook.getToken();
                            webhookURL = MCPConfig.discordWebhookProxy() + "/" + webhookID + "/" + webhookToken;
                        } else
                            webhookURL = webhook.getUrl();
                        break;
                    }
                }

                if (webhookURL.length() == 0) {
                    Webhook webhook = chatChannel.createWebhook(client.getName()).complete();
                    if (MCPConfig.discordWebhookProxy().length() > 0) {
                        String webhookID = webhook.getId();
                        String webhookToken = webhook.getToken();
                        webhookURL = MCPConfig.discordWebhookProxy() + "/" + webhookID + "/" + webhookToken;
                    } else
                        webhookURL = webhook.getUrl();
                }

            } catch (InsufficientPermissionException ex) {
                MCP.logger.severe(ex.getMessage());
                webhookURL = "";
            }

        }

    }

    /**
     * Disables this module
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void disable() {

        loggedIn = false;
        loginError = false;

        //jda.shutdown();
        jda = null;
        presence = null;
        client = null;
        verificationChannel = null;
        chatChannel = null;

        discordFile = null;
        discordCache = null;

        discordLinkInfo.clear();

    }

    /**
     * Reloads the cache file
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void reloadFile() {

        if (MCPConfig.enableDiscordVerification()) {

            discordLinkInfo = new ArrayList<DiscordLink>();

            discordFile = loadFile();
            discordCache = YamlConfiguration.loadConfiguration(discordFile);

            int playerCount = discordCache.getInt("playerCount");
            String curPath = "";

            for (int i = 0; i < playerCount; i++) {

                curPath = "player" + i;

                String uuid = discordCache.getString(curPath + ".uuid");
                String id = discordCache.getString(curPath + ".discordid");
                String logincode = discordCache.getString(curPath + ".logincode");

                discordLinkInfo.add(new DiscordLink(uuid, id, logincode));

            }

        }

    }

    /**
     * Updates the presense of the discord bot
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void updatePresense() {
        if (loggedIn && !loginError) {
            LangTemplate template = new LangTemplate().add("playercount", MCPAfk.allPlayers.size()).add("afkcount",
                    MCPAfk.playersAFK.size());

            /*
             * <template> playercount: number of players online afkcount: number of people
             * afk
             */

            presence.setActivity(Activity.playing(MCP.lang.string("discord.presence", template)));
        }
    }

    /**
     * Adds a user to the cache file and generates a verification code
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param uuid UUID of minecraft player
     * @return verification code
     */
    public static String addUser(String uuid) {

        for (DiscordLink userInfo : discordLinkInfo)
            if (userInfo.uuid.equals(uuid))
                return userInfo.loginCode;

        DiscordLink userInfo = new DiscordLink(uuid);

        discordLinkInfo.add(userInfo);

        if (userInfo.loginCode != null) {

            int playerCount = discordCache.getInt("playerCount");
            discordCache.set("playerCount", playerCount + 1);

            String curPath = "player" + playerCount;

            discordCache.set(curPath + ".uuid", uuid);
            discordCache.set(curPath + ".discordid", "");
            discordCache.set(curPath + ".logincode", userInfo.loginCode);

            try {
                discordCache.save(discordFile);
            } catch (IOException e) {
                MCP.logger.severe(MCP.lang.string("error.discordCache"));
            }

        }

        return userInfo.loginCode;
    }

    /**
     * Adds a user to the cache file and generates a verification code
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param uuid UUID of minecraft player
     * @return verification code
     */
    public static String addUser(UUID uuid) {
        return addUser(uuid.toString());
    }

    /**
     * Checks if a given player can join the server
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param uuid UUID of minecraft player
     * @return boolean if player can join
     */
    public static boolean canJoin(String uuid) {

        for (DiscordLink info : discordLinkInfo)
            if (info.uuid.equals(uuid) && info.discordID.length() > 0) {
                giveRole(info.getMember());
                return true;
            }

        return false;

    }

    /**
     * Ensures the given member has the configured verified role, will do nothing if
     * configured role is empty
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2021-03-04
     * @param member discord member
     */
    private static void giveRole(Member member) {
        if (verifiedRole != null && verificationChannel != null
                && member.getGuild().getId().equals(verificationChannel.getGuild().getId())) {

            member.getGuild().addRoleToMember(member, verifiedRole).queue();

        }
    }

    /**
     * Checks if a given player can join the server
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param uuid UUID of minecraft player
     * @return boolean if player can join
     */
    public static boolean canJoin(UUID uuid) {
        return canJoin(uuid.toString());
    }

    /**
     * add verified user to file
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param info an instance of DiscordLink
     */
    public static void verifyUserFile(DiscordLink info) {

        String curPath = "";

        boolean found = false;

        for (int i = 0; i < discordCache.getInt("playerCount") && !found; i++) {
            curPath = "player" + i;

            if (discordCache.getString(curPath + ".uuid").equals(info.uuid)) {
                found = true;

                discordCache.set(curPath + ".discordid", info.discordID);
                discordCache.set(curPath + ".logincode", "");

            }

        }

        try {
            discordCache.save(discordFile);
        } catch (IOException e) {
            MCP.logger.severe(MCP.lang.string("error.discordCache"));
        }

    }

    /**
     * Checks the verification code to see if the user should be able to join
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param discordID ID of Discord User
     * @param code      Code given by user
     * @return integer correlating to response (0=incorrect, 1=done, 2=done
     *         previously)
     */
    public static int verifyUser(String discordID, String code) {

        code = code.toUpperCase();

        if (code.length() != MCPConfig.discordCodeLength())
            return 0;

        for (DiscordLink info : discordLinkInfo)
            if (info.loginCode.equals(code)) {
                if (info.discordID.equals(discordID)) {
                    return 2;
                } else {
                    info.setDiscordID(discordID);
                    verifyUserFile(info);
                    giveRole(info.getMember());
                    return 1;
                }
            }

        return 0;

    }

    /**
     * Gets the Discord Member from a Minecraft UUID
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param uuid Minecraft Player's UUID
     * @return a discord member
     */
    public static Member getMember(String uuid) {
        for (DiscordLink info : discordLinkInfo)
            if (info.uuid.equals(uuid) && info.discordID.length() > 0) {

                Guild guild = chatChannel.getGuild();

                return guild.getMemberById(info.discordID);
            }

        return null;
    }

    /**
     * Gets the Discord Member from a Minecraft UUID
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param uuid Minecraft Player's UUID
     * @return a discord member
     */
    public static Member getMember(UUID uuid) {
        return getMember(uuid.toString());
    }

    /**
     * Gets the Discord Member from a Minecraft Player
     *
     * @author Ashley Zomo
     * @version 1.0.1
     * @since 2021-01-06
     * @param player Minecraft Player
     * @return a discord member
     */
    public static Member getMember(org.bukkit.OfflinePlayer player) {
        return getMember(player.getUniqueId());
    }

    public static String encodeURI(String string) {
        try {
            return URLEncoder.encode(string, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            return "";
        }
    }

    /**
     * Sends a webhook to discord
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-16
     * @return whether or not webhook was sent
     */
    private static boolean sendWebhook(org.bukkit.entity.Player player, LangTemplate template) {

        if (MCPConfig.discordUseWebhook() && webhookURL.length() > 0) {

            try {
                URL url = new URL(webhookURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");

                /* Payload support */
                con.setDoOutput(true);
                DataOutputStream out = new DataOutputStream(con.getOutputStream());

                String messageContent = LangTemplate.escapeColors(MCP.lang.string("discord.chat.onDiscord.chatWebhook", template), true);
                messageContent = cleanMessage(messageContent);
                String displayName = LangTemplate.escapeColors(MCPNick.getNick(player), true);
                String username = player.getName();
                if (displayName.length() > 0)
                    username = " (" + username + ")";
                displayName = displayName.substring(0, Math.min(32 - username.length(), displayName.length()));
                out.writeBytes("{\"content\":\"" + messageContent +
                            "\",\"username\":\"" + (displayName + username).trim() +
                            "\",\"avatar_url\":\"" + "https://crafatar.com/renders/head/" + player.getUniqueId().toString() + "\"}");
                out.flush();
                out.close();

                int status = con.getResponseCode();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();
                //MCP.log("Response status: " + status);
                //MCP.log(content.toString());

                if (String.valueOf(status).startsWith("2"))
                    return true;

            } catch(Exception ex) {
                MCP.logger.warning(ex.getMessage());
                ex.printStackTrace();
            }

        }

        return false;
    }

    /**
     * Sends a Minecraft chat message to Discord
     *
     * @author Ashley Zomo
     * @version 1.0.1
     * @since 2020-12-15
     * @param player  Minecraft Player
     * @param message message sent
     */
    public static void sendChat(org.bukkit.entity.Player player, String message) {

        LangTemplate template = MCP.genPlayerTemplate(player, true)
            .add("message", message);

        /*
         * <template> (from afk.*)
         * message: message content
         */

        if (chatChannel != null) {

            if (!sendWebhook(player, template))
                sendMessage(chatChannel,
                        LangTemplate.escapeColors(MCP.lang.string("discord.chat.onDiscord.chat", template), true));
        }

    }

    /**
     * Sends a Minecraft join event message to Discord
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param player Minecraft Player
     */
    public static void sendJoin(org.bukkit.entity.Player player) {

        LangTemplate template = MCP.genPlayerTemplate(player, true);

        /*
         * <template> (from afk.*)
         */
        
        if (chatChannel != null)
            sendMessage(chatChannel,
                    LangTemplate.escapeColors(MCP.lang.string("discord.chat.onDiscord.join", template), true));

    }

    /**
     * Sends a Minecraft leave event message to Discord
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param player Minecraft Player
     */
    public static void sendLeave(org.bukkit.entity.Player player) {

        LangTemplate template = MCP.genPlayerTemplate(player, true);

        /*
         * <template> (from afk.*)
         */
        
        if (chatChannel != null)
            sendMessage(chatChannel,
                    LangTemplate.escapeColors(MCP.lang.string("discord.chat.onDiscord.leave", template), true));

    }

    //taken from DiscordSRV
    private static final Map<Advancement, String> ADVANCEMENT_TITLE_CACHE = new HashMap<>();
    public static String getAdvancementName(Advancement advancement) {
        return ADVANCEMENT_TITLE_CACHE.computeIfAbsent(advancement, v -> {
            try {
                Object handle = advancement.getClass().getMethod("getHandle").invoke(advancement);
                Object advancementDisplay = Arrays.stream(handle.getClass().getMethods())
                        .filter(method -> method.getReturnType().getSimpleName().equals("AdvancementDisplay"))
                        .filter(method -> method.getParameterCount() == 0).findFirst()
                        .orElseThrow(() -> new RuntimeException(
                                "Failed to find AdvancementDisplay getter for advancement handle"))
                        .invoke(handle);
                if (advancementDisplay == null)
                    //throw new RuntimeException("Advancement doesn't have display properties");
                    return "";

                try {
                    Field advancementMessageField = advancementDisplay.getClass().getDeclaredField("a");
                    advancementMessageField.setAccessible(true);
                    Object advancementMessage = advancementMessageField.get(advancementDisplay);
                    Object advancementTitle = advancementMessage.getClass().getMethod("getString")
                            .invoke(advancementMessage);
                    return (String) advancementTitle;
                } catch (Exception e) {
                    //DiscordSRV.debug("Failed to get title of advancement using getString, trying JSON method");
                }

                Field titleComponentField = Arrays.stream(advancementDisplay.getClass().getDeclaredFields())
                        .filter(field -> field.getType().getSimpleName().equals("IChatBaseComponent")).findFirst()
                        .orElseThrow(() -> new RuntimeException("Failed to find advancement display properties field"));
                titleComponentField.setAccessible(true);
                Object titleChatBaseComponent = titleComponentField.get(advancementDisplay);
                String title = (String) titleChatBaseComponent.getClass().getMethod("getText")
                        .invoke(titleChatBaseComponent);
                if (StringUtils.isNotBlank(title))
                    return title;
                Class<?> chatSerializerClass = Arrays.stream(titleChatBaseComponent.getClass().getDeclaredClasses())
                        .filter(clazz -> clazz.getSimpleName().equals("ChatSerializer")).findFirst()
                        .orElseThrow(() -> new RuntimeException("Couldn't get component ChatSerializer class"));
                String componentJson = (String) chatSerializerClass.getMethod("a", titleChatBaseComponent.getClass())
                        .invoke(null, titleChatBaseComponent);
                
                return LegacyComponentSerializer.INSTANCE
                        .serialize(GsonComponentSerializer.INSTANCE.deserialize(componentJson));
            } catch (Exception e) {
                MCP.logger.warning(
                        "Failed to get title of advancement " + advancement.getKey().getKey() + ": " + e.getMessage());

                /*String rawAdvancementName = advancement.getKey().getKey();
                return Arrays
                        .stream(rawAdvancementName.substring(rawAdvancementName.lastIndexOf("/") + 1).toLowerCase()
                                .split("_"))
                        .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1)).collect(Collectors.joining(" "));*/
                return "";
            }
        });
    }

    /**
     * Sends a Minecraft advancement event message to Discord
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param player      Minecraft Player
     * @param advancement Minecraft Advancement
     */
    public static void sendAdvancement(org.bukkit.entity.Player player, org.bukkit.advancement.Advancement advancement) {
                
        String name = getAdvancementName(advancement);

        if (name.length() > 0) {
           
            LangTemplate template = MCP.genPlayerTemplate(player, true)
                .add("advancement", name);

            /*
            * <template> (from afk.*)
            */
        
            if (chatChannel != null)
                sendMessage(chatChannel,
                        LangTemplate.escapeColors(MCP.lang.string("discord.chat.onDiscord.advancement", template), true));

        }

    }

    /**
     * Sends a Minecraft death event message to Discord
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param player       Minecraft Player
     * @param deathMessage Minecraft death message
     */
    public static void sendDeath(org.bukkit.entity.Player player, String deathMessage) {

        String deathNoName = "";
        String[] deathNoNameArr = deathMessage.split(" ");
        for (int i = 1; i < deathNoNameArr.length; i++) {
            if (deathNoName.length() > 0)
                deathNoName+= " ";
            deathNoName+= deathNoNameArr[i];
        }

        LangTemplate template = MCP.genPlayerTemplate(player, true)
            .add("deathmessage", deathNoName)
            .add("deathmessagewhole", deathMessage);

        /*
         * <template> (from afk.*)
         * deathmessage: death message without name
         * deathmessagewhole: entire death message
         */
        
        if (chatChannel != null)
            sendMessage(chatChannel,
                    LangTemplate.escapeColors(MCP.lang.string("discord.chat.onDiscord.death", template), true));

    }

    /**
     * Sends a plugin start message to Discord
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void sendStart() {
        if (chatChannel != null)
            sendMessage(chatChannel,
                    LangTemplate.escapeColors(MCP.lang.string("discord.chat.onDiscord.start"), true));

    }

    /**
     * Sends a plugin stop message to Discord
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void sendStop() {
        if (chatChannel != null)
            sendMessage(chatChannel,
                    LangTemplate.escapeColors(MCP.lang.string("discord.chat.onDiscord.stop"), true));

    }

    /**
     * Gets Live information from a member
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @param member Discord member
     * @return Live information from a member
     */
    public static DiscordLiveInfo liveInfo(Member member) {
        String livelink = "";
        String livetitle = "";

        if (member != null) {
            List<Activity> activities = member.getActivities();
            for (Activity activity : activities) {
                if (activity.getType().equals(Activity.ActivityType.STREAMING)) {
                    livelink = activity.getUrl();
                    if (activity.isRich())
                        livetitle = activity.asRichPresence().getDetails();
                    else
                        livetitle = activity.getName();
                }
            }
        }

        if (livelink.length() > 0)
            return new DiscordLiveInfo(livelink, livetitle);
            
        return new DiscordLiveInfo(false);

    }

    /**
     * Gets the Discord Member from a Minecraft UUID
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2021-02-16
     * @param uuid Minecraft Player's UUID
     * @return whether or not the player is in the discord server
     */
    public static boolean isInServer(String uuid) {
        return getMember(uuid) != null;
    }

    /**
     * Gets the Discord Member from a Minecraft UUID
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2021-02-16
     * @param uuid Minecraft Player's UUID
     * @return whether or not the player is in the discord server
     */
    public static boolean isInServer(UUID uuid) {
        return isInServer(uuid.toString());
    }

    /**
     * Gets the Discord Member from a Minecraft Player
     *
     * @author Ashley Zomo
     * @version 1.0.1
     * @since 2021-02-16
     * @param player Minecraft Player
     * @return whether or not the player is in the discord server
     */
    public static boolean isInServer(org.bukkit.OfflinePlayer player) {
        return isInServer(player.getUniqueId());
    }
    
    public static ArrayList<org.bukkit.OfflinePlayer> getPlayers(String id) {
        ArrayList<org.bukkit.OfflinePlayer> players = new ArrayList<org.bukkit.OfflinePlayer>();
        
        for (DiscordLink info : discordLinkInfo) {
            if (info.discordID.equals(id)) {
                players.add(info.offlinePlayer);
            }
        }

        return players;
    }

}
