import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class ModerationListener implements Listener {
    private final ModerationService service;
    private final ChatLockManager chatLockManager;

    public ModerationListener(ModerationService service, ChatLockManager chatLockManager) {
        this.service = service;
        this.chatLockManager = chatLockManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Check mute
        if (service.isPlayerMuted(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are muted and cannot chat.");
            return;
        }

        // Check chat lock
        if (chatLockManager.isLocked() && !chatLockManager.canBypass(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Chat is currently locked!");
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        // Native Minecraft ban system handles this automatically
        // We keep this for any additional checks if needed
    }
}