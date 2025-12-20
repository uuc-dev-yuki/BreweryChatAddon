# Brewery Chat Addon

Серверний аддон для Fabric-мода **Brewery**: коли гравець п'яний, його повідомлення в чаті можуть випадково замінюватись на фрази з конфігу.

## Вимоги

- Minecraft `1.21.0-1.21.11` (фокус/збірка: `1.21.10`)
- Fabric Loader `0.18.2`
- Fabric API (як у збірці)
- Brewery (Patbox) `0.13.0+` (перевірено на `0.13.0+1.21.9-rc1`)

## Встановлення

- Скопіюй `brewery-chat-addon-<version>.jar` у `mods/`.
- Запусти сервер.

Після першого запуску створиться конфіг:

- `config/BreweryChatAddon/config.json`

## Налаштування

У `config.json`:

- `enabled`: вмикає/вимикає функцію
- `alcoholRules`: список правил по рівню алкоголю

### alcoholRules

Аддон відстежує стан сп'яніння за `alcoholLevel` (з Brewery). Для кожного діапазону задається шанс і список повідомлень.

Формат елемента:

- `min` (обов'язково)
- `max` (опціонально, якщо немає — діапазон без верхньої межі)
- `chance` (можна задавати як `0..1` або як відсотки `0..100`)
- `messages` (масив рядків; випадкова фраза обирається з цього списку)

## Команди

- `/brreload`

## Permissions

- `brewerychataddon.reload`
  - Дозволяє виконувати `/brreload`.
  - Якщо Permissions API відсутній, використовується fallback: OP level 2.

- `brewerychataddon.bypass`
  - Гравець з цим правом **ніколи** не отримує підміну повідомлень.

## Інтеграції

- Якщо встановлено **Carbon Chat**, аддон підхоплюється через entrypoint `carbonchat` і змінює повідомлення на ранній стадії (early event).
- Якщо Carbon Chat відсутній, використовується vanilla/Fabric-хук `ServerMessageEvents.ALLOW_CHAT_MESSAGE`.

## Credits

Це аддон для моду **Brewery** від **Patbox (pb4)**:

- https://modrinth.com/mod/brewery

## About

- Автор: **Yuki**
- Власник: **ЄУК - Єдине Україномовне Ком'юніті**

## License

See: [LICENSE](LICENSE)
