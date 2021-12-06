package fr.benco11.urne.commands;

import fr.benco11.urne.UrneBot;
import fr.benco11.urne.config.ServersConfiguration;
import fr.benco11.urne.vote.VotingSystem;
import fr.benco11.urne.utils.UrneUtils;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.ParseException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class CommandVote implements Command {

    private final List<VotingSystem> votingSystems;
    private final ServersConfiguration serversConfig;
    private final UrneBot urneBot;

    public CommandVote(List<VotingSystem> votingSystems, ServersConfiguration serversConfig, UrneBot bot) {
        this.votingSystems = votingSystems;
        this.serversConfig = serversConfig;
        this.urneBot = bot;
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
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "get", "Renvoie un vote au choix", Collections.singletonList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "poll-name", "Nom du vote", true)
                )),
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "poll", "Commence un vote configuré manuellement", Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "poll-name", "Nom du vote", true),
                        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "voting-system","Mode de scrutin", true,
                                votingSystems.stream().map(a -> SlashCommandOptionChoice.create(a.getName(), a.getName())).collect(Collectors.toList())),
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "poll-end", "Date de fin du vote avec le format : dd/MM/yyyy-hh:mm:ss", true),
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "proposals", "Différentes propositions (max 25) avec le format : prop1;prop2;prop3", true),
                        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "public-vote", "Si le vote de chaque personne est public ou non (par défaut non)", false,
                                Arrays.asList(SlashCommandOptionChoice.create("False", "False"), SlashCommandOptionChoice.create("True", "True"))),
                        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "use-urn", "Si le vote se fait par urne, sinon par réactions (réactions par défaut)", false,
                                Arrays.asList(SlashCommandOptionChoice.create("False", "False"), SlashCommandOptionChoice.create("True", "True"))),
                        SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "template-message-channel", "Channel du message modèle (OBLIGATOIRE POUR UTILISER UN MESSAGE MODÈLE)", false),
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "template-message-id", "ID du message modèle que le bot utilisera lors de l'annonce du vote", false),
                        SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "urn-channel", "Channel d'urne à utiliser", false)
                )),
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "poll-model", "Commence un vote suivant un modèle", Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "model", "Modèle à utiliser", true),
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "poll-name", "Nom du vote", true),
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "poll-end", "Date de fin du vote avec le format : dd/MM/yyyy-hh:mm:ss", true)
                )),
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "polls-list", "Renvoie la liste des votes"),
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "create-model", "Crée un modèle de vote", Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "model-name", "Nom du modèle", true),
                        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "voting-system","Mode de scrutin", true,
                                votingSystems.stream().map(a -> SlashCommandOptionChoice.create(a.getName(), a.getName())).collect(Collectors.toList())),
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "proposals", "Différentes propositions (max 25) avec le format : prop1;prop2;prop3", true),
                        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "public-vote", "Si le vote de chaque personne est public ou non (par défaut non)", false,
                                Arrays.asList(SlashCommandOptionChoice.create("False", "False"), SlashCommandOptionChoice.create("True", "True"))),
                        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "use-urn", "Si le vote se fait par urne, sinon par réactions (réactions par défaut)", false,
                                Arrays.asList(SlashCommandOptionChoice.create("False", "False"), SlashCommandOptionChoice.create("True", "True"))),
                        SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "template-message-channel", "Channel du message modèle (OBLIGATOIRE POUR UTILISER UN MESSAGE MODÈLE)", false),
                        SlashCommandOption.create(SlashCommandOptionType.INTEGER, "template-message-id", "Message modèle que le bot utilisera lors de l'annonce du vote", false),
                        SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "urn-channel", "Channel d'urne à utiliser", false)
                )),
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "delete-model", "Supprime un modèle de vote", Collections.singletonList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "model", "Modèle à supprimer", true)
                )),
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "end-poll", "Annule un vote (seul un administrateur du bot peut exécuter cette commande)", Collections.singletonList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "poll", "Vote à annuler", true)
                ))));
    }

    @Override
    public void run(@NotNull SlashCommandCreateEvent event) {
            SlashCommandInteractionOption firstOption = event.getSlashCommandInteraction().getFirstOption().get();
            switch(firstOption.getName()) {
                case "for":
                    switch(serversConfig.vote(event.getInteraction().getUser().getId(), firstOption.getFirstOptionStringValue().get(),
                            firstOption.getSecondOptionStringValue().get()).join()) {
                        case -1:
                            event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Erreur").setFooter("Une erreur s'est produite !\nVeuillez vérifier le nom du vote et le format de votre vote puis réessayez !")
                                    .setColor(Color.RED)).setFlags(MessageFlag.EPHEMERAL).respond();
                            break;
                        case 0:
                            event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Erreur").setFooter("Une erreur s'est produite avec la base de données ! Veuillez contacter l'administrateur du bot !")
                                    .setColor(Color.RED)).setFlags(MessageFlag.EPHEMERAL).respond();
                            break;
                        case 1:
                            event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Erreur").setFooter("Une erreur s'est produite lors de la mise à jour de votre vote !")
                                    .setColor(Color.RED)).setFlags(MessageFlag.EPHEMERAL).respond();
                            break;
                        case 2:
                            event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Succès").setFooter("Votre vote a bien été mis à jour et comptabilisé !")
                                    .setColor(Color.GREEN)).setFlags(MessageFlag.EPHEMERAL).respond();
                            break;
                    }
                    break;
                case "get":
                    Map.Entry<Byte, String> vote = serversConfig.getVote(event.getInteraction().getUser().getId(), firstOption.getFirstOptionStringValue().get()).join();
                    switch(vote.getKey()) {
                        case -1:
                            event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Erreur").setFooter("Une erreur s'est produite !\nVeuillez vérifier le nom du vote !")
                                    .setColor(Color.RED)).setFlags(MessageFlag.EPHEMERAL).respond();
                            break;
                        case 0:
                            event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Erreur").setFooter("Une erreur s'est produite avec la base de donnée ! Veuillez contacter l'administrateur du bot !")
                                    .setColor(Color.RED)).setFlags(MessageFlag.EPHEMERAL).respond();
                            break;
                        case 1:
                            event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Erreur").setFooter("Une erreur s'est produite, votre vote n'a pas été trouvé ! Avez-vous voté ?")
                                    .setColor(Color.ORANGE)).setFlags(MessageFlag.EPHEMERAL).respond();
                            break;
                        case 2:
                            event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Succès").setFooter("Voici votre vote " + vote.getValue())
                                    .setColor(Color.GREEN)).setFlags(MessageFlag.EPHEMERAL).respond();
                            break;
                    }
                    break;
                case "poll":
                    try {
                        VotingSystem votingSystem = votingSystems.stream().filter(a -> a.getName().equals(firstOption.getSecondOptionStringValue().get())).findAny().get();
                        boolean publicVote = Boolean.parseBoolean(firstOption.getOptionStringValueByName("public-vote").orElse("false").toLowerCase());
                        boolean useUrn = Boolean.parseBoolean(firstOption.getOptionStringValueByName("use-urn").orElse("false").toLowerCase());
                        Channel urnChannel = firstOption.getOptionChannelValueByName("urn-channel").orElse((useUrn) ? event.getInteraction().getChannel().get().asServerChannel().get() : null);
                        Date pollEnd = UrneUtils.DateUtils.POLL_END_FORMAT.parse(firstOption.getOptionStringValueByName("poll-end").get());
                        String templateMessageId;
                        Channel templateMessageChannel;
                        Message template = null;
                        if((templateMessageId = firstOption.getOptionStringValueByName("template-message-id").orElse(null)) != null) {
                            if((templateMessageChannel = firstOption.getOptionChannelValueByName("template-message-channel").orElse(null)) == null) {
                                event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Erreur").setFooter("Le channel du message modèle n'a pas été rentré/est invalide !")
                                        .setColor(Color.RED)).setFlags(MessageFlag.EPHEMERAL).respond();
                                break;
                            } else {
                                if(!UrneUtils.isLong(templateMessageId) || (template = event.getApi().getMessageById(templateMessageId, templateMessageChannel.asTextChannel().get()).join()) == null) {
                                    event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Erreur").setFooter("L'id du message modèle est invalide !")
                                            .setColor(Color.RED)).setFlags(MessageFlag.EPHEMERAL).respond();
                                    break;
                                }
                            }
                        }

                        String pollName = firstOption.getFirstOptionStringValue().get();

                        switch(serversConfig.startVote(pollName, votingSystem, publicVote, useUrn, pollEnd,
                                firstOption.getOptionStringValueByName("proposals").get()).join()) {
                            case -2:
                                event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Erreur").setFooter("Une erreur s'est produite : vous avez saisi plus de 25 propositions !")
                                        .setColor(Color.RED)).setFlags(MessageFlag.EPHEMERAL).respond();
                                break;
                            case -1:
                                event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Erreur").setFooter("Une erreur s'est produite : le nom du vote dépasse 30 caractères !")
                                        .setColor(Color.RED)).setFlags(MessageFlag.EPHEMERAL).respond();
                                break;
                            case 0:
                                event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Erreur").setFooter("Une erreur inconnue s'est produite : il est possible qu'un vote avec le même nom soit en cours !")
                                        .setColor(Color.RED)).setFlags(MessageFlag.EPHEMERAL).respond();
                                break;
                            case 1:
                                event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Succès").setFooter("Le vote a bien été lancé !")
                                        .setColor(Color.GREEN)).setFlags(MessageFlag.EPHEMERAL).respond();
                                urneBot.scheduleVoteCount(pollEnd, pollName, votingSystem, a -> new MessageBuilder().addEmbed(new EmbedBuilder().setColor(Color.GREEN).setTitle("Résultats de '" + pollName + "'")
                                        .setFooter("Voici les résultats du vote '" + pollName + "' qui vient de se terminer :\n\n" + ((a == null) ? "Il n'y a pas eu de vainqueurs !" : a))).send(event.getInteraction().getChannel().get()));
                                List<String> proposalsList = votingSystem.formatProposals(firstOption.getOptionStringValueByName("proposals").get());
                                if(template != null) {
                                    new MessageBuilder().copy(template).append("\n").append(votingSystem.template(publicVote, proposalsList, pollEnd, urnChannel))
                                            .send(event.getInteraction().getChannel().get());
                                } else {
                                    new MessageBuilder().addEmbed(new EmbedBuilder().setTitle("Vote " + pollName).setColor(UrneUtils.getRandomVoteColor())
                                                    .setDescription("Un nouveau vote **'" + pollName + "'** a été lancé par " + event.getInteraction().getUser().getName()+" !").addField("__Détails du vote :__", votingSystem.template(publicVote, proposalsList, pollEnd, urnChannel)))
                                            .send(event.getInteraction().getChannel().get()).thenAccept(a -> a.addReactions(UrneUtils.proposalsToEmojisMap(proposalsList).keySet().toArray(new String[proposalsList.size()]))).join();

                                }
                                break;
                        }
                    } catch(ParseException e) {
                        event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Erreur").setFooter("Une erreur s'est produite : le format de la date est invalide !")
                                .setColor(Color.ORANGE)).setFlags(MessageFlag.EPHEMERAL).respond();
                    }
                    break;
                default:
                    event.getSlashCommandInteraction().createImmediateResponder().addEmbed(new EmbedBuilder().setTitle("Commande non-implémentée !")).setFlags(MessageFlag.EPHEMERAL).respond();
                    break;
            }
    }
}
