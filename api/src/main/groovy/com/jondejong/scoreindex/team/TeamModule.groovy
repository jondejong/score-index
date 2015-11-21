package com.jondejong.scoreindex.team

import com.google.inject.AbstractModule
import com.google.inject.Scopes

/**
 * Created by jondejong on 11/21/15.
 */
class TeamModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TeamService).in(Scopes.SINGLETON);
        bind(TeamRepository).in(Scopes.SINGLETON);
        bind(GameRepository).in(Scopes.SINGLETON);
    }
}
