package fr.benco11.urne.config;

import com.password4j.Password;
import fr.benco11.urne.database.Database;
import fr.benco11.urne.vote.VotingSystem;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;
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
                "                            urn_channel BIGINT,\n"+
                "                            vote_models TEXT(150)\n"+
                "                        );", false).join();
        database.update(
                "CREATE TABLE IF NOT EXISTS polls(\n"+
                        "                            name VARCHAR(30) PRIMARY KEY UNIQUE NOT NULL,\n" +
                        "                            use_urn BOOLEAN NOT NULL,\n"+
                        "                            public_vote BOOLEAN NOT NULL,\n"+
                        "                            poll_end_date DATETIME NOT NULL\n," +
                        "                            proposals TEXT(160) NOT NULL\n" +
                        "                        );", false).join();
        database.update(
                "CREATE TABLE IF NOT EXISTS votes(\n"+
                        "                            key TEXT PRIMARY KEY UNIQUE NOT NULL,\n" +
                        "                            voted_proposal TEXT(30) NOT NULL\n"+
                        "                        );", false).join();

    }

    public CompletableFuture<Integer> updateServerUrnChannel(long serverId, long channelId) {
        return database.update("INSERT INTO servers(server_id, urn_channel, vote_models) VALUES(?, ?, '') ON CONFLICT(server_id) DO UPDATE SET urn_channel = ?", false,
                serverId, channelId, channelId);
    }

    public CompletableFuture<Long> getServerUrnChannel(long serverId) {
        return CompletableFuture.supplyAsync(() -> {
            long value = 0L;
            try(ResultSet result = database.query(database.preparedStatement("SELECT urn_channel FROM servers WHERE server_id = ?", serverId)).join()) {
                if(result.next()) {
                    value = result.getLong("urn_channel");
                }
            } catch(SQLException e) {
                e.printStackTrace();
            }
            return value;
        });
    }

    public CompletableFuture<Byte> vote(long authorId, String poll, String votedProposal) {
        return CompletableFuture.supplyAsync(() -> {
            String key = poll + "\t" +Password.hash(Long.toHexString(authorId)).withBCrypt().getResult();
            byte value = 0;
            try(ResultSet resultPollCheck = database.query(database.preparedStatement("SELECT name FROM polls WHERE name = ? AND ? REGEXP proposals", poll, votedProposal)).join()) {
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
            if(pollName.length() > 30) return (byte) -1;
            return (byte) ((database.update("INSERT INTO polls(name, use_urn, public-vote, poll_end_date, proposals) VALUES(?, ?, ?, ?, ?)", false,
                    pollName, doUseUrn, isVotePublic, pollEnd, votingSystem.proposalFormattedToRegex(votingSystem.formatProposals(proposals))).join()) > -1 ? 1 : 0);
        });
    }
}
