package com.jondejong.scoreindex.team.repositories

import com.jondejong.scoreindex.datastore.MongoConnection

import javax.inject.Inject

/**
 * Created by jondejong on 11/27/15.
 */
class NbaRepository extends TeamRepository {

    @Inject
    NbaRepository(MongoConnection mongoConnection) {
        this.sport = 'nba'
        this.database = mongoConnection.database
    }
}
