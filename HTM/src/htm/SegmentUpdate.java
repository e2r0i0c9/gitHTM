package htm;

import java.util.ArrayList;

public class SegmentUpdate {
	public Segment updateSegment;
	//positive reinforce synapses
	public ArrayList<Synapse> updateSynapsesList;

	public SegmentUpdate(Segment segment,ArrayList<Synapse> synapses) {
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
