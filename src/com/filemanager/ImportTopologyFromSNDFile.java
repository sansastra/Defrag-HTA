package com.filemanager;

import com.graph.elements.edge.EdgeElement;
import com.graph.elements.edge.params.EdgeParams;
import com.graph.elements.edge.params.impl.BasicEdgeParams;
import com.graph.elements.vertex.VertexElement;
import com.graph.graphcontroller.Gcontroller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportTopologyFromSNDFile extends com.graph.topology.importers.ImportTopology{

    private static final String classIdentifier = "SNDLibImportTopology";

    private static List<String> listOfDemands;

    public static List<String> getListOfDemands() {
        return listOfDemands;
    }

    @Override
    public void importTopology(Gcontroller graph, String filename) {

        listOfDemands = new ArrayList<String>();
        // add vertices to the graph
        //            BufferedReader reader = new BufferedReader(new FileReader(filename));
        new ReadFile(filename);

        String temp;
        VertexElement vertex1, vertex2;

        // Read till we get to Node definition)
        while ((temp = ReadFile.readLine()).trim().compareTo("NODES (") != 0) {
        }

        // read till we reach the end of node definitions
        while ((temp = ReadFile.readLine()) != null) {
            temp = temp.trim();
            // System.out.println(temp);
            // if (temp.length()==1){
            // break;
            // }
            if (temp.trim().compareTo(")") == 0) {
                break;
            }

            Pattern p;
            Matcher m;

            String sourceID = "";
            p = Pattern.compile("[a-zA-Z0-9\\.]+");
            m = p.matcher(temp);
            if (m.find()) {
                sourceID = m.group(0);
            }

            // p = Pattern.compile("[0-9\\.]+");
            // m = p.matcher(temp);
            double[] temp1 = new double[2];
            int count = 0;
            while (m.find()) {
                temp1[count] = Double.parseDouble(m.group(0));
                count++;
                if (count == 2)
                    break;
            }

            vertex1 = new VertexElement(sourceID, graph, temp1[0], temp1[1]);
            graph.addVertex(vertex1);
            // System.out.println("Vertex Added: VertexID=" +
            // vertex1.getVertexID()+ ", X=" + vertex1.getXCoord() + ", Y="
            // + vertex1.getYCoord());
        }

        // Read till we get to Edge definition)
        while ((temp = ReadFile.readLine()).trim().compareTo("LINKS (") != 0) {
        }

        // read till we reach the end of the edge definition
        while ((temp = ReadFile.readLine()) != null) {
            temp = temp.trim();
            if (temp.length() == 1) {
                break;
            }

            Pattern p;
            Matcher m;

            p = Pattern.compile("[a-zA-Z0-9\\.]+");
            m = p.matcher(temp);
            String[] temp1 = new String[3];
            int count = 0;
            while (m.find()) {
                temp1[count] = m.group(0);
                count++;
                if (count == 3)
                    break;
            }

            vertex1 = graph.getVertex(temp1[1]);
            vertex2 = graph.getVertex(temp1[2]);

            EdgeElement edge1 = new EdgeElement(temp1[0]+".1", vertex1, vertex2,
                    graph);
            EdgeElement edge2 = new EdgeElement(temp1[0]+".2", vertex2, vertex1,
                    graph);

            // System.out.println("Edge Added: Edge ID=" + edge.getEdgeID()
            // + ", sourceID=" + vertex1.getVertexID() +
            // ", destinationID = " + vertex2.getVertexID());
            // Compute delay using X and Y Coords from Vertices
            double distance = Math.sqrt(Math.pow(vertex1.getXCoord()
                    - vertex2.getXCoord(), 2)
                    + Math
                    .pow(vertex1.getYCoord() - vertex2.getYCoord(),
                            2));

            double delay = distance / 29.9792458; // (in ms)
            // @TODO import parameters for link weight and delay from brite
            EdgeParams params1 = new BasicEdgeParams(edge1, delay, 1, 40);
            EdgeParams params2 = new BasicEdgeParams(edge2, delay, 1, 40);
            edge1.setEdgeParams(params1);
            edge2.setEdgeParams(params2);
            graph.addEdge(edge1);
            graph.addEdge(edge2);
        }

        // Read till we get to Edge definition)
        while ((temp = ReadFile.readLine()).trim().compareTo("DEMANDS (") != 0) {
        }
        // read till we reach the end of the demands definition
        while ((temp = ReadFile.readLine()) != null) {
            temp = temp.trim();
            if (temp.length() == 1) {
                break;
            }
            Pattern p;
            Matcher m;

            p = Pattern.compile("[a-zA-Z0-9\\.]+");
            m = p.matcher(temp);
            String[] temp1 = new String[3];
            int count = 0;
            while (m.find()) {
                temp1[count] = m.group(0);
                count++;
                if (count == 3)
                    break;
            }
            listOfDemands.add(temp1[1] + "-" + temp1[2]);
        }

//            reader.close();

    }

    @Override
    public void importTopologyFromString(Gcontroller graph, String[] topology) {

    }

}
