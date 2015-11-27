'use strict';

function RankingsCtrl(sport, teams) {
  'ngInject';

  // ViewModel
  var vm = this;
  vm.date = teams.date;
  vm.teams = teams.teams;
  vm.sport = sport;
}

export default {
  name: 'RankingsCtrl',
  fn: RankingsCtrl
};