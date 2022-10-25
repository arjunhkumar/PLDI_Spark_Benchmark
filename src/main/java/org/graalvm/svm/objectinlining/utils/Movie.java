package org.graalvm.svm.objectinlining.utils;

import java.io.Serializable;

import com.google.gson.GsonBuilder;

public class Movie implements Serializable {

	public String genres;
	
	public Actor[] actors;
	
	public String name;
	
	// TODO - move to date!
	public Integer year;
	
	public Integer votes;
	
	public Float rating;
	
	@Override
	public String toString() {
		return new GsonBuilder().create().toJson(this);
	}
	
	public ValueMovie toValueMovie() {
		
		ValueActor[] actors = new ValueActor[this.actors.length];
		
		for (int i = 0; i < this.actors.length; i++) {
			actors[i] = this.actors[i].toValueActor();
		}
		ValueMovie movie = new ValueMovie(genres,actors,name,year,votes,rating);
		return movie;
	}
}