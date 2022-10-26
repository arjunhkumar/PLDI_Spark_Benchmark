package org.graalvm.svm.objectinlining.utils;

import java.io.Serializable;

import com.google.gson.GsonBuilder;

public class UnboxedMovie implements Serializable {

	public String genres;
	
	public UnboxedActor[] actors;
	
	public String name;
	
	// TODO - move to date!
	public Integer year;
	
	public int votes;
	
	public float rating;
	
	@Override
	public String toString() {
		return new GsonBuilder().create().toJson(this);
	}
}