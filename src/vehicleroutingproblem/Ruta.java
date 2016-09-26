/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vehicleroutingproblem;

import java.util.ArrayList;

/**
 *
 * @author Cesar Bonilla
 */
public class Ruta {
	
	public double permitido;
	public double actual;
	public double costoTotal;
	
	
	public int[] nodes;
	public Arista[] aristasEntrada;
	public Arista[] aristasSalida;
	
    /**
     *Contains the route current EDGES
     */
    public ArrayList<Arista> edges;
	
	public Ruta(int nodesNumber){
		edges = new ArrayList<Arista>();
		
		nodes = new int[nodesNumber];
		aristasEntrada = new Arista[nodesNumber];
		aristasSalida = new Arista[nodesNumber];
	}
	
	public void add(Arista e){
		edges.add(e);
		aristasSalida[e.n1.indice] = e;
		aristasEntrada[e.n2.indice] = e;
		
		e.n1.routa = this;
		e.n2.routa = this;
		
		costoTotal+= e.val;
	}
	
	public void removeEdgeToNode(int index){
		Arista e = aristasEntrada[index];
		aristasSalida[e.n1.indice] = null;
		
		costoTotal-= e.val;
		
		edges.remove(e);
		aristasEntrada[index] = null;
	}
	
	public void removeEdgeFromNode(int index){
		Arista e = aristasSalida[index];
		aristasEntrada[e.n2.indice] = null;
		
		costoTotal-=e.val;
		edges.remove(e);
		aristasSalida[index] = null;
	}
	
	public int predecessor(int nodeIndex){
		return aristasEntrada[nodeIndex].n1.indice;
	}
	
	
	public int successor(int nodeIndex){
		return aristasSalida[nodeIndex].n2.indice;
	}
	
	public boolean merge(Ruta r2,Arista mergingEdge){

		int from = mergingEdge.n1.indice;
		int to = mergingEdge.n2.indice;
		
		int predecessorI = this.predecessor(from);
		int predecessorJ = r2.predecessor(to);
		
		int successorI = this.successor(from);
		int successorJ = r2.successor(to);
		
		if(successorI == 0 && predecessorJ == 0){
			this.removeEdgeToNode(0);
			r2.removeEdgeFromNode(0);
			for(Arista e:r2.edges){
				this.add(e);
			}
			this.actual+= r2.actual;
			this.add(mergingEdge);
			return true;

		}else if(successorJ == 0 && predecessorI == 0){
			mergingEdge.reverse();
			this.removeEdgeFromNode(0);
			r2.removeEdgeToNode(0);
			for(Arista e:r2.edges){
				this.add(e);
			}
			this.actual+= r2.actual;
			this.add(mergingEdge);
			return true;
		}
		
		return false;
	}
}