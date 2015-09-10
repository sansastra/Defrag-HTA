package com.launcher;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.algorithm.type.MAL_Method;
import com.algorithm.type.MAC_Method;
import com.algorithm.type.NoAlgorithm;
import com.filemanager.ReadFile;
import com.sim.event.eventhandler.EventHandler;

public class Launcher {

    private static String networkFile;
    private static String pathsFile;
    private static double simulationTime;
    private static int numberOfTotalRequests;
    private static int numberOfGeneratorsPerNode;
    private static double initLambda;
    private static double lambda;
    private static int numberOfRuns;
    private static int multiFactorPerRun;
    private static String distributionTypeForHT;
    private static double meanHoldingTime;
    private static int numberOfPortTypes;
    private static String[] bandwidths;
    private static String[] numberOfPortsPerPortType;
    private static String algorithm;
    private static boolean withGuardBands;
    private static int numberOfCarriersPerLink;
    private static double carrierBandWidth;
    private static int _runNumber;
    private static double probabilityOfUnknownCon;
    private static double cov;
    private static int streamIndex;
    private static Date date;


    public static void main(String[] args) throws IOException {

        date = new Date();
        streamIndex = -1;
        readConfigFile("config.txt");
        startSimulation();
    }

    public static void startSimulation() {
        for (int runNumber = 0; runNumber < numberOfRuns; runNumber++) {
            lambda = initLambda + runNumber * multiFactorPerRun;
            _runNumber = runNumber;
            runSimulation();
        }
    }

    public static void runSimulation() {

        /** Input network from a SNDLib file */
        NetworkParameters parameters = new NetworkParameters(networkFile);

        /** Initialize the event handler specifying the simulation time */
        EventHandler.initEventHandler(simulationTime);

        for (int i = 0; i < numberOfPortTypes; i++)
            parameters.setPorts(Integer.parseInt(numberOfPortsPerPortType[i]), i, Double.parseDouble(bandwidths[i]));

        parameters.setDecisionProbForKnownOrUnknown(probabilityOfUnknownCon);

        parameters.setGenerators();

        switch (algorithm) {
            case "MAC":
                parameters.setAlgorithm(new MAC_Method());
                break;
            case "MAL":
                parameters.setAlgorithm(new MAL_Method());
                break;
            case "FF":
                parameters.setAlgorithm(new NoAlgorithm());
                break;
            default:
                System.out.println("Wrong Algorithm");
                System.exit(0);
        }

        parameters.setWithGuardBands(withGuardBands);

        try {
            parameters.readPathsFromFile(pathsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * specify the number of carriers per link and carrier bandwidth
         */
        parameters.setNumberOfCarriersPerLink(numberOfCarriersPerLink);
        parameters.setMaxCarrierBW(carrierBandWidth);
        parameters.setCarriers();

        /**
         * Get up generators
         */
        parameters.getUpGenerators();

        /** Run the simulation */
        EventHandler.runEventHandler();
    }

//    public static void readConfigFile(String configFile) throws IOException {
    public static void readConfigFile(String pathFile) throws IOException {

        new ReadFile(pathFile);
        String line = ReadFile.readLine();
        int lineCounter = 0;
        while (line != null) {
            if (!line.startsWith("#")) {
                switch (lineCounter) {
                    case 0:
                        networkFile = line;
                        break;
                    case 1:
                        pathsFile = line;
                        break;
                    case 2:
                        simulationTime = Double.parseDouble(line);
                        break;
                    case 3:
                        numberOfTotalRequests = Integer.parseInt(line);
                        break;
                    case 4:
                        numberOfGeneratorsPerNode = Integer.parseInt(line);
                        break;
                    case 5:
                        initLambda = Double.parseDouble(line);
                        break;
                    case 6:
                        numberOfRuns = Integer.parseInt(line);
                        break;
                    case 7:
                        multiFactorPerRun = Integer.parseInt(line);
                        break;
                    case 8:
                        distributionTypeForHT = line;
                        break;
                    case 9:
                        meanHoldingTime = Double.parseDouble(line);
                        break;
                    case 10:
                        numberOfPortTypes = Integer.parseInt(line);
                        break;
                    case 11:
                        bandwidths = line.split("-");
                        break;
                    case 12:
                        numberOfPortsPerPortType = line.split("-");
                        break;
                    case 13:
                        algorithm = line;
                        break;
                    case 14:
                        if (line.equals("true"))
                            withGuardBands = true;
                        break;
                    case 15:
                        numberOfCarriersPerLink = Integer.parseInt(line);
                        break;
                    case 16:
                        carrierBandWidth = Double.parseDouble(line);
                        break;
                    case 17:
                        probabilityOfUnknownCon = Double.parseDouble(line);
                        break;
                    case 18:
                        cov = Double.parseDouble(line);
                        break;
                }
                lineCounter++;
            }
            line= ReadFile.readLine();
        }
    }

    public static double calculateStdForHyperExp(double meanHT, double a) {
        double std;

        std = meanHT
                * Math.sqrt((4 * (1 + Math.pow(a, 2)) / Math.pow(1 + a, 2)) - 1);

        return std;

    }

    public static double calculateStDForLogNormal(double cov) {
        double std;

        std = cov * ((1 / meanHoldingTime) / 1000.0);

        return std;

    }

    public static String getDistributionTypeForHT() {
        return distributionTypeForHT;
    }

    public static int getNumberOfTotalRequests() {
        return numberOfTotalRequests;
    }

    public static int get_runNumber() {
        return _runNumber;
    }

    public static double getMeanHoldingTime() {
        return meanHoldingTime;
    }

    public static int getSeed(){
        streamIndex++;
        if(streamIndex==10)
            streamIndex = 0;
        return streamIndex;
    }

    public static double getLambda() {
        return lambda;
    }

    public static Date getDate() {
        return date;
    }

    public static int getNumberOfPortTypes() {
        return numberOfPortTypes;
    }

    public static String getAlgorithm() {
        return algorithm;
    }

    public static boolean isWithGuardBands() {
        return withGuardBands;
    }

    public static int getNumberOfCarriersPerLink() {
        return numberOfCarriersPerLink;
    }

    public static double getCov() {
        return cov;
    }
}
