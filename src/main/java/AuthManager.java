import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AuthManager {
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, Long> authenticatedSessions;
    private final Map<UUID, Integer> failedAttempts;
    private final Map<UUID, Long> lockoutTimers;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION = TimeUnit.MINUTES.toMillis(5);
    private static final long SESSION_DURATION = TimeUnit.MINUTES.toMillis(5);

    public AuthManager(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.authenticatedSessions = new HashMap<>();
        this.failedAttempts = new HashMap<>();
        this.lockoutTimers = new HashMap<>();
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS staff_auth (" +
            "uuid TEXT PRIMARY KEY," +
            "password_hash TEXT NOT NULL," +
            "created_at INTEGER NOT NULL," +
            "last_login INTEGER" +
        ")";
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info("✅ Staff auth table created/verified");
        } catch (SQLException e) {
            plugin.getLogger().severe("❌ Failed to create staff_auth table: " + e.getMessage());
        }
    }

    public boolean register(Player player, String password) {
        UUID uuid = player.getUniqueId();

        if (isRegistered(uuid)) {
            player.sendMessage("§c❌ You are already registered!");
            return false;
        }

        if (password.length() < 6) {
            player.sendMessage("§c❌ Password must be at least 6 characters!");
            return false;
        }

        // Hash the password immediately - never store plaintext
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));

        String sql = "INSERT INTO staff_auth (uuid, password_hash, created_at) VALUES (?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, hashedPassword);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
            
            failedAttempts.remove(uuid);
            lockoutTimers.remove(uuid);
            
            player.sendMessage("§a✅ Registration successful! Use /singham login <password> to authenticate.");
            plugin.getLogger().info("✅ Staff registered: " + player.getName() + " (" + uuid + ")");
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("❌ Failed to register staff: " + e.getMessage());
            player.sendMessage("§c❌ Registration failed! Please contact an administrator.");
            return false;
        }
    }

    public boolean login(Player player, String password) {
        UUID uuid = player.getUniqueId();

        if (!isRegistered(uuid)) {
            player.sendMessage("§c❌ You are not registered! Use /singham register <password>");
            return false;
        }

        if (isLockedOut(uuid)) {
            long remaining = getLockoutRemaining(uuid);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60;
            player.sendMessage("§c❌ Too many failed attempts! Try again in " + minutes + "m " + seconds + "s");
            return false;
        }

        String storedHash = getPasswordHash(uuid);
        if (storedHash == null) {
            player.sendMessage("§c❌ Authentication error. Please contact an administrator.");
            return false;
        }

        boolean success = BCrypt.checkpw(password, storedHash);

        if (success) {
            failedAttempts.remove(uuid);
            lockoutTimers.remove(uuid);
            authenticatedSessions.put(uuid, System.currentTimeMillis() + SESSION_DURATION);
            updateLastLogin(uuid);
            
            player.sendMessage("§a✅ Login successful!");
            player.sendMessage("§7You are now authenticated for 5 minutes.");
            plugin.getLogger().info("✅ Staff logged in: " + player.getName() + " (" + uuid + ")");
            return true;
        } else {
            int attempts = failedAttempts.getOrDefault(uuid, 0) + 1;
            failedAttempts.put(uuid, attempts);
            
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                lockoutTimers.put(uuid, System.currentTimeMillis() + LOCKOUT_DURATION);
                failedAttempts.remove(uuid);
                player.sendMessage("§c❌ Too many failed attempts! Account locked for 5 minutes.");
                plugin.getLogger().warning("⚠️ Staff account locked: " + player.getName() + " (" + uuid + ")");
            } else {
                int remaining = MAX_FAILED_ATTEMPTS - attempts;
                player.sendMessage("§c❌ Incorrect password! " + remaining + " attempt(s) remaining.");
            }
            return false;
        }
    }

    public void logout(Player player) {
        UUID uuid = player.getUniqueId();
        authenticatedSessions.remove(uuid);
        failedAttempts.remove(uuid);
        lockoutTimers.remove(uuid);
        player.sendMessage("§a🔒 Logged out successfully!");
        plugin.getLogger().info("✅ Staff logged out: " + player.getName() + " (" + uuid + ")");
    }

    public boolean changePassword(Player player, String oldPassword, String newPassword) {
        UUID uuid = player.getUniqueId();

        if (!isAuthenticated(player)) {
            player.sendMessage("§c❌ You must be logged in to change your password!");
            return false;
        }

        if (newPassword.length() < 6) {
            player.sendMessage("§c❌ New password must be at least 6 characters!");
            return false;
        }

        String storedHash = getPasswordHash(uuid);
        if (storedHash == null) {
            player.sendMessage("§c❌ Authentication error. Please contact an administrator.");
            return false;
        }

        if (!BCrypt.checkpw(oldPassword, storedHash)) {
            player.sendMessage("§c❌ Incorrect old password!");
            return false;
        }

        String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));

        String sql = "UPDATE staff_auth SET password_hash = ? WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHashedPassword);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
            
            player.sendMessage("§a✅ Password changed successfully!");
            plugin.getLogger().info("✅ Staff changed password: " + player.getName() + " (" + uuid + ")");
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("❌ Failed to change password: " + e.getMessage());
            player.sendMessage("§c❌ Failed to change password. Please contact an administrator.");
            return false;
        }
    }

    public boolean isAuthenticated(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!authenticatedSessions.containsKey(uuid)) {
            return false;
        }
        
        if (System.currentTimeMillis() > authenticatedSessions.get(uuid)) {
            authenticatedSessions.remove(uuid);
            player.sendMessage("§c❌ Your session has expired! Please login again with /singham login <password>");
            return false;
        }
        
        return true;
    }

    public boolean isRegistered(UUID uuid) {
        String sql = "SELECT uuid FROM staff_auth WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private String getPasswordHash(UUID uuid) {
        String sql = "SELECT password_hash FROM staff_auth WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password_hash");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("❌ Failed to get password hash: " + e.getMessage());
        }
        return null;
    }

    private void updateLastLogin(UUID uuid) {
        String sql = "UPDATE staff_auth SET last_login = ? WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to update last login: " + e.getMessage());
        }
    }

    private boolean isLockedOut(UUID uuid) {
        if (!lockoutTimers.containsKey(uuid)) {
            return false;
        }
        return System.currentTimeMillis() < lockoutTimers.get(uuid);
    }

    private long getLockoutRemaining(UUID uuid) {
        if (!lockoutTimers.containsKey(uuid)) {
            return 0;
        }
        return lockoutTimers.get(uuid) - System.currentTimeMillis();
    }

    public void removeSession(UUID uuid) {
        authenticatedSessions.remove(uuid);
    }

    public void clearAllSessions() {
        authenticatedSessions.clear();
    }

    public long getSessionEnd(UUID uuid) {
        if (!authenticatedSessions.containsKey(uuid)) {
            return 0;
        }
        return authenticatedSessions.get(uuid);
    }
}