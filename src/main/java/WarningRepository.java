import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WarningRepository {
    private final DatabaseManager databaseManager;

    public WarningRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void save(Warning warning) {
        String sql = "INSERT INTO warnings (id, player_uuid, moderator_uuid, reason, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, warning.getId().toString());
            stmt.setString(2, warning.getPlayerUuid().toString());
            stmt.setString(3, warning.getModeratorUuid().toString());
            stmt.setString(4, warning.getReason());
            stmt.setLong(5, warning.getTimestamp());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Warning> findByPlayer(UUID playerUuid) {
        List<Warning> warnings = new ArrayList<>();
        String sql = "SELECT * FROM warnings WHERE player_uuid = ? ORDER BY timestamp DESC";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                warnings.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return warnings;
    }

    public int countByPlayer(UUID playerUuid) {
        String sql = "SELECT COUNT(*) FROM warnings WHERE player_uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void deleteByPlayer(UUID playerUuid) {
        String sql = "DELETE FROM warnings WHERE player_uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Warning mapResultSet(ResultSet rs) throws SQLException {
        return new Warning(
            UUID.fromString(rs.getString("id")),
            UUID.fromString(rs.getString("player_uuid")),
            UUID.fromString(rs.getString("moderator_uuid")),
            rs.getString("reason"),
            rs.getLong("timestamp")
        );
    }
}