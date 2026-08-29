import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final PlayerRepository repository;
    private final VanishManager vanishManager;

    public PlayerListener(PlayerRepository repository, VanishManager vanishManager) {
        this.repository = repository;
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Hide vanished players from new joiners
        for (Player vanished : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (vanishManager.isVanished(vanished) && !vanished.equals(player)) {
                player.hidePlayer(vanished);
            }
        }
        
        PlayerData data = repository.findById(player.getUniqueId());
        
        if (data == null) {
            data = new PlayerData(
                player.getUniqueId(),
                player.getName(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
            );
        } else {
            data.setLastSeen(System.currentTimeMillis());
            if (!data.getName().equals(player.getName())) {
                data.setName(player.getName());
            }
        }
        
        repository.save(data);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Clear authentication session on quit
        if (SinghamCore.getInstance() != null) {
            AuthManager authManager = SinghamCore.getInstance().getAuthManager();
            if (authManager != null) {
                authManager.removeSession(player.getUniqueId());
            }
        }
        
        // If player is vanished, cancel their leave message
        if (vanishManager.isVanished(player)) {
            event.setQuitMessage(null);
        }
        
        PlayerData data = repository.findById(player.getUniqueId());
        
        if (data != null) {
            data.setLastSeen(System.currentTimeMillis());
            repository.save(data);
        }
    }
}