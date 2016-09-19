/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vehicleroutingproblem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import Models.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Cesar Bonilla
 */
public class VehicleRoutingProblem {

    public static int CAR_LIMIT = 0;
    public static int FLEET_SIZE = 0;
    public static double[][] COST_MATRIX;
    public static Node[] NODES;
    public static ArrayList<Route> routes;
    public static Node DEPOSIT_NODE;
    //private static Point[] V;

    public static void main(String[] args) throws Exception {
        //Load # of Trucks, Deposit and Clients
        loadFile();
        //Generate the Cost Matrix
        COST_MATRIX = calculateCostMatrix();
        //Solve with Sweep Algorithm
        String result = SolveVRPbySweep();
        System.out.println(result);

    }

    public static void loadFile() throws Exception {
        StringBuilder sb = new StringBuilder("");

        try {
            String inputPath = System.getProperty("user.dir");
            inputPath += "\\Data\\entrada.txt";
            BufferedReader br = new BufferedReader(new FileReader(inputPath));

            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VehicleRoutingProblem.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(VehicleRoutingProblem.class.getName()).log(Level.SEVERE, null, ex);
        }

        String[] fileLines = sb.toString().split(System.lineSeparator());

        if (fileLines.length < 3) {
            throw new Exception("Input file has not enough lines");
        }

        //Number of trucks available
        FLEET_SIZE = Integer.valueOf(fileLines[0].isEmpty() ? "0" : fileLines[0]);

        //Will now initialize the nodes
        NODES = new Node[fileLines.length - 1];
        for (int i = 1; i < fileLines.length; i++) {

            String[] currentLine = fileLines[i].split(",");
            int posX = Integer.valueOf(currentLine[0]);
            int posY = Integer.valueOf(currentLine[1]);

            if (i == 1) {
                DEPOSIT_NODE = new Node(0);
                DEPOSIT_NODE.x = posX;
                DEPOSIT_NODE.y = posY;
                DEPOSIT_NODE.amount = 0;
                DEPOSIT_NODE.add = "DEPO";
                NODES[0] = DEPOSIT_NODE;

            } else {
                int nodeIndex = i -1;
                NODES[nodeIndex] = new Node(nodeIndex);
                NODES[nodeIndex].x = posX;
                NODES[nodeIndex].y = posY;
                NODES[nodeIndex].amount = 1;
                NODES[nodeIndex].add = "N" + (nodeIndex);

            }

        }

        CAR_LIMIT = (NODES.length / FLEET_SIZE) + 1;

        System.out.println("File succesfully loaded");

    }

    public static double[][] calculateCostMatrix() {
        double[][] localCostMatrix = new double[NODES.length][NODES.length];
        int matrixSize = localCostMatrix.length;

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (i == j) {
                    localCostMatrix[i][j] = 0;
                } else {
                    //System.out.println("Calculating distance from V["+i+"] to V["+j+"]");
                    localCostMatrix[i][j] = calculateDistance(NODES[i], NODES[j]);
                }

            }

        }
        return localCostMatrix;
    }

    private static double calculateDistance(Node n1, Node n2) {
        double distance;
        double x1 = n1.x;
        double x2 = n2.x;
        double y1 = n1.y;
        double y2 = n2.y;
        distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        return distance;
    }

    private static void printCostMatrix() {
        StringBuilder sb = new StringBuilder();
        for (double[] costMatrix1 : COST_MATRIX) {
            sb.append("[");
            for (int i = 0; i < costMatrix1.length; i++) {
                sb.append(String.valueOf(costMatrix1[i]));
                if (i < costMatrix1.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            sb.append(System.lineSeparator());
        }
        System.out.println(sb.toString());
    }

    public static int compClusterCost(Cluster cl, double distances[][]) {
        int cost = 0;
        for (int i = 0; i < cl.tsp.size() - 1; i++) {
            Node n = cl.tsp.get(i);
            Node n1 = cl.tsp.get(i + 1);

            cost += distances[n.index][n1.index];
        }
        return cost;
    }

    public static String SolveVRPbySweep() {
        ArrayList<Node> nodesList = cluster();
        Collections.sort(nodesList);

        //Cluster
        Cluster actualCluster = new Cluster();

        ArrayList<Cluster> clusters = new ArrayList<>();

        //Add the deposit to the actual Cluster
        actualCluster.add(NODES[0]);
        for (int i = 0; i < nodesList.size(); i++) {
            Node n = nodesList.get(i);

            //If the demand is greater than the capacity, then create a new cluster
            if (actualCluster.amount + n.amount > CAR_LIMIT) {
                clusters.add(actualCluster);
                actualCluster = new Cluster();
                //Add the Depot node to the new Cluster
                actualCluster.add(NODES[0]);
            }

            //pridam uzel do clusteru
            //pridam vsechny hrany ktere inciduji s uzly ktere jiz jsou v clusteru
            actualCluster.add(n);
            for (int j = 0; j < actualCluster.nodes.size(); j++) {
                Node nIn = actualCluster.nodes.get(j);
                Edge e = new Edge(nIn, n, (int) COST_MATRIX[nIn.index][n.index]);

                Edge eReverse = new Edge(n, nIn, (int) COST_MATRIX[n.index][nIn.index]);

                actualCluster.edges.add(e);
                actualCluster.edges.add(eReverse);
            }

            //v pripade posledni polozky musim pridat i cluster.
            if (i == nodesList.size() - 1) {
                clusters.add(actualCluster);
            }
        }

        int totalCost = 0;
        int clusterCount = clusters.size();

        StringBuilder sb = new StringBuilder();
        sb.append(clusterCount).append("\r\n");

        for (int i = 0; i < clusterCount; i++) {
            clusters.get(i).mst();
            clusters.get(i).dfsONMST();
            clusters.get(i).printTSP(sb);
            sb.append("");
            sb.append("\r\n");
            totalCost += compClusterCost(clusters.get(i), COST_MATRIX);
        }

        for (int i = 0; i < clusterCount; i++) {
            clusters.get(i).printTSPAdds(sb);
            sb.append("\r\n");
        }
        sb.append("TOTAL COST OF THE ROUTES:").append(totalCost);
        return sb.toString();
    }

    public static ArrayList<Node> cluster() {
        Node depo = NODES[0];
        ArrayList<Node> nodesList = new ArrayList<Node>();

        for (int i = 1; i < NODES.length; i++) {
            Node n = NODES[i];
            if (n.x >= depo.x) {
                if (n.y >= depo.y) {
                    n.cluster = 1;
                } else {
                    n.cluster = 4;
                }
            } else if (n.y >= depo.y) {
                n.cluster = 2;
            } else {
                n.cluster = 3;
            }

            for (int j = 1; j < 5; j++) {
                if (n.cluster == j) {
                    double difx = Math.abs(n.x - depo.x);
                    double dify = Math.abs(n.y - depo.y);

                    if (dify != 0) {
                        double tangA = (double) dify / difx;

                        if (n.cluster == 2 || n.cluster == 4) {
                            tangA = 1 / tangA;
                        }
                        n.angle += Math.atan(tangA);
                    }

                    break;
                } else {
                    n.angle += Math.PI / 2;
                }
            }
            nodesList.add(n);
        }
        return nodesList;
    }
}
