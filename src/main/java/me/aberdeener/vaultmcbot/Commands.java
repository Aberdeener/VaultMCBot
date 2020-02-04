package me.aberdeener.vaultmcbot;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Commands extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        String[] args = event.getMessage().getContentRaw().split(" ");
        Message message = event.getMessage();
        String msg = event.getMessage().getContentStripped();
        Member member = event.getMember();
        TextChannel channel = event.getTextChannel();

        if (message.getAuthor().isBot()) {
            return;
        }

        if (msg.contains("!purge")) {
            if (member.getRoles().toString().contains("staff")) {
                if (args.length == 2 && !args[1].equals("1")) {
                    try {
                        System.out.println(member + " purged " + args[1] + " messages from the " + event.getTextChannel().getName() + " channel");
                        event.getTextChannel().deleteMessages(event.getChannel().getHistory().retrievePast(Integer.parseInt(args[1])).complete()).queue();
                        return;
                    } catch (NumberFormatException e) {
                        event.getTextChannel().sendMessage(member.getAsMention() + " you must supply a number of messages to purge. (Minumum 2)").queue();
                        return;
                    }
                } else {
                    event.getTextChannel().sendMessage(member.getAsMention() + " you must supply a number of messages to purge. (Minumum 2)").queue();
                    return;
                }
            }
        }

        if (channel.getId().equalsIgnoreCase("626594297835290624")) {

            if (msg.equals("!help")) {
                System.out.println(member + " used help command");
                member.getGuild().getTextChannelById("626594297835290624").sendMessage(member.getAsMention() + " here are some helpful commands for the VaultMC Discord Bot:\n" +
                        "`!help` Shows you all available commands." +
                        "\n`!updateme` Changed your Minecraft name or got a new rank on the server? Update it in this guild using this command.").queue();
                return;
            }

            if (msg.equals("!updateme")) {
                updateMember(member);
            }

            else {
                System.out.println(member + " tried to talk in bot channel");
                message.delete().queue();
                member.getGuild().getTextChannelById("626594297835290624").sendMessage(member.getAsMention() + " this channel is only for bot commands!").queue();
                return;
            }
        }
    }

    public static void updateMember(Member member) {

        try {
            PreparedStatement select = VaultMCBot.connection
                    .prepareStatement("SELECT username, rank FROM players WHERE discord_id = ?");
            select.setString(1, member.getId());
            ResultSet rs = select.executeQuery();

            if (rs.next()) {

                String nickname = rs.getString("username");
                String rank = rs.getString("rank");

                // if their username and rank are the same
                if (nickname.equals(member.getEffectiveName()) && member.getRoles().toString().contains(rank)) {
                    System.out.println(member + " tried to update their username or rank but their minecraft name or rank is the same.");
                    member.getGuild().getTextChannelById("626594297835290624").sendMessage(member.getAsMention() + " your Minecraft name and rank have not been changed.").queue();
                    return;
                }

                // if username and rank are different
                if (!member.getEffectiveName().toString().equals(nickname) && !member.getRoles().toString().contains(rank)) {

                    updateNickName(member, nickname);

                    if (rank.equalsIgnoreCase("default")) {
                        Role role = member.getGuild().getRolesByName("default", true).get(0);
                        Role removeRole = null;
                        updateRole(member, role, removeRole);
                        return;
                    }
                    if (rank.equalsIgnoreCase("member")) {
                        Role role = member.getGuild().getRolesByName("member", true).get(0);
                        Role removeRole = member.getGuild().getRolesByName("default", true).get(0);
                        updateRole(member, role, removeRole);
                        return;
                    }
                    if (rank.equalsIgnoreCase("patreon")) {
                        Role role = member.getGuild().getRolesByName("patreon", true).get(0);
                        Role removeRole = member.getGuild().getRolesByName("member", true).get(0);
                        updateRole(member, role, removeRole);
                        return;
                    }
                    if (rank.equalsIgnoreCase("trusted")) {
                        Role role = member.getGuild().getRolesByName("trusted", true).get(0);
                        Role removeRole = member.getGuild().getRolesByName("patreon", true).get(0);
                        updateRole(member, role, removeRole);
                        return;
                    }
                    return;
                }

                //if username is same but rank is different
                if (member.getEffectiveName().toString().equals(nickname) && !member.getRoles().toString().contains(rank)) {

                    if (rank.equalsIgnoreCase("default")) {
                        Role role = member.getGuild().getRolesByName("default", true).get(0);
                        Role removeRole = null;
                        updateRole(member, role, removeRole);
                        return;
                    }
                    if (rank.equalsIgnoreCase("member")) {
                        Role role = member.getGuild().getRolesByName("member", true).get(0);
                        Role removeRole = member.getGuild().getRolesByName("default", true).get(0);
                        updateRole(member, role, removeRole);
                        return;
                    }
                    if (rank.equalsIgnoreCase("patreon")) {
                        Role role = member.getGuild().getRolesByName("patreon", true).get(0);
                        Role removeRole = member.getGuild().getRolesByName("member", true).get(0);
                        updateRole(member, role, removeRole);
                        return;
                    }
                    if (rank.equalsIgnoreCase("trusted")) {
                        Role role = member.getGuild().getRolesByName("trusted", true).get(0);
                        Role removeRole = member.getGuild().getRolesByName("patreon", true).get(0);
                        updateRole(member, role, removeRole);
                        return;
                    }
                    return;
                }

                // if username is different but rank is same.
                if (!member.getEffectiveName().toString().equals(nickname) && member.getRoles().toString().contains(rank)) {
                    updateNickName(member, nickname);
                    return;
                }

                else {
                    member.getGuild().getTextChannelById("626594297835290624").sendMessage(member.getAsMention() + " an unknown error occured, please contact an Administrator.").queue();
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateNickName(Member member, String nickname) {
        System.out.println(member + "'s nickname set to " + nickname);
        member.getGuild().getTextChannelById("626594297835290624").sendMessage(member.getAsMention() + " your nickname has been updated to `" + nickname + "`").queue();
        member.modifyNickname(nickname).queue();
        return;
    }

    private static void updateRole(Member member, Role role, Role removeRole) {
        System.out.println(member + "'s rank set to " + role.toString());
        member.getGuild().getTextChannelById("626594297835290624").sendMessage(member.getAsMention() + " your rank has been updated to `" + role.getName() + "`").queue();
        if (removeRole == null) {
            member.getGuild().addRoleToMember(member, role).queue();
            return;
        }
        member.getGuild().removeRoleFromMember(member, removeRole).queue();
        member.getGuild().addRoleToMember(member, role).queue();
        return;
    }
}
