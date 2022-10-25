package org.graalvm.svm.objectinlining.utils;

// import java.io.Serializable;

import com.google.gson.GsonBuilder;

public primitive class ValueActor {
	
	public int birth;
	
	public int death;
	
	public byte[] name;

	// @Override
	// public String toString() {
	// 	return new GsonBuilder().create().toJson(toActor());
	// }
	
	public ValueActor(Integer birth, Integer death, String name) {
		this.birth = birth;
		this.death = death;
		this.name = name.getBytes();
	}

	public Actor toActor() {
		Actor actor = new Actor();
		actor.birth = this.birth;
		actor.death = this.death;
		actor.name = new String(name);
		return actor;
	}
}
