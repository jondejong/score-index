var awspublish = require('gulp-awspublish');
var gulp = require('gulp');
var fs = require('fs-extra');

gulp.task('publish', function () {

  //create a file at UI root called aws.json with these values in
  //order to publish to S3

  var s3Config = fs.readJsonSync('./aws.json');
  console.log(s3Config);

  var publisher = awspublish.create({
        params: {
          Bucket: s3Config.bucket
        }
        ,
        accessKeyId: s3Config.accessKeyId,
        secretAccessKey: s3Config.secretAccessKey
      });

  gulp.src('./build/**/*')
      .pipe(publisher.publish([], {options: {force: true}}))
      .pipe(publisher.sync())
      .pipe(awspublish.reporter());

});
