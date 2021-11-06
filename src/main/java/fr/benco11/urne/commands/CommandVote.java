package fr.benco11.urne.commands;

import fr.benco11.urne.vote.VotingSystem;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandVote implements Command {

    private final List<VotingSystem> votingSystems;

    public CommandVote(List<VotingSystem> votingSystems) {
        this.votingSystems = votingSystems;
    }

    @Override
    public String getName() {
        return "vote";
    }

    @Override
    public SlashCommandBuilder getSlashCommandBuilder() {
        return SlashCommand.with(getName(), "Permet d'utiliser le système de votes", Arrays.asList(
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "for", "Vote", Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "poll-name", "Nom du vote", true),
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "vote", "Vote suivant le mode de scrutin", true)
                )),
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "poll", "Commence un vote configuré manuellement", Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "poll-name", "Nom du vote", true),
                        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "voting-system","Mode de scrutin", true,
                                votingSystems.stream().map(a -> SlashCommandOptionChoice.create(a.getName(), a.getName())).collect(Collectors.toList())),
                        SlashCommandOption.create(SlashCommandOptionType.BOOLEAN, "public-vote", "Si le vote de chaque personne est public ou non", true),
                        SlashCommandOption.create(SlashCommandOptionType.BOOLEAN, "use-urn", "Si le vote se fait par urne, sinon par réactions", true),
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "poll-end", "Date de fin du vote avec le format : dd-MM-yyyy;hh:mm:ss", true),
                        SlashCommandOption.create(SlashCommandOptionType.INTEGER, "template-message-id", "Message modèle que le bot utilisera lors de l'annonce du vote", false),
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "proposals", "Liste des différentes propositions sous la forme : prop1;prop2;prop3", true)
                )),
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "poll-model", "Commence un vote suivant un modèle", Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "model", "Modèle utilisé", true),
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "poll-name", "Nom du vote", true),
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "poll-end", "Date de fin du vote avec le format : dd-MM-yyyy;hh:mm:ss", true)
                )),
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "create-model", "Crée un modèle de vote", Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "model-name", "Nom du modèle", true),
                        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "voting-system","Mode de scrutin", true,
                                votingSystems.stream().map(a -> SlashCommandOptionChoice.create(a.getName(), a.getName())).collect(Collectors.toList())),
                        SlashCommandOption.create(SlashCommandOptionType.BOOLEAN, "public-vote", "Si le vote de chaque personne est public ou non", true),
                        SlashCommandOption.create(SlashCommandOptionType.BOOLEAN, "use-urn", "Si le vote se fait par urne, sinon par réactions", true),
                        SlashCommandOption.create(SlashCommandOptionType.INTEGER, "template-message-id", "Message modèle que le bot utilisera lors de l'annonce du vote", false)
                )),
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "delete-model", "Supprime un modèle de vote", Collections.singletonList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "model", "Modèle à supprimer")
                )),
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "end-poll", "Annule un vote (seul un administrateur du bot peut exécuter cette commande)", Collections.singletonList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "poll", "Vote à annuler")
                ))));
    }

    @Override
    public void run(SlashCommandCreateEvent event) {

    }
}
