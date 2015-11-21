package com.jondejong.scoreindex.user

import com.jondejong.scoreindex.datastore.MongoConnection
import org.bson.types.ObjectId
import ratpack.server.Service

import javax.inject.Inject

class TokenRepository  implements Service  {

    def database

    @Inject
    UserRepository(MongoConnection mongoConnection) {
        this.database = mongoConnection.database
    }

    def saveToken(userKey) {
        def token = [userKey: userKey]
        database.token.insert(token)
        token
    }

    def find(id) {
        database.token.findOne([_id: new ObjectId(id)])
    }
}
