package com.huskydreaming.link.common.discord.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class SetupCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        var guild = event.getGuild();
        if (guild == null) {
            event.reply("Discord command guild is null!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        var linkButton = Button.success("link_button", "✅ Authenticate Account");
        var linkEmbed = new EmbedBuilder()
                .setTitle("Account Linking")
                .setDescription("Link your account to get in game rewards!")
                .addField("Linking Guide:", " - Launch Minecraft\n - Join Wilden: `play.wilden.fun`\n - Run the link command `/link`\n - Input your Auth Code in <#1465655776306528353>", false)
                .addField("Linking Rewards:", " - \uD83E\uDE99 2500 Gold\n - \uD83D\uDC8E 10 Gems\n - \uD83E\uDEBD 5 Minutes of Flight time", false)
                .setColor(Color.decode("#97BA52"))
                .build();

        event.getChannel().sendMessageEmbeds(linkEmbed)
                .setComponents(ActionRow.of(linkButton))
                .queue();

        event.reply("Successfully set the account linking instructions in " + event.getChannel().getAsMention())
                .setEphemeral(true)
                .queue();
    }
}