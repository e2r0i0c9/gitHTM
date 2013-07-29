package htm;

import java.util.ArrayList;

public class Cell {
	public Column parent;
	public ArrayList<Segment> distSegments=new ArrayList<Segment>();
	public ArrayList<SegmentUpdate> segmentUpdates=new ArrayList<SegmentUpdate>();
	
	public boolean pActiveState=false;
	public boolean tActiveState=false;
	public boolean pPredictiveState=false;
	public boolean tPredictiveState=false;
	public boolean pLearnState=false;
	public boolean tLearnState=false;
	
	public ArrayList<SegmentUpdate> segmentUpdateList=new ArrayList<SegmentUpdate>();
	
	public ArrayList<Segment> getActiveSegments() {
		ArrayList<Segment> activeSegments = new ArrayList<Segment>();
		for(Segment s : distSegments){
			if(s.activeState == true)activeSegments.add(s);
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
				if(seg.activeState == true){
					System.out.print("P");
					tPredictiveState=true;
					//update active synapses
					segmentUpdateList.add(new SegmentUpdate(seg,seg.getActiveSynapses(true,false,null)));
					//update a segment that could have predict this activation
					Segment bestMatchingSegment = this.getBestMatchingSegment();
					if(bestMatchingSegment != null){
						segmentUpdateList.add(new SegmentUpdate(bestMatchingSegment,bestMatchingSegment.getActiveSynapses(false,true, destRegion)));
					}else{
						bestMatchingSegment = new Segment();
						bestMatchingSegment.synapses=bestMatchingSegment.getActiveSynapses(false, true, destRegion);
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
				//System.out.print(sUpdate.updateSegment.synapses.size()+"-"+sUpdate.updateSynapsesList.size()+";");
				if(positiveReinforcement){
					for(Synapse s : sUpdate.updateSynapsesList){
						s.permanenceInc();
						s.updated=true;
					}
					
					for(Synapse s : sUpdate.updateSegment.synapses){
						if(s.updated==false){
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
	
	public String toString(){
		String s ="distSegment: "+distSegments.size()+"\n";
		s+="Active State ("+pActiveState+", "+tActiveState+")\n";
		s+="Predictive State ("+pPredictiveState+", "+tPredictiveState+")\n";
		s+="Learn State ("+pLearnState+", "+tLearnState+")\n";
		return s;
	}
	
	public static void main(String[] args) {
		

	}





}
