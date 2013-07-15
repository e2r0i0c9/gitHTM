package htm;

import java.util.ArrayList;

public class Segment {
	//add a parent but it can be a cell or a column, how to solve this?
	public ArrayList<Synapse> synapses = new ArrayList<Synapse>();
	
	
	public void addSynapse(Region input,int ir, int ic,double bias) {
		synapses.add(new Synapse(input, ir, ic, bias));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}



}
