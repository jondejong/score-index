package com.jondejong.scoreindex.team


import com.google.inject.Singleton
import com.jondejong.scoreindex.datastore.MongoConnection
import org.bson.types.ObjectId

import javax.inject.Inject

@Singleton
class TeamRepository {
    def database

    @Inject
    TeamRepository(MongoConnection mongoConnection) {
        this.database = mongoConnection.database
    }

    def clear() {
        database.team.drop()
    }

    def load(id) {
        documentToTeam(database.team.findOne(_id: id))
    }

    def getTeams() {
        def teams = []
        def results = database.team.find().asList()
        results.each { team ->
            teams << documentToTeam(team)
        }
        teams
    }

    def getTeamByName(name) {
        documentToTeam(database.team.findOne(name: name))
    }

    def saveTeam(team) {
        database.team << teamToDocument(team)
    }

    def updateTeam(team) {
        def doc = teamToDocument(team)
        database.team.update(
                [_id: new ObjectId(team.id)],
                [$set: doc]
        )
    }

    protected Team documentToTeam(team) {
        if (!team) {
            return null
        }

        new Team(
                id: team._id,
                name: team.name,
                gameScores: team.gameScores,
                score: team.score
        )
    }

    protected teamToDocument(Team team) {
        [
                name      : team.name,
                gameScores: team.gameScores,
                score     : team.score
        ]
    }

}
