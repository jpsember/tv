# This is a script to run the program from the command line,
# since running it within thd IDE fails due to the lanterna library not having
# access to the screen from there.
#


java \
-Dfile.encoding=UTF-8 \
-classpath /Users/jeff/github_projects/tv/target/classes:/Users/jeff/.m2/repository/com/jsbase/java-core/1000/java-core-1000.jar:/Users/jeff/.m2/repository/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar:/Users/jeff/.m2/repository/com/googlecode/lanterna/lanterna/3.1.2/lanterna-3.1.2.jar tv.TvMain \
log_file log.txt \
"$@"
