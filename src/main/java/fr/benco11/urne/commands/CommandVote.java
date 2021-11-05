package fr.benco11.urne.commands;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOption;

import java.util.Arrays;

public class CommandVote implements Command {
    @Override
    public String getName() {
        return "vote";
    }

    @Override
    public SlashCommandBuilder getSlashCommandBuilder() {
        return SlashCommand.with(getName(), "Permet d'utiliser le système de votes", Arrays.asList(SlashCommandOption.create()));
    }

    @Override
    public void run(SlashCommandCreateEvent event) {

    }
}
