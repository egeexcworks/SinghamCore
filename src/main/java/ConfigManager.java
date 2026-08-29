import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    public String getDatabaseUrl() {
        return plugin.getConfig().getString("database.url", "jdbc:sqlite:plugins/SinghamCore/data.db");
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return plugin.getConfig().getBoolean(path, defaultValue);
    }

    public String getString(String path, String defaultValue) {
        return plugin.getConfig().getString(path, defaultValue);
    }

    public int getInt(String path, int defaultValue) {
        return plugin.getConfig().getInt(path, defaultValue);
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}