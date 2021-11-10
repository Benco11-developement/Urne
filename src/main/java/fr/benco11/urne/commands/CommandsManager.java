package fr.benco11.urne.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandsManager {
    private final List<Command> commands;

    public CommandsManager() {
        commands = new ArrayList<>();
    }

    public void addCommand(Command command) {
        commands.add(command);
    }

    public void registerCommands(DiscordApi api) {
        api.bulkOverwriteGlobalSlashCommands(commands.stream().map(Command::getSlashCommandBuilder).collect(Collectors.toList())).join();
    }

    public void runCommand(String commandName, SlashCommandCreateEvent event) {
        commands.stream().filter(a -> a.getName().equals(commandName)).findAny().ifPresent(a -> a.run(event));
    }

    public void unregisterInvalidCommands(@org.jetbrains.annotations.NotNull List<SlashCommand> commands) {
        commands.stream().filter(a -> !commands.stream().anyMatch(b -> a.getName().equals(b.getName()))).forEach(a -> a.deleteGlobal().join());
    }
}
