import java.util.UUID;

public class Warning {
    private final UUID id;
    private final UUID playerUuid;
    private final UUID moderatorUuid;
    private final String reason;
    private final long timestamp;

    public Warning(UUID id, UUID playerUuid, UUID moderatorUuid, String reason, long timestamp) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.moderatorUuid = moderatorUuid;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public UUID getModeratorUuid() {
        return moderatorUuid;
    }

    public String getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }
}