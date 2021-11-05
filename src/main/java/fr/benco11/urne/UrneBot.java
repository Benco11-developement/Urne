package fr.benco11.urne;

import fr.benco11.urne.commands.CommandChannelUrne;
import fr.benco11.urne.commands.CommandsManager;
import fr.benco11.urne.config.ServersConfiguration;
import fr.benco11.urne.config.UrneConfiguration;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class UrneBot {
    private UrneConfiguration botConfig;
    private ServersConfiguration serversConfig;
    private CommandsManager commandsManager;
    private DiscordApi api;

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
    }

    public void init() {
        try {
            api = new DiscordApiBuilder().setToken(botConfig.getToken()).login().get();
            api.disconnect();
        } catch(InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        api = new DiscordApiBuilder().setToken(botConfig.getToken()).login().join();
        commandsManager = new CommandsManager();
        commandsManager.addCommand(new CommandChannelUrne(serversConfig));
        try {
            api.getGlobalSlashCommands().thenAccept(a -> commandsManager.unregisterInvalidCommands(a, api)).get();
        } catch(InterruptedException | ExecutionException e) {
            System.err.println("Une erreur s'est produite :");
            e.printStackTrace();
        }
        commandsManager.registerCommands(api);
        api.addSlashCommandCreateListener(event -> commandsManager.runCommand(event.getSlashCommandInteraction().getCommandName(), event));
    }
}
