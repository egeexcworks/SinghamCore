import org.bukkit.entity.Player;

public class ChatLockManager {
    private boolean locked = false;

    public boolean isLocked() {
        return locked;
    }

    public void toggleLock() {
        locked = !locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean canBypass(Player player) {
        return player.hasPermission("singham.chatlock.bypass");
    }
}