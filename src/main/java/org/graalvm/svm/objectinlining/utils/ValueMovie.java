package org.graalvm.svm.objectinlining.utils;

import java.io.Serializable;

import com.google.gson.GsonBuilder;

public primitive class ValueMovie implements Serializable {

	public byte[] genres;
	
	public ValueActor[] actors;
	
	public byte[] name;
	
	public int year;
	
	public int votes;
	
	public float rating;
	
	public ValueMovie(String genres, ValueActor[] actors, String name, Integer year,Integer votes, Float rating) {
		this.genres = genres.getBytes();
		this.actors = actors;
		this.name = name.getBytes();
		this.year = year;
		this.votes = votes;
		this.rating = rating;
	}
	
	@Override
	public String toString() {
		return new GsonBuilder().create().toJson(toMovie());
	}
	
	public Movie toMovie() {
		Movie movie = new Movie();
		Actor[] actors = new Actor[this.actors.length];
		
		for (int i = 0; i < this.actors.length; i++) {
			actors[i] = this.actors[i].toActor();
		}
		
		movie.genres = new String(genres);
		movie.actors = actors;
		movie.name = new String(name);
		movie.year = year;
		movie.votes = votes;
		movie.rating = rating;
		return movie;
	}
}