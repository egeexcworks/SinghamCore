🚀 SinghamCore

Advanced Minecraft Moderation Plugin for Paper 1.21.11+

SinghamCore is a powerful, secure, and feature-rich moderation plugin for Minecraft servers running on Paper 1.21.11+. It provides a complete suite of administrative tools with a focus on security, performance, and ease of use.

✨ Features
🔐 Secure Authentication System
BCrypt password hashing - Never stores plaintext passwords

Chat-based authentication - Passwords are entered through chat and never shown in console or logs

Session management - 5-minute session timeout with auto-logout

Brute force protection - 5 failed attempts = 5-minute lockout

UUID-based identification - Uses player UUID, not username

👮 Moderation Tools
Permanent bans - Native Minecraft ban system

Temporary bans - Support for seconds, minutes, hours, days, weeks

Ban list - Paginated view of all active bans

Kicks - Customizable kick messages

Mutes - Permanent mute support

Warnings - Auto-ban on configurable warning limit

👻 Advanced Vanish System
Fully invisible - Hides from players, tab list, and entity targeting

DiscordSRV integration - Sends fake join/leave messages to Discord

Fake messages - In-game and Discord messages look exactly like real join/leave events

📝 Notes & History
Staff notes - Add private notes to player profiles

Complete history - View player's entire moderation record

Color-coded punishments - Active vs expired punishments clearly distinguished

🔒 Chat Management
Global chat lock - Freeze chat instantly to stop raids/spam

Staff bypass - Staff can chat even when chat is locked

🌐 Discord Integration
Automatic DiscordSRV detection - No additional configuration needed

Fake join/leave messages - Perfect DiscordSRV embed style

No Discord configuration required - Works with DiscordSRV's existing setup

📋 Command Reference
🔑 Authentication Commands
Command	Description
/singham register	Start registration (type password in chat - hidden)
/singham login	Login (type password in chat - hidden)
/singham changepassword	Change password (type old then new - hidden)
/singham logout	Logout from staff mode
/singham status	Check authentication status
👮 Moderation Commands
Command	Description
/singham ban <player> <reason>	Permanently ban a player
/singham unban <player>	Unban a player
/singham banlist [page]	View all active bans
/singham tempban <player> <duration> [reason]	Temporarily ban a player
/singham tempbanlist	View active temp bans
/singham kick <player> <reason>	Kick a player
/singham mute <player> <reason>	Mute a player
/singham unmute <player>	Unmute a player
/singham warn <player> <reason>	Issue a warning
👻 Privacy Commands
Command	Description
/singham vanish or /singham v	Toggle vanish mode
📝 Notes System
Command	Description
/singham note add <player> <text>	Add a private note
/singham note remove <player> <id>	Remove a note
/singham note show <player>	Show notes for a player
📋 History System
Command	Description
/singham history <player>	View complete player history
🔒 Chat Control
Command	Description
/singham chatlock or /singham lockchat	Toggle global chat lock
ℹ️ Help
Command	Description
/singham help	Show all commands
🔧 Duration Formats
Format	Meaning
30s	30 seconds
15m	15 minutes
12h	12 hours
7d	7 days
2w	2 weeks
Example: /singham tempban John 7d Spamming

🛡️ Permission Nodes
Permission	Command Access
singham.staff	Authentication commands
singham.ban	/singham ban
singham.unban	/singham unban
singham.banlist	/singham banlist
singham.tempban	/singham tempban
singham.tempbanlist	/singham tempbanlist
singham.kick	/singham kick
singham.mute	/singham mute
singham.unmute	/singham unmute
singham.warn	/singham warn
singham.vanish	/singham vanish
singham.note	/singham note
singham.history	/singham history
singham.chatlock	/singham chatlock
singham.chatlock.bypass	Bypass chat lock
📦 Installation
Requirements
Paper 1.21.11+ (or compatible fork)

Java 21 or higher

DiscordSRV (optional - for Discord integration)

Steps
Download the latest singham-core-1.0.0.jar

Place it in your server's plugins/ folder

Start the server (this generates the config files)

Configure plugins/SinghamCore/config.yml

Give staff permissions or OP

Enjoy! 🎉

Configuration
yaml
# SinghamCore Configuration
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
🎯 Quick Start Guide
For Staff Members
Register: /singham register → type password in chat

Login: /singham login → type password in chat

Check status: /singham status

Start moderating: Use any moderation command

Logout: /singham logout when done

Example Workflow
text
/singham login → [type password]
/singham status → Check session time
/singham vanish → Go invisible
/singham ban John Spamming → Ban a player
/singham note add John "Suspected hacking" → Add note
/singham history John → Check player history
/singham logout → Logout when done
🔐 Security Features
BCrypt password hashing - Industry-standard secure hashing

Never stores plaintext passwords - Only hashed passwords are stored

No password logging - Passwords are never written to console or logs

Chat-based authentication - Passwords are entered through chat events that are cancelled

Failed attempt tracking - 5 failed attempts = lockout

Session management - Auto-logout after 5 minutes

UUID-based identification - Prevents username spoofing

🌐 Discord Integration
How It Works
When a staff member uses /singham vanish:

In-game: Egeexc_ left the game (fake message)

Discord: Egeexc_ left the server (fake embed)

Staff becomes invisible to all players

Staff appears offline in Discord player list

When unvanishing:

In-game: Egeexc_ joined the game (fake message)

Discord: Egeexc_ joined the server (fake embed)

Staff becomes visible again

DiscordSRV Integration
Automatic detection - No configuration needed

Perfect embed style - Matches DiscordSRV's join/leave messages exactly

No external dependencies - Works with or without DiscordSRV

🐛 Reporting Issues
Found a bug? Have a feature request? Please open an issue on GitHub.

📝 License
All rights reserved. This software is proprietary and confidential.

👨‍💻 Author
SinghamCore Team

🙏 Acknowledgments
PaperMC - Minecraft server software

DiscordSRV - Discord integration

📊 Statistics
Total Commands: 20+

Languages: Java 21

Database: SQLite

Optional Integrations: DiscordSRV

SinghamCore - Advanced Moderation, Simplified! 🚀
