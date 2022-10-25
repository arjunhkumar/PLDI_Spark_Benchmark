To try Spark with Object Inlining:

Note: we have three flavours of Spark queries:
a) Vanilla (original)
b) Unboxed (original, but no Boxed types whenever possible)
c) Inlined (using value fields)

We mainly use b) and c) for a more fair comparison.

1 - To launch the benchmark, simply run:
> ./run-standalone.sh

2 - The script is pre-configured and will run the two flavours. It will also
produce several log files which can be used to produce some plots and get numbers:
> cat run-UnboxedMovieQueries.log | grep "Vanilla-"
> cat run-ValueMovieQueries.log | grep "Vanilla-"
> gnuplot spark-gc-memory.gplot
> gnuplot spark-gc-latency.gplot
