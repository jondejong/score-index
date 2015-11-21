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

    def createTeam(Team team) {
        teamRepository.saveTeam(team)
    }

    def init(start, end) {
        println "Loading games from ${start} to ${end}"

        teamRepository.clear()
        gameRepository.clear()

        parseFile(end)

        Date startDate = format.parse(start)
        Date endDate = format.parse(end)

        while(startDate <= endDate) {
            parseFile(format.format(startDate))
            startDate++
        }

        [message: 'initialization complete']
    }

    protected parseFile(fileDate) {

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
                    awayScore: Integer.parseInt(line.AwayScore)
            )

            gameRepository.saveGame(game)

        }

    }

    protected scrubTeamName(String name) {
        name.split('\\(')[0]
    }
}
