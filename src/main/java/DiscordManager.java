import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordManager {
    private final JavaPlugin plugin;
    private ScheduledExecutorService executor;
    private boolean discordSrvEnabled = false;
    private Object discordSrvInstance = null;

    public DiscordManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        
        // Check if DiscordSRV is loaded
        try {
            if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null) {
                Class<?> discordSRVClass = Class.forName("github.scarsz.discordsrv.DiscordSRV");
                this.discordSrvInstance = discordSRVClass.getMethod("getPlugin").invoke(null);
                this.discordSrvEnabled = true;
                plugin.getLogger().info("✅ DiscordSRV integration enabled!");
            }
        } catch (Exception e) {
            this.discordSrvEnabled = false;
            plugin.getLogger().warning("❌ DiscordSRV not found!");
        }
    }

    public void start() {
        if (discordSrvEnabled) {
            plugin.getLogger().info("✅ Discord manager started!");
        } else {
            plugin.getLogger().warning("⚠️ DiscordSRV not available!");
        }
    }

    public void stop() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    public void sendFakeLeaveMessage(String playerName) {
        if (!discordSrvEnabled || discordSrvInstance == null) {
            plugin.getLogger().info("[Discord] " + playerName + " left the server (no DiscordSRV)");
            return;
        }

        executor.execute(() -> {
            try {
                // Create a fake player object for DiscordSRV to get avatar
                Player fakePlayer = getFakePlayer(playerName);
                if (fakePlayer != null) {
                    // Use DiscordSRV's native sendLeaveMessage method
                    String leaveMessage = playerName + " left the game";
                    discordSrvInstance.getClass()
                        .getMethod("sendLeaveMessage", Player.class, String.class)
                        .invoke(discordSrvInstance, fakePlayer, leaveMessage);
                    plugin.getLogger().info("✅ Sent fake leave via DiscordSRV: " + playerName);
                } else {
                    // Fallback: use broadcast
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                            "discord broadcast " + playerName + " left the server");
                    });
                }
            } catch (Exception e) {
                plugin.getLogger().warning("❌ Failed to send leave message: " + e.getMessage());
                // Fallback to broadcast
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                        "discord broadcast " + playerName + " left the server");
                });
            }
        });
    }

    public void sendFakeJoinMessage(String playerName) {
        if (!discordSrvEnabled || discordSrvInstance == null) {
            plugin.getLogger().info("[Discord] " + playerName + " joined the server (no DiscordSRV)");
            return;
        }

        executor.execute(() -> {
            try {
                // Create a fake player object for DiscordSRV to get avatar
                Player fakePlayer = getFakePlayer(playerName);
                if (fakePlayer != null) {
                    // Use DiscordSRV's native sendJoinMessage method
                    String joinMessage = playerName + " joined the game";
                    discordSrvInstance.getClass()
                        .getMethod("sendJoinMessage", Player.class, String.class)
                        .invoke(discordSrvInstance, fakePlayer, joinMessage);
                    plugin.getLogger().info("✅ Sent fake join via DiscordSRV: " + playerName);
                } else {
                    // Fallback: use broadcast
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                            "discord broadcast " + playerName + " joined the server");
                    });
                }
            } catch (Exception e) {
                plugin.getLogger().warning("❌ Failed to send join message: " + e.getMessage());
                // Fallback to broadcast
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                        "discord broadcast " + playerName + " joined the server");
                });
            }
        });
    }

    private Player getFakePlayer(String playerName) {
        // Try to get real player first
        Player realPlayer = Bukkit.getPlayer(playerName);
        if (realPlayer != null && realPlayer.isOnline()) {
            return realPlayer;
        }

        // If player is not online, we need to create a fake player
        // DiscordSRV needs a Player object to get the avatar
        // We'll try to get the player from the offline player list
        try {
            // Check if player exists in offline players
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().equalsIgnoreCase(playerName)) {
                    return online;
                }
            }
            
            // If not found online, try to get from cache or use a workaround
            // Some DiscordSRV versions accept null for player and just use the name
            try {
                // Try calling sendLeaveMessage with null player
                String leaveMessage = playerName + " left the game";
                discordSrvInstance.getClass()
                    .getMethod("sendLeaveMessage", Player.class, String.class)
                    .invoke(discordSrvInstance, null, leaveMessage);
                plugin.getLogger().info("✅ Sent fake leave with null player: " + playerName);
                return null;
            } catch (Exception e) {
                plugin.getLogger().warning("Could not send with null player: " + e.getMessage());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not get player: " + e.getMessage());
        }
        return null;
    }

    public boolean isEnabled() {
        return discordSrvEnabled;
    }
}