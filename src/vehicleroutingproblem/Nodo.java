/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vehicleroutingproblem;

/**
 *
 * @author Cesar Bonilla
 */
public class Nodo {

    public Ruta routa;

    public int indice;
    public double x;
    public double y;
    public double distanciaAlDeposito;
    
    public String nombreNodo;

    public Nodo(int i, double x, double y, String nombre) {
        indice = i;
        this.x = x;
        this.y = y;
        this.nombreNodo = nombre;
    }
}
