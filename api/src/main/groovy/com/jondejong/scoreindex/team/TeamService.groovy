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

    def get(String id) {
        teamRepository.load(new ObjectId(id))
    }

    def getByName(String name) {
        teamRepository.getTeamByName(name)
    }

    def list() {
        teamRepository.getShortTeams().sort {Team a, Team b ->
            a.rank <=> b.rank
        }
    }

    def getGamesByTeam(name) {

        def games = []

        def team = teamRepository.getTeamByName(name)

        def homeGames = gameRepository.find(home: team.id)

        homeGames.each {Game game ->
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
        def loop = 0..10
        loop.each {
            adjustScores()
        }

        // Add rankings
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
                BigDecimal score = new BigDecimal(it.adjustedScore) * (new BigDecimal(0.5D) + new BigDecimal(otherTeam.score))
                it.adjustedScore = score.doubleValue()
            }
            updateTeamScore(team)
            teamRepository.updateTeam(team)
        }

        // Remap Scores..
        def teams = teamRepository.getTeams();

        def scores = teams.collect {
            it.score
        }

        scores.sort {a,b ->
            a <=> b
        }

        def lowScore = new BigDecimal(scores[0])
        def highScore = new BigDecimal(scores[scores.size() - 1])
        teams.each { Team team ->
            def newScore = mapRange(lowScore, highScore, new BigDecimal(team.score), (team.name == 'Louisville'))

            if(team.name == 'Louisville') {
                println "remapping ${team.name} score from ${team.score} to ${newScore.doubleValue()}"
            }

            team.score = newScore.doubleValue()
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

        if(team.name == 'Louisville') {
//            println "changing ${team.name} from ${team.score} to ${newScore}"
        }

        team.score = newScore
        team
    }

    protected parseFile(Date date) {

        def fileDate = format.format(date)
        println "parsing games from ${fileDate}"

        String fileName = "${initialTeams.fileLocation}/${fileDate}.csv"
        String contents = new File(fileName).text

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
                    home: homeTeam.id,
                    away: awayTeam.id,
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

    protected BigDecimal mapRange(BigDecimal baseLow, BigDecimal baseHigh, BigDecimal value, debugLog){
        BigDecimal mapLow = new BigDecimal(0);
        BigDecimal mapHigh = new BigDecimal(1.0D);
        def newVal =  mapLow + ((value - baseLow)*(mapHigh - mapLow))/(baseHigh - baseLow);

        if(newVal.doubleValue() > 1.0D) {
            println "##################"
            println "mapLow ${mapLow}"
            println "mapHigh ${mapHigh}"
            println "baseLow ${mapLow}"
            println "baseHigh ${mapLow}"
            println "value ${value}"
            println "newValue ${newVal}"
            println "newValue.doubleValue ${newVal.doubleValue()}"
            println "##################"
            throw new RuntimeException("invalid mapping occurred ");
        }
        if(debugLog) {
            println "returning mapped value of ${newVal}"
        }
        newVal
    }
}
