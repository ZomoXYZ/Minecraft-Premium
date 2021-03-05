package dev.zomo.mcpremium;

import com.google.cloud.translate.Translation;

import org.jsoup.Jsoup;

import dev.zomo.MCLang.LangTemplate;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.chat.BaseComponent;

public class MCPDiscordEvents extends ListenerAdapter {

    public boolean isVerificaitonChannel(String id) {

        if (id.equals(MCPDiscord.verificationChannel.getId()))
            return true;

        return false;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (MCPDiscord.verificationChannel != null && isVerificaitonChannel(event.getChannel().getId())) {

            String message = event.getMessage().getContentRaw().toLowerCase();
            if (message.startsWith(MCP.lang.string("discord.command").toLowerCase())) {
                message = message.substring(MCP.lang.string("discord.command").length()).trim();

                int verification = MCPDiscord.verifyUser(event.getAuthor().getId(), message);

                String toSend = "";

                LangTemplate template = MCPDiscord.userTemplate(event.getAuthor());

                if (verification == 0)
                    toSend = MCP.lang.string("discord.verification.invalidCode", template);
                else if (verification == 1)
                    toSend = MCP.lang.string("discord.verification.verificationSucceed", template);
                else if (verification == 2)
                    toSend = MCP.lang.string("discord.verification.previouslyVerified", template);
                
                event.getChannel().sendMessage(toSend).queue();

            }

        } else if (MCPDiscord.chatChannel != null && event.getChannel().getId().equals(MCPDiscord.chatChannel.getId()) && !event.getAuthor().isBot()) {

            String messageContent = event.getMessage().getContentStripped().replaceAll("\\\\", "\\\\\\\\");

            if (messageContent.length() > 0) {

                String messageTranslatedStr = "";

                if (MCP.translate != null) {

                    Translation messageTranslated = MCP.translate.translate(messageContent);

                    String tempMessageTranslate = Jsoup.parse(messageTranslated.getTranslatedText()).text();

                    if (!messageTranslated.getSourceLanguage().equals(MCPConfig.langShort())
                            && !tempMessageTranslate.toLowerCase().equals(messageContent.toLowerCase())) {

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
                                .add("message", tempMessageTranslate);

                        /*
                         * <template> from: input language to: output language message: translated
                         * message content
                         */

                        messageTranslatedStr = MCP.lang.string("translate.response", transTemplate);
                    }

                }

                LangTemplate template = MCPDiscord.memberTemplate(event.getMember())
                    .add("message", messageContent)
                    .add("translation", messageTranslatedStr);

                /*
                * <template> (from afk.*)
                * message: message
                */

                BaseComponent[] message = MCP.genChatMessage(template, "discord.chat.onMinecraft.chat");

                MCP.server.broadcast(message);
                
                if (MCPConfig.logChat())
                    MCP.log(MCP.stripColors(MCP.lang.string("discord.chat.onMinecraft.chat.chat", template)).replaceAll("\\|", ""));
                
            }

        }

    }
}