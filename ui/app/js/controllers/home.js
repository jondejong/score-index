'use strict';

// ngAnnotate
function HomeCtrl(HomeService, teams) {
  'ngInject';

  console.log('in Hc with teams', teams);

  // ViewModel
  var vm = this;
  vm.date = teams.date;
  vm.teams = teams.teams;
}

export default {
  name: 'HomeCtrl',
  fn: HomeCtrl
};