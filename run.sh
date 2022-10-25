#!/bin/bash

export JAVA_HOME=/home/arjun/Working_Directory/Softwares/Utilities/jdk_20_vt/jdk/
# export SPARK_HOME=/opt/spark

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

mvn clean
mvn package

input=$DIR/dataset/movies.json

# check: https://spark.apache.org/docs/latest/submitting-applications.html#master-urls
#exec_mode="local" # single JVM (driver + executor), 1 thread
exec_mode="local[4]" # single JVM (driver + executor), 4 threads
#exec_mode="spark://elara.systems.ethz.ch:7077" # local cluster (master, executor, driver)

SPARK_OPTS="$SPARK_OPTS --conf spark.broadcast.compress=false"
SPARK_OPTS="$SPARK_OPTS --conf spark.checkpoint.compress=false"
SPARK_OPTS="$SPARK_OPTS --conf spark.shuffle.spill.compress=false"
SPARK_OPTS="$SPARK_OPTS --conf spark.rdd.compress=false"
SPARK_OPTS="$SPARK_OPTS --conf spark.shuffle.compress=false"
SPARK_OPTS="$SPARK_OPTS --conf spark.executor.instances=2"
SPARK_OPTS="$SPARK_OPTS --conf spark.executor.cores=1"
SPARK_OPTS="$SPARK_OPTS --conf spark.executor.memory=1g"
SPARK_OPTS="$SPARK_OPTS --conf spark.executorEnv.JAVA_HOME=/home/arjun/Working_Directory/Softwares/Utilities/jdk_20_vt/jdk/"
SPARK_OPTS="$SPARK_OPTS --driver-memory 16g"
# SPARK_OPTS="$SPARK_OPTS --driver-java-options"
# SPARK_OPTS="$SPARK_OPTS --add-opens=java.base/java.lang=ALL-UNNAMED"
# SPARK_OPTS="$SPARK_OPTS --add-opens=java.base/java.lang.invoke=ALL-UNNAMED"
# SPARK_OPTS="$SPARK_OPTS --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
# SPARK_OPTS="$SPARK_OPTS --add-opens=java.base/java.io=ALL-UNNAMED"
# SPARK_OPTS="$SPARK_OPTS --add-opens=java.base/java.net=ALL-UNNAMED"
# SPARK_OPTS="$SPARK_OPTS --add-opens=java.base/java.nio=ALL-UNNAMED"
# SPARK_OPTS="$SPARK_OPTS --add-opens=java.base/java.util=ALL-UNNAMED"
# SPARK_OPTS="$SPARK_OPTS --add-opens=java.base/java.util.concurrent=ALL-UNNAMED"
# SPARK_OPTS="$SPARK_OPTS --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED"
# SPARK_OPTS="$SPARK_OPTS --add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
# SPARK_OPTS="$SPARK_OPTS --add-opens=java.base/sun.nio.cs=ALL-UNNAMED"
# SPARK_OPTS="$SPARK_OPTS --add-opens=java.base/sun.security.action=ALL-UNNAMED"
# SPARK_OPTS="$SPARK_OPTS --add-opens=java.base/sun.util.calendar=ALL-UNNAMED"

function run {
	gclog="run-$app.gc"
	JVM_OPTS="-Xlog:gc=info:file=$gclog"
	JVM_OPTS="$JVM_OPTS --add-opens=java.base/java.lang=ALL-UNNAMED"
	JVM_OPTS="$JVM_OPTS --add-opens=java.base/java.lang.invoke=ALL-UNNAMED"
	JVM_OPTS="$JVM_OPTS --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
	JVM_OPTS="$JVM_OPTS --add-opens=java.base/java.io=ALL-UNNAMED"
	JVM_OPTS="$JVM_OPTS --add-opens=java.base/java.net=ALL-UNNAMED"
	JVM_OPTS="$JVM_OPTS --add-opens=java.base/java.nio=ALL-UNNAMED"
	JVM_OPTS="$JVM_OPTS --add-opens=java.base/java.util=ALL-UNNAMED"
	JVM_OPTS="$JVM_OPTS --add-opens=java.base/java.util.concurrent=ALL-UNNAMED"
	JVM_OPTS="$JVM_OPTS --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED"
	JVM_OPTS="$JVM_OPTS --add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
	JVM_OPTS="$JVM_OPTS --add-opens=java.base/sun.nio.cs=ALL-UNNAMED"
	JVM_OPTS="$JVM_OPTS --add-opens=java.base/sun.security.action=ALL-UNNAMED"
	JVM_OPTS="$JVM_OPTS --add-opens=java.base/sun.util.calendar=ALL-UNNAMED"
	$SPARK_HOME/bin/spark-submit \
	    --class $class \
	    --name $app \
	    --master $exec_mode \
	    $SPARK_OPTS \
	    --conf "spark.driver.extraJavaOptions=$JVM_OPTS" \
	    --conf "spark.executor.extraJavaOptions=$JVM_OPTS" \
	    target/simple-project-1.0.jar $input &> run-$app.log

	# Process gc files:
	# cat file | filter per Pause Young and Full       | skip explicit gcs   | get timestamps   |  remove tags             | clean timestamp
	cat $gclog | grep -e "Pause Young" -e "Pause Full" | grep -v "System.gc" | awk '{print $1}' | sed 's/\[info\]\[gc\]//' | sed 's/\[//' | sed 's/s\]//' > $gclog.time
	# cat file | filter per Pause Young and Full       | skip explicit gcs   | get gc latency    |  remove time unit
	cat $gclog | grep -e "Pause Young" -e "Pause Full" | grep -v "System.gc" | awk '{print $NF}' | sed 's/ms//' > $gclog.latency
	# cat file | filter per Pause Young and Full       | skip explicit gcs   | get memory footprint  | remove separators            | print used memory| remove unit
	cat $gclog | grep -e "Pause Young" -e "Pause Full" | grep -v "System.gc" | awk '{print $(NF-1)}' | sed 's/->/ /' | sed 's/(/ /' | awk '{print $2}' | sed 's/M//' > $gclog.memory

}

for flavour in UnboxedMovieQueries ValueMovieQueries
do
    	class="org.graalvm.svm.objectinlining.$flavour"
    	app=$flavour
	echo "Running $flavour..."
	run
	echo "Running $flavour... done!"
done

gnuplot spark-gc-latency.gplot
echo "Generated $DIR/spark-gc-latency.png"
gnuplot spark-gc-memory.gplot
echo "Generated $DIR/spark-gc-memory.png"
