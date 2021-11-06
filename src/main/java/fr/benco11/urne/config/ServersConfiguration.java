package fr.benco11.urne.config;

import fr.benco11.urne.database.Database;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class ServersConfiguration {
    private Database database;

    public ServersConfiguration() throws SQLException, IOException, ClassNotFoundException {
        this.database = new Database();
    }

    public void init() {
        database.update(
                "CREATE TABLE IF NOT EXISTS servers(\n"+
                "                            server_id BIGINT PRIMARY KEY UNIQUE NOT NULL,\n"+
                "                            urn_channel BIGINT,\n"+
                "                            vote_models TEXT(150)\n"+
                "                        );", false).join();
        database.update(
                "CREATE TABLE IF NOT EXISTS polls(\n"+
                        "                            name VARCHAR(30) PRIMARY KEY UNIQUE NOT NULL,\n" +
                        "                            poll_message_id BIGINT NOT NULL,\n"+
                        "                            use_urn BOOLEAN NOT NULL,\n"+
                        "                            public_vote BOOLEAN NOT NULL,\n"+
                        "                            poll_end_date DATETIME NOT NULL,\n" +
                        "                            votes TEXT\n" +
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
}
