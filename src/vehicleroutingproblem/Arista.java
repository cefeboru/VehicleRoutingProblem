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
public class Arista{
	public Nodo n1;
	public Nodo n2;
	
	public double val;
	
	public Arista next;
	
	public Arista(Nodo ln1,Nodo ln2,double dist){
		this.n1 = ln1;
		this.n2 = ln2;
		this.val = dist;
	}
	
	public void reverse(){
		Nodo swap = this.n2;
		this.n2 = n1;
		this.n1 = swap;
	}
}