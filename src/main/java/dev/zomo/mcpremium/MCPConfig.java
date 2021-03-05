package dev.zomo.mcpremium;

import org.bukkit.configuration.file.FileConfiguration;

public class MCPConfig {

    private static FileConfiguration config = null;
    //private static FileConfiguration configDefault = null;
    
    /**
     * Enables this module
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void enable() {
        //MCP.plugin.saveDefaultConfig();
        config = MCP.plugin.getConfig();
        //InputStream configDefaultStream = MCP.plugin.getResource("config.yml");
        //configDefault = YamlConfiguration.loadConfiguration(new InputStreamReader(configDefaultStream));
    }

    /**
     * Disables this module
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void disable() {
        config = null;
    }

    /**
     * Reloads the cache of the config file
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     */
    public static void reloadFile() {
        MCP.plugin.reloadConfig();
        config = MCP.plugin.getConfig();
        MCP.lang.setLang(lang());
        MCPDiscord.configreload();
    }

    /**
     * Gets the configured language
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return a string containing the language (ex "en_us")
     */
    public static String lang() {
        return config.getString("lang");
    }

    /**
     * Gets the shortened version of the configured language
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return a string containing the shortened language (ex "en")
     */
    public static String langShort() {
        return config.getString("lang").split("_")[0];
    }

    /**
     * Whether or not debug mode is configured
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return a boolean whether debug mode is configured
     */
    public static boolean debug() {
        return config.getBoolean("debug");
    }

    /**
     * Whether or not chat messages should be logged
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2021-01-22
     * @return a boolean whether chat messages should be logged
     */
    public static boolean logChat() {
        return config.getBoolean("logChat");
    }

    /**
     * Gets the configured sleep percent
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return the configured sleep percent
     */
    public static int sleepPercent() {
        return config.getInt("sleepPercent");
    }

    /**
     * Gets the configured afk timeout
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return the configured afk timeout
     */
    public static int afkTimeout() {
        return config.getInt("afkTimeout");
    }

    /**
     * Gets the configured maxumun nickname length
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return the configured maxumun nickname length
     */
    public static int nickMaxLength() {
        return config.getInt("nickMaxLength");
    }

    /**
     * Gets the configured boolean to enable discord verification
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return the configured boolean to enable discord verification
     */
    public static boolean enableDiscordVerification() {
        return config.getBoolean("discord.verificationEnabled");
    }

    /**
     * Gets the configured discord verification code length
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return the configured discord verification code length
     */
    public static int discordCodeLength() {
        return config.getInt("discord.codeLength");
    }

    /**
     * Whether or not a webhook should be used in the chat channel
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return the configured discord verification code length
     */
    public static boolean discordUseWebhook() {
        return config.getBoolean("discord.useWebhook");
    }

    /**
     * A webhook proxy in case directly connecting doesn't work
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return the configured discord verification code length
     */
    public static String discordWebhookProxy() {
        String proxyURL = config.getString("discord.webhookProxy");
        if (proxyURL.endsWith("/"))
            proxyURL = proxyURL.substring(0, proxyURL.length()-1);
        return proxyURL;
    }

    /**
     * Gets the configured discord bot token
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return the configured discord bot token
     */
    public static String discordToken() {
        return config.getString("discord.token");
    }

    /**
     * Gets the configured discord verification channel id
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return the configured discord verification channel id
     */
    public static String discordVerificationChannel() {
        return config.getString("discord.verificationChannel");
    }

    /**
     * Gets the configured discord chat channel id
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return the configured discord chat channel id
     */
    public static String discordChatChannel() {
        return config.getString("discord.chatChannel");
    }

    /**
     * Gets the configured verified role id
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2021-03-04
     * @return the configured verified role id
     */
    public static String discordVerifiedRole() {
        return config.getString("discord.verifiedRole");
    }

    /**
     * Gets the configured google api key
     *
     * @author Ashley Zomo
     * @version 1.0.0
     * @since 2020-12-03
     * @return the configured google api key
     */
    public static String googlAPIKey() {
        return config.getString("googleapikey");
    }

}
