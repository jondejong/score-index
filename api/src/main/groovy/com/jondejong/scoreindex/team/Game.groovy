package com.jondejong.scoreindex.team

import org.bson.types.ObjectId

class Game {
    String id
    ObjectId home
    ObjectId away

    Integer homeScore
    Integer awayScore

}
