'use strict';

function HomeService($http) {
  'ngInject';

  const service = {};

  service.get = function() {
    return new Promise((resolve, reject) => {
      $http.get('http://localhost:5050/api/teams').success((data) => {
        console.log('back from teams fetch', data);
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