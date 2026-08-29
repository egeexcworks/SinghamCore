import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PunishmentRepository {
    private final DatabaseManager databaseManager;

    public PunishmentRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void save(Punishment punishment) {
        String sql = "INSERT INTO punishments (id, player_uuid, moderator_uuid, type, reason, timestamp, duration, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, punishment.getId().toString());
            stmt.setString(2, punishment.getPlayerUuid().toString());
            stmt.setString(3, punishment.getModeratorUuid().toString());
            stmt.setString(4, punishment.getType().name());
            stmt.setString(5, punishment.getReason());
            stmt.setLong(6, punishment.getTimestamp());
            stmt.setLong(7, punishment.getDuration());
            stmt.setBoolean(8, punishment.isActive());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Punishment> findByPlayer(UUID playerUuid) {
        List<Punishment> punishments = new ArrayList<>();
        String sql = "SELECT * FROM punishments WHERE player_uuid = ? ORDER BY timestamp DESC";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                punishments.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    public List<Punishment> findActiveByPlayer(UUID playerUuid) {
        List<Punishment> punishments = new ArrayList<>();
        String sql = "SELECT * FROM punishments WHERE player_uuid = ? AND active = true ORDER BY timestamp DESC";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                punishments.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    public boolean hasActivePunishment(UUID playerUuid, Punishment.PunishmentType type) {
        String sql = "SELECT COUNT(*) FROM punishments WHERE player_uuid = ? AND type = ? AND active = true";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, type.name());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void removeActivePunishment(UUID playerUuid, Punishment.PunishmentType type) {
        String sql = "UPDATE punishments SET active = false WHERE player_uuid = ? AND type = ? AND active = true";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, type.name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Punishment getActivePunishment(UUID playerUuid, Punishment.PunishmentType type) {
        String sql = "SELECT * FROM punishments WHERE player_uuid = ? AND type = ? AND active = true LIMIT 1";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, type.name());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Punishment mapResultSet(ResultSet rs) throws SQLException {
        return new Punishment(
            UUID.fromString(rs.getString("id")),
            UUID.fromString(rs.getString("player_uuid")),
            UUID.fromString(rs.getString("moderator_uuid")),
            Punishment.PunishmentType.valueOf(rs.getString("type")),
            rs.getString("reason"),
            rs.getLong("timestamp"),
            rs.getLong("duration"),
            rs.getBoolean("active")
        );
    }
}