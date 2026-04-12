package com.huskydreaming.link.common.discord.commands;

import com.huskydreaming.link.common.configuration.DiscordConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class SetupCommand extends ListenerAdapter {

    private final DiscordConfig discordConfig;

    public SetupCommand(DiscordConfig discordConfig) {
        this.discordConfig = discordConfig;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("setup")) return;

        var guild = event.getGuild();
        if (guild == null) {
            event.reply("Discord command guild is null!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        var embedConfig = discordConfig.embedConfig();
        var linkButton = Button.success("link_button", embedConfig.buttonLabel());

        var embedBuilder = new EmbedBuilder()
                .setTitle(embedConfig.title())
                .setDescription(embedConfig.description());

        for (var field : embedConfig.fields()) {
            embedBuilder.addField(field.name(), field.value(), field.inline());
        }

        try {
            embedBuilder.setColor(Color.decode(embedConfig.color()));
        } catch (NumberFormatException e) {
            embedBuilder.setColor(Color.decode("#97BA52"));
        }

        event.getChannel().sendMessageEmbeds(embedBuilder.build())
                .setComponents(ActionRow.of(linkButton))
                .queue();

        event.reply("Successfully set the account linking instructions in " + event.getChannel().getAsMention())
                .setEphemeral(true)
                .queue();
    }
}