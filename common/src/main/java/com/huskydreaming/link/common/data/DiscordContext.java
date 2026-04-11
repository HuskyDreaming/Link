package com.huskydreaming.link.common.data;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public record DiscordContext(
        Guild guild,
        Member member,
        Role role
) { }
