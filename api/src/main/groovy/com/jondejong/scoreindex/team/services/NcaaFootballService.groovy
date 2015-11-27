package com.jondejong.scoreindex.team.services

import com.jondejong.scoreindex.team.GameRepository
import com.jondejong.scoreindex.team.InitialTeams
import com.jondejong.scoreindex.team.repositories.NcaaFootballRepository

import javax.inject.Inject

/**
 * Created by jondejong on 11/27/15.
 */
class NcaaFootballService extends TeamService {

    @Inject
    def NcaaFootballService(NcaaFootballRepository teamRepository, InitialTeams initialTeams, GameRepository gameRepository) {
        this.teamRepository = teamRepository
        this.initialTeams = initialTeams
        this.gameRepository = gameRepository
    }
}
