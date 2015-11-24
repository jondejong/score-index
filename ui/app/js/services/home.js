'use strict';

function HomeService($http) {
  'ngInject';

  const service = {};

  service.get = function() {
    return new Promise((resolve, reject) => {
      $http.get('/data/teams.json').success((data) => {
        resolve(data);
      }).error((err, status) => {
        reject(err, status);
      });
    });
  };

  return service;

}

export default {
  name: 'HomeService',
  fn: HomeService
};