package com.sim.event;

import java.util.List;

import com.launcher.NetworkParameters;
import com.sim.event.eventhandler.EventHandler;
import com.sim.objects.Carrier;
import com.sim.objects.Connection;
import com.sim.objects.Flow;
import com.sim.objects.Generator;
import com.sim.objects.LightPath;

/**
 * Class used to release a circuit in the network
 * 
 * @author Fran
 * 
 */
public class CircuitReleaseEvent extends Event {

	private Generator generator;
	private List<Carrier> carriersToRelease;
	private Flow flow;
	private double bwToRelease;
	private List<LightPath> lightPathsToRelease;
	private Connection connectionToRelease;

	public CircuitReleaseEvent(Generator generator, double holdingTime,
			Flow flow, List<Carrier> carriersToRelease, double bwToRelease,
			List<LightPath> lightPathsToRelease, Connection connectionToRelease) {
		this.setTime(EventHandler.getTime() + holdingTime);
		this.generator = generator;
		this.flow = flow;
		this.carriersToRelease = carriersToRelease;
		this.bwToRelease = bwToRelease;
		this.lightPathsToRelease = lightPathsToRelease;
		this.connectionToRelease = connectionToRelease;
	}

	@Override
	public void execute() {

		for (Carrier c : carriersToRelease){
			c.releaseBw(bwToRelease/ lightPathsToRelease.size());
			if(c.getFreeBw()== NetworkParameters.getMaxCarrierBW())
				c.setTraversingFlow(null);
		}
			
		int guardBandCounter = 0;
		for (LightPath lp : lightPathsToRelease)
			if (lp.isForGuardBand())
				guardBandCounter++;
		/** Release guard bands */
		if (guardBandCounter > 1)
			for (LightPath lp : lightPathsToRelease)
				if (lp.isForGuardBand())
					lp.setForGuardBand(false);
				

		/** Remove connection */
		for (LightPath lp : lightPathsToRelease)
			for (int i = 0; i < lp.getSetOfConnections().size(); i++)
				if (lp.getSetOfConnections().get(i).equals(connectionToRelease))
					lp.getSetOfConnections().remove(i);

//		for (LightPath lp : lightPathsToRelease)
//			System.out.println(generator.getVertex().getVertexID() + "-->"
//					+ flow.getDstNode().getVertexID()
//					+ ": Connection released in carrier " + lp.getCarrierID() + " --> "
//					+ EventHandler.getTime());
	}
}
