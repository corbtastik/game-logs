package io.corbs.gamelogs

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import java.io.File

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

fun CharSequence.toChar() = single()
fun String.unquote() = replace("\"", "")
fun String.asDateTime(pattern: String) = DateTime.parse(this.unquote(), DateTimeFormat.forPattern(pattern))
fun String.asDateTime() = asDateTime("MM-dd-yyyy")
fun DateTime.asString() = toString("MM-dd-yyyy")

@ShellComponent
@SpringBootApplication
class GameLogsApplication {

    init {
        Database.connect("jdbc:h2:mem:gamelogs;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    }

    @ShellMethod("Load Game Logs: game-log, ex: /path/to/gamelog.csv")
    fun load(gameLog: String) {
        transaction {
            SchemaUtils.create(GameLogsTable)
            var lines = 0
            File(gameLog).forEachLine {
                val parts = it.split(",")
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
                lines++
            }

            println("Loaded $lines Game Log records.")
        }
    }

    @ShellMethod("Games On: MM-dd-yyyy, ex: 07-04-2017")
    fun gamesOn(gameDate: String) {
        transaction {
            val query = GameLogsTable.select {
                GameLogsTable.dateOfGame eq gameDate.asDateTime()
            }
            fetchGames(query)
        }
    }

    @ShellMethod("Games Between: MM-dd-yyyy MM-dd-yyyy, ex: 07-04-2017 07-05-2017")
    fun gamesBetween(@ShellOption(defaultValue = "01-01-2017") start: String,
                     @ShellOption(defaultValue = "12-31-2017") end: String) {
        transaction {
            val query = GameLogsTable.select {
                (GameLogsTable.dateOfGame greaterEq start.asDateTime()) and
                (GameLogsTable.dateOfGame lessEq end.asDateTime())
            }
            fetchGames(query)
        }
    }

    @ShellMethod("Team Games: team, ex: TEX or: TEX 04-01-2017 04-30-2017")
    fun teamGames(team: String,
                  @ShellOption(defaultValue = "01-01-2017") start: String,
                  @ShellOption(defaultValue = "12-31-2017") end: String) {
        transaction {
            val query = GameLogsTable.select {
                (GameLogsTable.dateOfGame greaterEq start.asDateTime()) and
                (GameLogsTable.dateOfGame lessEq end.asDateTime()) and
                ((GameLogsTable.homeTeam eq team) or (GameLogsTable.visitingTeam eq team))
            }
            fetchGames(query)
        }
    }

    @ShellMethod("Home Team Games: team, ex: TEX or: TEX 04-01-2017 04-30-2017")
    fun homeTeamGames(team: String,
                      @ShellOption(defaultValue = "01-01-2017") start: String,
                      @ShellOption(defaultValue = "12-31-2017") end: String) {
        transaction {
            val query = GameLogsTable.select {
                (GameLogsTable.dateOfGame greaterEq start.asDateTime()) and
                (GameLogsTable.dateOfGame lessEq end.asDateTime()) and
                (GameLogsTable.homeTeam eq team)
            }
            fetchGames(query)
        }
    }

    @ShellMethod("Print Game Log: game-log")
    fun print(gameLog: String) {
        File(gameLog).forEachLine {
            println(it)
        }
    }

    private fun fetchGames(query: Query) {
        printDiv()
        query.forEach {
            printGame(it)
        }
        printDiv()
    }

    private fun printGame(row: ResultRow) {
        val dateOfGame: DateTime = row[GameLogsTable.dateOfGame]
        val visitingTeam: String = row[GameLogsTable.visitingTeam]
        val visitingTeamLeague: String = row[GameLogsTable.visitingTeamLeague]
        val homeTeam: String = row[GameLogsTable.homeTeam]
        val homeTeamLeague: String = row[GameLogsTable.homeTeamLeague]
        val visitingTeamScore: String = row[GameLogsTable.visitingTeamScore].toString().padStart(3)
        val homeTeamScore: String = row[GameLogsTable.homeTeamScore].toString().padStart(3)

        println("|${dateOfGame.asString()}|$visitingTeam|$visitingTeamLeague|$visitingTeamScore" +
                "|$homeTeam|$homeTeamLeague|$homeTeamScore|")
    }

    private fun printDiv() {
        println("+--------------------------------+")
    }
}

fun main(args: Array<String>) {
    runApplication<GameLogsApplication>(*args)
}
