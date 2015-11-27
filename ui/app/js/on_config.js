'use strict';

function OnConfig($stateProvider, $locationProvider, $urlRouterProvider) {
  'ngInject';

  $locationProvider.html5Mode(true);

  $stateProvider
  .state('home', {
    url: '/home',
    controller: 'HomeCtrl as home',
    templateUrl: 'home.html',
    title: 'Home'
  })
      .state('home.ncaam', {
        url: '/ncaam',
        controller: 'RankingsCtrl as rankings',
        templateUrl: 'rankings.html',
        title: 'Rankings',
        resolve: {
          sport: function () {return "NCAA Men's Basketball"},
          teams: function(RankingsService) {
            'ngInject';
            return RankingsService.get('ncaam').then(function(data) {
              return data;
            });
          }
        }

      })
      .state('home.nfl', {
        url: '/nfl',
        controller: 'RankingsCtrl as rankings',
        templateUrl: 'rankings.html',
        title: 'Rankings',
        resolve: {
          sport: function () {return "NFL"},
          teams: function(RankingsService) {
            'ngInject';
            return RankingsService.get('nfl').then(function(data) {
              return data;
            });
          }
        }

      });;

  $urlRouterProvider.otherwise('/home');

}

export default OnConfig;