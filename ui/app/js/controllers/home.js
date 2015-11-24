'use strict';

// ngAnnotate
function HomeCtrl(HomeService, teams) {
  'ngInject';

  // ViewModel
  var vm = this;
  vm.date = teams.date;
  vm.teams = teams.teams;
}

export default {
  name: 'HomeCtrl',
  fn: HomeCtrl
};