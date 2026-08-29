import java.util.UUID;

public class Note {
    private final UUID id;
    private final UUID playerUuid;
    private final UUID staffUuid;
    private final String note;
    private final long timestamp;

    public Note(UUID id, UUID playerUuid, UUID staffUuid, String note, long timestamp) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.staffUuid = staffUuid;
        this.note = note;
        this.timestamp = timestamp;
    }

    public UUID getId() { return id; }
    public UUID getPlayerUuid() { return playerUuid; }
    public UUID getStaffUuid() { return staffUuid; }
    public String getNote() { return note; }
    public long getTimestamp() { return timestamp; }
}