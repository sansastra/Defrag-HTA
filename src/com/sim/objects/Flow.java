package com.sim.objects;

import java.util.ArrayList;
import java.util.List;

import com.graph.elements.vertex.VertexElement;
import com.graph.path.PathElement;
import com.launcher.NetworkParameters;

/**
 * Class to represent a Flow in the network
 *
 * @author Fran
 */
public class Flow {

    private VertexElement srcNode;
    private VertexElement dstNode;
    private List<Integer> blockingCounters;
    private List<Integer> knownOrNotblockingCounters;
    private List<Integer> flowRequestCounters;
    private List<LightPath> setOfLightPaths;

    public Flow(VertexElement srcNode, VertexElement dstNode, int numberOfPorts) {
        this.srcNode = srcNode;
        this.dstNode = dstNode;
        this.setOfLightPaths = new ArrayList<>();
        this.blockingCounters = new ArrayList<>();
        this.knownOrNotblockingCounters = new ArrayList<>();
        this.flowRequestCounters = new ArrayList<>();
        for (int i = 0; i < numberOfPorts; i++) {
            blockingCounters.add(0);
            knownOrNotblockingCounters.add(0);
            flowRequestCounters.add(0);
        }
    }

    public List<LightPath> selectBestCandidateLightPathsWithGuardBands(
            List<LightPath> setOfCandidateLightPaths, double requiredBW,
            double holdingTime) {

        List<LightPath> selectedLightPaths = new ArrayList<LightPath>();

        /** Select the best candidate lightPath with GuardBands */
        if (requiredBW <= NetworkParameters.getMaxCarrierBW()) {
            if (!setOfCandidateLightPaths.isEmpty()) {
                LightPath selectedLP = NetworkParameters.getAlgorithm().execute(
                        setOfCandidateLightPaths, holdingTime);
                /** Check if the selected lightpath is traversed by this flow */
                Boolean isTraversed = selectedLP.isTraversedBy(this);
                /** If totally free, we add the next carrier to the connection */
                if (!isTraversed) {
                    /** We add the selected carrier and the next one */
                    selectedLightPaths.add(selectedLP);
                    PathElement pathElement = selectedLP.getPath();
                    for (int i = 0; i < setOfCandidateLightPaths.size() - 1; i++) {
                        if (selectedLP.equals(setOfCandidateLightPaths.get(i)))
                            if (setOfCandidateLightPaths.get(i + 1).getPath()
                                    .equals(pathElement))
                                setOfCandidateLightPaths.get(i + 1)
                                        .setForGuardBand(true);
                        selectedLightPaths.add(setOfCandidateLightPaths
                                .get(i + 1));
                        break;

                    }
                }/** If not we add only the selected carrier */
                else
                    selectedLightPaths.add(selectedLP);
            }
        } else {
            PathElement pathElement = null;
            int numberOfCarriersToSelect = (int) (requiredBW / NetworkParameters
                    .getMaxCarrierBW());
            int guardBand = 1;
            numberOfCarriersToSelect += guardBand;
            /**
             * Check if there are continuity in the carriers to select plus the
             * guard band
             */
            if (setOfCandidateLightPaths.size() >= numberOfCarriersToSelect) {
                int counterPath = 0;
                for (LightPath lightPath : setOfCandidateLightPaths) {
                    counterPath = 0;
                    for (LightPath neighbourlightPath : setOfCandidateLightPaths)
                        if (lightPath.getPath().equals(
                                neighbourlightPath.getPath()))
                            counterPath++;
                    if (counterPath >= numberOfCarriersToSelect) {
                        pathElement = lightPath.getPath();
                        break;
                    }
                }

                List<LightPath> setOfLPsToCheck = new ArrayList<LightPath>();
                for (int j = 0; j < setOfCandidateLightPaths.size(); j++) {
                    if (pathElement == null)
                        break;
                    if (!setOfCandidateLightPaths.get(j).getPath()
                            .equals(pathElement))
                        continue;
                    setOfLPsToCheck.add(setOfCandidateLightPaths.get(j));
                    if (setOfLPsToCheck.size() == numberOfCarriersToSelect) {
                        Boolean isContinuty = isThereContinuity(setOfLPsToCheck);
                        if (isContinuty) {
                            for (int k = 0; k < setOfLPsToCheck.size(); k++) {
                                if (k == setOfLPsToCheck.size() - 1)
                                    setOfLPsToCheck.get(k)
                                            .setForGuardBand(true);
                                selectedLightPaths.add(setOfLPsToCheck.get(k));
                            }
                            break;
                        } else {
                            setOfLPsToCheck = new ArrayList<LightPath>();
                            j = j - (numberOfCarriersToSelect - 1);
                        }
                    }
                }
            }

        }

        return selectedLightPaths;
    }

    public List<LightPath> selectBestCandidateLightPathsWithGuardBandsIMPROVED(
            List<LightPath> setOfCandidateLightPaths, double requiredBW,
            double holdingTime) {

        List<LightPath> selectedLightPaths = new ArrayList<LightPath>();

        /** Select the best candidate lightPath with GuardBands */
        if (requiredBW <= NetworkParameters.getMaxCarrierBW()) {
            if (!setOfCandidateLightPaths.isEmpty()) {
                LightPath selectedLP = NetworkParameters.getAlgorithm().execute(
                        setOfCandidateLightPaths, holdingTime);
                /** Check if the selected lightpath is traversed by this flow */
                Boolean isTraversed = selectedLP.isTraversedBy(this);
                /** If totally free, we add the next carrier to the connection */
                if (!isTraversed) {
                    /** We add the selected carrier and the next one */
                    selectedLightPaths.add(selectedLP);
                    PathElement pathElement = selectedLP.getPath();
                    for (int i = 0; i < setOfCandidateLightPaths.size() - 1; i++) {
                        if (selectedLP.equals(setOfCandidateLightPaths.get(i)))
                            if (setOfCandidateLightPaths.get(i + 1).getPath()
                                    .equals(pathElement))
                                setOfCandidateLightPaths.get(i + 1)
                                        .setForGuardBand(true);
                        selectedLightPaths.add(setOfCandidateLightPaths
                                .get(i + 1));
                        break;

                    }
                }/** If not we add only the selected carrier */
                else
                    selectedLightPaths.add(selectedLP);
            }
        } else {
            PathElement pathElement = null;
            int numberOfCarriersToSelect = (int) (requiredBW / NetworkParameters
                    .getMaxCarrierBW());
            int guardBand = 1;
            numberOfCarriersToSelect += guardBand;
            /**
             * Check if there are continuity in the carriers to select plus the
             * guard band
             */
            if (setOfCandidateLightPaths.size() >= numberOfCarriersToSelect) {

                int counterPath = 0;
                List<PathElement> pathElements = new ArrayList<>();

                for (LightPath lightPath : setOfCandidateLightPaths)
                    pathElements.add(lightPath.getPath());


                for (int i = 1; i < pathElements.size(); i++) {
                    if (pathElements.get(i).equals(pathElements.get(i - 1))) {
                        pathElements.remove(i);
                        i--;
                    }
                }

                for (PathElement path : pathElements) {
                    for (LightPath lightPath : setOfCandidateLightPaths) {
                        if (!lightPath.getPath().equals(path))
                            continue;
                        counterPath = 0;
                        for (LightPath neighbourlightPath : setOfCandidateLightPaths)
                            if (lightPath.getPath().equals(
                                    neighbourlightPath.getPath()))
                                counterPath++;
                        if (counterPath >= numberOfCarriersToSelect) {
                            pathElement = lightPath.getPath();
                            break;
                        }
                    }

                    Boolean isContinuity = false;
                    List<LightPath> setOfLPsToCheck = new ArrayList<LightPath>();
                    for (int j = 0; j < setOfCandidateLightPaths.size(); j++) {
                        if (pathElement == null)
                            break;
                        if (!setOfCandidateLightPaths.get(j).getPath()
                                .equals(pathElement))
                            continue;
                        setOfLPsToCheck.add(setOfCandidateLightPaths.get(j));
                        if (setOfLPsToCheck.size() == numberOfCarriersToSelect) {
                            isContinuity = isThereContinuity(setOfLPsToCheck);
                            if (isContinuity) {
                                for (int k = 0; k < setOfLPsToCheck.size(); k++) {
                                    if (k == setOfLPsToCheck.size() - 1)
                                        setOfLPsToCheck.get(k)
                                                .setForGuardBand(true);
                                    selectedLightPaths.add(setOfLPsToCheck.get(k));
                                }
                                break;
                            } else {
                                setOfLPsToCheck = new ArrayList<LightPath>();
                                j = j - (numberOfCarriersToSelect - 1);
                            }
                        }
                    }
                    if(isContinuity)
                        break;
                }
            }

        }

        return selectedLightPaths;
    }

    public List<LightPath> selectBestCandidateLightPathsWithoutGuardBands(
            List<LightPath> setOfCandidateLightPaths, double requiredBW,
            double holdingTime, boolean isNotKnown) {

        List<LightPath> selectedLightPaths = new ArrayList<>();

        /** Select the best candidate lightPath with GuardsBands */
        if (requiredBW <= NetworkParameters.getMaxCarrierBW()) {
            if (!setOfCandidateLightPaths.isEmpty())
                selectedLightPaths.add(NetworkParameters.getAlgorithm().execute(
                        setOfCandidateLightPaths, holdingTime));
        } else {
//            PathElement pathElement = null;
            int numberOfCarriersToSelect = (int) (requiredBW / NetworkParameters
                    .getMaxCarrierBW());

            /**
             * Check if there are continuity in the carriers to select plus the
             * guard band
             */
            if (setOfCandidateLightPaths.size() >= numberOfCarriersToSelect) {

//                int counterPath = 0;
                List<PathElement> pathElements = new ArrayList<>();

                for (LightPath lightPath : setOfCandidateLightPaths)
                    pathElements.add(lightPath.getPath());


                for (int i = 1; i < pathElements.size(); i++) {
                    if (pathElements.get(i).equals(pathElements.get(i - 1))) {
                        pathElements.remove(i);
                        i--;
                    }
                }

                for (PathElement path : pathElements) {
//                    for (LightPath lightPath : setOfCandidateLightPaths) {
//                        if (!lightPath.getPath().equals(path))
//                            continue;
//                        counterPath = 0;
//                        for (LightPath neighbourlightPath : setOfCandidateLightPaths)
//                            if (lightPath.getPath().equals(
//                                    neighbourlightPath.getPath()))
//                                counterPath++;
//                        if (counterPath >= numberOfCarriersToSelect) {
//                            pathElement = lightPath.getPath();
//                            break;
//                        }
//                    }

                    Boolean isContinuity = false;
                    List<LightPath> setOfLPsToCheck = new ArrayList<LightPath>();
                    for (int j = 0; j < setOfCandidateLightPaths.size(); j++) {
//                        if (pathElement == null)
//                            break;
//                        if (!setOfCandidateLightPaths.get(j).getPath()
//                                .equals(pathElement))
                        if (!setOfCandidateLightPaths.get(j).getPath()
                                .equals(path))
                            continue;
                        setOfLPsToCheck.add(setOfCandidateLightPaths.get(j));
                        if (setOfLPsToCheck.size() == numberOfCarriersToSelect) {
                            isContinuity = isThereContinuity(setOfLPsToCheck);
                            if (isContinuity) {
                                for (int k = 0; k < setOfLPsToCheck.size(); k++) {
                                    selectedLightPaths.add(setOfLPsToCheck.get(k));
                                }
                                break;
                            } else {
                                setOfLPsToCheck = new ArrayList<LightPath>();
                                j = j - (numberOfCarriersToSelect - 1);
                            }
                        }
                    }
                    if(isContinuity)
                        break;
                }
            }

        }

        return selectedLightPaths;
    }

    public List<LightPath> selectLightPath(double requiredBW,
                                           double holdingTime, boolean isNotKnown, boolean withGuardBand) {

        boolean isNotEnoughBW = false;

        List<LightPath> setOfCandidateLightPaths = new ArrayList<LightPath>();

        /** Look for a free lightPath in the flow */
        for (LightPath lp : setOfLightPaths) {
            isNotEnoughBW = false;

            /**
             * Check if the lightpath is being traversed by this flow, by
             * another flow or is free
             */
            // Flow traversingFlow = lp.getTraversingFlowByLightPath(this);
            Boolean isTraversed = lp.isTraversedBy(this);

            /** New SpectrumPath: If the lightpath is totally free */
            // if (traversingFlow == null)
            /**
             * If lightpath is traversed by another flow, check next lightpath
             */
            if (isTraversed == null)
                continue;

            if (!isTraversed && !lp.isForGuardBand())
                setOfCandidateLightPaths.add(lp);
            /**
             * Grooming: If the lightpath is being used by this flow, check
             * remaining BW
             */
                // else if (traversingFlow.equals(this)
            else if (isTraversed && requiredBW < NetworkParameters.getMaxCarrierBW()) {
                if (lp.isEnoughFreeCapacity(requiredBW))
                    setOfCandidateLightPaths.add(lp);
                else {
                    isNotEnoughBW = true;
                    continue;
                }
            }

        }

        List<LightPath> selectedLightPaths = new ArrayList<>();
        if (!withGuardBand) {
            selectedLightPaths = this
                    .selectBestCandidateLightPathsWithoutGuardBands(
                            setOfCandidateLightPaths, requiredBW, holdingTime,
                            isNotKnown);
        } else {
            selectedLightPaths = this
                    .selectBestCandidateLightPathsWithGuardBandsIMPROVED(
                            setOfCandidateLightPaths, requiredBW, holdingTime);
        }

//		if (selectedLightPaths.size() == 0 && isNotEnoughBW)
//			System.out
//					.println(srcNode.getVertexID()
//							+ "-->"
//							+ dstNode.getVertexID()
//							+ ": Connection blocked (no free carriers and no grooming))--> "
//							+ EventHandler.getTime());
//		else if (selectedLightPaths.size() == 0)
//			System.out.println(srcNode.getVertexID() + "-->"
//					+ dstNode.getVertexID()
//					+ ": Connection blocked (no free carriers)--> "
//					+ EventHandler.getTime());

        return selectedLightPaths;
    }

    public boolean isThereContinuity(List<LightPath> lightPaths) {

        boolean continuity = false;
        int counter = 0;

        for (int i = 0; i < lightPaths.size() - 1; i++)
            if ((lightPaths.get(i).getCarrierID() + 1) == lightPaths.get(i + 1)
                    .getCarrierID())
                counter++;
        if (counter == lightPaths.size() - 1)
            continuity = true;

        return continuity;

    }

    public VertexElement getSrcNode() {
        return srcNode;
    }

    public VertexElement getDstNode() {
        return dstNode;
    }

    public void increaseBlockingCounter(int portType, boolean isNotKnown) {
        blockingCounters.set(portType, blockingCounters.get(portType) + 1);
        if (isNotKnown)
            knownOrNotblockingCounters.set(portType,
                    knownOrNotblockingCounters.get(portType) + 1);
    }

    public void increaseFlowRequestCounter(int portType) {
        flowRequestCounters
                .set(portType, flowRequestCounters.get(portType) + 1);
    }

    public List<Integer> getBlockingCounters() {
        return blockingCounters;
    }

    public List<Integer> getKnownOrNotblockingCounters() {
        return knownOrNotblockingCounters;
    }

    public List<Integer> getFlowRequestCounters() {
        return flowRequestCounters;
    }

    public List<LightPath> getSetOfLightPaths() {
        return setOfLightPaths;
    }
}
