package com.huskydreaming.link.common.discord.listeners;

import com.huskydreaming.link.common.LinkCommonPlugin;
import com.huskydreaming.link.common.discord.events.LinkEvent;
import com.huskydreaming.link.common.discord.events.LinkEventBus;
import com.huskydreaming.link.common.services.interfaces.CodeService;
import com.huskydreaming.link.common.services.interfaces.DiscordService;
import com.huskydreaming.link.common.services.interfaces.LinkService;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;

public class ModalListener extends ListenerAdapter {

    private final CodeService codeService;
    private final DiscordService discordService;
    private final LinkService linkService;
    private final LinkEventBus linkEventBus;

    private final Logger logger;

    public ModalListener(LinkCommonPlugin plugin, Logger logger) {
        this.codeService = plugin.getCodeService();
        this.discordService = plugin.getDiscordService();
        this.linkService = plugin.getLinkService();
        this.linkEventBus = plugin.getLinkEventBus();

        this.logger = logger;
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("link_modal")) return;

        var linkCode = event.getValue("link_code");
        if (linkCode == null) {
            reply(event, "❌ Invalid link code.");
            return;
        }

        var codeString = linkCode.getAsString();
        var optionalUuid = codeService.consume(codeString);

        if (optionalUuid.isEmpty()) {
            reply(event, "❌ That code is invalid or expired.");
            return;
        }

        var uuid = optionalUuid.get();
        var discordId = event.getUser().getIdLong();

        event.deferReply(true).queue();

        linkService.link(uuid, discordId).thenAccept(result -> {
            switch (result) {
                case SUCCESS_REWARD -> {
                    editReply(event, "✅ Linked! You received your reward 🎉");
                    linkEventBus.fire(new LinkEvent(uuid, discordId));
                    discordService.addRole(discordId).exceptionally(ex -> {
                        event.reply("⚠️ Linked successfully, but failed to assign role. Please contact an administrator.")
                                .setEphemeral(true)
                                .queue();
                        return null;
                    });
                }
                case SUCCESS_NO_REWARD -> {
                    editReply(event, "✅ Linked! You have already received your reward.");
                    discordService.addRole(discordId).exceptionally(ex -> {
                        event.reply("⚠️ Linked successfully, but failed to assign role. Please contact an administrator.")
                                .setEphemeral(true)
                                .queue();
                        return null;
                    });
                }
                case ALREADY_LINKED -> editReply(event, "❌ This account is already linked.");
                case COOLDOWN -> editReply(event, "❌ You are on cooldown. Try again later.");
                default -> editReply(event, "❌ Unable to link. Try again from Minecraft.");
            }
        }).exceptionally(ex -> {
            logger.error("Error processing link for UUID: {} and Discord ID: {}", uuid, discordId, ex);
            editReply(event, "❌ A database error occurred. Please contact an administrator.");
            return null;
        });
    }

    private void reply(ModalInteractionEvent event, String message) {
        event.reply(message).setEphemeral(true).queue();
    }

    private void editReply(ModalInteractionEvent event, String message) {
        event.getHook().editOriginal(message).queue();
    }
}