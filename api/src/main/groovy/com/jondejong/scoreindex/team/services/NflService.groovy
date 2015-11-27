package com.jondejong.scoreindex.team.services

import com.jondejong.scoreindex.team.GameRepository
import com.jondejong.scoreindex.team.InitialTeams
import com.jondejong.scoreindex.team.repositories.NflRepository

import javax.inject.Inject

/**
 * Created by jondejong on 11/27/15.
 */
class NflService extends TeamService {

    @Inject
    def NflService(NflRepository teamRepository, InitialTeams initialTeams, GameRepository gameRepository) {
        this.teamRepository = teamRepository
        this.initialTeams = initialTeams
        this.gameRepository = gameRepository
    }
}
