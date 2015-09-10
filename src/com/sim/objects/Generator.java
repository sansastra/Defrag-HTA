package com.sim.objects;

import java.util.ArrayList;
import java.util.List;

import com.graph.elements.vertex.VertexElement;
import com.launcher.Launcher;
import com.launcher.NetworkParameters;
import com.rng.distribution.Distribution;
import com.rng.distribution.type.*;
import com.sim.event.CircuitRequestEvent;
import com.sim.event.eventhandler.EventHandler;

/**
 * Class to represent a generator of circuits in the network
 *
 * @author Fran
 */
public class Generator {

    private Distribution requestDistribution;
    private Distribution holdingTimeDistribution;
    private DiscreteUniform destinationsDistribution;
    private DiscreteUniform portDistribution;
    private ContinuousUniform knownUnknownDistribution;
    private List<Flow> setOfFlows;
    private VertexElement vertex;
    private List<VertexElement> listOfDestinations;
    private double decisionProbForKnownOrUnknown;

    public Generator(VertexElement vertex,
                     List<VertexElement> listOfDestinations, int numberOfPorts, double decisionProbForKnownOrUnknown) {

        this.vertex = vertex;
        this.setOfFlows = new ArrayList<>();
        this.listOfDestinations = new ArrayList<>();
        this.listOfDestinations = listOfDestinations;
        this.decisionProbForKnownOrUnknown = decisionProbForKnownOrUnknown;
        this.requestDistribution = new ExponentialDistribution(1.0 / Launcher.getLambda(), Launcher.getSeed());

        switch (Launcher.getDistributionTypeForHT()) {
            case "Exp":
                this.holdingTimeDistribution = new ExponentialDistribution(Launcher.getMeanHoldingTime(), Launcher.getSeed());
                break;
            case "HyperExp":
                this.holdingTimeDistribution = new HyperExponentialDistribution(Launcher.getMeanHoldingTime(), Launcher.calculateStdForHyperExp(Launcher.getMeanHoldingTime(), 4), Launcher.getSeed());
                break;
            case "LogN":
                this.holdingTimeDistribution = new LogNormalDistribution(Launcher.getMeanHoldingTime(), Launcher.calculateStDForLogNormal(2), Launcher.getSeed());
                break;
        }

        /** Set distribution to choose the incoming port */
        this.portDistribution = new DiscreteUniform(0, numberOfPorts - 1);

        /** Set the number of possible flows between the source and destinations */
        for (VertexElement dstNode : listOfDestinations)
            setOfFlows.add(new Flow(vertex, dstNode, Launcher.getNumberOfPortTypes()));

        /** Set the distribution to select the destination for each request */
        this.destinationsDistribution = new DiscreteUniform(0,
                setOfFlows.size() - 1);

        /** Set distribution to choose if ht is known or unknown */
        this.knownUnknownDistribution = new ContinuousUniform(0, 1.0);

    }

    public void getUp() {
        EventHandler.addEvent(new CircuitRequestEvent(this,
                requestDistribution.execute(), NetworkParameters.getListOfPorts().get(portDistribution.execute().intValue())));
    }

    public boolean isKnownOrUnknown() {
        if (knownUnknownDistribution.execute() < decisionProbForKnownOrUnknown)
            return true;
        return false;
    }

    public VertexElement getVertex() {
        return vertex;
    }

    public List<Flow> getSetOfFlows() {
        return setOfFlows;
    }

    public DiscreteUniform getUniform() {
        return destinationsDistribution;
    }

    public DiscreteUniform getPortDistribution() {
        return portDistribution;
    }

    public Distribution getRequestDistribution() {
        return requestDistribution;
    }

    public Distribution getHoldingTimeDistribution() {
        return holdingTimeDistribution;
    }

}
