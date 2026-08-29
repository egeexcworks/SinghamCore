import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private final ConfigManager configManager;
    private Connection connection;

    public DatabaseManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void connect() {
        try {
            String url = configManager.getDatabaseUrl();
            
            // Extract the path from the URL and create directories
            // URL format: jdbc:sqlite:plugins/SinghamCore/data.db
            String path = url.replace("jdbc:sqlite:", "");
            File dbFile = new File(path);
            File parentDir = dbFile.getParentFile();
            
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                configManager.getPlugin().getLogger().info("📁 Created database directory: " + parentDir.getPath());
            }
            
            connection = DriverManager.getConnection(url);
            configManager.getPlugin().getLogger().info("✅ Database connected successfully!");
        } catch (SQLException e) {
            configManager.getPlugin().getLogger().severe("❌ Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                configManager.getPlugin().getLogger().info("✅ Database disconnected!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}