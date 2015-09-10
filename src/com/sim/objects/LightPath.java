package com.sim.objects;

import java.util.ArrayList;
import java.util.List;

import com.graph.path.PathElement;
import com.launcher.Launcher;
import com.launcher.NetworkParameters;

/**
 * Class to represent a lightpath between two end-points
 * 
 * @author Fran
 * 
 */
public class LightPath {

	private int lightPathId;
	private int carrierId;
	private boolean isForGuardBand;
	private PathElement pathElement;
	private List<Link> lightPathLinks;
	private List<Connection> setOfConnections;

	public LightPath(int lightPathId, PathElement pathElement,
			List<Link> lightPathLinks) {
		this.lightPathId = lightPathId;
		int tmp = lightPathId / Launcher.getNumberOfCarriersPerLink();
		this.carrierId = lightPathId
				- (Launcher.getNumberOfCarriersPerLink() * tmp);
		this.pathElement = pathElement;
		this.lightPathLinks = lightPathLinks;
		this.setOfConnections = new ArrayList<Connection>();
	}

	public Boolean isTraversedBy(Flow f) {

		Boolean isTraversed;
		int counterNullFlow = 0;
		int counterSelfFlow = 0;

		List<Carrier> setOfCarriers = new ArrayList<Carrier>();

		for (Link lightPathLink : lightPathLinks)
			setOfCarriers.add(lightPathLink.getCarrier(carrierId));

		for (Carrier c : setOfCarriers)
			if (c.getTraversingFlow() == null) {
				counterNullFlow++;
				continue;
			} else if (c.getTraversingFlow().equals(f))
				counterSelfFlow++;

		if (counterNullFlow == setOfCarriers.size())
			isTraversed = false;

		else if (counterSelfFlow == setOfCarriers.size())
			isTraversed = true;
		else
			isTraversed = null;

		return isTraversed;
	}

	public boolean isEnoughFreeCapacity(double requiredBW) {

		int counter = 0;
		for (Link l : lightPathLinks) {
			Carrier linkCarrier = l.getCarrier(carrierId);
			if (linkCarrier.getFreeBw() >= requiredBW)
				counter++;
		}
		if (counter == lightPathLinks.size())
			return true;
		else
			return false;
	}

	public double getRemainingBW() {

		for (Carrier carrier : lightPathLinks.get(0).getSetOfCarriers())
			if (carrier.getId() == carrierId)
				return carrier.getFreeBw();
		return 0;
	}

	public int getCarrierID() {
		return carrierId;
	}

	public List<Link> getLightPathLinks() {
		return lightPathLinks;
	}

	public PathElement getPath() {
		return pathElement;
	}

	public List<Connection> getSetOfConnections() {
		return setOfConnections;
	}

	public void setPathLinks(List<Link> pathLinks) {
		this.lightPathLinks = pathLinks;
	}

	public boolean isForGuardBand() {
		return isForGuardBand;
	}

	public void setForGuardBand(boolean isForGuardBand) {
		this.isForGuardBand = isForGuardBand;
		/** If the carrier is for guardband, set the used bw at maximum */
		if (isForGuardBand) {
			for (Link l : lightPathLinks) {
				Carrier linkCarrier = l.getCarrier(carrierId);
				linkCarrier.requestBW(linkCarrier.getTotalBW());
				linkCarrier.setForGuardBand(isForGuardBand);
			}
		}/** If not, release all bw */
		else {
			for (Link l : lightPathLinks) {
				Carrier linkCarrier = l.getCarrier(carrierId);
				linkCarrier.releaseBw(linkCarrier.getTotalBW());
				linkCarrier.setForGuardBand(isForGuardBand);
			}
		}
	}

	public int getLightPathId() {
		return lightPathId;
	}

	public int getNumberOfUnknownConnections() {

		int i = 0;
		for (Connection con : setOfConnections)
			if (con.isNotKnown())
				i++;

		return i;

	}

}
