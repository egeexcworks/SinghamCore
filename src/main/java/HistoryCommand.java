import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class HistoryCommand implements CommandExecutor {
    private final ModerationService moderationService;
    private final NoteRepository noteRepository;
    private final PlayerRepository playerRepository;

    public HistoryCommand(ModerationService moderationService, NoteRepository noteRepository, 
                          PlayerRepository playerRepository) {
        this.moderationService = moderationService;
        this.noteRepository = noteRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /history <player> [page]");
            return true;
        }

        String playerName = args[0];
        int page = args.length > 1 ? Integer.parseInt(args[1]) : 1;

        PlayerData playerData = playerRepository.findByName(playerName);
        if (playerData == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        UUID playerUuid = playerData.getUuid();
        
        // Get punishments
        List<Punishment> punishments = moderationService.getPlayerPunishments(playerUuid);
        List<Warning> warnings = moderationService.getPlayerWarnings(playerUuid);
        List<Note> notes = noteRepository.findByPlayer(playerUuid);

        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        sender.sendMessage(ChatColor.GOLD + "📋 History for " + playerName);
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        
        // Ban count
        long banCount = punishments.stream()
            .filter(p -> p.getType() == Punishment.PunishmentType.BAN)
            .count();
        sender.sendMessage(ChatColor.YELLOW + "Total Bans: " + ChatColor.WHITE + banCount);
        
        // Active punishments
        long activePunishments = punishments.stream()
            .filter(p -> p.isActive() && !p.isExpired())
            .count();
        sender.sendMessage(ChatColor.YELLOW + "Active Punishments: " + ChatColor.RED + activePunishments);
        
        sender.sendMessage(ChatColor.GRAY + "─────────────────────────────────");
        
        // Show punishments
        if (!punishments.isEmpty()) {
            sender.sendMessage(ChatColor.AQUA + "Punishments:");
            for (Punishment p : punishments) {
                String color = p.isActive() && !p.isExpired() ? ChatColor.RED.toString() : ChatColor.GRAY.toString();
                sender.sendMessage(color + "• " + p.getType() + ": " + p.getReason() + 
                    (p.getDuration() == -1 ? " (Permanent)" : ""));
            }
        }

        // Show warnings
        if (!warnings.isEmpty()) {
            sender.sendMessage(ChatColor.AQUA + "Warnings:");
            for (Warning w : warnings) {
                sender.sendMessage(ChatColor.YELLOW + "• " + w.getReason());
            }
        }

        // Show notes
        if (!notes.isEmpty()) {
            sender.sendMessage(ChatColor.AQUA + "Notes:");
            for (Note n : notes) {
                sender.sendMessage(ChatColor.GREEN + "• [" + n.getId().toString().substring(0, 8) + "] " + n.getNote());
            }
        }

        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        return true;
    }
}