import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthChatListener implements Listener {
    private final AuthManager authManager;
    private final Map<UUID, String> pendingAction = new HashMap<>();
    private final Map<UUID, Long> pendingTimeout = new HashMap<>();
    private final Map<UUID, String> pendingOldPassword = new HashMap<>();

    public AuthChatListener(AuthManager authManager) {
        this.authManager = authManager;
    }

    // Handle /singham register, /singham login, /singham changepassword commands
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();

        if (message.equals("/singham register") || 
            message.equals("/singham login") || 
            message.equals("/singham changepassword") ||
            message.equals("/singham auth")) {
            
            // Cancel the command so it doesn't get logged
            event.setCancelled(true);
            
            UUID uuid = player.getUniqueId();
            boolean isRegistered = authManager.isRegistered(uuid);

            if (message.equals("/singham register")) {
                if (isRegistered) {
                    player.sendMessage("§c❌ You are already registered!");
                    return;
                }
                pendingAction.put(uuid, "register");
                pendingTimeout.put(uuid, System.currentTimeMillis() + 60000);
                player.sendMessage("§e📝 Type your password in chat (will be hidden):");
                player.sendMessage("§c⚠️ Your message will not be shown in chat or console.");
                player.sendMessage("§c⚠️ Password must be at least 6 characters!");
                return;
            }

            if (message.equals("/singham login")) {
                if (!isRegistered) {
                    player.sendMessage("§c❌ You are not registered! Use /singham register to create an account.");
                    return;
                }
                pendingAction.put(uuid, "login");
                pendingTimeout.put(uuid, System.currentTimeMillis() + 60000);
                player.sendMessage("§e📝 Type your password in chat (will be hidden):");
                player.sendMessage("§c⚠️ Your message will not be shown in chat or console.");
                return;
            }

            if (message.equals("/singham changepassword") || message.equals("/singham auth")) {
                if (!isRegistered) {
                    player.sendMessage("§c❌ You are not registered! Use /singham register to create an account.");
                    return;
                }
                if (!authManager.isAuthenticated(player)) {
                    player.sendMessage("§c❌ You must be logged in to change your password!");
                    player.sendMessage("§c❌ Use /singham login to authenticate first.");
                    return;
                }
                pendingAction.put(uuid, "changepassword");
                pendingTimeout.put(uuid, System.currentTimeMillis() + 60000);
                player.sendMessage("§e📝 Type your OLD password in chat (will be hidden):");
                player.sendMessage("§c⚠️ Your message will not be shown in chat or console.");
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim();

        // Check if player is in authentication mode (pending password)
        if (pendingAction.containsKey(player.getUniqueId())) {
            String action = pendingAction.get(player.getUniqueId());
            
            // Check timeout (60 seconds)
            if (System.currentTimeMillis() > pendingTimeout.get(player.getUniqueId())) {
                pendingAction.remove(player.getUniqueId());
                pendingTimeout.remove(player.getUniqueId());
                pendingOldPassword.remove(player.getUniqueId());
                player.sendMessage("§c❌ Authentication timed out! Please try again.");
                event.setCancelled(true);
                return;
            }

            // Cancel the chat event so it never shows in chat or logs
            event.setCancelled(true);

            if (action.equals("register")) {
                if (message.length() < 6) {
                    player.sendMessage("§c❌ Password must be at least 6 characters!");
                    pendingAction.remove(player.getUniqueId());
                    pendingTimeout.remove(player.getUniqueId());
                    return;
                }
                authManager.register(player, message);
                pendingAction.remove(player.getUniqueId());
                pendingTimeout.remove(player.getUniqueId());
                return;
            }

            if (action.equals("login")) {
                authManager.login(player, message);
                pendingAction.remove(player.getUniqueId());
                pendingTimeout.remove(player.getUniqueId());
                return;
            }

            if (action.equals("changepassword")) {
                // For password change, we need two steps
                if (pendingOldPassword.containsKey(player.getUniqueId())) {
                    // This is the new password
                    String oldPassword = pendingOldPassword.get(player.getUniqueId());
                    if (message.length() < 6) {
                        player.sendMessage("§c❌ New password must be at least 6 characters!");
                        pendingAction.remove(player.getUniqueId());
                        pendingTimeout.remove(player.getUniqueId());
                        pendingOldPassword.remove(player.getUniqueId());
                        return;
                    }
                    authManager.changePassword(player, oldPassword, message);
                    pendingAction.remove(player.getUniqueId());
                    pendingTimeout.remove(player.getUniqueId());
                    pendingOldPassword.remove(player.getUniqueId());
                } else {
                    // Store old password and ask for new one
                    pendingOldPassword.put(player.getUniqueId(), message);
                    pendingAction.put(player.getUniqueId(), "changepassword");
                    pendingTimeout.put(player.getUniqueId(), System.currentTimeMillis() + 60000);
                    player.sendMessage("§e📝 Now type your NEW password in chat (will be hidden):");
                    player.sendMessage("§c⚠️ Your message will not be shown in chat or console.");
                }
                return;
            }
            return;
        }
    }

    public void clearPending(UUID uuid) {
        pendingAction.remove(uuid);
        pendingTimeout.remove(uuid);
        pendingOldPassword.remove(uuid);
    }
}