package htm;

import java.util.ArrayList;

public class Cell {
	public Column parent;
	public ArrayList<Segment> distSegments=new ArrayList<Segment>();
	public ArrayList<SegmentUpdate> segmentUpdates=new ArrayList<SegmentUpdate>();
	
	public boolean pPredictiveState=false;
	public boolean tPredictiveState=false;
	public boolean pActiveState=false;
	public boolean tActiveState=false;
	public boolean pLearnState=false;
	public boolean tLearnState=false;
	
	public ArrayList<SegmentUpdate> segmentUpdateList=new ArrayList<SegmentUpdate>();
	
	public ArrayList<Segment> getActiveSegments() {
		ArrayList<Segment> activeSegments = new ArrayList<Segment>();
		for(Segment s : distSegments){
			if(s.pActiveState==true)activeSegments.add(s);
		}
		return activeSegments;
	}
	
	public Segment getBestMatchingSegment() {
		boolean found=false;
		Segment bestMatchingSegment = new Segment();
		int max=0,count=0;
		if(distSegments.size()>0){
			for(Segment seg : distSegments){
				for(Synapse s : seg.synapses){
					if(s.destRegion.columns[s.destCoor[0]][s.destCoor[1]].cells[s.destCoor[2]].pActiveState==true){
						count++;
					}
				}
				if(count>max){
					found=true;
					max=count;
					bestMatchingSegment=seg;
				}
			}	
		}
		if(found==true)return bestMatchingSegment;
		else return null;
	}
	
	public void calculatePredictiveState(Region destRegion){
		if(distSegments.size()>0){
			for(Segment seg : distSegments){
				if(seg.tActiveState == true){
					tPredictiveState=true;
					//update active synapses
					segmentUpdateList.add(new SegmentUpdate(seg,seg.getActiveSynapses(false,null,true)));
					//update a segment that could have predict this activation
					Segment bestMatchingSegment = this.getBestMatchingSegment();
					if(bestMatchingSegment != null){
						segmentUpdateList.add(new SegmentUpdate(bestMatchingSegment,bestMatchingSegment.getActiveSynapses(true, destRegion,false)));
					}else{
						bestMatchingSegment = new Segment();
						bestMatchingSegment.synapses=bestMatchingSegment.getActiveSynapses(true, destRegion,false);
						distSegments.add(bestMatchingSegment);
						segmentUpdateList.add(new SegmentUpdate(bestMatchingSegment,bestMatchingSegment.synapses));
					}
				}
			}
		}
	}
	
	public void adaptSegments(boolean positiveReinforcement){
		if(segmentUpdateList.size()>0){
			for(SegmentUpdate sUpdate : segmentUpdateList){
				if(positiveReinforcement){
					for(Synapse s : sUpdate.updateSynapsesList){
						s.permanenceInc();
					}
					for(Synapse s : sUpdate.updateSegment.synapses){
						if(!sUpdate.updateSynapsesList.contains(s)){
							s.permanenceDec();
						}
					}
				}else{
					for(Synapse s : sUpdate.updateSynapsesList){
						s.permanenceDec();
					}
				}
			}
			segmentUpdateList=new ArrayList<SegmentUpdate>();
		}
	}
	
	
	public static void main(String[] args) {
		

	}





}
