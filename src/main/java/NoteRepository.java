import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NoteRepository {
    private final DatabaseManager databaseManager;

    public NoteRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void save(Note note) {
        String sql = "INSERT INTO notes (id, player_uuid, staff_uuid, note, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, note.getId().toString());
            stmt.setString(2, note.getPlayerUuid().toString());
            stmt.setString(3, note.getStaffUuid().toString());
            stmt.setString(4, note.getNote());
            stmt.setLong(5, note.getTimestamp());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Note> findByPlayer(UUID playerUuid) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes WHERE player_uuid = ? ORDER BY timestamp DESC";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public void delete(UUID noteId) {
        String sql = "DELETE FROM notes WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, noteId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Note mapResultSet(ResultSet rs) throws SQLException {
        return new Note(
            UUID.fromString(rs.getString("id")),
            UUID.fromString(rs.getString("player_uuid")),
            UUID.fromString(rs.getString("staff_uuid")),
            rs.getString("note"),
            rs.getLong("timestamp")
        );
    }
}