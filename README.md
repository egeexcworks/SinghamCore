🚀 SinghamCore
Advanced Moderation Plugin for Paper 1.21.11+


✨ Features
🔐 Secure Authentication - BCrypt hashing, chat-based login (passwords never shown)

👮 Full Moderation - Ban, TempBan, Kick, Mute, Warn with auto-ban

👻 Advanced Vanish - Fully invisible with DiscordSRV fake join/leave messages

📝 Notes & History - Private staff notes & complete player history

🔒 Chat Lock - Global chat freeze with staff bypass

🌐 Discord Integration - Automatic DiscordSRV detection

📋 Commands
🔑 Authentication
yaml
/singham register      # Create account (type password in chat)
/singham login         # Login (type password in chat)
/singham changepassword # Change password
/singham logout        # Logout
/singham status        # Check auth status
👮 Moderation
yaml
/singham ban <player> <reason>         # Permanent ban
/singham unban <player>                # Unban player
/singham banlist [page]                # View all bans
/singham tempban <player> <dur> [reason] # Temp ban (30s, 15m, 12h, 7d, 2w)
/singham tempbanlist                   # View temp bans
/singham kick <player> <reason>        # Kick player
/singham mute <player> <reason>        # Mute player
/singham unmute <player>               # Unmute player
/singham warn <player> <reason>        # Warn player
👻 Vanish
yaml
/singham vanish   # Toggle vanish
/singham v        # Toggle vanish (shortcut)
📝 Notes
yaml
/singham note add <player> <text>     # Add note
/singham note remove <player> <id>    # Remove note
/singham note show <player>           # Show notes
📋 History
yaml
/singham history <player>   # View complete player history
🔒 Chat Control
yaml
/singham chatlock   # Toggle chat lock
/singham lockchat   # Toggle chat lock (alias)
🛡️ Permissions
Permission	Access
singham.staff	Authentication commands
singham.ban	Ban commands
singham.unban	Unban commands
singham.banlist	Ban list
singham.tempban	Temp ban
singham.tempbanlist	Temp ban list
singham.kick	Kick
singham.mute	Mute
singham.unmute	Unmute
singham.warn	Warn
singham.vanish	Vanish
singham.note	Notes system
singham.history	History
singham.chatlock	Chat lock
singham.chatlock.bypass	Bypass chat lock
📦 Installation
Download singham-core-1.0.0.jar

Place in plugins/ folder

Start server

Configure plugins/SinghamCore/config.yml

Give staff permissions or OP

Config Example
yaml
database:
  url: "jdbc:sqlite:plugins/SinghamCore/data.db"

discord:
  webhook-url: "https://discord.com/api/webhooks/YOUR_WEBHOOK"

moderation:
  max_warnings: 5
  auto_ban_on_max_warnings: true

vanish:
  fake_messages: true
  hide_from_tab: true
🔐 Security
✅ BCrypt password hashing

✅ Never logs passwords

✅ 5 failed attempts = 5-min lockout

✅ 5-min session timeout

✅ UUID-based identification

🌐 Discord Integration
🔄 Auto-detects DiscordSRV

👻 Fake join/leave messages when vanished

🎨 Perfect embed style matching DiscordSRV

🔒 No extra config needed

🚀 Quick Start
yaml
# Register
/singham register → [type password]

# Login
/singham login → [type password]

# Start moderating
/singham status          # Check auth
/singham vanish          # Go invisible
/singham ban John Spam   # Ban player
/singham history John    # Check history
/singham logout          # Logout
📝 License
All rights reserved. Proprietary software.

SinghamCore - Advanced Moderation, Simplified! 🚀
