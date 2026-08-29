import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {
    private final Set<UUID> vanishedPlayers;
    private final JavaPlugin plugin;
    private final DiscordManager discordManager;
    private boolean fakeMessages;

    public VanishManager(JavaPlugin plugin, DiscordManager discordManager) {
        this.plugin = plugin;
        this.discordManager = discordManager;
        this.vanishedPlayers = new HashSet<>();
        this.fakeMessages = plugin.getConfig().getBoolean("vanish.fake_messages", true);
    }

    public void vanish(Player player) {
        if (player == null) return;
        if (isVanished(player)) return;
        
        vanishedPlayers.add(player.getUniqueId());
        
        // Hide from all online players
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                online.hidePlayer(plugin, player);
            }
        }
        
        // Remove from tab list
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                // Hide from tab list by removing them
                online.hidePlayer(plugin, player);
            }
        }
        
        // Make fully invisible - remove all effects and visibility
        player.setInvisible(true);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setCollidable(false);
        
        // Remove from all entity targeting
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                online.hidePlayer(plugin, player);
            }
        }
        
        if (fakeMessages) {
            // Send fake leave message to Discord
            if (discordManager != null) {
                plugin.getLogger().info("📤 Sending fake leave message for: " + player.getName());
                discordManager.sendFakeLeaveMessage(player.getName());
            }
            
            // Broadcast fake quit message in game
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " left the game");
        }
        
        player.sendMessage(ChatColor.GREEN + "👻 You are now vanished!");
        player.sendMessage(ChatColor.GRAY + "You are completely invisible to all players.");
        player.sendMessage(ChatColor.GRAY + "You cannot be seen, targeted, or interacted with.");
        if (fakeMessages) {
            player.sendMessage(ChatColor.GRAY + "A fake leave message was sent to Discord.");
        }
    }

    public void unvanish(Player player) {
        if (player == null) return;
        if (!isVanished(player)) return;
        
        vanishedPlayers.remove(player.getUniqueId());
        
        // Show to all online players
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
        
        // Restore visibility
        player.setInvisible(false);
        player.setFlying(false);
        player.setCollidable(true);
        
        if (fakeMessages) {
            // Send fake join message to Discord
            if (discordManager != null) {
                plugin.getLogger().info("📤 Sending fake join message for: " + player.getName());
                discordManager.sendFakeJoinMessage(player.getName());
            }
            
            // Broadcast fake join message in game
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " joined the game");
        }
        
        player.sendMessage(ChatColor.GREEN + "👻 You are no longer vanished!");
        player.sendMessage(ChatColor.GRAY + "You are now visible to all players.");
        if (fakeMessages) {
            player.sendMessage(ChatColor.GRAY + "A fake join message was sent to Discord.");
        }
    }

    public boolean isVanished(Player player) {
        if (player == null) return false;
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public boolean isVanished(UUID uuid) {
        if (uuid == null) return false;
        return vanishedPlayers.contains(uuid);
    }

    public Set<UUID> getVanishedPlayers() {
        return new HashSet<>(vanishedPlayers);
    }

    public void setFakeMessages(boolean enabled) {
        this.fakeMessages = enabled;
    }
}