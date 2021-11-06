package fr.benco11.urne;

import fr.benco11.urne.commands.CommandChannelUrne;
import fr.benco11.urne.commands.CommandVote;
import fr.benco11.urne.commands.CommandsManager;
import fr.benco11.urne.config.ServersConfiguration;
import fr.benco11.urne.config.UrneConfiguration;
import fr.benco11.urne.vote.AbsoluteMajority;
import fr.benco11.urne.vote.VotingSystem;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UrneBot {
    private UrneConfiguration botConfig;
    private ServersConfiguration serversConfig;
    private CommandsManager commandsManager;
    private DiscordApi api;
    private final List<VotingSystem> votingSystems;

    public UrneBot(UrneConfiguration botConfig) {
        this.botConfig = botConfig;
        try {
            serversConfig = new ServersConfiguration();
            serversConfig.init();
        } catch(SQLException | IOException | ClassNotFoundException e) {
            System.err.println("Une erreur s'est produite :");
            e.printStackTrace();
            System.exit(0);
        }
        commandsManager = new CommandsManager();
        votingSystems = new ArrayList<>();
    }

    public void init() {
        try {
            api = new DiscordApiBuilder().setToken(botConfig.getToken()).login().get();
            api.disconnect();
        } catch(InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        votingSystems.add(new AbsoluteMajority());
        api = new DiscordApiBuilder().setToken(botConfig.getToken()).login().join();
        commandsManager.addCommand(new CommandChannelUrne(serversConfig));
        commandsManager.addCommand(new CommandVote(votingSystems));
        try {
            api.getGlobalSlashCommands().thenAccept(commandsManager::unregisterInvalidCommands).get();
        } catch(InterruptedException | ExecutionException e) {
            System.err.println("Une erreur s'est produite :");
            e.printStackTrace();
        }
        commandsManager.registerCommands(api);
        api.addSlashCommandCreateListener(event -> commandsManager.runCommand(event.getSlashCommandInteraction().getCommandName(), event));
    }
}
