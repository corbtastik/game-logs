package io.corbs.gamelogs

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
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

    @ShellMethod("Load Game Logs: game-log")
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

    @ShellMethod("Games Between: MM-dd-yyyy MM-dd-yyyy, for example 07-04-2017 07-05-2017")
    fun gamesBetween(start: String, end: String) {
        transaction {
            val query = GameLogsTable.select {
                (GameLogsTable.dateOfGame greaterEq start.asDateTime()) and
                (GameLogsTable.dateOfGame lessEq end.asDateTime())
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

    @ShellMethod("Print Game Log: game-log")
    fun print(gameLog: String) {
        File(gameLog).forEachLine {
            println(it)
        }
    }
}

fun main(args: Array<String>) {
    runApplication<GameLogsApplication>(*args)
}
