package com.sim.objects;

import java.util.ArrayList;
import java.util.List;

import com.graph.elements.vertex.VertexElement;

/**
 * Class to represent a link
 * 
 * @author Fran
 * 
 */
public class Link {

	private List<Carrier> setOfCarriers;
	private VertexElement srcNode;
	private VertexElement dstNode;
	private double cumulativeLinkUtilization;
	private double cumulativeLinkUtilizationGB;

	public Link(VertexElement srcNode, VertexElement dstNode,
			int numberOfCarriers, double carrierBW) {

		this.srcNode = srcNode;
		this.dstNode = dstNode;
		setOfCarriers = new ArrayList<Carrier>();
		for (int i = 0; i < numberOfCarriers; i++)
			setOfCarriers.add(new Carrier(i, carrierBW));
	}

	public Double getRemainingBw() {

		double remainingLinkBw = 0;
		for (Carrier carrier : setOfCarriers)
			remainingLinkBw += carrier.getFreeBw();

		return remainingLinkBw;
	}

	public Carrier getCarrier(int carrier) {
		for (Carrier cr : setOfCarriers)
			if (cr.getId() == carrier)
				return cr;
		return null;
	}

	public Carrier getCarrier(Carrier carrier) {
		for (Carrier cr : setOfCarriers)
			if (cr.equals(carrier))
				return cr;
		return null;
	}

	public List<Carrier> getSetOfCarriers() {
		return setOfCarriers;
	}

	public VertexElement getSrcNode() {
		return srcNode;
	}

	public VertexElement getDstNode() {
		return dstNode;
	}

	public int getNumberOfGuardBandCarriers() {
		int numberOfGuardBandCarriers = 0;

		for (Carrier carrier : setOfCarriers)
			if (carrier.isForGuardBand())
				numberOfGuardBandCarriers++;

		return numberOfGuardBandCarriers;
	}
	
	public double getBwUsedForGuardBand(){
		double totalLinkBWForGuardBands = 0;
		
		for (Carrier carrier : setOfCarriers)
			if (carrier.isForGuardBand())
				totalLinkBWForGuardBands+= carrier.getTotalBW();
				
		return totalLinkBWForGuardBands;
	}

	public double getTotalLinkBW() {
		double totalLinkBW = 0;

		for (Carrier carrier : setOfCarriers)
			totalLinkBW += carrier.getTotalBW();

		return totalLinkBW;

	}

	public double getCumulativeLinkUtilization() {
		return cumulativeLinkUtilization;
	}

	public void setCumulativeLinkUtilization(double cumulativeLinkUtilization) {
		this.cumulativeLinkUtilization = cumulativeLinkUtilization;
	}

	public double getCumulativeLinkUtilizationGB() {
		return cumulativeLinkUtilizationGB;
	}

	public void setCumulativeLinkUtilizationGB(double cumulativeLinkUtilizationGB) {
		this.cumulativeLinkUtilizationGB = cumulativeLinkUtilizationGB;
	}
}
