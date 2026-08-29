import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ModerationCommand implements CommandExecutor {
    private final ModerationService service;

    public ModerationCommand(ModerationService service) {
        this.service = service;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player> <reason>");
            return true;
        }

        UUID moderator = sender instanceof Player ? ((Player) sender).getUniqueId() : UUID.randomUUID();
        StringBuilder reason = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }
        String reasonStr = reason.toString().trim();

        switch (label.toLowerCase()) {
            case "warn": {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                service.warnPlayer(target.getUniqueId(), moderator, reasonStr);
                sender.sendMessage(ChatColor.GREEN + "Warned " + target.getName() + ": " + reasonStr);
                target.sendMessage(ChatColor.RED + "You have been warned: " + reasonStr);
                break;
            }

            case "ban": {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                service.banPlayer(target.getUniqueId(), moderator, reasonStr);
                target.kickPlayer(ChatColor.RED + "Banned: " + reasonStr);
                sender.sendMessage(ChatColor.GREEN + "Banned " + target.getName() + ": " + reasonStr);
                break;
            }

            case "unban": {
                service.unbanPlayer(args[0], moderator, reasonStr);
                sender.sendMessage(ChatColor.GREEN + "Unbanned " + args[0] + ": " + reasonStr);
                break;
            }

            case "mute": {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                service.mutePlayer(target.getUniqueId(), moderator, reasonStr);
                sender.sendMessage(ChatColor.GREEN + "Muted " + target.getName() + ": " + reasonStr);
                target.sendMessage(ChatColor.RED + "You have been muted: " + reasonStr);
                break;
            }

            case "unmute": {
                service.unmutePlayer(args[0], moderator, reasonStr);
                sender.sendMessage(ChatColor.GREEN + "Unmuted " + args[0] + ": " + reasonStr);
                break;
            }

            case "kick": {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                target.kickPlayer(ChatColor.RED + "Kicked: " + reasonStr);
                sender.sendMessage(ChatColor.GREEN + "Kicked " + target.getName() + ": " + reasonStr);
                break;
            }

            default:
                sender.sendMessage(ChatColor.RED + "Unknown command.");
        }
        return true;
    }
}