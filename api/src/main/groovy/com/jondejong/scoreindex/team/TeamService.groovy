package com.jondejong.scoreindex.team

import org.bson.types.ObjectId

import javax.inject.Inject
import java.text.SimpleDateFormat

import static com.xlson.groovycsv.CsvParser.parseCsv

/**
 * Created by jondejong on 11/21/15.
 */
class TeamService {

    SimpleDateFormat format = new SimpleDateFormat('yyyy-MM-dd')

    TeamRepository teamRepository
    InitialTeams initialTeams
    GameRepository gameRepository

    @Inject
    def UserService(TeamRepository teamRepository, InitialTeams initialTeams, GameRepository gameRepository) {
        this.teamRepository = teamRepository
        this.initialTeams = initialTeams
        this.gameRepository = gameRepository
    }

    def list() {
        teamRepository.getTeams()
    }

    def getGamesByTeam(name) {

        def games = []

        def team = teamRepository.getTeamByName(name)

        def homeGames = gameRepository.find(home: new ObjectId(team.id))

        homeGames.each {Game game ->
            games << [
                    home: name,
                    homeScore: game.homeScore,
                    away: teamRepository.load(game.away).name,
                    awayScore: game.awayScore
            ]

        }

        def awayGames = gameRepository.find(away: new ObjectId(team.id))

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

        // Calculate each score
        gameRepository.getGames().each { Game game ->
            // For each game...
            // Calculate the home team point value

            BigDecimal homeTeamValue = new BigDecimal(game.homeScore)/new BigDecimal(game.homeScore + game.awayScore)
            def homeTeam = teamRepository.load(game.home)
            if(!homeTeam.gameScores) {
                homeTeam.gameScores = []
            }
            homeTeam.gameScores << [game: game.id, score: homeTeamValue.doubleValue()]
            teamRepository.updateTeam(homeTeam)

            // Calculate the away team point value
            BigDecimal awayTeamValue = new BigDecimal(game.awayScore)/new BigDecimal(game.homeScore + game.awayScore)
            def awayTeam = teamRepository.load(game.away)
            if(!awayTeam.gameScores) {
                awayTeam.gameScores = []
            }
            awayTeam.gameScores << [game: game.id, score: awayTeamValue.doubleValue()]
            teamRepository.updateTeam(awayTeam)

        }

        teamRepository.getTeams().each { Team team ->
            // For each team...
            // Average out it's points values
            BigDecimal total = 0;
            team.gameScores.each {
                total += it.score
            }

            team.score = new BigDecimal(total/team.gameScores.size()).doubleValue()
            teamRepository.updateTeam(team)
        }

        // For some number of loops...
        [0..1000].each {
            //
        }

        [message: 'initialization complete']
    }

    protected parseFile(Date date) {

        def fileDate = format.format(date)
        println "parsing games from ${fileDate}"

        String fileName = "${initialTeams.fileLocation}/${fileDate}.csv"
        String contents = new File(fileName).text

        def games = []

        def data = parseCsv(contents)

        for (line in data) {

            def homeTeamName = scrubTeamName(line.Home)
            def awayTeamName = scrubTeamName(line.Away)

            def homeTeam = teamRepository.getTeamByName(homeTeamName)

            if (!homeTeam) {
                homeTeam = new Team(name: homeTeamName)
                teamRepository.saveTeam(homeTeam)
                homeTeam = teamRepository.getTeamByName(homeTeamName)
            }

            def awayTeam = teamRepository.getTeamByName(awayTeamName)

            if (!awayTeam) {
                awayTeam = new Team(name: awayTeamName)
                teamRepository.saveTeam(awayTeam)
                awayTeam = teamRepository.getTeamByName(awayTeamName)
            }

            def game = new Game(
                    home: new ObjectId(homeTeam.id),
                    away: new ObjectId(awayTeam.id),
                    homeScore: Integer.parseInt(line.HomeScore),
                    awayScore: Integer.parseInt(line.AwayScore),
                    date: date
            )

            gameRepository.saveGame(game)

        }

    }

    protected scrubTeamName(String name) {
        name.split('\\(')[0].trim()
    }
}
