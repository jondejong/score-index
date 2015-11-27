package com.jondejong.scoreindex.team

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.jondejong.scoreindex.team.repositories.NbaRepository
import com.jondejong.scoreindex.team.repositories.NcaaFootballRepository
import com.jondejong.scoreindex.team.repositories.NcaaMenRepository
import com.jondejong.scoreindex.team.repositories.NflRepository
import com.jondejong.scoreindex.team.services.NbaService
import com.jondejong.scoreindex.team.services.NcaaFootballService
import com.jondejong.scoreindex.team.services.NcaaMenService
import com.jondejong.scoreindex.team.services.NflService
import com.jondejong.scoreindex.team.services.TeamService

/**
 * Created by jondejong on 11/21/15.
 */
class TeamModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NcaaMenService).in(Scopes.SINGLETON);
        bind(NcaaFootballService).in(Scopes.SINGLETON);
        bind(NflService).in(Scopes.SINGLETON);
        bind(NbaService).in(Scopes.SINGLETON);
        bind(NcaaMenRepository).in(Scopes.SINGLETON);
        bind(NcaaFootballRepository).in(Scopes.SINGLETON);
        bind(NbaRepository).in(Scopes.SINGLETON);
        bind(NflRepository).in(Scopes.SINGLETON);
        bind(GameRepository).in(Scopes.SINGLETON);
    }
}
