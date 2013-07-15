package main;

import htm.*;
import util.*;

public class main {
	static final int MaxCalculateRadius=30;
	static final int DefaultInhibitionRadius=7;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Initialization
		// Generate input data
		int time = 200;
		Input input = new Region(100,350,time);
		
		NeighborMap neighborMap = new NeighborMap(MaxCalculateRadius);
		// Create HTM region, initialize receptive field and connection permanence
		Region htmRegion = new Region(50,50,input);
		//can not set up neighbors in previous step
	    //because not all neighbors has been instantiated yet
		htmRegion.setNeighbor(neighborMap, DefaultInhibitionRadius);
		
		
		for(int t=0; t<time; t++){
			//Spatial Pooling
			htmRegion.overlap(input.get(t),t);
			htmRegion.inhibition();
			htmRegion.learning(input.get(t),t);
			//Recalculate inhibition radius
			int inhibitionRadius = 
					htmRegion.averageReceptiveFieldSize((double)htmRegion.row/input.row,(double)htmRegion.column/input.column);	
			htmRegion.setNeighbor(neighborMap, inhibitionRadius);
			//System.out.print("("+t+")");
			
			//Temporal Pooling
			//for each region now we have ArrayList activeColumns suggest the active columns in this time step
			
		}
	}

}
