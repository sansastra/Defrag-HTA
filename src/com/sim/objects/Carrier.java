package com.sim.objects;

/**
 * Class to represent a carrier
 * 
 * @author Fran
 * 
 */
public class Carrier {

	private int id;
	private Flow traversingFlow;
	private double totalBW;
	private boolean isForGuardBand;
	private double usedBW;

	public Carrier(int id, double totalBW) {
		this.id = id;
		this.totalBW = totalBW;
	}

	public int getId() {
		return id;
	}

	public double getFreeBw() {
		return totalBW - usedBW;
	}

	public void requestBW(double bwToRequest) {
		this.usedBW += bwToRequest;
//		if (this.usedBW > this.totalBW){
//			System.out.println("RequestBW Known Bug");
//			System.exit(0);
//		}
	}

	public void releaseBw(double bwToRelease) {
//		if (bwToRelease > this.usedBW){
//			System.out.println("ReleaseBW Known Bug");
//			System.exit(0);
//		}
		this.usedBW -= bwToRelease;
	}
	
	public Flow getTraversingFlow() {
		return traversingFlow;
	}

	public void setTraversingFlow(Flow traversingFlow) {
		this.traversingFlow = traversingFlow;
	}

	public double getTotalBW() {
		return totalBW;
	}

	public boolean isForGuardBand() {
		return isForGuardBand;
	}

	public void setForGuardBand(boolean isForGuardBand) {
		this.isForGuardBand = isForGuardBand;
	}
}
