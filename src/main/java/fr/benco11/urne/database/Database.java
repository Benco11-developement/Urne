package fr.benco11.urne.database;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class Database {
    private Connection connection;

    public Database() throws ClassNotFoundException, IOException, SQLException {
        File f = new File(System.getProperty("user.dir"), "database.sqlite");
        if(!f.exists()) f.createNewFile();
        Class.forName("org.sqlite.JDBC");;
        connection = DriverManager.getConnection("jdbc:sqlite:database.sqlite");
    }

    public PreparedStatement preparedStatement(String sql, Object... args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for(int i = 0; i < args.length; i++) statement.setObject(i+1, args[i]);
        return statement;
    }

    public CompletableFuture<ResultSet> query(PreparedStatement statement) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return statement.executeQuery();
            } catch(SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public CompletableFuture<Integer> update(String sql, boolean isLargeUpdate, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try(PreparedStatement statement = preparedStatement(sql, args)) {
                if(isLargeUpdate)
                    statement.executeLargeUpdate();
                else
                    statement.executeUpdate();
                return 1;
            } catch(SQLException e) {
                e.printStackTrace();
                return -1;
            }
        });
    }

}
