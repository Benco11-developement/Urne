package fr.benco11.urne.config;

import com.password4j.Password;
import fr.benco11.urne.database.Database;
import fr.benco11.urne.vote.Vote;
import fr.benco11.urne.vote.VotingSystem;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ServersConfiguration {
    private final Database database;

    public ServersConfiguration() throws SQLException, IOException, ClassNotFoundException {
        this.database = new Database();
    }

    public void init() {
        database.initFunctions();
        database.update(
                "CREATE TABLE IF NOT EXISTS servers(\n"+
                "                            server_id BIGINT PRIMARY KEY UNIQUE NOT NULL,\n"+
                "                            vote_models TEXT(150)\n"+
                "                        );", false).join();
        database.update(
                "CREATE TABLE IF NOT EXISTS polls(\n"+
                        "                            name VARCHAR(30) PRIMARY KEY UNIQUE NOT NULL,\n" +
                        "                            use_urn BOOLEAN NOT NULL,\n"+
                        "                            urn_channel BIGINT,\n" +
                        "                            public_vote BOOLEAN NOT NULL,\n"+
                        "                            poll_end_date DATETIME NOT NULL,\n" +
                        "                            proposals TEXT(500) NOT NULL,\n" +
                        "                            proposals_regex TEXT(550) NOT NULL\n" +
                        "                        );", false).join();
        database.update(
                "CREATE TABLE IF NOT EXISTS votes(\n"+
                        "                            key TEXT PRIMARY KEY UNIQUE NOT NULL,\n" +
                        "                            voted_proposal TEXT(30) NOT NULL\n"+
                        "                        );", false).join();

    }

    public CompletableFuture<Byte> vote(long authorId, String poll, String votedProposal) {
        return CompletableFuture.supplyAsync(() -> {
            String key = poll + "\t" +Password.hash(Long.toHexString(authorId)).withBCrypt().getResult();
            byte value = 0;
            try(ResultSet resultPollCheck = database.query(database.preparedStatement("SELECT name FROM polls WHERE name = ? AND ? REGEXP proposals_regex", poll, votedProposal)).join()) {
                if(resultPollCheck.next()) {
                    value = 1;
                    database.update("INSERT INTO votes(key, voted_proposal) VALUES(?, ?) ON CONFLICT(key) DO UPDATE SET voted_proposal = ?",
                            false, key, votedProposal, votedProposal).join();
                    value = 2;
                } else {
                    value = -1;
                }
            } catch(SQLException e) {
                e.printStackTrace();
            }
            return value;
        });
    }

    public CompletableFuture<Map.Entry<Byte, String>> getVote(long authorId, String poll) {
        return CompletableFuture.supplyAsync(() -> {
            String key = poll + "\t" +Password.hash(Long.toHexString(authorId)).withBCrypt().getResult();
            String vote = null;
            byte value = 0;
            try(ResultSet resultPollCheck = database.query(database.preparedStatement("SELECT name FROM polls WHERE name = ?", poll)).join()) {
                if(resultPollCheck.next()) {
                    value = 1;
                    ResultSet voteResultSet = database.query(database.preparedStatement("SELECT voted_proposal FROM votes WHERE key = ?", key)).join();
                    if(voteResultSet.next()) {
                        vote = voteResultSet.getNString("voted_proposal");
                        value = 2;
                    }
                    voteResultSet.close();
                } else {
                    value = -1;
                }
            } catch(SQLException e) {
                e.printStackTrace();
            }
            return new AbstractMap.SimpleEntry<>(value, vote);
        });
    }

    public CompletableFuture<Byte> startVote(String pollName, VotingSystem votingSystem, boolean isVotePublic, boolean doUseUrn, Date pollEnd, String proposals) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> proposalsList = votingSystem.formatProposals(proposals);
            if(proposalsList.size() > 25) return (byte) -2;
            if(pollName.length() > 30) return (byte) -1;
            return (byte) ((database.update("INSERT or IGNORE INTO polls(name, use_urn, public_vote, poll_end_date, proposals, proposals_regex) VALUES(?, ?, ?, ?, ?, ?)", false,
                    pollName, doUseUrn, isVotePublic, pollEnd, proposals, votingSystem.proposalFormattedToRegex(proposalsList)).join()) > -1 ? 1 : 0);
        });
    }

    public CompletableFuture<List<Vote>> getVotes(String pollName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Vote> result = new ArrayList<>();
                try(ResultSet r = database.query(database.preparedStatement("SELECT voted_proposal FROM votes WHERE key LIKE ?", pollName + "%")).join()) {
                    while(r.next()) result.add(Vote.getVote(r.getNString("voted_proposal"), pollName));
                }
                return result;
            } catch(SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
