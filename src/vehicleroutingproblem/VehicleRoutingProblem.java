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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 *
 * @author Cesar Bonilla
 */
public class VehicleRoutingProblem {

    public static double CAR_LIMIT = 0;
    public static int FLEET_SIZE = 0;
    public static double[][] COST_MATRIX;
    private static double[][] savings;
    public static Nodo[] NODES;
    public static ArrayList<Ruta> routes;
    public static Nodo DEPOSIT_NODE;
    public static int maxIterations = 50;

    public static void main(String[] args) throws Exception {
        Date start_date =  new Date();
        //Load # of Trucks, Deposit and Clients
        loadFile();

        //Generate the Cost Matrix
        COST_MATRIX = calculateCostMatrix();

        //Calculate a initial limit for the vehicles
        calculteInitialLimit();

        //Run algorithm once
        solveVRP();

        int counter = 0;
        //Iterate car limit until optimal limit found
        do {
            System.out.println("Iteracion: " + counter);
            if (routes.size() < FLEET_SIZE) {
                CAR_LIMIT--;
            } else if (routes.size() > FLEET_SIZE) {
                CAR_LIMIT++;
            } else {
                break;
            }
            solveVRP();
            counter++;
        } while (counter < maxIterations);

        //Generate files
        writeFiles();

        printRoutesTotalCost(routes);
        Date end_date = new Date();
        
        long total_time = end_date.getTime() - start_date.getTime();
        total_time = total_time / 1000;
        System.out.println("Total Time: " + total_time + " seconds");

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
        NODES = new Nodo[fileLines.length - 1];
        for (int i = 1; i < fileLines.length; i++) {

            String[] currentLine = fileLines[i].split(",");
            int posX = Integer.valueOf(currentLine[0]);
            int posY = Integer.valueOf(currentLine[1]);

            if (i == 1) {
                DEPOSIT_NODE = new Nodo(0,posX,posY,"0");
                DEPOSIT_NODE.distanciaAlDeposito = 0;
                NODES[0] = DEPOSIT_NODE;

            } else {
                int nodeIndex = i - 1;
                String nombre = String.valueOf(nodeIndex);
                NODES[nodeIndex] = new Nodo(nodeIndex,posX,posY,nombre);
                NODES[nodeIndex].distanciaAlDeposito = calculateDistance(NODES[nodeIndex], DEPOSIT_NODE);

            }

        }

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

    public  static void solveVRP() {
        routes = new ArrayList<Ruta>();

        for (int i = 0; i < NODES.length; i++) {

            Nodo n = NODES[i];

            //PARA TODOS LOS NODOS MENOS EL DEPOSITO
            if (i != 0) {
                //creating the two edges
                Arista e = new Arista(DEPOSIT_NODE, n, COST_MATRIX[0][n.indice]);
                Arista e2 = new Arista(n, DEPOSIT_NODE, COST_MATRIX[0][n.indice]);

                Ruta localRoute = new Ruta(NODES.length);
                localRoute.permitido = CAR_LIMIT;
                localRoute.add(e);
                localRoute.add(e2);
                localRoute.actual += n.distanciaAlDeposito;

                routes.add(localRoute);
            }
        }

        //CALCULAS LOS AHORROS DE LAS RUTAS
        ArrayList<Ahorro> listaAhorros = computeSaving(COST_MATRIX, NODES.length, savings, NODES);
        //ORDENAMOS LOS AHORROS
        Collections.sort(listaAhorros);

        //BUSCAMOS AHORROS ENTRE LAS RUTAS ACTUALES
        while (!listaAhorros.isEmpty()) {
            Ahorro ahorroActual = listaAhorros.get(0);

            Nodo n1 = ahorroActual.desde;
            Nodo n2 = ahorroActual.hasta;

            Ruta r1 = n1.routa;
            Ruta r2 = n2.routa;

            int from = n1.indice;
            int to = n2.indice;

            //Verificamos que las distancias no sean mayores a la permitida
            if (ahorroActual.val > 0 && r1.actual + r2.actual < r1.permitido && !r1.equals(r2)) {

                Arista outgoingR2 = r2.aristasSalida[to];
                Arista incommingR1 = r1.aristasEntrada[from];

                if (outgoingR2 != null && incommingR1 != null) {
                    boolean succ = r1.merge(r2, new Arista(n1, n2, COST_MATRIX[n1.indice][n2.indice]));
                    if (succ) {
                        routes.remove(r2);
                    }
                } else {
                    System.out.println("Problem");
                }

            }

            listaAhorros.remove(0);
        }
    }

    public static ArrayList<Ahorro> computeSaving(double[][] dist, int n, double[][] sav, Nodo[] nodes) {
        sav = new double[n][n];
        ArrayList<Ahorro> sList = new ArrayList<Ahorro>();
        for (int i = 1; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                sav[i][j] = dist[0][i] + dist[j][0] - dist[i][j];
                Nodo n1 = nodes[i];
                Nodo n2 = nodes[j];
                Ahorro s = new Ahorro(sav[i][j], n1, n2);
                sList.add(s);
            }
        }
        return sList;
    }

    public static void printSaving(Ahorro s) {
        int from = s.desde.indice;
        int to = s.hasta.indice;
        System.out.println("Saving - From: " + from + " To: " + to + " Val: " + s.val);
    }

    public static void printRoute(Ruta r) {
        System.out.print("Route: ");
        Arista edge = r.aristasSalida[0];

        System.out.print("(" + edge.n1.indice + "->");

        do {
            edge = r.aristasSalida[edge.n2.indice];
            System.out.print(edge.n1.indice + "->");
        } while (edge.n2.indice != 0);

        System.out.print(edge.n2.indice + ")");
        System.out.println("");
    }
    
        public static void printRoute(Ruta r, PrintWriter pw) {
        Arista edge = r.aristasSalida[0];
        
        StringBuilder sb = new StringBuilder();
        int numberOfNodes = r.edges.size() - 1;
        
        sb.append(String.valueOf(numberOfNodes));
        sb.append(System.lineSeparator());
        
        do {
            edge = r.aristasSalida[edge.n2.indice];
            sb.append(edge.n1.indice);
            sb.append(System.lineSeparator());
        } while (edge.n2.indice != 0);
        pw.write(sb.toString());
        
        
    }

    public static void printRoutesTotalCost(ArrayList<Ruta> routes) {
        int counter = 1;
        for (Ruta r : routes) {
            printRoute(r);
            System.out.println("Route " + (counter++) + ": " + r.costoTotal);
        }
    }

    public static void printRoutesTotalCost(ArrayList<Ruta> routes, PrintWriter pw) {
        int counter = 1;
        for (Ruta r : routes) {
            //printRoute(r);
            StringBuilder sb = new StringBuilder();
            sb.append("Route ");
            sb.append(String.valueOf(counter++));
            sb.append(": ").append(r.costoTotal);
            sb.append(System.lineSeparator());
            pw.write(sb.toString());
        }
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

    private static double calculateDistance(Nodo n1, Nodo n2) {
        double distance;
        double x1 = n1.x;
        double x2 = n2.x;
        double y1 = n1.y;
        double y2 = n2.y;
        distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        return distance;
    }

    public static void calculteInitialLimit() {
        
        double routesLength = 0.0;
        if (CAR_LIMIT == 0) {
            Nodo n0 = NODES[0];
            for (int i = 1; i < NODES.length; i++) {
                routesLength += calculateDistance(n0, NODES[i]);
            }
            CAR_LIMIT = routesLength / FLEET_SIZE;
        }
    }

    public static void writeFiles() {
        PrintWriter writer = null;
        try {
            String folder = System.getProperty("user.dir") + "\\Data\\";
            writer = new PrintWriter(folder + "calculos.txt", "UTF-8");
            printRoutesTotalCost(routes, writer);
            writer.close();
            writer = new PrintWriter(folder + "salidas.txt", "UTF-8");
            for (Ruta route : routes) {
                printRoute(route,writer);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VehicleRoutingProblem.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(VehicleRoutingProblem.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (writer != null) {
                writer.close();
            }

        }

    }

}
