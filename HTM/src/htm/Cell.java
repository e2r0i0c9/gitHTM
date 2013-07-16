package htm;

import java.util.ArrayList;

public class Cell {
	public Column parent;
	public ArrayList<Segment> distSegments;
	public ArrayList<SegmentUpdate> segmentUpdates;
	public boolean pPredictiveState=false;
	public boolean tPredictiveState=false;
	public boolean pActiveState=false;
	public boolean tActiveState=false;
	public boolean pLearnState=false;
	public boolean tLearnState=false;
	
	public ArrayList<Segment> getActiveSegments() {
		ArrayList<Segment> activeSegments = new ArrayList<Segment>();
		for(Segment s : distSegments){
			if(s.pActiveState==true)activeSegments.add(s);
		}
		return activeSegments;
	}
	
	public static void main(String[] args) {
		

	}





}
