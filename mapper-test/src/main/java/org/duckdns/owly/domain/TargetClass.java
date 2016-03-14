package org.duckdns.owly.domain;

import org.duckdns.owly.annotation.Dto;

@Dto(entity = SourceClass.class)
public class TargetClass {
	private int id;

	public TargetClass() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "" + this.id;
	}
}
