package com.example.chess_clock_app.chess

data class PgnGame(
    // Header lines like [Event "Grenke"], kept unchanged
    val tags: List<String> = emptyList(),
    // One entry per half-move, in SAN: e4, e5, Nf3, ...
    val sans: List<String> = emptyList(),
    // One entry per half-move: the remaining clock time from {[%clk 0:04:58]}
    val clocks: List<String> = emptyList(),
    val result: String = "*",
)

// TODO format according to esp format
object Pgn {


    private val TAG_LINE = Regex("""^\s*\[.*\]\s*$""")
    private val COMMENT = Regex("""\{([^\}]*)\}""")
    private val CLOCK = Regex("""\[%clk\s+([0-9:.]+)\]""")
    private val MOVE_NUMBER = Regex("""^\d+\.+""")
    private val RESULTS = setOf("1-0", "0-1", "1/2-1/2", "*")

    fun parse(text: String): PgnGame {
        val lines = text.lines()
        val tags = lines.filter { TAG_LINE.matches(it) }.map { it.trim() }
        val movetext = lines.filterNot { TAG_LINE.matches(it) }.joinToString(" ")

        // Pull the clock times out of the comments, then drop the comments
        val clocks = mutableListOf<String>()
        val withoutComments = COMMENT.replace(movetext) { comment ->
            CLOCK.find(comment.groupValues[1])?.let { clocks += it.groupValues[1] }
            " "
        }

        val sans = mutableListOf<String>()
        var result = "*"
        for (rawToken in withoutComments.split(Regex("""\s+"""))) {
            // Handles "1." and "1..." as well as "1.e4" written without a space
            val token = rawToken.replace(MOVE_NUMBER, "")
            when {
                token.isEmpty() -> {}
                token in RESULTS -> result = token
                token.startsWith("$") -> {} // annotation like $1, not a move
                else -> sans += token
            }
        }

        return PgnGame(tags, sans, clocks, result)
    }

    fun write(game: PgnGame): String {
        val parts = mutableListOf<String>()
        val halfMoves = maxOf(game.sans.size, game.clocks.size)

        for (ply in 0 until halfMoves) {
            val san = game.sans.getOrNull(ply)
            if (san != null) {
                if (ply % 2 == 0) parts += "${ply / 2 + 1}."
                parts += san
            }
            game.clocks.getOrNull(ply)?.let { parts += "{[%clk $it]}" }
        }
        parts += game.result

        val movetext = parts.joinToString(" ")
        return if (game.tags.isEmpty()) movetext else game.tags.joinToString("\n") + "\n\n" + movetext
    }
}
