package htm;

import java.util.ArrayList;
import java.util.Iterator;

public class SegmentUpdate {
	static final int NewSynapseCount=120;
	
	public Segment updateSegment;
	//positive reinforce synapses
	public ArrayList<Synapse> updateSynapsesList;

	public SegmentUpdate(Segment segment,ArrayList<Synapse> synapses) {
		for(Synapse s : synapses){
			if(!segment.synapses.contains(s)){
				segment.synapses.add(s);
			}
		}
		Iterator<Synapse> iter = segment.synapses.iterator();  
		while(iter.hasNext()){  
		    Synapse s = iter.next();  
		    if(s.permanence<0.1){  
		        iter.remove();  
		    }
		}
		if(NewSynapseCount < segment.synapses.size()){
			segment.synapses=new ArrayList<Synapse>(segment.synapses.subList(0, NewSynapseCount-1));
		}
		updateSynapsesList=synapses;
		updateSegment=segment;
		updateSegment.sequenceSegment=true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
