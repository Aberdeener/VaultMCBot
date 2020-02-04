package me.aberdeener.vaultmcbot;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TokenValidator extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        User user = event.getAuthor();
        Message msg = event.getMessage();
        String message = event.getMessage().getContentStripped();
        MessageChannel channel = event.getChannel();

        if (event.isFromType(ChannelType.TEXT)) {

            if (user.isBot()) {
                return;
            }
            if (channel.getId().equalsIgnoreCase("643313973592195093")) {
                Member member = event.getMember();
                String name = member.getEffectiveName();
                validateToken(message, member, channel, msg);
            }
        }
    }

    private void validateToken(String message, Member member, MessageChannel channel, Message msg) {

        try {
            PreparedStatement select = VaultMCBot.connection
                    .prepareStatement("SELECT username, role FROM web_accounts WHERE token = ?");
            select.setString(1, message);
            ResultSet select_rs = select.executeQuery();

            if (select_rs.next()) {
                String nickname = select_rs.getString("username");
                System.out.println(member + " entered valid token. Nickname set to: " + nickname);
                member.modifyNickname(nickname).queue();
                member.getGuild().getTextChannelById("618221832801353728").sendMessage(member.getAsMention() + " Welcome to the Guild! Your nickname has been set to: `" + nickname + "`").queue();
                Role Players = member.getGuild().getRolesByName("Players", true).get(0);
                member.getGuild().addRoleToMember(member, Players).queue();
                msg.delete().queue();

                try {
                    PreparedStatement insert = VaultMCBot.connection
                            .prepareStatement("UPDATE players SET discord_id = ? WHERE token = ?");
                    insert.setString(1, member.getId());
                    insert.setString(2, message);
                    insert.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            else {
                channel.sendMessage(member.getAsMention() + ", that token is invalid. If you need help, ask a staff member!").queue();
                msg.delete().queue();
                System.out.println(member + " entered invalid token.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}