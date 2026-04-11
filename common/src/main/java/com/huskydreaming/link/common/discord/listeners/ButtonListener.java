package com.huskydreaming.link.common.discord.listeners;

import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.modals.Modal;

public class ButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        var componentId = event.getComponentId();
        if (!componentId.equals("link_button")) return;

        var linkInput = TextInput.create("link_code", TextInputStyle.SHORT)
                .setPlaceholder("Minecraft Link Code")
                .setRequired(true)
                .build();

        var linkModal = Modal.create("link_modal", "Link Minecraft Account")
                .addComponents(Label.of("Minecraft Link Code", linkInput))
                .build();

        event.replyModal(linkModal).queue();
    }
}