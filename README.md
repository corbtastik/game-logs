# Retrosheet Game Log kinda fun

## Totes Props to Retrosheet

Original Game Log Field schema is [here](https://www.retrosheet.org/gamelogs/glfields.txt).

## What is this?

Simply stated its just fun with:
 
* [Kotlin](https://kotlinlang.org) because its compact and cute
* [Exposed](https://github.com/JetBrains/Exposed) because sql can be elegant too
* [Baseball](https://www.retrosheet.org) historical record of the great game
* [Spring Boot](https://github.com/spring-projects/spring-boot) as the application framework
* [Spring Shell](https://github.com/spring-projects/spring-shell) as the cli (awesomeness)

## Build and Run

```bash
$ ./mvn clean package
$ java -jar ./target/game-logs-0.0.1-SNAPSHOT.jar
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.0.6.RELEASE)
shell:>help
AVAILABLE COMMANDS
 
Built-In Commands
    clear: Clear the shell screen.
    exit, quit: Exit the shell.
    help: Display help about available commands.
    script: Read and execute commands from a file.
    stacktrace: Display the full stacktrace of the last error.
 
Game Logs Application
    games-between: Games Between: MM-dd-yyyy MM-dd-yyyy, for example 07-04-2017 07-05-2017
    games-on: Games On: MM-dd-yyyy, for example 07-04-2017
    load: Load Game Logs: game-log
    print: Print Game Log: game-log
shell:>

```

## Play

Type ``help`` to get a listing of methods to run.  The ``load`` method takes a [retrosheet](https://www.retrosheet.org) game-log file and loads [H2](http://www.h2database.com) using [Exposed](https://github.com/JetBrains/Exposed)

### Load Game Log

```bash
shell:>load src/main/resources/GL2017.TXT
Loaded 2430 Game Log records.
```

#### Insert Snippet

```kotlin
    GameLogsTable.insert({
        it[dateOfGame] = parts[0].asDateTime("yyyyMMdd")
        it[numberOfGame] = parts[1].unquote().toChar()
        it[dayOfWeek] = parts[2].unquote()
        it[visitingTeam] = parts[3].unquote()
        it[visitingTeamLeague] = parts[4].unquote()
        it[visitingTeamGameNumber] = parts[5].toInt()
        it[homeTeam] = parts[6].unquote()
        it[homeTeamLeague] = parts[7].unquote()
        it[homeTeamGameNumber] = parts[8].toInt()
        it[visitingTeamScore] = parts[9].toInt()
        it[homeTeamScore] = parts[10].toInt()
    })
```

### List Games

#### Spring-Shell method to list games

```kotlin
    @ShellMethod("Games On: MM-dd-yyyy, for example 07-04-2017")
    fun gamesOn(gameDate: String) {
        transaction {
            val query = GameLogsTable.select {
                GameLogsTable.dateOfGame eq gameDate.asDateTime()
            }
            println("+--------------------------------+")
            query.forEach {
                val dateOfGame: DateTime = it[GameLogsTable.dateOfGame]
                val visitingTeam: String = it[GameLogsTable.visitingTeam]
                val visitingTeamLeague: String = it[GameLogsTable.visitingTeamLeague]
                val homeTeam: String = it[GameLogsTable.homeTeam]
                val homeTeamLeague: String = it[GameLogsTable.homeTeamLeague]
                val visitingTeamScore: String = it[GameLogsTable.visitingTeamScore].toString().padStart(3)
                val homeTeamScore: String = it[GameLogsTable.homeTeamScore].toString().padStart(3)

                println("|${dateOfGame.asString()}|$visitingTeam|$visitingTeamLeague|$visitingTeamScore" +
                        "|$homeTeam|$homeTeamLeague|$homeTeamScore|")
            }
            println("+--------------------------------+")
        }
    }
```

```bash
shell:>games-on 07-04-2017
+--------------------------------+
|07-04-2017|HOU|AL| 16|ATL|NL|  4|
|07-04-2017|TBA|AL|  6|CHN|NL|  5|
|07-04-2017|CIN|NL|  8|COL|NL|  1|
|07-04-2017|ARI|NL|  3|LAN|NL|  4|
|07-04-2017|BAL|AL|  2|MIL|NL|  6|
|07-04-2017|PIT|NL|  3|PHI|NL|  0|
|07-04-2017|MIA|NL|  5|SLN|NL|  2|
|07-04-2017|NYN|NL|  4|WAS|NL| 11|
|07-04-2017|SDN|NL|  1|CLE|AL|  0|
|07-04-2017|SFN|NL|  3|DET|AL|  5|
|07-04-2017|ANA|AL|  4|MIN|AL|  5|
|07-04-2017|TOR|AL|  4|NYA|AL|  1|
|07-04-2017|CHA|AL|  6|OAK|AL|  7|
|07-04-2017|KCA|AL|  7|SEA|AL|  3|
|07-04-2017|BOS|AL| 11|TEX|AL|  4|
+--------------------------------+
```

### Kotlin extension functions

Love the way extension functions reduce code clutter.  For example its nice to have simple ``String`` to ``DateTime`` and ``DateTime`` to ``String`` conversions via extensions.

```kotlin
fun CharSequence.toChar() = single()
// "W".toChar()
fun String.unquote() = replace("\"", "")
// "\"20170403\"".unquote()
fun String.asDateTime(pattern: String) = DateTime.parse(this.unquote(), DateTimeFormat.forPattern(pattern))
// "20170704".asDateTime("yyyyMMdd")
fun String.asDateTime() = asDateTime("MM-dd-yyyy")
// "07-04-2017".asDateTime()
fun DateTime.asString() = toString("MM-dd-yyyy")
// dateTime.asString()
```

### Exposed

Simply elegant and type-safe SQL!  Easily define tables and create schemas.

```kotlin
object GameLogsTable: Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val dateOfGame = date("date_of_game")
    val numberOfGame = char("number_of_game")
    val dayOfWeek = varchar("day_of_week", 3)
    val visitingTeam = varchar("visiting_team", 3)
    val visitingTeamLeague = varchar("visiting_team_league", 2)
    val visitingTeamGameNumber = integer("visiting_team_game_number")
    val homeTeam = varchar("home_team", 3)
    val homeTeamLeague = varchar("home_team_league", 2)
    val homeTeamGameNumber = integer("home_team_game_number")
    val visitingTeamScore = integer("visiting_team_score")
    val homeTeamScore = integer("home_team_score")
}
```

### Have Fun

Watch more baseball and code less with Kotlin!