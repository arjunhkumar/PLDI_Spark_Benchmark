package org.graalvm.svm.objectinlining;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.graalvm.svm.objectinlining.utils.Movie;
import org.graalvm.svm.objectinlining.utils.ValueActor;
import org.graalvm.svm.objectinlining.utils.ValueMovie;

import com.google.gson.Gson;

import scala.Tuple2;


public class ValueMovieQueries {
	
	public static List<ValueMovie.ref> loadMovies(String jsonPath) throws FileNotFoundException, IOException {
		List<ValueMovie.ref> movies = new ArrayList<>();
		Gson gson = new Gson();
		long memoryBefore;
		long memoryAfter;
		long startTime;
		long finishTime;
		
		memoryBefore = MovieQueries.memory();
		startTime = System.currentTimeMillis();
		
		try (BufferedReader br = new BufferedReader(new FileReader(jsonPath))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	Movie movie = gson.fromJson(line, Movie.class);
		    	ValueMovie valuemovie = movie.toValueMovie();
		    	movies.add(valuemovie);
		    }
		}
		
		finishTime = System.currentTimeMillis();
		memoryAfter = MovieQueries.memory();
		
		System.out.println(String.format("[Vanilla-Memory] Loading movies took %d ms and use %d MB of heap memory",
				finishTime - startTime,
				memoryAfter - memoryBefore));
		
		return movies;
	}
	
	// Print how many movies were released in 1970.
	public static void q1(JavaRDD<ValueMovie.ref> movies) {
		long startTime;
		long finishTime;
		long count;
		
		startTime = System.currentTimeMillis();
		count = movies.filter(s -> s.year == 1970).count();
		finishTime = System.currentTimeMillis();
		
		System.out.println(String.format("[Vanilla-Q1] took %d ms; result = %d", finishTime - startTime, count));
	}
	
	
	// Print one of the movies with highest rating.
	public static void q2(JavaRDD<ValueMovie.ref> movies) {
		long startTime;
		long finishTime;
		List<ValueMovie.ref> result;

		startTime = System.currentTimeMillis();
		result = movies.sortBy(s -> s.rating, false, 1).take(1);
		finishTime = System.currentTimeMillis();
		
		System.out.println(String.format("[Vanilla-Q2] took %d ms; result = %s", finishTime - startTime, result.get(0)));
		
	}
	
	// Print the average age of mob town actors.
	public static void q3(JavaRDD<ValueMovie.ref> movies) {
		long startTime;
		long finishTime;
		
		startTime = System.currentTimeMillis();
		JavaRDD<ValueMovie.ref> mobtown = movies.filter(s -> new String(s.name).equals("Mob Town"));
		int year = mobtown.take(1).get(0).year;
		JavaRDD<ValueActor.ref> actors = mobtown.flatMap(s -> Arrays.asList(s.actors).iterator());
		int numActors = actors.collect().size();
		JavaRDD<Integer> ages = actors.map(s -> year - s.birth);
		int averageAge = ages.reduce((s, d) -> s + d) / numActors;
		finishTime = System.currentTimeMillis();
	
		System.out.println(String.format("[Vanilla-Q3] took %d ms; result = %s", finishTime - startTime, averageAge));
	}
	
	// Print the most popular actor name and the number of occurrences in movies.
	public static void q4(JavaRDD<ValueMovie.ref> movies) {
		long startTime;
		long finishTime;
		
		startTime = System.currentTimeMillis();
		JavaRDD<ValueActor.ref> actors = movies.flatMap(s -> Arrays.asList(s.actors).iterator());
		JavaPairRDD<String, Integer> tuplesNameNumber = actors.mapToPair(s -> new Tuple2<>(new String(s.name), 1));
		tuplesNameNumber = tuplesNameNumber.reduceByKey((s, d) -> s + d);
		JavaPairRDD<Integer, String> tuplesNumberName = tuplesNameNumber.mapToPair(s -> s.swap());
		Tuple2<Integer, String> popularNumberName = tuplesNumberName.sortByKey(false).first();
		finishTime = System.currentTimeMillis();
		
		System.out.println(String.format("[Vanilla-Q4] took %d ms; result = %s", finishTime - startTime, popularNumberName));
	}

	// Print the year with more votes.
	public static void q5(JavaRDD<ValueMovie.ref> movies) {
		long startTime;
		long finishTime;
		
		startTime = System.currentTimeMillis();
		JavaPairRDD<Integer, Integer> tuplesYearVotes = movies.mapToPair(s -> new Tuple2<>(s.year, s.votes));
		tuplesYearVotes = tuplesYearVotes.reduceByKey((s, d) -> s + d);
		JavaPairRDD<Integer, Integer> tuplesVotesYear = tuplesYearVotes.mapToPair(s -> s.swap());
		Tuple2<Integer, Integer> mostVotesYear = tuplesVotesYear.sortByKey(false).first();
		finishTime = System.currentTimeMillis();
		
		System.out.println(String.format("[Vanilla-Q5] took %d ms; result = %s", finishTime - startTime, mostVotesYear));
	}
	
	// Actor with highest average rating of all moves he/she participated.
	public static void q6(JavaRDD<ValueMovie.ref> movies) {
		long startTime;
		long finishTime;
		
		startTime = System.currentTimeMillis();
		JavaPairRDD<ValueActor.ref, Float> tuplesActorRating = movies.flatMapToPair(s -> {
			float rating = s.rating;
			Set<Tuple2<ValueActor.ref, Float>> tuples = new HashSet<>();
			for (ValueActor actor : s.actors) {
				tuples.add(new Tuple2<>(actor, rating));
			}
			return tuples.iterator();
		});
		tuplesActorRating = tuplesActorRating.reduceByKey((s, d) -> s + d);
		JavaPairRDD<Float, ValueActor.ref> tuplesRatingActor = tuplesActorRating.mapToPair(s -> s.swap());
		Tuple2<Float, ValueActor.ref> highestRateActor = tuplesRatingActor.sortByKey(false).first();
		finishTime = System.currentTimeMillis();
		
		System.out.println(String.format("[Vanilla-Q6] took %d ms; result = %s", finishTime - startTime, highestRateActor));
	}
	

	public static void printDataset(JavaRDD<ValueMovie.ref> movies) {
		for (ValueMovie movie : movies.collect()) {
			System.out.println(movie);
		}
	}
	
	public static void main(String[] args) throws Exception {

		if (args.length < 1) {
			System.err.println("Usage: <appname> <input file>");
			System.exit(1);
		}
		
		SparkConf conf = new SparkConf();
		JavaSparkContext context = new JavaSparkContext(conf);
		List<ValueMovie.ref> movies = loadMovies(args[0]);
		JavaRDD<ValueMovie.ref> moviesRDD = context.parallelize(movies).cache();
		
		q1(moviesRDD);
		System.gc();
		q2(moviesRDD);
		System.gc();
		q3(moviesRDD);
		System.gc();
		q4(moviesRDD);
		System.gc();
		q5(moviesRDD);
		System.gc();
		q6(moviesRDD);	
		
	    context.close();
	}
}
