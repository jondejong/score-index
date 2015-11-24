import com.fasterxml.jackson.databind.ObjectMapper
import com.jondejong.scoreindex.api.command.LoginCommand
import com.jondejong.scoreindex.datastore.MongoConfig
import com.jondejong.scoreindex.datastore.MongoConnection
import com.jondejong.scoreindex.handlers.ErrorHandler
import com.jondejong.scoreindex.jackson.ObjectIdObjectMapper
import com.jondejong.scoreindex.team.InitialTeams
import com.jondejong.scoreindex.team.TeamModule
import com.jondejong.scoreindex.team.TeamService
import com.jondejong.scoreindex.user.User
import com.jondejong.scoreindex.user.UserModule
import com.jondejong.scoreindex.user.UserService
import ratpack.config.ConfigData
import ratpack.error.ServerErrorHandler
import ratpack.exec.Blocking


import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

ratpack {

    bindings {
        ConfigData configData = ConfigData.of { c ->
            c.props("$serverConfig.baseDir.file/application.properties")
            c.env()
            c.sysProps()
        }

        bindInstance(ServerErrorHandler, new ErrorHandler())

        bindInstance(MongoConfig, configData.get("/mongo", MongoConfig))
        bindInstance(InitialTeams, configData.get("/teams", InitialTeams))
        bind(MongoConnection)

        module UserModule
        module TeamModule
        add(ObjectMapper.class, new ObjectIdObjectMapper())
    }

    handlers {
        all {
            response.headers.add 'Access-Control-Allow-Origin', '*'
            response.headers.add 'Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE'
            response.headers.add 'Access-Control-Allow-Headers', 'X-Auth-Token, Content-Type,X-Requested-With'
            next()
        }

        post('login') { UserService userService ->
            parse(LoginCommand).then { command ->
                User user
                def key
                Blocking.get {
                    user = userService.getUserByEmail(command.username)

                    if (!user) {
                        throw new IllegalAccessException()
                    }

                    if (user?.password == userService.generatePassword(user, command.password)) {
                        key = userService.createToken(user)
                    } else {
                        throw new IllegalAccessException()
                    }
                }.then {
                    render json([auth: key])
                }
            }

        }

        prefix('api') {

            get('teams') { TeamService teamService, InitialTeams initialTeams ->
                def teams
                Blocking.get {
                    teams = teamService.list()
                }.then {
                    def data = [
                        date: initialTeams.date,
                        teams: teams
                    ]
                    render json(data)
                }
            }

            get('teams/:name') { TeamService teamService ->
                def team
                Blocking.get {
                    team = teamService.getByName(pathTokens.name)
                }.then {
                    render json(team    )
                }
            }

            get('games/:name') { TeamService teamService ->
                def teams
                Blocking.get {
                    teams = teamService.getGamesByTeam(pathTokens.name)
                }.then {
                    render json(teams)
                }
            }

            prefix('admin') {
                def user
                all { UserService userService ->
                    def tokenString = request.headers.get('X-Auth-Token')
                    Blocking.get {
                        user = userService.getUserByToken(tokenString)
                    }.then {
                        next()
                    }
                }
                get('init/:start/:end') { TeamService teamService ->
                    def teams
                    Blocking.get {
                        init = teamService.init(pathTokens.start, pathTokens.end)
                    }.then {
                        println "${init}"
                    }

                    render json([message: 'initialization started'])
                }
                get('users') { UserService userService ->
                    def users
                    Blocking.get {
                        users = userService.list()
                    }.then {
                        render json(users)
                    }
                }
                post('create') { UserService userService ->
                    parse(User).then { newUser ->
                        Blocking.get {
                            userService.createNewUser(newUser)
                        }.then {
                            render json(message: 'user created')
                        }
                    }
                }
            }
        }

    }
}
