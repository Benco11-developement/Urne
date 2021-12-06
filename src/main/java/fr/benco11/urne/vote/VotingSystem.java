package fr.benco11.urne.vote;

import org.javacord.api.entity.channel.Channel;

import java.util.Date;
import java.util.List;

public interface VotingSystem {
    String getName();
    String voteResult(List<Vote> votes);
    String template(boolean publicVote, List<String> proposals, Date endVote, Channel channel);
    List<String> formatProposals(String proposals);
    String proposalFormattedToRegex(List<String> proposals);
}
