import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTables {
    private final DatabaseManager databaseManager;

    public DatabaseTables(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void createTables() {
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Players table
            String playersTable = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "first_seen INTEGER NOT NULL," +
                "last_seen INTEGER NOT NULL" +
            ")";
            stmt.execute(playersTable);

            // Punishments table
            String punishmentsTable = "CREATE TABLE IF NOT EXISTS punishments (" +
                "id TEXT PRIMARY KEY," +
                "player_uuid TEXT NOT NULL," +
                "moderator_uuid TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "reason TEXT," +
                "timestamp INTEGER NOT NULL," +
                "duration INTEGER," +
                "active INTEGER NOT NULL," +
                "FOREIGN KEY (player_uuid) REFERENCES players(uuid)" +
            ")";
            stmt.execute(punishmentsTable);

            // Warnings table
            String warningsTable = "CREATE TABLE IF NOT EXISTS warnings (" +
                "id TEXT PRIMARY KEY," +
                "player_uuid TEXT NOT NULL," +
                "moderator_uuid TEXT NOT NULL," +
                "reason TEXT," +
                "timestamp INTEGER NOT NULL," +
                "FOREIGN KEY (player_uuid) REFERENCES players(uuid)" +
            ")";
            stmt.execute(warningsTable);
            // Add this to createTables() method:
String notesTable = "CREATE TABLE IF NOT EXISTS notes (" +
    "id TEXT PRIMARY KEY," +
    "player_uuid TEXT NOT NULL," +
    "staff_uuid TEXT NOT NULL," +
    "note TEXT NOT NULL," +
    "timestamp INTEGER NOT NULL," +
    "FOREIGN KEY (player_uuid) REFERENCES players(uuid)" +
")";
stmt.execute(notesTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}