'use strict';

function RankingsService($http) {
  'ngInject';

  const service = {};

  service.get = function(sport) {
    return new Promise((resolve, reject) => {
      // For deployment to S3
      $http.get('/data/' + sport + '.json').success((data) => {
        resolve(data);
      }).error((err, status) => {
        reject(err, status);
      });
    });
  };

  return service;

}

export default {
  name: 'RankingsService',
  fn: RankingsService
};