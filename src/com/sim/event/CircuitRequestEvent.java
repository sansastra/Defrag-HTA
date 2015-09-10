package com.sim.event;

import java.util.ArrayList;
import java.util.List;

import com.launcher.Launcher;
import com.launcher.NetworkParameters;
import com.filemanager.Results;
import com.sim.event.eventhandler.EventHandler;
import com.sim.objects.Carrier;
import com.sim.objects.Connection;
import com.sim.objects.Flow;
import com.sim.objects.Generator;
import com.sim.objects.LightPath;
import com.sim.objects.Link;
import com.sim.objects.Port;

/**
 * Class used to establish a circuit (channel) in the network
 * 
 * @author Fran
 * 
 */
public class CircuitRequestEvent extends Event {

	private Generator generator;
	private double interArrivalTime;
	private Port port;

	public CircuitRequestEvent(Generator generator, double interArrivalTime,
			Port port) {
		this.setTime(EventHandler.getTime() + interArrivalTime);
		this.generator = generator;
		this.interArrivalTime = interArrivalTime;
		this.port = port;
	}

	@Override
	public void execute() {

		/** Generate holding time from exp distribution */
		double holdingTime = generator.getHoldingTimeDistribution().execute();

		/** Decide if the holding time is know or unknown */
		boolean isNotKnown = generator.isKnownOrUnknown();
		if (isNotKnown)
			holdingTime = Launcher.getMeanHoldingTime();

		/** Generate a random destination following a uniform distribution */
		Flow rndFlow = generator.getSetOfFlows().get(
				generator.getUniform().execute().intValue());

		double requiredBW = port.getBw();

		/** Select a lightpath based on algorithm */
		List<LightPath> selectedLPs = rndFlow.selectLightPath(requiredBW,
				holdingTime, isNotKnown, NetworkParameters.isWithGuardBands());

		/** If no LightPath or not enough BW the request is blocked */
		if (selectedLPs.size() == 0)
			rndFlow.increaseBlockingCounter(port.getType(), isNotKnown);

		/** If not, set the connection */
		else {
			int lpCounter = 0;
			for (LightPath lp : selectedLPs) {
				if (lp == null) {
					System.out.println();
				}
				if (!lp.isForGuardBand())
					lpCounter++;
			}

			List<Carrier> selectedCarriers = new ArrayList<>();
			Carrier selectedCarrier;

			Connection newConnection = new Connection(EventHandler.getTime(),
					holdingTime, requiredBW, isNotKnown);

			for (LightPath lp : selectedLPs) {
				lp.getSetOfConnections().add(newConnection);
				for (Link link : lp.getLightPathLinks()) {
					selectedCarrier = link.getCarrier(lp.getCarrierID());
					if (!lp.isForGuardBand())
						selectedCarrier.requestBW(requiredBW / lpCounter);
					if (selectedCarrier.getTraversingFlow() == null)
						selectedCarrier.setTraversingFlow(rndFlow);
					if (!lp.isForGuardBand())
						selectedCarriers.add(selectedCarrier);
				}
			}

//			for (LightPath lp : selectedLPs)
//				System.out.println(generator.getVertex().getVertexID() + "-->"
//						+ rndFlow.getDstNode().getVertexID()
//						+ ": Connection established in carrier "
//						+ lp.getCarrierID() + " --> " + EventHandler.getTime());

			/****************** Holding Time Results **************************/
//			Results.writeHoldingTime(generator, rndFlow, port.getType(),
//					isNotKnown, holdingTime);

			/** Generate the release event for this request */
			EventHandler.addEvent(new CircuitReleaseEvent(generator,
					holdingTime, rndFlow, selectedCarriers, requiredBW,
					selectedLPs, newConnection));
		}

		/** Increase request counter for this flow */
		rndFlow.increaseFlowRequestCounter(port.getType());

		/*********************** Blocking filemanager *************************/
		Results.writeBlockingResults(rndFlow);

		/******************* Link Utilization filemanager *********************/
		Results.writeLinkUtilizationResults();
//		Results.writeTotalLinkUtilizationResults();
		Results.increaseTotalRequestCounter();

		/******************** InterArrival Time Results *******************/
//		Results.writeInterArrivalTime(generator, rndFlow, port.getType(),
//				interArrivalTime);

		/** Add a new request event */
		int nextPortNumber = generator.getPortDistribution().execute().intValue();
		Port nextPort = NetworkParameters.getListOfPorts().get(nextPortNumber);

		double nextInterArrivalTime = generator.getRequestDistribution().execute();

		EventHandler.addEvent(new CircuitRequestEvent(generator,
				nextInterArrivalTime, nextPort));

	}
}
