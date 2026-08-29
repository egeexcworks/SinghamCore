import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Date;

public class BanListCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        java.util.Set<org.bukkit.BanEntry> banEntries = Bukkit.getBanList(BanList.Type.NAME).getBanEntries();

        if (banEntries.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "There are no active bans.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        sender.sendMessage(ChatColor.GOLD + "Active Bans (" + banEntries.size() + ")");
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");

        for (org.bukkit.BanEntry ban : banEntries) {
            String playerName = ban.getTarget();
            String reason = ban.getReason() != null ? ban.getReason() : "No reason provided";
            String source = ban.getSource() != null ? ban.getSource() : "Unknown";
            Date created = ban.getCreated();
            Date expires = ban.getExpiration();
            
            String duration = expires == null ? "Permanent" : 
                (expires.getTime() - created.getTime()) / 1000 / 60 + " minutes";
            
            sender.sendMessage(ChatColor.YELLOW + "Player: " + ChatColor.WHITE + playerName);
            sender.sendMessage(ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + reason);
            sender.sendMessage(ChatColor.YELLOW + "Banned by: " + ChatColor.WHITE + source);
            sender.sendMessage(ChatColor.YELLOW + "Duration: " + ChatColor.WHITE + duration);
            sender.sendMessage(ChatColor.GRAY + "─────────────────────────────────");
        }

        return true;
    }
}