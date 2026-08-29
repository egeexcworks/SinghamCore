import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class TempBanCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /tempban <player> <duration> [reason]");
            sender.sendMessage(ChatColor.GRAY + "Examples: /tempban John 7d, /tempban John 12h, /tempban John 30m");
            return true;
        }

        String playerName = args[0];
        String durationStr = args[1];
        
        StringBuilder reason = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }
        String reasonStr = reason.length() > 0 ? reason.toString().trim() : "No reason provided";

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or not online.");
            return true;
        }

        long duration = parseDuration(durationStr);
        if (duration == -1) {
            sender.sendMessage(ChatColor.RED + "Invalid duration format. Use: 7d, 12h, 30m");
            return true;
        }

        long expiration = System.currentTimeMillis() + duration;
        Bukkit.getBanList(BanList.Type.NAME).addBan(
            target.getName(),
            reasonStr,
            new java.util.Date(expiration),
            sender.getName()
        );
        target.kickPlayer(ChatColor.RED + "Temporarily Banned: " + reasonStr + 
                          "\n" + ChatColor.YELLOW + "Expires in: " + durationStr);

        sender.sendMessage(ChatColor.GREEN + "✅ Temporarily banned " + target.getName() + 
                          " for " + durationStr + ": " + reasonStr);
        return true;
    }

    private long parseDuration(String duration) {
        char unit = duration.charAt(duration.length() - 1);
        long value = Long.parseLong(duration.substring(0, duration.length() - 1));
        
        switch (unit) {
            case 'm': return TimeUnit.MINUTES.toMillis(value);
            case 'h': return TimeUnit.HOURS.toMillis(value);
            case 'd': return TimeUnit.DAYS.toMillis(value);
            case 'w': return TimeUnit.DAYS.toMillis(value * 7);
            default: return -1;
        }
    }
}