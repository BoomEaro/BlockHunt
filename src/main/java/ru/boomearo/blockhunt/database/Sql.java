package ru.boomearo.blockhunt.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sqlite.JDBC;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.database.sections.SectionStats;
import ru.boomearo.blockhunt.objects.statistics.BHStatsType;

public class Sql {
    private static Sql instance = null;
    private static final String CON_STR = "jdbc:sqlite:[path]database.db";

    public static synchronized Sql getInstance() throws SQLException {
        if (instance == null)
            instance = new Sql();
        return instance;
    }

    private Connection connection;

    private Sql() throws SQLException {
        DriverManager.registerDriver(new JDBC());
        this.connection = DriverManager.getConnection(CON_STR.replace("[path]", BlockHunt.getInstance().getDataFolder() + File.separator));
    }

    public synchronized List<SectionStats> getAllStatsData(BHStatsType type) {
        try (Statement statement = this.connection.createStatement()) {
            List<SectionStats> collections = new ArrayList<SectionStats>();
            ResultSet resSet = statement.executeQuery("SELECT id, name, value FROM " + type.getDBName());
            while (resSet.next()) {
                collections.add(new SectionStats(resSet.getInt("id"), resSet.getString("name"), resSet.getInt("value")));
            }
            return collections;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public synchronized void putStatsData(BHStatsType type, String name, double value) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO " + type.getDBName() + "(`name`, `value`) " +
                        "VALUES(?, ?)")) {
            statement.setString(1, name);
            statement.setDouble(2, value);
            statement.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateStatsData(BHStatsType type, String name, double value) {
        String sql = "UPDATE " + type.getDBName() + " SET value = ? "
                + "WHERE name = ?";

        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {

            pstmt.setDouble(1, value);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void createNewDatabaseStatsData(BHStatsType type) {
        String sql = "CREATE TABLE IF NOT EXISTS " + type.getDBName() + " (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	name text NOT NULL,\n"
                + "	value double NOT NULL\n"
                + ");";

        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute(sql);
            BlockHunt.getInstance().getLogger().info("Таблица " + type.getDBName() + " успешно загружена.");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void Disconnect() throws SQLException {
        this.connection.close();
    }
}
