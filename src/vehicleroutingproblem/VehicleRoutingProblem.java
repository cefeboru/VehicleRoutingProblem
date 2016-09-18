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

class Point{
    int x,y = 0;
    Point(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString(){
        return "("+x+","+y+")";
    }
}

/**
 *
 * @author Cesar Bonilla
 */
public class VehicleRoutingProblem {
    
    private static int numberOfTrucks = 0;
    private static double[][] costMatrix;
    private static Point[] V;
    
    public static void main(String[] args) throws Exception {
        //Load # of Trucks, Headquarter and Clients
        loadFile();
        //Generate the Cost Matrix
        costMatrix = calculateCostMatrix();
        printCostMatrix();
        
        
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
        
        String [] fileLines = sb.toString().split(System.lineSeparator());
        
        if(fileLines.length < 3){
            throw new Exception("Input file has not enough lines");
        }
        
        //Available Trucks
        numberOfTrucks= Integer.valueOf(fileLines[0].isEmpty() ? "0" : fileLines[0] );
        
        //All the vertices
        V = new Point[fileLines.length-1];
        
        //Initialize the Headquarter
        String[] headquarter = fileLines[1].split(",");        
        int posX = Integer.valueOf(headquarter[0]);
        int posY = Integer.valueOf(headquarter[1]);
        //V0 will be the headquarter
        V[0] = new Point(posX, posY);
        posX=0;posY = 0; 
        //Set the rest of Vertices
        for (int i = 2; i < fileLines.length; i++) {
            String[] currentLine = fileLines[i].split(",");
            posX = Integer.valueOf(currentLine[0]);
            posY = Integer.valueOf(currentLine[1]);
            V[i-1] = new Point(posX, posY);
        }
        
        
    }
    
    public static double[][] calculateCostMatrix(){
        double[][] localCostMatrix = new double[V.length][V.length];
        int matrixSize = localCostMatrix.length;
        
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if(i == j)
                    localCostMatrix[i][j] = 0;
                else {
                    //System.out.println("Calculating distance from V["+i+"] to V["+j+"]");
                    localCostMatrix[i][j] = calculateDistance(V[i],V[j]);
                }
                
            }
            
        }
        return localCostMatrix;
    }
    
    private static double calculateDistance(Point p1, Point p2) {
        double distance = 0.0;
        distance = Math.sqrt(Math.pow(p1.x-p2.x, 2) + Math.pow(p1.y - p2.y, 2));
        return distance;
    }
    
    private static void printCostMatrix(){
    StringBuilder sb = new StringBuilder();
        for (double[] costMatrix1 : costMatrix) {
            sb.append("[");
            for (int i = 0; i < costMatrix1.length; i++) {
                    sb.append(String.valueOf(costMatrix1[i]));
                    if(i < costMatrix1.length -1)
                        sb.append(",");
            }
            sb.append("]");
            sb.append(System.lineSeparator());
        }
        System.out.println(sb.toString());
    }

}



