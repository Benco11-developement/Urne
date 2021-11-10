package fr.benco11.urne.vote;

import java.util.List;

public interface VotingSystem {
    String getName();
    String voteResult(List<Vote> votes);
    List<String> formatProposals(String proposals);
    String proposalFormattedToRegex(List<String> proposals);
}
