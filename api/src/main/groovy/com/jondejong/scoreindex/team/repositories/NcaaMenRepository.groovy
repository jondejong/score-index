package com.jondejong.scoreindex.team.repositories

import com.jondejong.scoreindex.datastore.MongoConnection

import javax.inject.Inject

/**
 * Created by jondejong on 11/27/15.
 */
class NcaaMenRepository extends TeamRepository {

    @Inject
    NcaaMenRepository(MongoConnection mongoConnection) {
        this.sport = 'ncaam'
        this.database = mongoConnection.database
    }
}
