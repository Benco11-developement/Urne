package fr.benco11.urne.vote;

import java.util.AbstractMap;
import java.util.List;
import java.util.stream.Collectors;

public class AbsoluteMajority implements VotingSystem {

    @Override
    public String getName() {
        return "majorité absolue";
    }

    @Override
    public String voteResult(List<Vote> votes) {
        return votes.stream().collect(Collectors.groupingBy(Vote::getProposal)).entrySet().stream().map(a -> new AbstractMap.SimpleEntry<>(a.getKey(), a.getValue().size()))
                .filter(a -> a.getValue() >= votes.size() * 0.51d).map(AbstractMap.SimpleEntry::getKey).findAny().orElse(null);
    }
}
