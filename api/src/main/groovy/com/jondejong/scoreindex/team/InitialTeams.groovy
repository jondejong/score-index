package com.jondejong.scoreindex.team

import com.google.inject.Inject

/**
 * Created by jondejong on 11/21/15.
 */
class InitialTeams {
    String fileLocation

    @Inject
    InitialTeams(String fileLocation) {
        this.fileLocation = fileLocation
    }
}
