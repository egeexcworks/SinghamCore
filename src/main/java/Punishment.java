import java.util.UUID;

public class Punishment {
    private final UUID id;
    private final UUID playerUuid;
    private final UUID moderatorUuid;
    private final PunishmentType type;
    private final String reason;
    private final long timestamp;
    private final long duration;
    private final boolean active;

    public enum PunishmentType {
        BAN,
        MUTE,
        WARN,
        UNBAN,
        UNMUTE,
        KICK
    }

    public Punishment(UUID id, UUID playerUuid, UUID moderatorUuid, PunishmentType type, String reason, long timestamp, long duration, boolean active) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.moderatorUuid = moderatorUuid;
        this.type = type;
        this.reason = reason;
        this.timestamp = timestamp;
        this.duration = duration;
        this.active = active;
    }

    public UUID getId() { return id; }
    public UUID getPlayerUuid() { return playerUuid; }
    public UUID getModeratorUuid() { return moderatorUuid; }
    public PunishmentType getType() { return type; }
    public String getReason() { return reason; }
    public long getTimestamp() { return timestamp; }
    public long getDuration() { return duration; }
    public boolean isActive() { return active; }
    public boolean isExpired() {
        if (duration == -1) return false;
        return System.currentTimeMillis() > timestamp + duration;
    }
}