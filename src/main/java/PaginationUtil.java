import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PaginationUtil {
    private static final int ITEMS_PER_PAGE = 10;

    public static <T> void sendPaginated(CommandSender sender, List<T> items, String title, 
                                          java.util.function.Function<T, String> itemFormatter, 
                                          int page) {
        if (items.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No items to display.");
            return;
        }

        int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, items.size());

        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        sender.sendMessage(ChatColor.GOLD + title + " (" + items.size() + " total) - Page " + page + "/" + totalPages);
        sender.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");

        for (int i = start; i < end; i++) {
            sender.sendMessage(itemFormatter.apply(items.get(i)));
        }

        if (totalPages > 1) {
            sender.sendMessage(ChatColor.GRAY + "Type /" + title.toLowerCase().replace(" ", "") + " " + 
                               (page + 1) + " for next page");
        }
    }
}