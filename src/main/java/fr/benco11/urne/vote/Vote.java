package fr.benco11.urne.vote;

public interface Vote {
    String getProposal();
    String getPollName();


    static Vote getVote(String proposal, String poll) {
        return new Vote() {
            @Override
            public String getProposal() {
                return proposal;
            }

            @Override
            public String getPollName() {
                return poll;
            }
        };
    }
}
