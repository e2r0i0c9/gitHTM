package htm;

import java.util.ArrayList;

public class SegmentUpdate {
	public ArrayList<Synapse> updateSynapsesList;
	public Segment updateSegment;

	public SegmentUpdate(Segment segment,ArrayList<Synapse> synapses) {
		updateSynapsesList=synapses;
		updateSegment=segment;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
