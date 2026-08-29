import org.bukkit.plugin.java.JavaPlugin;

public class SinghamCore extends JavaPlugin {
    private static SinghamCore instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private DatabaseTables databaseTables;
    private PlayerRepository playerRepository;
    private PunishmentRepository punishmentRepository;
    private WarningRepository warningRepository;
    private NoteRepository noteRepository;
    private ModerationService moderationService;
    private VanishManager vanishManager;
    private DiscordManager discordManager;
    private AuthManager authManager;
    private ChatLockManager chatLockManager;
    private PlayerListener playerListener;
    private ModerationListener moderationListener;
    private AuthChatListener authChatListener;
    private SinghamCommand singhamCommand;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        databaseManager = new DatabaseManager(configManager);
        databaseManager.connect();

        databaseTables = new DatabaseTables(databaseManager);
        databaseTables.createTables();

        playerRepository = new PlayerRepository(databaseManager);
        punishmentRepository = new PunishmentRepository(databaseManager);
        warningRepository = new WarningRepository(databaseManager);
        noteRepository = new NoteRepository(databaseManager);

        moderationService = new ModerationService(
            playerRepository,
            punishmentRepository,
            warningRepository
        );

        discordManager = new DiscordManager(this);
        authManager = new AuthManager(this, databaseManager);
        vanishManager = new VanishManager(this, discordManager);
        chatLockManager = new ChatLockManager();

        playerListener = new PlayerListener(playerRepository, vanishManager);
        moderationListener = new ModerationListener(moderationService, chatLockManager);
        authChatListener = new AuthChatListener(authManager);

        // Register listeners
        getServer().getPluginManager().registerEvents(authChatListener, this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(moderationListener, this);

        singhamCommand = new SinghamCommand(
            moderationService, 
            vanishManager, 
            discordManager, 
            authManager,
            noteRepository,
            playerRepository,
            chatLockManager
        );

        getCommand("singham").setExecutor(singhamCommand);
        getCommand("singham").setTabCompleter(singhamCommand);
        
        discordManager.start();

        getLogger().info("SinghamCore has been enabled!");
    }

    @Override
    public void onDisable() {
        if (discordManager != null) discordManager.stop();
        if (databaseManager != null) databaseManager.disconnect();
        if (authManager != null) authManager.clearAllSessions();
        getLogger().info("SinghamCore has been disabled!");
    }

    public static SinghamCore getInstance() {
        return instance;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }
}