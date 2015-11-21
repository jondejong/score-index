package com.jondejong.scoreindex.team

import com.jondejong.scoreindex.datastore.MongoConnection

import javax.inject.Inject

/**
 * Created by jondejong on 11/21/15.
 */
class GameRepository {

    def database

    @Inject
    GameRepository(MongoConnection mongoConnection) {
        this.database = mongoConnection.database
    }

    def clear() {
        database.game.drop()
    }

    def getGames() {
        def games = []
        def results = database.game.find().asList()
        results.each { game ->
            games << documentToGame(game)
        }
        games
    }

    def saveGame(game) {
        database.game << gameToDocument(game)
    }

    protected Game documentToGame(game) {
        if (!game) {
            return null
        }

        new Game(
                id: game._id,
                home: game.home,
                away: game.away,
                homeScore: game.homeScore,
                awayScore: game.awayScore

        )
    }

    protected gameToDocument(Game game) {
        [
                home: game.home,
                away: game.away,
                homeScore: game.homeScore,
                awayScore: game.awayScore
        ]
    }
}
