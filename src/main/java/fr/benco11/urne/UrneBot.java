package fr.benco11.urne;


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
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class UrneBot {
    private final UrneConfiguration botConfig;
    private ServersConfiguration serversConfig;
    private final CommandsManager commandsManager;
    private DiscordApi api;
    private final List<VotingSystem> votingSystems;
    private final ScheduledExecutorService executor;
    private final Map<String, ScheduledFuture<?>> voteCount;

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
        voteCount = new HashMap<>();
        executor = Executors.newScheduledThreadPool(5);
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
        commandsManager.addCommand(new CommandVote(votingSystems, serversConfig, this));
        try {
            api.getGlobalSlashCommands().thenAccept(commandsManager::unregisterInvalidCommands).get();
        } catch(InterruptedException | ExecutionException e) {
            System.err.println("Une erreur s'est produite :");
            e.printStackTrace();
        }
        commandsManager.registerCommands(api);
        api.addSlashCommandCreateListener(event -> commandsManager.runCommand(event.getSlashCommandInteraction().getCommandName(), event));
    }

    public void scheduleVoteCount(Date pollEnd, String pollName, VotingSystem votingSystem, Consumer<String> result) {
        voteCount.put(pollName, executor.schedule(() -> {
            result.accept(serversConfig.getVotes(pollName).thenApply(votingSystem::voteResult).join());
            voteCount.remove(pollName);
        }, Duration.between(Instant.now(), pollEnd.toInstant()).getSeconds(), TimeUnit.SECONDS));
    }

    public void cancelVoteCount(String pollName, Runnable r) {
        ScheduledFuture<?> future;
        if((future = voteCount.getOrDefault(pollName, null)) != null) {
            future.cancel(false);
            voteCount.remove(pollName);
            r.run();
        }
    }
}
