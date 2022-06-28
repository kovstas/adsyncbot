# AD Sync Telegram Bot [WIP]
A telegram bot connects your organization members in Active Directory (AD) with telegram users.

## Usage

### Admin
For synchronisation, you should be an administrator of your organization, and you need to follow these steps:
1. Add the AD app "AD telegram sync app" to your AD organization.
2. Run `/start` command in a private chat with the bot and follow the bot instructions.

After you can work with this bot in chats. As the bot is added, run `/start` command in the chat.

### Member
Just run `/start` command in a private chat. The bot will ask you to authorise in your organization.

Demo bot - [@AdSyncBot](https://t.me/AdSyncBot).

## Features
- [x] delete inactive organization members from telegram chats
- [x] limit unauthorised users in chats
- [ ] associate a chat with AD group
- [ ] send AD group's notifications to the corresponded chat
- [ ] support a channel and a supergroup
- [ ] support multiple AD organizations

## Limits
- Telegram API doesn't allow getting information about group members. So if you add the bot to the already existing chat, the bot could only check the user's permission after its first message.


## Development
For local development, you need an instance of Postgres with `ad_sync_bot` database, a telegram bot token and an AD application. 
The telegram token can be taken from [@BotFather](https://t.me/BotFather). This token should be in the `TELEGRAM_TOKEN` environment variable.
Also, `MS_CLIENT_ID`, `MS_CLIENT_SECRET` env variables should be defined. You can take it from your AD application. 

After you can run the app with the command `sbt reStart`.

You can check work of the app with `http://localhost:8080/api/v1/health` endpoint or just send command `/start` to your bot in Telegram either.

## AD Application Settings
If you want to connect the bot to your AD application, you can use settings below.
[How register an application link](https://docs.microsoft.com/en-us/azure/active-directory/develop/quickstart-register-app)

### Authentication
- Redirect URIs:
  - `YOUR_DOMAIN/api/v1/auth/organization-login`
  - `YOUR_DOMAIN/api/v1/auth/organization-member-login`
- Implicit grant and hybrid flows: select `Access tokens (used for implicit flows)`
- Supported account types: `Accounts in any organizational directory (Any Azure AD directory - Multitenant)`
- Allow public client flow: `Yes`
### API permissions
- Application: `Group.Read.All`, `GroupMember.Read.All`, `User.Read.All`, `organization.Read.All`
- Delegated: `email`, `openid`, `profile`, `User.read`


## Deployment
`sbt docker:publishLocal` - the command for publishing docker image of the app

Environment variables:
- `PORT` - the port of the http server for handling OAuth callbacks (default: `8080`)
- `TELEGRAM_TOKEN` - a telegram bot token
- `DB_URL` - postgres jdbc url. it should be without auth data (default: `jdbc:postgresql://localhost:5432/ad_sync_bot`)
- `DB_USER` - postgres user (default: `postgres`)
- `DB_PASS` - postgres password (default: `123`)
- `MS_CLIENT_ID` - AD application ID
- `MS_CLIENT_SECRET` - AD application secret
- `BOT_URI` - bot uri (default: `https://t.me/AdSyncBot`)
- `BOT_ID` - bot id (default: `5537852768`)
- `OAUTH_LOGIN_REDIRECT` - organization login redirect uri (default: `http://localhost:8080/api/v1/auth/organization-login`)
- `OAUTH_LOGIN_REDIRECT` - user login redirect uri (default: `http://localhost:8080/api/v1/auth/organization-member-login`)

