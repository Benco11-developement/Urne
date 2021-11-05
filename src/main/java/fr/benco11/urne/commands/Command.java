package fr.benco11.urne.commands;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandBuilder;

public interface Command {
    String getName();
    SlashCommandBuilder getSlashCommandBuilder();
    void run(SlashCommandCreateEvent event);
}
