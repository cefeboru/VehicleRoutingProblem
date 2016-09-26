/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vehicleroutingproblem;

public class Ahorro implements Comparable<Ahorro>{
	public double val;
	public Nodo desde;
	public Nodo hasta;
	
	public Ahorro(double v,Nodo f,Nodo t){
		val = v;
		desde = f;
		hasta = t;
	}

	@Override
	public int compareTo(Ahorro o) {
		if(o.val<this.val){
			return -1;
		}else if(o.val == this.val){
			return 0;
		}else{
			return 1;
		}
	}
}