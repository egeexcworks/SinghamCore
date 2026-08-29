import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TempBanListCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        java.util.Set<org.bukkit.BanEntry> allBans = Bukkit.getBanList(BanList.Type.NAME).getBanEntries();
        List<org.bukkit.BanEntry> tempBans = new ArrayList<>();
        
        for (org.bukkit.BanEntry ban : allBans) {
            if (ban.getExpiration() != null) {
                tempBans.add(ban);
            }
        }

        if (tempBans.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "There are no active temporary bans.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        sender.sendMessage(ChatColor.GOLD + "Temporary Bans (" + tempBans.size() + ")");
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");

        for (org.bukkit.BanEntry ban : tempBans) {
            long remaining = ban.getExpiration().getTime() - System.currentTimeMillis();
            String remainingStr = formatTime(remaining);
            
            sender.sendMessage(ChatColor.YELLOW + "Player: " + ChatColor.WHITE + ban.getTarget());
            sender.sendMessage(ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + ban.getReason());
            sender.sendMessage(ChatColor.YELLOW + "Remaining: " + ChatColor.RED + remainingStr);
            sender.sendMessage(ChatColor.GRAY + "─────────────────────────────────");
        }

        return true;
    }

    private String formatTime(long millis) {
        long days = millis / (1000 * 60 * 60 * 24);
        long hours = (millis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);
        
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }
}