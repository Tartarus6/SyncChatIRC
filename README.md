# SyncChatIRC

This is a Fabric Minecraft mod that enables communication through IRC to an external IRC server.
It works well as simply an IRC bridge, but some features have been added specifically to mesh with the partner project, also called [SyncChatIRC](https://github.com/greenturtle537/SyncChatIRC), which is a bridge between the Synchronet BBS multinode chat and IRC.


### Features
- Chat messages in MC show up in IRC, and vice versa
- Death messages from Minecraft appear over IRC
- Advancement messages from Minecraft appear over IRC

### Commands
- `/irc`
  - `/irc connect`: attempts connection to IRC server
  - `/irc disconnect`: disconnects from IRC server
  - `/irc reload`: (NOT IMPLEMENTED) reloads configuration
  - `/irc send`: manually send message to IRC server

### Configuration
The configuration file is located at `config/syncchatirc.json`

#### Password Configuration
- `serverPassword`: Password required to connect to the IRC server (if required)
- `userPassword`: Password for user authentication via NickServ IDENTIFY command - used when your nickname is registered and requires authentication
- `channelPassword`: Password for joining password-protected channels

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
  "userPassword": "",
  "channelPassword": "",
  "relayServerMessages": true,
  "relayJoinLeave": true,
  "relayDeathMessages": true,
  "relayAdvancements": true,
  "messageFormat": "<%s> %s",
  "joinFormat": "%s joined the game",
  "leaveFormat": "%s left the game",
  "deathFormat": "%s",
  "advancementFormat": "%s has made the advancement [%s]"
}
```

### Screenshots
<img width="1632" height="492" alt="image" src="https://github.com/user-attachments/assets/061994af-3a66-4c1b-b7cf-79c3207b14f6" />
<img width="1416" height="368" alt="image" src="https://github.com/user-attachments/assets/b4158f06-ade3-4d64-9d2c-6490c68e6940" />