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

    def getShortTeams() {
        def teams = []
        def results = database.team.find().asList()
        results.each { team ->
            teams << documentToShortTeam(team)
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
                [_id: team.id],
                [$set: doc]
        )
    }

    protected Team documentToTeam(document) {
        if (!document) {
            return null
        }

        Team team = documentToShortTeam(document)
        team.gameScores = document.gameScores
        team
    }

    protected documentToShortTeam(document) {
        if (!document) {
            return null
        }

        new Team(
                id: document._id,
                name: document.name,
                score: document.score,
                baseScore: document.baseScore,
                rank: document.rank,
                wins: document.wins,
                losses: document.losses

        )
    }

    protected teamToDocument(Team team) {
        [
                name      : team.name,
                gameScores: team.gameScores,
                score     : team.score,
                baseScore : team.baseScore,
                rank      : team.rank,
                wins      : team.wins,
                losses    : team.losses
        ]
    }

}
