/* BT pairing test
    Prototype of the game transfer between the chess clock and the app.
    Holds 2 hardcoded games and answers the app over Bluetooth serial (Classic SPP).
    The app always asks, the clock only answers.

    Protocol - one command per line, ending with \n (\r is ignored)
        HELLO       -> HELLO <protocol version> <number of games>
        LIST        -> GAME <id> <length> <crc32>   (one line per game)
                       END
        GET <id>    -> DATA <id> <length> <crc32>
                       <exactly length bytes of game data>\n   (the \n is not counted in length)
                       END <id>
        DEL <id>    -> OK <id>     (also OK if it was already deleted, so a repeated DEL is harmless)
        anything else / unknown id -> ERR <reason>

    <crc32> is 8 hex digits, the same CRC32 as java.util.zip.CRC32 in the app.

    Game data: the remaining time after every half-move in 10ms ticks, separated by spaces.
    The clock does not know the moves, so the app builds the PGN from these times.
*/
#include "BluetoothSerial.h"
#include "esp_rom_crc.h"

BluetoothSerial BT;

#define PROTOCOL_VERSION 1

struct stored_game {
  int id;
  const char* data;
  bool deleted;   // only in RAM: after a restart both games are back, handy for testing
};

stored_game games[] = {
  {1, "30000 29850 29720 29410 29180 28950", false},   // 5 min, 6 half-moves
  {2, "60000 59700 59650 59200", false},               // 10 min, 4 half-moves
};
const int game_count = sizeof(games) / sizeof(games[0]);

// Incoming command, collected until \n
char line[24];
int line_length = 0;
bool line_too_long = false;

char bt_name[16];

//-------------------------------------------------------------------------------------

uint32_t game_crc(const stored_game& game) {
  return esp_rom_crc32_le(0, (const uint8_t*)game.data, strlen(game.data));
}

// Also finds deleted games, the caller decides what to do with them
stored_game* find_game(int id) {
  for (int i = 0; i < game_count; i++) {
    if (games[i].id == id) return &games[i];
  }
  return NULL;
}

int games_left() {
  int count = 0;
  for (int i = 0; i < game_count; i++) {
    if (!games[i].deleted) count++;
  }
  return count;
}

// GAME/DATA <id> <length> <crc32>
void send_header(const char* type, const stored_game& game) {
  BT.printf("%s %d %u %08lX\n", type, game.id, (unsigned)strlen(game.data), (unsigned long)game_crc(game));
}

//-------------------------------------------------------------------------------------

void handle_command(const char* command) {
  //Serial.println(command);

  if (strcmp(command, "HELLO") == 0) {
    BT.printf("HELLO %d %d\n", PROTOCOL_VERSION, games_left());

  } else if (strcmp(command, "LIST") == 0) {
    for (int i = 0; i < game_count; i++) {
      if (!games[i].deleted) send_header("GAME", games[i]);
    }
    BT.print("END\n");

  } else if (strncmp(command, "GET ", 4) == 0) {
    int id = atoi(command + 4);
    stored_game* game = find_game(id);

    if (game == NULL || game->deleted) {
      BT.printf("ERR NO_GAME %d\n", id);
      return;
    }
    send_header("DATA", *game);
    BT.write((const uint8_t*)game->data, strlen(game->data));
    BT.printf("\nEND %d\n", id);

  } else if (strncmp(command, "DEL ", 4) == 0) {
    int id = atoi(command + 4);
    stored_game* game = find_game(id);

    if (game == NULL) {
      BT.printf("ERR NO_GAME %d\n", id);
      return;
    }
    game->deleted = true;
    BT.printf("OK %d\n", id);

  } else {
    BT.print("ERR UNKNOWN\n");
  }
}

void read_commands() {
  while (BT.available()) {
    char c = BT.read();

    if (c == '\r') continue;

    if (c == '\n') {
      line[line_length] = '\0';

      if (line_too_long) {
        BT.print("ERR TOO_LONG\n");
      } else if (line_length > 0) {
        handle_command(line);
      }
      line_length = 0;
      line_too_long = false;

    } else if (line_length < (int)sizeof(line) - 1) {
      line[line_length++] = c;
    } else {
      line_too_long = true;   // ignore the rest until \n
    }
  }
}

//-------------------------------------------------------------------------------------

void setup() {
  Serial.begin(115200);

  // Last 2 bytes of the MAC, so two clocks don't show up with the same name
  uint64_t mac = ESP.getEfuseMac();
  snprintf(bt_name, sizeof(bt_name), "ChessClock-%02X%02X",
           (uint8_t)(mac >> 32), (uint8_t)(mac >> 40));

  BT.begin(bt_name);
  Serial.println(bt_name);
}

void loop() {
  // A half received command is thrown away when the app disconnects
  if (!BT.hasClient()) {
    line_length = 0;
    line_too_long = false;
  }

  read_commands();
  delay(10);
}
