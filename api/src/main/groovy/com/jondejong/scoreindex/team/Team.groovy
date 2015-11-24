package com.jondejong.scoreindex.team

import org.bson.types.ObjectId

class Team {
    ObjectId id
    Integer rank
    String name
    List gameScores
    Double baseScore
    Double score
    Integer wins
    Integer losses
}
