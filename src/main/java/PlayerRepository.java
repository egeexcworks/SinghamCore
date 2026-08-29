import java.sql.*;
import java.util.UUID;

public class PlayerRepository {
    private final DatabaseManager databaseManager;

    public PlayerRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void save(PlayerData player) {
        String sql = "INSERT OR REPLACE INTO players (uuid, name, first_seen, last_seen) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, player.getUuid().toString());
            stmt.setString(2, player.getName());
            stmt.setLong(3, player.getFirstSeen());
            stmt.setLong(4, player.getLastSeen());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PlayerData findById(UUID uuid) {
        String sql = "SELECT * FROM players WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PlayerData findByName(String name) {
        String sql = "SELECT * FROM players WHERE name = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean exists(UUID uuid) {
        String sql = "SELECT COUNT(*) FROM players WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PlayerData mapResultSet(ResultSet rs) throws SQLException {
        return new PlayerData(
            UUID.fromString(rs.getString("uuid")),
            rs.getString("name"),
            rs.getLong("first_seen"),
            rs.getLong("last_seen")
        );
    }
}