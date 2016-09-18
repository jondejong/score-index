const AWS = require('aws-sdk')
const fs = require('fs-extra')
const uuid = require('uuid')

const awsConfig = fs.readJsonSync('./aws.json')
const cloudfront = new AWS.CloudFront({
  accessKeyId: awsConfig.accessKeyId,
  secretAccessKey: awsConfig.secretAccessKey
})

const params = {
  DistributionId: awsConfig.distribution,
  InvalidationBatch: { /* required */
    CallerReference: uuid.v1(), /* required */
    Paths: { /* required */
      Quantity: 1, /* required */
      Items: [
        '/data/*'
      ]
    }
  }
};
cloudfront.createInvalidation(params, function(err, data) {
  if (err) {
    console.log('An error occurred invalidating CloudFront Distribution', err)
  } else {
    console.log('CloudFront distribution invalidation started')
  }
});