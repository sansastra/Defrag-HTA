package com.filemanager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//import com.launcher.Launcher_old2;
import com.launcher.Launcher;
import com.launcher.NetworkParameters;
import com.sim.event.Event;
import com.sim.event.eventhandler.EventHandler;
import com.sim.objects.Flow;
import com.sim.objects.Generator;
import com.sim.objects.Link;

public class Results {

	private static WriteFile blockingWriteFile;
	private static WriteFile totalLinkUtilizationWriteFile;
	private static WriteFile linkUtilizationWriteFile;
	private static WriteFile interArrivalWriteFile;
	private static WriteFile holdingTimeWriteFile;
	private static int requestCounter;
	private static int linkRequestCounter;
	private static int totalRequestCounter;
	private static double initialTime;
	private static double cumulativeTime;

	public Results() {

		try {
			totalRequestCounter =0;
			requestCounter = 0;
			linkRequestCounter=0;
			initialTime = 0;
			cumulativeTime = 0;
			SimpleDateFormat MY_FORMAT = new SimpleDateFormat(
					"dd-MM-yyyy HH-mm-ss", Locale.getDefault());
			Date date = new Date();
			blockingWriteFile = new WriteFile("BlockingProb-run-"
					+ Launcher.get_runNumber(), false);
			blockingWriteFile.write(MY_FORMAT.format(date) + "\n");
			blockingWriteFile.write("Blocking per SRC-DST nodes\n\n");
			blockingWriteFile.write("S	D	T	Req	Blocked	U	SimTime\n");

			totalLinkUtilizationWriteFile = new WriteFile(
					"TotalLinkUtilization-run-" + Launcher.get_runNumber(),
					false);
			totalLinkUtilizationWriteFile.write(MY_FORMAT.format(date) + "\n");
			totalLinkUtilizationWriteFile.write("Total link utilization\n\n");
			totalLinkUtilizationWriteFile
					.write("Req	SimTime	Utilization\n");

			linkUtilizationWriteFile = new WriteFile("LinkUtilization-run-"
					+ Launcher.get_runNumber(), false);
			linkUtilizationWriteFile.write(MY_FORMAT.format(date) + "\n");
			linkUtilizationWriteFile.write("Link utilization\n\n");
			linkUtilizationWriteFile
					.write("Link	Requests	SimTime	Utilization(%)\n");

			interArrivalWriteFile = new WriteFile(
					"MeanInterarrivalTimes-run-" + Launcher.get_runNumber(),
					false);
			interArrivalWriteFile.write(MY_FORMAT.format(date) + "\n");
			interArrivalWriteFile.write("Requests \n\n");
			interArrivalWriteFile.write("S	D	T	lambda(i)	simTime\n");

			holdingTimeWriteFile = new WriteFile("MeanHoldingTimes-run-"
					+ Launcher.get_runNumber(), false);
			holdingTimeWriteFile.write(MY_FORMAT.format(date) + "\n");
			holdingTimeWriteFile.write("Releases \n\n");
			holdingTimeWriteFile.write("S	D	T	K/U	ht(i)	simTime\n");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void increaseTotalRequestCounter() {
		totalRequestCounter++;
		if (totalRequestCounter >= Launcher.getNumberOfTotalRequests())
			EventHandler.stopEventHandler();
        if(totalRequestCounter%1000==0)
            System.out.println("Processed Requests: "+totalRequestCounter);
	}

	public static void writeLinkUtilizationResults() {


		double deltaT = EventHandler.getTime() - initialTime;
		for (Link l : NetworkParameters.getListOfLinks()) {

			double usedLinkBW = l.getTotalLinkBW() - l.getRemainingBw();
			double utilization = usedLinkBW / l.getTotalLinkBW();
			l.setCumulativeLinkUtilization(l.getCumulativeLinkUtilization() + (deltaT * utilization));

			double usedLinkBWForGuardBands = l.getBwUsedForGuardBand();
			double utilizationGB = usedLinkBWForGuardBands / l.getTotalLinkBW();
			l.setCumulativeLinkUtilizationGB(l.getCumulativeLinkUtilizationGB() + (deltaT * utilizationGB));
		}

		cumulativeTime = cumulativeTime + deltaT;
		initialTime = EventHandler.getTime();

		linkRequestCounter++;
		if (linkRequestCounter >= 1000) {
			for (Link l : NetworkParameters.getListOfLinks()) {
				double averageUtilization = l.getCumulativeLinkUtilization() / cumulativeTime;
				double utilizationForGuardBand = l.getCumulativeLinkUtilizationGB() / cumulativeTime;
				linkUtilizationWriteFile.write(l.getSrcNode().getVertexID()
						.substring(1)
						+ " "
						+ l.getDstNode().getVertexID().substring(1)
						+ "	"
						+ linkRequestCounter
						+ "	"
						+ EventHandler.getTime()
						+ "	"
						+ averageUtilization
						+ "	"
						+ utilizationForGuardBand
						+ "\n");

				l.setCumulativeLinkUtilization(0);
				l.setCumulativeLinkUtilizationGB(0);
			}
			linkRequestCounter = 0;
			cumulativeTime = 0;

		}
	}

	public static void writeTotalLinkUtilizationResults() {

		requestCounter++;
		if (requestCounter >= 100) {
			double remBW = 0;
			for (Link l : NetworkParameters.getListOfLinks()) {
				remBW += l.getRemainingBw();
			}

			double utilization = (1 - (remBW / (16 * 25 * 320))) * 100;

			totalLinkUtilizationWriteFile.write(requestCounter + "	"
					+ EventHandler.getTime() + "	" + utilization + "%\n");
			requestCounter = 0;
		}
	}

	public static void writeBlockingResults(Flow flow) {

		int totalRequestCounter = 0;

		for (int i : flow.getFlowRequestCounters())
			totalRequestCounter += i;

		if (totalRequestCounter >= 100) {
			for (int i = 0; i < flow.getBlockingCounters().size(); i++) {
				blockingWriteFile.write(flow.getSrcNode().getVertexID()
						.substring(1)
						+ "	"
						+ flow.getDstNode().getVertexID().substring(1)
						+ "	"
						+ i
						+ "	"
						+ flow.getFlowRequestCounters().get(i)
						+ "	"
						+ flow.getBlockingCounters().get(i)
						+ "	"
						+ flow.getKnownOrNotblockingCounters().get(i)
						+ "	"
						+ EventHandler.getTime() + "\n");
				flow.getBlockingCounters().set(i, 0);
				flow.getKnownOrNotblockingCounters().set(i, 0);
				flow.getFlowRequestCounters().set(i, 0);
			}

//			writeLinkUtilizationResults(totalRequestCounter);
		}
	}

	public static void writeHoldingTime(Generator gen, Flow flow, int portType,
			boolean isKnown, double ht) {
		String knownOrNot;
		if (isKnown)
			knownOrNot = "U";
		else
			knownOrNot = "K";

		holdingTimeWriteFile.write(gen.getVertex().getVertexID().substring(1)
				+ "	" + flow.getDstNode().getVertexID().substring(1) + "	"
				+ portType + "	" + knownOrNot + "	" + ht + "	"
				+ EventHandler.getTime() + "\n");
	}

	public static void writeInterArrivalTime(Generator gen, Flow flow,
			int portType, double interArrivalTime) {

		interArrivalWriteFile.write(gen.getVertex().getVertexID().substring(1)
				+ "	" + flow.getDstNode().getVertexID().substring(1) + "	"
				+ portType + "	" + interArrivalTime + "	"
				+ EventHandler.getTime() + "\n");
	}
}
