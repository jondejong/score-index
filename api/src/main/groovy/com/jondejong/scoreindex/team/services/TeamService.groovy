package com.jondejong.scoreindex.team.services

import com.jondejong.scoreindex.team.Game
import com.jondejong.scoreindex.team.GameRepository
import com.jondejong.scoreindex.team.InitialTeams
import com.jondejong.scoreindex.team.Team
import com.jondejong.scoreindex.team.repositories.TeamRepository
import org.bson.types.ObjectId

import java.text.SimpleDateFormat

import static com.xlson.groovycsv.CsvParser.parseCsv

/**
 * Created by jondejong on 11/21/15.
 */
abstract class TeamService {

    SimpleDateFormat format = new SimpleDateFormat('yyyy-MM-dd')

    TeamRepository teamRepository
    InitialTeams initialTeams
    GameRepository gameRepository


    def get(String id) {
        teamRepository.load(new ObjectId(id))
    }

    def getByName(String name) {
        teamRepository.getTeamByName(name)
    }

    def list() {
        teamRepository.getShortTeams().sort { Team a, Team b ->
            a.rank <=> b.rank
        }
    }

    def getGamesByTeam(name) {

        def games = []

        def team = teamRepository.getTeamByName(name)

        def homeGames = gameRepository.find(home: team.id)

        homeGames.each { Game game ->
            games << [
                    home: name,
                    homeScore: game.homeScore,
                    away: teamRepository.load(game.away).name,
                    awayScore: game.awayScore
            ]

        }

        def awayGames = gameRepository.find(away: team.id)

        awayGames.each {Game game ->
            games << [
                    home: teamRepository.load(game.home).name,
                    homeScore: game.homeScore,
                    away: name,
                    awayScore: game.awayScore
            ]

        }

        games
    }

    def createTeam(Team team) {
        teamRepository.saveTeam(team)
    }

    def init(start, end) {
        println "Loading games from ${start} to ${end}"

        teamRepository.clear()
        gameRepository.clear()

        Date startDate = format.parse(start)
        Date endDate = format.parse(end)

        while(startDate <= endDate) {
            parseFile(startDate)
              startDate++
        }

        calculateBaseScores()

        // For some number of loops...
        def loop = 0..25
        loop.each {
            println "Adjusting rankings on loop ${it}..."
            adjustScores()
        }

        // Add rankings
        println "Adding rankings to teams..."
        teamRepository.getTeams().sort {Team a, Team b ->
            b.score <=> a.score
        }.eachWithIndex {Team t, i ->
            t.rank = i + 1
            teamRepository.updateTeam(t)
        }

        [message: 'initialization complete']
    }

    protected adjustScores() {
        teamRepository.getTeams().each {Team team ->

            team.gameScores.each {
                Game game = gameRepository.load(it.game)
                Team otherTeam

                if(game.home == team.id) {
                    otherTeam = teamRepository.load(game.away)
                } else {
                    otherTeam = teamRepository.load(game.home)
                }

                // Add .5 to the other teams score. That way a "positive" team increases the value
                // of this game to a team, while a "negative" team decreases it

                BigDecimal score = new BigDecimal(it.baseScore) * (new BigDecimal(0.5D) + new BigDecimal(otherTeam.score))
                it.adjustedScore = score.doubleValue()
            }
            updateTeamScore(team)
            teamRepository.updateTeam(team)
        }

    }

    protected calculateBaseScores() {
        // Calculate each score
        gameRepository.getGames().each { Game game ->
            // For each game...
            // Calculate the home team point value

            BigDecimal homeTeamValue = new BigDecimal(game.homeScore)/new BigDecimal(game.homeScore + game.awayScore)
            def homeTeam = teamRepository.load(game.home)
            if(!homeTeam.gameScores) {
                homeTeam.gameScores = []
            }
            homeTeam.gameScores << [game: game.id,
                                    baseScore: homeTeamValue.doubleValue(),
                                    adjustedScore: homeTeamValue.doubleValue()]
            teamRepository.updateTeam(homeTeam)

            // Calculate the away team point value
            BigDecimal awayTeamValue = new BigDecimal(game.awayScore)/new BigDecimal(game.homeScore + game.awayScore)
            def awayTeam = teamRepository.load(game.away)
            if(!awayTeam.gameScores) {
                awayTeam.gameScores = []
            }
            awayTeam.gameScores << [game: game.id,
                                    baseScore: awayTeamValue.doubleValue(),
                                    adjustedScore: awayTeamValue.doubleValue()]

            teamRepository.updateTeam(awayTeam)

        }

        teamRepository.getTeams().each { Team team ->
            // For each team...
            // Average out it's points values
            updateTeamScore(team)
            team.baseScore = team.score
            teamRepository.updateTeam(team)
        }
    }

    protected updateTeamScore(Team team) {
        BigDecimal total = 0;
        team.gameScores.each {
            total += it.adjustedScore
        }

        def newScore = new BigDecimal(total/team.gameScores.size()).doubleValue()

        team.score = newScore
        team
    }

    protected parseFile(Date date) {

        def fileDate = format.format(date)
        println "parsing games from ${fileDate}"

        String fileName = "${initialTeams.file}/${teamRepository.sport}/${fileDate}.csv"
        File file = new File(fileName)
        if(!file.exists()) {
            return
        }
        String contents = file.text

        def data = parseCsv(contents)

        for (line in data) {

            def homeTeamName = scrubTeamName(line.Home)
            def awayTeamName = scrubTeamName(line.Away)

            def homeTeam = teamRepository.getTeamByName(homeTeamName)

            if (!homeTeam) {
                homeTeam = new Team(name: homeTeamName, wins: 0, losses: 0)
                teamRepository.saveTeam(homeTeam)
                homeTeam = teamRepository.getTeamByName(homeTeamName)
            }

            def awayTeam = teamRepository.getTeamByName(awayTeamName)

            if (!awayTeam) {
                awayTeam = new Team(name: awayTeamName, wins: 0, losses: 0)
                teamRepository.saveTeam(awayTeam)
                awayTeam = teamRepository.getTeamByName(awayTeamName)
            }

            def game = new Game(
                    home: homeTeam.id,
                    away: awayTeam.id,
                    homeScore: Integer.parseInt(line.HomeScore),
                    awayScore: Integer.parseInt(line.AwayScore),
                    date: date
            )

            if(game.homeScore > game.awayScore) {
                homeTeam.wins++
                awayTeam.losses++
            } else {
                homeTeam.losses++
                awayTeam.wins++
            }

            teamRepository.updateTeam(homeTeam)
            teamRepository.updateTeam(awayTeam)

            gameRepository.saveGame(game)

        }

    }

    protected scrubTeamName(String name) {
        name.split('\\(')[0].trim()
    }
}
