package fr.benco11.urne.vote;

import fr.benco11.urne.utils.UrneUtils;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AbsoluteMajority implements VotingSystem {

    @Override
    public String getName() {
        return "Majorité absolue";
    }

    @Override
    public String voteResult(List<Vote> votes) {
        return votes.stream().collect(Collectors.groupingBy(Vote::getProposal)).entrySet().stream().map(a -> new AbstractMap.SimpleEntry<>(a.getKey(), a.getValue().size()))
                .filter(a -> a.getValue() >= votes.size() * 0.51d).map(AbstractMap.SimpleEntry::getKey).findAny().orElse(null);
    }

    @Override
    public String template(boolean publicVote, List<String> proposalsList, Date endVote, Channel channel) {
        boolean useUrn = channel != null;
        MessageBuilder b = new MessageBuilder().append("Vote par **").append((useUrn) ? "urne " : "réactions ").append(((publicVote) ? "publique" : "privé"))
                .append("**\nLe mode de scrutin utilisé est la ").append("majorité absolue ", MessageDecoration.BOLD).append(": ").append("la proposition gagnante est celle dont le nombre de vote est supérieur ou égale à la moitié des voix + 1", MessageDecoration.ITALICS, MessageDecoration.UNDERLINE).append(".\nLe vote ")
                .append("se terminera ", MessageDecoration.BOLD).append("le ").append(UrneUtils.DateUtils.POLL_END_GOOD_FORMAT.format(endVote), MessageDecoration.BOLD).append("\nLe vote se fera par ");
        if(useUrn) {
            b.append("le biais du channel ").append(channel.asServerTextChannel().get().getName(), MessageDecoration.BOLD).append(" comme urne. Voici la liste des ").append("différentes options", MessageDecoration.BOLD)
                    .append(" :\n").append(String.join("\n", proposalsList), MessageDecoration.ITALICS, MessageDecoration.BOLD, MessageDecoration.UNDERLINE);
        } else {
            b.append("réactions", MessageDecoration.BOLD).append(".\n");
            UrneUtils.proposalsToEmojisMap(proposalsList).forEach((emoji, proposal) -> b.append(emoji).append(" = ").append(proposal + "\n", MessageDecoration.BOLD, MessageDecoration.ITALICS, MessageDecoration.UNDERLINE));
        }
        return b.getStringBuilder().toString();
    }

    @Override
    public List<String> formatProposals(String proposals) {
        return Arrays.asList(proposals.split(";"));
    }

    @Override
    public String proposalFormattedToRegex(List<String> proposals) {
        return "^[ ]{0,}(" + String.join("|", proposals) + ")[ ]{0,}$";
    }
}
