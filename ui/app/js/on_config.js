'use strict';

function OnConfig($stateProvider, $locationProvider, $urlRouterProvider) {
  'ngInject';

  $locationProvider.html5Mode(true);

  $stateProvider
  .state('Home', {
    url: '/',
    controller: 'HomeCtrl as home',
    templateUrl: 'home.html',
    title: 'Home',
    resolve: {
      teams: function(HomeService) {
        'ngInject';
        return HomeService.get().then(function(data) {
          return data;
        });
      }
    }
  });

  $urlRouterProvider.otherwise('/');

}

export default OnConfig;