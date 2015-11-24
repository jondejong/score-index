'use strict';

function HomeService($http) {
  'ngInject';

  const service = {};

  service.get = function() {
    return new Promise((resolve, reject) => {
       //For local test development
      //$http.get('http://localhost:5050/api/teams').success((data) => {

      // For deployment to S3
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