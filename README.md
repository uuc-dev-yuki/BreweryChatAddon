# Brewery Chat Addon

A server-side Fabric addon for **Brewery**: while a player is drunk, their chat messages may be randomly replaced with configured phrases.

## Requirements

- Minecraft `1.21.0-1.21.11` (focus/build: `1.21.10`)
- Fabric Loader `0.18.2`
- Fabric API (as used by your modpack)
- Brewery (Patbox) `0.13.0+` (tested on `0.13.0+1.21.9-rc1`)

## Installation

- Copy `brewery-chat-addon-<version>.jar` into `mods/`.
- Start the server.

Config file will be created on first start:

- `config/BreweryChatAddon/config.json`

## Configuration

In `config.json`:

- `enabled`: enable/disable
- `language`: active language (`english`, `ukrainian`)
- `alcoholRules`: list of rules based on alcoholLevel

### alcoholRules

The addon tracks drunkenness using Brewery `alcoholLevel`. Each alcohol range defines a chance and a list of messages.

Rule format:

- `min` (required)
- `max` (optional; if missing, the range has no upper bound)
- `chance` (can be `0..1` or `0..100` percent)
- `messages` (either a string list, or an object with language keys; a random phrase is chosen from the active language list)

Language files are created on first start:

- `config/BreweryChatAddon/languages/*.json`

## Commands

- `/brreload`

## Permissions

- `brewerychataddon.reload`
  - Allows running `/brreload`.
  - If the Permissions API is not present, falls back to OP level 2.

- `brewerychataddon.bypass`
  - Players with this permission will **never** have their messages replaced.

## Integrations

- If **Carbon Chat** is installed, the addon uses the `carbonchat` entrypoint and mutates messages via Carbon's early chat event.
- Without Carbon Chat, the addon falls back to `ServerMessageEvents.ALLOW_CHAT_MESSAGE`.

## Credits

This is an addon for **Brewery** by **Patbox (pb4)**:

- https://modrinth.com/mod/brewery

## License

See: [LICENSE](LICENSE)
