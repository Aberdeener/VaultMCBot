package me.aberdeener.vaultmcbot;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Runnables extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        for (Member member : guild.getMembers()) {
            Commands.updateMember(member);
        }
    }
}
