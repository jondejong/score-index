'use strict';

import config       from '../config';
import gulp         from 'gulp';

gulp.task('data', function () {
  return gulp.src(config.data.src)
      .pipe(gulp.dest(config.data.dest));

});