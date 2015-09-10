package com.launcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.algorithm.SelectionLPAlgorithm;
import com.filemanager.ImportTopologyFromSNDFile;
import com.filemanager.ReadFile;
import com.graph.elements.edge.EdgeElement;
import com.graph.elements.vertex.VertexElement;
import com.graph.graphcontroller.Gcontroller;
import com.graph.graphcontroller.impl.GcontrollerImpl;
import com.graph.path.PathElement;
import com.graph.path.algorithms.PathComputationAlgorithm;
import com.graph.path.algorithms.constraints.Constraint;
import com.graph.path.algorithms.constraints.impl.SimplePathComputationConstraint;
import com.graph.path.algorithms.impl.SimplePathComputationAlgorithm;
import com.graph.path.pathelementimpl.PathElementImpl;
import com.filemanager.Results;
import com.rng.distribution.Distribution;
import com.sim.objects.Flow;
import com.sim.objects.LightPath;
import com.sim.objects.Link;
import com.sim.objects.Generator;
import com.sim.objects.Port;

/**
 * Class to load the network topology and set up the parameters for the
 * simulation.
 *
 * @author Fran Carpio
 */
public class NetworkParameters {

    /**
     * Network graph
     */
    private Gcontroller graph;
    private static List<Generator> listOfGenerators;
    private static List<Link> listOfLinks;
    private static List<Port> listOfPorts;
    private static SelectionLPAlgorithm algorithm;
    private static double maxCarrierBW;
    private static int numberOfCarriersPerLink;
    private static double decisionProbForKnownOrUnknown;
    private static List<PathElement> listOfPaths;
    private static List<String> listOfDemands;
    private static boolean withGuardBands;

    /**
     * Constructor class
     *
     * @param networkTopologyFileName
     */
    public NetworkParameters(String networkTopologyFileName) {

        graph = new GcontrollerImpl();
        ImportTopologyFromSNDFile importer = new ImportTopologyFromSNDFile();
        importer.importTopology(graph, networkTopologyFileName);
        listOfDemands = importer.getListOfDemands();
        listOfGenerators = new ArrayList<>();
        listOfLinks = new ArrayList<>();
        listOfPorts = new ArrayList<>();
        listOfPaths = new ArrayList<>();
        new Results();
    }

    /**
     * Set the incoming ports for the generator
     */
    public void setPorts(int number, int portType, double bw) {
        for (int i = 0; i < number; i++)
            listOfPorts.add(new Port(portType, bw));
    }


    public void setGenerators() {

        VertexElement src = null;
        List<VertexElement> setOfDestinations = new ArrayList<>();
        String srcNodeString = null;
        for (String demand : listOfDemands) {
            String[] nodes = demand.split("-");

            if (srcNodeString != null)
                if (!srcNodeString.equals(nodes[0])) {
                    listOfGenerators.add(new Generator(src, setOfDestinations, listOfPorts.size(),
                            decisionProbForKnownOrUnknown));
                    setOfDestinations = new ArrayList<>();
                }

            for (VertexElement vertex : graph.getVertexSet()) {
                if (vertex.getVertexID().equals(nodes[0])) {
                    src = vertex;
                    srcNodeString = vertex.getVertexID();
                }
                if (vertex.getVertexID().equals(nodes[1]))
                    setOfDestinations.add(vertex);
            }
        }
        //add the last generator
        listOfGenerators.add(new Generator(src, setOfDestinations, listOfPorts.size(),
                decisionProbForKnownOrUnknown));
    }

    /**
     * Function to compute the shortest paths
     */
    public void computeShortestPath() {

        PathComputationAlgorithm pathComputationAlgorithm = new SimplePathComputationAlgorithm();
        Constraint constraint;

        for (Generator generator : listOfGenerators) {
            for (Flow f : generator.getSetOfFlows()) {
                constraint = new SimplePathComputationConstraint(
                        graph.getVertex(generator.getVertex().getVertexID()),
                        graph.getVertex(f.getDstNode().getVertexID()));
                PathElement p = pathComputationAlgorithm.computePath(graph,
                        constraint);
                listOfPaths.add(p);
                System.out.println(p.getVertexSequence());
            }
        }
    }

    public void readPathsFromFile(String pathsFile) throws IOException {

        new ReadFile(pathsFile);
        String path = ReadFile.readLine();
//        new ReadFromFile(pathsFile);
        List<String> paths = new ArrayList<>();
//        String path = ReadFromFile.readLine();
        while (path != null) {
            paths.add(path);
//            path = ReadFromFile.readLine();
            path = ReadFile.readLine();
        }
        setPathManually(paths);
    }

    /**
     * Function to specify the paths manually
     *
     * @param paths
     */
    public void setPathManually(List<String> paths) {

        List<String> listOfNodes;
        ArrayList<EdgeElement> listOfIntermediateLinks;

        for (String path : paths) {
            listOfNodes = new ArrayList<>();
            listOfIntermediateLinks = new ArrayList<>();
            String[] nodes = path.split("-");
            for (String node : nodes)
                listOfNodes.add(node);


            for (int i = 0; i < listOfNodes.size() - 1; i++) {
                for (EdgeElement link : graph.getEdgeSet()) {
                    if (link.getSourceVertex().getVertexID()
                            .equals(listOfNodes.get(i))
                            && link.getDestinationVertex().getVertexID()
                            .equals(listOfNodes.get(i + 1)))
                        listOfIntermediateLinks.add(link);
                }
            }

            PathElement pathElement = new PathElementImpl(graph,
                    graph.getVertex(listOfNodes.get(0)),
                    graph.getVertex(listOfNodes.get(listOfNodes.size() - 1)),
                    listOfIntermediateLinks);

            for (Generator generator : listOfGenerators) {
                if (!pathElement.getSource().getVertexID()
                        .equals(generator.getVertex().getVertexID()))
                    continue;
                for (Flow f : generator.getSetOfFlows()) {
                    if (!pathElement.getDestination().getVertexID()
                            .equals(f.getDstNode().getVertexID()))
                        continue;
                    listOfPaths.add(pathElement);
                    System.out.println(pathElement.getVertexSequence());
                    break;
                }
            }
        }

    }

    /**
     * Function to initialize the nodes and links
     */
    public void setCarriers() {

        for (EdgeElement edge : graph.getEdgeSet())
            listOfLinks.add(new Link(edge.getSourceVertex(), edge
                    .getDestinationVertex(), numberOfCarriersPerLink,
                    maxCarrierBW));

        this.setLightPaths();
    }

    public void setLightPaths() {
        for (Generator generator : listOfGenerators)
            for (Flow f : generator.getSetOfFlows()) {
                int z = 0;
                for (PathElement pathElement : listOfPaths) {
                    if (!pathElement.isConnected(f.getSrcNode(), f.getDstNode()))
                        continue;

                    List<Link> pathLinks = new ArrayList<>();
                    for (int i = 0; i < pathElement.getTraversedVertices().size() - 1; i++) {
                        VertexElement linkSrcNode = pathElement.getTraversedVertices().get(i);
                        VertexElement linkDstNode = pathElement.getTraversedVertices().get(i + 1);
                        for (Link link : listOfLinks)
                            if (linkSrcNode.equals(link.getSrcNode())
                                    && linkDstNode.equals(link.getDstNode())) {
                                pathLinks.add(link);
                                break;
                            }
                    }
                    for (int i = 0; i < numberOfCarriersPerLink; i++)
                        f.getSetOfLightPaths().add(new LightPath(i + (z * numberOfCarriersPerLink), pathElement, pathLinks));
                    z++;
                }
            }
    }

    public void getUpGenerators() {
        for (Generator src : listOfGenerators)
            src.getUp();
    }

    public static void setDecisionProbForKnownOrUnknown(
            double decisionProbForKnownOrUnknown) {
        NetworkParameters.decisionProbForKnownOrUnknown = decisionProbForKnownOrUnknown;
    }

    public static List<Link> getListOfLinks() {
        return listOfLinks;
    }

    public static SelectionLPAlgorithm getAlgorithm() {
        return algorithm;
    }

    public static void setAlgorithm(SelectionLPAlgorithm algorithm) {
        NetworkParameters.algorithm = algorithm;
    }

    public static double getMaxCarrierBW() {
        return maxCarrierBW;
    }

    public void setMaxCarrierBW(double maxCarrierBW) {
        NetworkParameters.maxCarrierBW = maxCarrierBW;
    }

    public static void setNumberOfCarriersPerLink(int _numberOfCarriersPerLink) {
        numberOfCarriersPerLink = _numberOfCarriersPerLink;
    }

    public static List<Port> getListOfPorts() {
        return listOfPorts;
    }

    public static boolean isWithGuardBands() {
        return withGuardBands;
    }

    public static void setWithGuardBands(boolean withGuardBands) {
        NetworkParameters.withGuardBands = withGuardBands;
    }
}
