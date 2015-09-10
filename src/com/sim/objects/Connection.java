package com.sim.objects;

import com.sim.event.eventhandler.EventHandler;

/**
 * Class to represent a connection between two end-points
 * 
 * @author Fran
 * 
 */
public class Connection {

	private double startingTime;
	private double holdingTime;
	private double bw;
	private boolean isNotKnown;

	public Connection(double startingTime, double holdingTime, double bw, boolean isNotKnown) {
		this.startingTime = startingTime;
		this.holdingTime = holdingTime;
		this.bw = bw;
		this.isNotKnown = isNotKnown;
	}

	public double getResidualTime() {
		double residualTime = (startingTime + holdingTime)
				- EventHandler.getTime();
		return residualTime;
	}

	public double getHoldingTime() {
		return holdingTime;
	}

	public double getBw() {
		return bw;
	}

	public boolean isNotKnown() {
		return isNotKnown;
	}
	
	public double getSpentTime(double t){
		
		return t-startingTime;
	}
	
	public double getStartingTime(){
		return startingTime;
	}
}
