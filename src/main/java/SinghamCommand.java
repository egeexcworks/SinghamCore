import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SinghamCommand implements CommandExecutor, TabCompleter {
    private final ModerationService moderationService;
    private final VanishManager vanishManager;
    private final DiscordManager discordManager;
    private final AuthManager authManager;
    private final NoteRepository noteRepository;
    private final PlayerRepository playerRepository;
    private final ChatLockManager chatLockManager;

    private static final List<String> COMMANDS = Arrays.asList(
        "ban", "unban", "banlist", "tempban", "tempbanlist",
        "kick", "mute", "unmute", "warn",
        "vanish", "v",
        "note", "history", "chatlock", "lockchat", "help",
        "register", "login", "changepassword"
    );

    private static final List<String> NOTE_ACTIONS = Arrays.asList("add", "remove", "show");

    public SinghamCommand(ModerationService moderationService, VanishManager vanishManager, 
                          DiscordManager discordManager, AuthManager authManager,
                          NoteRepository noteRepository, PlayerRepository playerRepository,
                          ChatLockManager chatLockManager) {
        this.moderationService = moderationService;
        this.vanishManager = vanishManager;
        this.discordManager = discordManager;
        this.authManager = authManager;
        this.noteRepository = noteRepository;
        this.playerRepository = playerRepository;
        this.chatLockManager = chatLockManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(player);
                break;

            case "register":
            case "login":
            case "changepassword":
                player.sendMessage(ChatColor.YELLOW + "⚠️ Please use this command in chat (without /singham)");
                player.sendMessage(ChatColor.GRAY + "Example: Type /register in chat");
                break;

            case "status":
                if (!player.hasPermission("singham.staff")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                showStatus(player);
                break;

            case "ban":
                if (!player.hasPermission("singham.ban")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                if (!authManager.isAuthenticated(player)) {
                    player.sendMessage(ChatColor.RED + "❌ You must authenticate first! Type /singham login in chat.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /singham ban <player> <reason>");
                    return true;
                }
                handleBan(player, args);
                break;

            case "unban":
                if (!player.hasPermission("singham.unban")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                if (!authManager.isAuthenticated(player)) {
                    player.sendMessage(ChatColor.RED + "❌ You must authenticate first! Type /singham login in chat.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /singham unban <player>");
                    return true;
                }
                handleUnban(player, args);
                break;

            case "banlist":
                if (!player.hasPermission("singham.banlist")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                handleBanList(player, args);
                break;

            case "tempban":
                if (!player.hasPermission("singham.tempban")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                if (!authManager.isAuthenticated(player)) {
                    player.sendMessage(ChatColor.RED + "❌ You must authenticate first! Type /singham login in chat.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /singham tempban <player> <duration> [reason]");
                    player.sendMessage(ChatColor.GRAY + "Examples: /singham tempban John 7d, 12h, 30m, 60s");
                    return true;
                }
                handleTempBan(player, args);
                break;

            case "tempbanlist":
                if (!player.hasPermission("singham.tempbanlist")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                handleTempBanList(player);
                break;

            case "kick":
                if (!player.hasPermission("singham.kick")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                if (!authManager.isAuthenticated(player)) {
                    player.sendMessage(ChatColor.RED + "❌ You must authenticate first! Type /singham login in chat.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /singham kick <player> <reason>");
                    return true;
                }
                handleKick(player, args);
                break;

            case "mute":
                if (!player.hasPermission("singham.mute")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                if (!authManager.isAuthenticated(player)) {
                    player.sendMessage(ChatColor.RED + "❌ You must authenticate first! Type /singham login in chat.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /singham mute <player> <reason>");
                    return true;
                }
                handleMute(player, args);
                break;

            case "unmute":
                if (!player.hasPermission("singham.unmute")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                if (!authManager.isAuthenticated(player)) {
                    player.sendMessage(ChatColor.RED + "❌ You must authenticate first! Type /singham login in chat.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /singham unmute <player>");
                    return true;
                }
                handleUnmute(player, args);
                break;

            case "warn":
                if (!player.hasPermission("singham.warn")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                if (!authManager.isAuthenticated(player)) {
                    player.sendMessage(ChatColor.RED + "❌ You must authenticate first! Type /singham login in chat.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /singham warn <player> <reason>");
                    return true;
                }
                handleWarn(player, args);
                break;

            case "vanish":
            case "v":
                if (!player.hasPermission("singham.vanish")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                if (!authManager.isAuthenticated(player)) {
                    player.sendMessage(ChatColor.RED + "❌ You must authenticate first! Type /singham login in chat.");
                    return true;
                }
                handleVanish(player);
                break;

            case "note":
                if (!player.hasPermission("singham.note")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                if (!authManager.isAuthenticated(player)) {
                    player.sendMessage(ChatColor.RED + "❌ You must authenticate first! Type /singham login in chat.");
                    return true;
                }
                handleNote(player, args);
                break;

            case "history":
                if (!player.hasPermission("singham.history")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                if (!authManager.isAuthenticated(player)) {
                    player.sendMessage(ChatColor.RED + "❌ You must authenticate first! Type /singham login in chat.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /singham history <player>");
                    return true;
                }
                handleHistory(player, args);
                break;

            case "chatlock":
            case "lockchat":
                if (!player.hasPermission("singham.chatlock")) {
                    player.sendMessage(ChatColor.RED + "❌ No permission!");
                    return true;
                }
                if (!authManager.isAuthenticated(player)) {
                    player.sendMessage(ChatColor.RED + "❌ You must authenticate first! Type /singham login in chat.");
                    return true;
                }
                handleChatLock(player);
                break;

            default:
                player.sendMessage(ChatColor.RED + "❌ Unknown command. Use /singham help for commands.");
                break;
        }

        return true;
    }

    // ===== TAB COMPLETER =====
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String cmd : COMMANDS) {
                if (cmd.startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2) {
            String subCmd = args[0].toLowerCase();
            
            if (subCmd.equals("note")) {
                for (String action : NOTE_ACTIONS) {
                    if (action.startsWith(args[1].toLowerCase())) {
                        completions.add(action);
                    }
                }
            } else if (subCmd.equals("ban") || subCmd.equals("unban") || subCmd.equals("kick") || 
                       subCmd.equals("mute") || subCmd.equals("unmute") || subCmd.equals("warn") ||
                       subCmd.equals("tempban") || subCmd.equals("history")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(p.getName());
                    }
                }
            }
        } else if (args.length == 3) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("note")) {
                String action = args[1].toLowerCase();
                if (action.equals("add") || action.equals("remove")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(p.getName());
                        }
                    }
                }
            }
        }

        return completions;
    }

    // ===== AUTHENTICATION HANDLERS =====
    private void showStatus(Player player) {
        boolean isAuth = authManager.isAuthenticated(player);
        boolean isRegistered = authManager.isRegistered(player.getUniqueId());
        
        player.sendMessage(ChatColor.GREEN + "═══════════════════════════════════");
        player.sendMessage(ChatColor.GOLD + "🔐 Authentication Status");
        player.sendMessage(ChatColor.GREEN + "Registered: " + (isRegistered ? ChatColor.GREEN + "✅ Yes" : ChatColor.RED + "❌ No"));
        
        if (isAuth) {
            long sessionEnd = authManager.getSessionEnd(player.getUniqueId());
            long remaining = sessionEnd - System.currentTimeMillis();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60;
            player.sendMessage(ChatColor.GREEN + "Authenticated: " + ChatColor.GREEN + "✅ Yes");
            player.sendMessage(ChatColor.GRAY + "Session expires in: " + minutes + "m " + seconds + "s");
        } else {
            player.sendMessage(ChatColor.GREEN + "Authenticated: " + ChatColor.RED + "❌ No");
        }
        
        player.sendMessage(ChatColor.GRAY + "Vanish: " + 
            (vanishManager.isVanished(player) ? ChatColor.GREEN + "✅ Hidden" : ChatColor.RED + "❌ Visible"));
        player.sendMessage(ChatColor.GREEN + "═══════════════════════════════════");
    }

    // ===== BAN HANDLERS =====
    private void handleBan(Player player, String[] args) {
        String targetName = args[1];
        StringBuilder reason = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }
        String reasonStr = reason.toString().trim();

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "❌ Player not found or not online.");
            return;
        }

        Bukkit.getBanList(BanList.Type.NAME).addBan(
            target.getName(),
            reasonStr,
            null,
            player.getName()
        );
        target.kickPlayer(ChatColor.RED + "Banned: " + reasonStr);
        player.sendMessage(ChatColor.GREEN + "✅ Banned " + target.getName() + ": " + reasonStr);
    }

    private void handleUnban(Player player, String[] args) {
        String targetName = args[1];
        Bukkit.getBanList(BanList.Type.NAME).pardon(targetName);
        player.sendMessage(ChatColor.GREEN + "✅ Unbanned " + targetName);
    }

    private void handleBanList(Player player, String[] args) {
        java.util.Set<org.bukkit.BanEntry> banEntries = Bukkit.getBanList(BanList.Type.NAME).getBanEntries();

        if (banEntries.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "There are no active bans.");
            return;
        }

        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid page number.");
                return;
            }
        }

        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil((double) banEntries.size() / itemsPerPage);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int start = (page - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, banEntries.size());

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        player.sendMessage(ChatColor.GOLD + "Active Bans (" + banEntries.size() + ") - Page " + page + "/" + totalPages);
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");

        int index = 0;
        for (org.bukkit.BanEntry ban : banEntries) {
            if (index >= start && index < end) {
                String duration = ban.getExpiration() == null ? "Permanent" : "Temporary";
                player.sendMessage(ChatColor.YELLOW + "Player: " + ChatColor.WHITE + ban.getTarget());
                player.sendMessage(ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + (ban.getReason() != null ? ban.getReason() : "No reason"));
                player.sendMessage(ChatColor.YELLOW + "Type: " + ChatColor.WHITE + duration);
                player.sendMessage(ChatColor.GRAY + "─────────────────────────────────");
            }
            index++;
        }

        if (totalPages > 1) {
            player.sendMessage(ChatColor.GRAY + "Use /singham banlist " + (page + 1) + " for next page");
        }
    }

    // ===== TEMP BAN HANDLERS =====
    private void handleTempBan(Player player, String[] args) {
        String targetName = args[1];
        String durationStr = args[2];
        
        StringBuilder reason = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }
        String reasonStr = reason.length() > 0 ? reason.toString().trim() : "No reason provided";

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "❌ Player not found or not online.");
            return;
        }

        long duration = parseDuration(durationStr);
        if (duration == -1) {
            player.sendMessage(ChatColor.RED + "❌ Invalid duration format. Use: 7d, 12h, 30m, 60s");
            return;
        }

        long expiration = System.currentTimeMillis() + duration;
        Bukkit.getBanList(BanList.Type.NAME).addBan(
            target.getName(),
            reasonStr,
            new java.util.Date(expiration),
            player.getName()
        );
        target.kickPlayer(ChatColor.RED + "Temporarily Banned: " + reasonStr + 
                          "\n" + ChatColor.YELLOW + "Expires in: " + durationStr);

        player.sendMessage(ChatColor.GREEN + "✅ Temporarily banned " + target.getName() + 
                          " for " + durationStr + ": " + reasonStr);
    }

    private void handleTempBanList(Player player) {
        java.util.Set<org.bukkit.BanEntry> allBans = Bukkit.getBanList(BanList.Type.NAME).getBanEntries();
        java.util.List<org.bukkit.BanEntry> tempBans = new java.util.ArrayList<>();
        
        for (org.bukkit.BanEntry ban : allBans) {
            if (ban.getExpiration() != null) {
                tempBans.add(ban);
            }
        }

        if (tempBans.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "There are no active temporary bans.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        player.sendMessage(ChatColor.GOLD + "Temporary Bans (" + tempBans.size() + ")");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");

        for (org.bukkit.BanEntry ban : tempBans) {
            long remaining = ban.getExpiration().getTime() - System.currentTimeMillis();
            String remainingStr = formatTime(remaining);
            
            player.sendMessage(ChatColor.YELLOW + "Player: " + ChatColor.WHITE + ban.getTarget());
            player.sendMessage(ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + ban.getReason());
            player.sendMessage(ChatColor.YELLOW + "Remaining: " + ChatColor.RED + remainingStr);
            player.sendMessage(ChatColor.GRAY + "─────────────────────────────────");
        }
    }

    // ===== KICK HANDLER =====
    private void handleKick(Player player, String[] args) {
        String targetName = args[1];
        StringBuilder reason = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }
        String reasonStr = reason.toString().trim();

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "❌ Player not found or not online.");
            return;
        }

        target.kickPlayer(ChatColor.RED + "Kicked: " + reasonStr);
        player.sendMessage(ChatColor.GREEN + "✅ Kicked " + target.getName() + ": " + reasonStr);
    }

    // ===== MUTE HANDLERS =====
    private void handleMute(Player player, String[] args) {
        String targetName = args[1];
        StringBuilder reason = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }
        String reasonStr = reason.toString().trim();

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "❌ Player not found or not online.");
            return;
        }

        moderationService.mutePlayer(target.getUniqueId(), player.getUniqueId(), reasonStr);
        player.sendMessage(ChatColor.GREEN + "✅ Muted " + target.getName() + ": " + reasonStr);
        target.sendMessage(ChatColor.RED + "You have been muted: " + reasonStr);
    }

    private void handleUnmute(Player player, String[] args) {
        String targetName = args[1];
        moderationService.unmutePlayer(targetName, player.getUniqueId(), "Unmuted by staff");
        player.sendMessage(ChatColor.GREEN + "✅ Unmuted " + targetName);
    }

    // ===== WARN HANDLER =====
    private void handleWarn(Player player, String[] args) {
        String targetName = args[1];
        StringBuilder reason = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }
        String reasonStr = reason.toString().trim();

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "❌ Player not found or not online.");
            return;
        }

        moderationService.warnPlayer(target.getUniqueId(), player.getUniqueId(), reasonStr);
        player.sendMessage(ChatColor.GREEN + "✅ Warned " + target.getName() + ": " + reasonStr);
        target.sendMessage(ChatColor.RED + "You have been warned: " + reasonStr);
    }

    // ===== VANISH HANDLER =====
    private void handleVanish(Player player) {
        if (vanishManager.isVanished(player)) {
            vanishManager.unvanish(player);
        } else {
            vanishManager.vanish(player);
        }
    }

    // ===== NOTE HANDLERS =====
    private void handleNote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /singham note <add|remove|show> <player> [text/id]");
            player.sendMessage(ChatColor.GRAY + "Examples:");
            player.sendMessage(ChatColor.GRAY + "  /singham note add John Suspicious activity");
            player.sendMessage(ChatColor.GRAY + "  /singham note remove John <note_id>");
            player.sendMessage(ChatColor.GRAY + "  /singham note show John");
            return;
        }

        String action = args[1].toLowerCase();

        if (action.equals("show")) {
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Usage: /singham note show <player>");
                return;
            }
            String targetName = args[2];
            PlayerData targetData = playerRepository.findByName(targetName);
            if (targetData == null) {
                player.sendMessage(ChatColor.RED + "❌ Player not found in database.");
                return;
            }
            
            List<Note> notes = noteRepository.findByPlayer(targetData.getUuid());
            if (notes.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "No notes found for " + targetName);
                return;
            }
            
            player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
            player.sendMessage(ChatColor.GOLD + "📝 Notes for " + targetName);
            player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
            for (Note n : notes) {
                player.sendMessage(ChatColor.YELLOW + "[" + n.getId().toString().substring(0, 8) + "] " + 
                    ChatColor.WHITE + n.getNote());
            }
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /singham note <add|remove> <player> [text/id]");
            return;
        }

        String targetName = args[2];
        PlayerData targetData = playerRepository.findByName(targetName);
        if (targetData == null) {
            player.sendMessage(ChatColor.RED + "❌ Player not found in database.");
            return;
        }

        if (action.equals("add")) {
            if (args.length < 4) {
                player.sendMessage(ChatColor.RED + "Usage: /singham note add <player> <text>");
                return;
            }
            StringBuilder noteText = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                noteText.append(args[i]).append(" ");
            }
            Note note = new Note(
                UUID.randomUUID(),
                targetData.getUuid(),
                player.getUniqueId(),
                noteText.toString().trim(),
                System.currentTimeMillis()
            );
            noteRepository.save(note);
            player.sendMessage(ChatColor.GREEN + "✅ Note added to " + targetName);
        } else if (action.equals("remove")) {
            if (args.length < 4) {
                player.sendMessage(ChatColor.RED + "Usage: /singham note remove <player> <note_id>");
                return;
            }
            try {
                UUID noteId = UUID.fromString(args[3]);
                noteRepository.delete(noteId);
                player.sendMessage(ChatColor.GREEN + "✅ Note removed from " + targetName);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "❌ Invalid note ID.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "❌ Unknown note action. Use add, remove, or show.");
        }
    }

    // ===== HISTORY HANDLER =====
    private void handleHistory(Player player, String[] args) {
        String playerName = args[1];
        PlayerData playerData = playerRepository.findByName(playerName);
        if (playerData == null) {
            player.sendMessage(ChatColor.RED + "❌ Player not found.");
            return;
        }

        UUID playerUuid = playerData.getUuid();
        
        List<Punishment> punishments = moderationService.getPlayerPunishments(playerUuid);
        List<Warning> warnings = moderationService.getPlayerWarnings(playerUuid);
        List<Note> notes = noteRepository.findByPlayer(playerUuid);

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        player.sendMessage(ChatColor.GOLD + "📋 History for " + playerName);
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        
        long banCount = punishments.stream()
            .filter(p -> p.getType() == Punishment.PunishmentType.BAN)
            .count();
        player.sendMessage(ChatColor.YELLOW + "Total Bans: " + ChatColor.WHITE + banCount);
        
        long activePunishments = punishments.stream()
            .filter(p -> p.isActive() && !p.isExpired())
            .count();
        player.sendMessage(ChatColor.YELLOW + "Active Punishments: " + (activePunishments > 0 ? ChatColor.RED : ChatColor.GREEN) + activePunishments);
        
        player.sendMessage(ChatColor.GRAY + "─────────────────────────────────");
        
        if (!punishments.isEmpty()) {
            player.sendMessage(ChatColor.AQUA + "Punishments:");
            for (Punishment p : punishments) {
                String color = p.isActive() && !p.isExpired() ? ChatColor.RED.toString() : ChatColor.GRAY.toString();
                player.sendMessage(color + "• " + p.getType() + ": " + p.getReason() + 
                    (p.getDuration() == -1 ? " (Permanent)" : ""));
            }
        }

        if (!warnings.isEmpty()) {
            player.sendMessage(ChatColor.AQUA + "Warnings:");
            for (Warning w : warnings) {
                player.sendMessage(ChatColor.YELLOW + "• " + w.getReason());
            }
        }

        if (!notes.isEmpty()) {
            player.sendMessage(ChatColor.AQUA + "Notes:");
            for (Note n : notes) {
                player.sendMessage(ChatColor.GREEN + "• [" + n.getId().toString().substring(0, 8) + "] " + n.getNote());
            }
        }

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
    }

    // ===== CHAT LOCK HANDLER =====
    private void handleChatLock(Player player) {
        chatLockManager.toggleLock();
        boolean locked = chatLockManager.isLocked();
        
        String status = locked ? ChatColor.RED + "LOCKED" : ChatColor.GREEN + "UNLOCKED";
        Bukkit.broadcastMessage(ChatColor.GOLD + "═══════════════════════════════════");
        Bukkit.broadcastMessage(ChatColor.GOLD + "🔒 Chat is now " + status + ChatColor.GOLD + "!");
        Bukkit.broadcastMessage(locked ? ChatColor.RED + "Only staff members can chat." : ChatColor.GREEN + "Chat is now open to everyone.");
        Bukkit.broadcastMessage(ChatColor.GOLD + "═══════════════════════════════════");
    }

    // ===== UTILITY METHODS =====
    private long parseDuration(String duration) {
        char unit = duration.charAt(duration.length() - 1);
        try {
            long value = Long.parseLong(duration.substring(0, duration.length() - 1));
            switch (unit) {
                case 's': return TimeUnit.SECONDS.toMillis(value);
                case 'm': return TimeUnit.MINUTES.toMillis(value);
                case 'h': return TimeUnit.HOURS.toMillis(value);
                case 'd': return TimeUnit.DAYS.toMillis(value);
                case 'w': return TimeUnit.DAYS.toMillis(value * 7);
                default: return -1;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String formatTime(long millis) {
        long days = millis / (1000 * 60 * 60 * 24);
        long hours = (millis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (millis % (1000 * 60)) / 1000;
        
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }

    // ===== HELP COMMAND =====
    private void sendHelp(Player player) {
        boolean hasStaff = player.hasPermission("singham.staff");
        boolean isAuth = authManager.isAuthenticated(player);
        boolean isRegistered = authManager.isRegistered(player.getUniqueId());

        if (!hasStaff) {
            player.sendMessage(ChatColor.RED + "❌ You don't have permission to use this command!");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.GOLD + "🔐 SinghamCore Staff Commands");
        player.sendMessage(ChatColor.GREEN + "═══════════════════════════════════════");
        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "🔑 Authentication (Chat-based - Password Hidden):");
        if (!isRegistered) {
            player.sendMessage(ChatColor.YELLOW + "  /singham register" + ChatColor.WHITE + " - Start registration (type password in chat)");
        }
        if (isRegistered && !isAuth) {
            player.sendMessage(ChatColor.YELLOW + "  /singham login" + ChatColor.WHITE + " - Login (type password in chat)");
        }
        if (isAuth) {
            player.sendMessage(ChatColor.YELLOW + "  /singham changepassword" + ChatColor.WHITE + " - Change password (type old then new)");
            player.sendMessage(ChatColor.YELLOW + "  /singham status" + ChatColor.WHITE + " - Check authentication status");
        }
        player.sendMessage(ChatColor.YELLOW + "  /singham logout" + ChatColor.WHITE + " - Logout from staff mode");
        
        if (isAuth) {
            player.sendMessage("");
            player.sendMessage(ChatColor.AQUA + "👮 Moderation Commands:");
            player.sendMessage(ChatColor.YELLOW + "  /singham ban <player> <reason>" + ChatColor.WHITE + " - Ban a player");
            player.sendMessage(ChatColor.YELLOW + "  /singham unban <player>" + ChatColor.WHITE + " - Unban a player");
            player.sendMessage(ChatColor.YELLOW + "  /singham banlist [page]" + ChatColor.WHITE + " - View all bans");
            player.sendMessage(ChatColor.YELLOW + "  /singham tempban <player> <duration> [reason]" + ChatColor.WHITE + " - Temp ban (7d, 12h, 30m, 60s)");
            player.sendMessage(ChatColor.YELLOW + "  /singham tempbanlist" + ChatColor.WHITE + " - View temp bans");
            player.sendMessage(ChatColor.YELLOW + "  /singham kick <player> <reason>" + ChatColor.WHITE + " - Kick a player");
            player.sendMessage(ChatColor.YELLOW + "  /singham mute <player> <reason>" + ChatColor.WHITE + " - Mute a player");
            player.sendMessage(ChatColor.YELLOW + "  /singham unmute <player>" + ChatColor.WHITE + " - Unmute a player");
            player.sendMessage(ChatColor.YELLOW + "  /singham warn <player> <reason>" + ChatColor.WHITE + " - Warn a player");
            player.sendMessage(ChatColor.YELLOW + "  /singham vanish" + ChatColor.WHITE + " - Toggle vanish (fully invisible)");
            player.sendMessage(ChatColor.YELLOW + "  /singham v" + ChatColor.WHITE + " - Toggle vanish (shortcut)");
            player.sendMessage("");
            player.sendMessage(ChatColor.AQUA + "📝 Notes System:");
            player.sendMessage(ChatColor.YELLOW + "  /singham note add <player> <text>" + ChatColor.WHITE + " - Add a note");
            player.sendMessage(ChatColor.YELLOW + "  /singham note remove <player> <id>" + ChatColor.WHITE + " - Remove a note");
            player.sendMessage(ChatColor.YELLOW + "  /singham note show <player>" + ChatColor.WHITE + " - Show notes for a player");
            player.sendMessage("");
            player.sendMessage(ChatColor.AQUA + "📋 History System:");
            player.sendMessage(ChatColor.YELLOW + "  /singham history <player>" + ChatColor.WHITE + " - View player history");
            player.sendMessage("");
            player.sendMessage(ChatColor.AQUA + "🔒 Chat Control:");
            player.sendMessage(ChatColor.YELLOW + "  /singham chatlock" + ChatColor.WHITE + " - Toggle chat lock");
            player.sendMessage(ChatColor.YELLOW + "  /singham lockchat" + ChatColor.WHITE + " - Toggle chat lock");
            player.sendMessage("");
            player.sendMessage(ChatColor.AQUA + "ℹ️ Other:");
            player.sendMessage(ChatColor.YELLOW + "  /singham help" + ChatColor.WHITE + " - Show this help menu");
        } else if (isRegistered) {
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "⚠️ You are not authenticated!");
            player.sendMessage(ChatColor.GRAY + "Type /singham login in chat to access moderation commands.");
        } else {
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "⚠️ You are not registered!");
            player.sendMessage(ChatColor.GRAY + "Type /singham register in chat to create your account.");
        }
        
        player.sendMessage(ChatColor.GREEN + "═══════════════════════════════════════");
    }
}