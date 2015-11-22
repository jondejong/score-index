'use strict';

// ngAnnotate
function HomeCtrl(HomeService) {
  'ngInject';

  // ViewModel
  const vm = this;

  HomeService.get().then(function(data){
    vm.teams = data;
  });

}

export default {
  name: 'HomeCtrl',
  fn: HomeCtrl
};