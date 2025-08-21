# SyncChatIRC

This is a Fabric Minecraft mod that enables communication through IRC to an external IRC server.


### Features
- Chat messages in MC show up in IRC, and vice versa
- Death messages from Minecraft appear over IRC
- Advancement messages from Minecraft appear over IRC

### Commands
- `/irc`
  - `/irc connect`: attempts connection to IRC server
  - `/irc disconnect`: disconnects from IRC server
  - `/irc reload`: (NOT IMPLEMENTED) reloads configuration
  - `/irc send`: manually send message to IRC server (for testing)
  - `/irc test`: (NON-FUNCTIONAL) test mod functionality

### Configuration
The configuration file is located at `config/syncchatirc.json`

Example Configuration:
```json
{
  "server": "irc.example.com",
  "port": 6667,
  "nickname": "MinecraftBot",
  "username": "MinecraftBot",
  "realname": "Minecraft IRC Bot",
  "channel": "#mc",
  "enabled": true,
  "useSSL": false,
  "serverPassword": "",
  "channelPassword": "",
  "relayServerMessages": true,
  "relayJoinLeave": true,
  "relayDeathMessages": true,
  "relayAdvancements": true,
  "messageFormat": "[MC] <%s> %s",
  "joinFormat": "[MC] * %s joined the game",
  "leaveFormat": "[MC] * %s left the game",
  "deathFormat": "[MC] * %s",
  "advancementFormat": "[MC] * %s has made the advancement [%s]"
}
```

### Screenshots
<img width="1632" height="492" alt="image" src="https://github.com/user-attachments/assets/061994af-3a66-4c1b-b7cf-79c3207b14f6" />
<img width="1416" height="368" alt="image" src="https://github.com/user-attachments/assets/b4158f06-ade3-4d64-9d2c-6490c68e6940" />


