package fr.benco11.urne.commands;

import fr.benco11.urne.config.ServersConfiguration;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.*;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

public class CommandChannelUrne implements Command {

    private final ServersConfiguration serversConfig;

    public CommandChannelUrne(ServersConfiguration serversConfig) {
        this.serversConfig = serversConfig;
    }
    
    @Override
    public String getName() {
        return "urne";
    }

    @Override
    public SlashCommandBuilder getSlashCommandBuilder() {
        return SlashCommand.with(getName(), "Gère le channel d'urne", Arrays.asList(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "set", "Modifie le channel d'urne",
                Collections.singletonList(SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "CHANNEL", "Nouveau channel d'urne", true))), SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "get", "Renvoie le channel d'urne")));
    }

    @Override
    public void run(SlashCommandCreateEvent event) {
        event.getSlashCommandInteraction().respondLater().thenAccept(interaction -> {
            SlashCommandInteraction inter = event.getSlashCommandInteraction();
            EmbedBuilder builder = new EmbedBuilder().setColor(Color.BLUE).setTitle("Traitement...");
            interaction.addEmbed(builder).update();
            if(inter.getFirstOption().get().getFirstOptionChannelValue().isPresent()) {
                serversConfig.updateServerUrnChannel(inter.getServer().get().getId(),
                        inter.getFirstOption().get().getFirstOptionChannelValue().get().getId()).thenAccept(result -> {
                            builder.setColor((result) == 1 ? Color.GREEN : Color.RED).setTitle((result == 1) ? "Succès" : "Erreur")
                                    .setFooter((result == 1) ? "Le channel d'urne a bien été modifié !" : "Une erreur s'est produite !");
                            interaction.update();
                        }).join();
            } else {
                serversConfig.getServerUrnChannel(inter.getServer().get().getId()).thenAccept(a -> builder.setTitle("").setColor((a == 0) ? Color.ORANGE : Color.GREEN)
                        .setFooter((a == 0) ? "Impossible de déterminer le channel d'urne !" : "Le channel d'urne est : " + event.getSlashCommandInteraction().getChannel().get().asServerChannel().get().getName() + "")).thenApply(a -> interaction.update()).join();
            }

        });
    }
}
