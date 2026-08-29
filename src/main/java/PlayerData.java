import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private String name;
    private long firstSeen;
    private long lastSeen;

    public PlayerData(UUID uuid, String name, long firstSeen, long lastSeen) {
        this.uuid = uuid;
        this.name = name;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(long firstSeen) {
        this.firstSeen = firstSeen;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }
}