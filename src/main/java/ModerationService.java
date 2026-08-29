import java.util.List;
import java.util.UUID;

public class ModerationService {
    private final PlayerRepository playerRepository;
    private final PunishmentRepository punishmentRepository;
    private final WarningRepository warningRepository;

    public ModerationService(PlayerRepository playerRepository, PunishmentRepository punishmentRepository, WarningRepository warningRepository) {
        this.playerRepository = playerRepository;
        this.punishmentRepository = punishmentRepository;
        this.warningRepository = warningRepository;
    }

    public void warnPlayer(UUID playerUuid, UUID moderatorUuid, String reason) {
        PlayerData player = playerRepository.findById(playerUuid);
        if (player == null) {
            return;
        }

        Warning warning = new Warning(
            UUID.randomUUID(),
            playerUuid,
            moderatorUuid,
            reason,
            System.currentTimeMillis()
        );
        warningRepository.save(warning);
        
        // Check if player should be auto-banned
        int warningCount = warningRepository.countByPlayer(playerUuid);
        int maxWarnings = 5; // Could be from config
        if (warningCount >= maxWarnings) {
            banPlayer(playerUuid, moderatorUuid, "Auto-banned for exceeding " + maxWarnings + " warnings");
        }
    }

    public void banPlayer(UUID playerUuid, UUID moderatorUuid, String reason) {
        PlayerData player = playerRepository.findById(playerUuid);
        if (player == null) {
            return;
        }

        // Remove any existing active ban
        punishmentRepository.removeActivePunishment(playerUuid, Punishment.PunishmentType.BAN);

        Punishment punishment = new Punishment(
            UUID.randomUUID(),
            playerUuid,
            moderatorUuid,
            Punishment.PunishmentType.BAN,
            reason,
            System.currentTimeMillis(),
            -1, // Permanent
            true
        );
        punishmentRepository.save(punishment);
        
        // Kick the player if online
        org.bukkit.entity.Player onlinePlayer = org.bukkit.Bukkit.getPlayer(playerUuid);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            onlinePlayer.kickPlayer(org.bukkit.ChatColor.RED + "Banned: " + reason);
        }
    }

    public void unbanPlayer(String playerName, UUID moderatorUuid, String reason) {
        PlayerData player = playerRepository.findByName(playerName);
        if (player == null) {
            return;
        }

        punishmentRepository.removeActivePunishment(player.getUuid(), Punishment.PunishmentType.BAN);
        
        Punishment unban = new Punishment(
            UUID.randomUUID(),
            player.getUuid(),
            moderatorUuid,
            Punishment.PunishmentType.UNBAN,
            reason,
            System.currentTimeMillis(),
            -1,
            false
        );
        punishmentRepository.save(unban);
    }

    public void mutePlayer(UUID playerUuid, UUID moderatorUuid, String reason) {
        PlayerData player = playerRepository.findById(playerUuid);
        if (player == null) {
            return;
        }

        // Remove any existing active mute
        punishmentRepository.removeActivePunishment(playerUuid, Punishment.PunishmentType.MUTE);

        Punishment punishment = new Punishment(
            UUID.randomUUID(),
            playerUuid,
            moderatorUuid,
            Punishment.PunishmentType.MUTE,
            reason,
            System.currentTimeMillis(),
            -1, // Permanent
            true
        );
        punishmentRepository.save(punishment);
    }

    public void unmutePlayer(String playerName, UUID moderatorUuid, String reason) {
        PlayerData player = playerRepository.findByName(playerName);
        if (player == null) {
            return;
        }

        punishmentRepository.removeActivePunishment(player.getUuid(), Punishment.PunishmentType.MUTE);
        
        Punishment unmute = new Punishment(
            UUID.randomUUID(),
            player.getUuid(),
            moderatorUuid,
            Punishment.PunishmentType.UNMUTE,
            reason,
            System.currentTimeMillis(),
            -1,
            false
        );
        punishmentRepository.save(unmute);
    }

    public void kickPlayer(UUID playerUuid, UUID moderatorUuid, String reason) {
        PlayerData player = playerRepository.findById(playerUuid);
        if (player == null) {
            return;
        }

        org.bukkit.entity.Player onlinePlayer = org.bukkit.Bukkit.getPlayer(playerUuid);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            onlinePlayer.kickPlayer(org.bukkit.ChatColor.RED + "Kicked: " + reason);
        }

        Punishment kick = new Punishment(
            UUID.randomUUID(),
            playerUuid,
            moderatorUuid,
            Punishment.PunishmentType.KICK,
            reason,
            System.currentTimeMillis(),
            -1,
            false
        );
        punishmentRepository.save(kick);
    }

    public boolean isPlayerBanned(UUID playerUuid) {
        return punishmentRepository.hasActivePunishment(playerUuid, Punishment.PunishmentType.BAN);
    }

    public boolean isPlayerMuted(UUID playerUuid) {
        return punishmentRepository.hasActivePunishment(playerUuid, Punishment.PunishmentType.MUTE);
    }

    public List<Punishment> getPlayerPunishments(UUID playerUuid) {
        return punishmentRepository.findByPlayer(playerUuid);
    }

    public List<Warning> getPlayerWarnings(UUID playerUuid) {
        return warningRepository.findByPlayer(playerUuid);
    }

    public int getWarningCount(UUID playerUuid) {
        return warningRepository.countByPlayer(playerUuid);
    }

    public Punishment getActiveBan(UUID playerUuid) {
        return punishmentRepository.getActivePunishment(playerUuid, Punishment.PunishmentType.BAN);
    }

    public Punishment getActiveMute(UUID playerUuid) {
        return punishmentRepository.getActivePunishment(playerUuid, Punishment.PunishmentType.MUTE);
    }
}