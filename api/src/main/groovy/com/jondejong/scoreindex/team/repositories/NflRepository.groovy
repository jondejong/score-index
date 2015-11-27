package com.jondejong.scoreindex.team.repositories

import com.jondejong.scoreindex.datastore.MongoConnection

import javax.inject.Inject

/**
 * Created by jondejong on 11/27/15.
 */
class NflRepository extends TeamRepository {

    @Inject
    NflRepository(MongoConnection mongoConnection) {
        this.sport = 'nfl'
        this.database = mongoConnection.database
    }
}
