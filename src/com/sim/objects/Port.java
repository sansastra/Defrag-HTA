package com.sim.objects;

public class Port {

	private int type;
	private double bw;

	public Port(int type, double bw) {
		this.type = type;
		this.bw = bw;
	}

	public int getType() {
		return type;
	}

	public double getBw() {
		return bw;
	}

}
