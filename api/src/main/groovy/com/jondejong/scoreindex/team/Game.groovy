package com.jondejong.scoreindex.team

import org.bson.types.ObjectId

class Game {
    ObjectId id

    Date date

    ObjectId home
    ObjectId away

    Integer homeScore
    Integer awayScore

}
