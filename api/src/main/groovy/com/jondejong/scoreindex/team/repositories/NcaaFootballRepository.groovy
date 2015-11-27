package com.jondejong.scoreindex.team.repositories

import com.jondejong.scoreindex.datastore.MongoConnection

import javax.inject.Inject

/**
 * Created by jondejong on 11/27/15.
 */
class NcaaFootballRepository extends TeamRepository {
    @Inject
    NcaaFootballRepository(MongoConnection mongoConnection) {
        this.sport = 'ncaaf'
        this.database = mongoConnection.database
    }
}
