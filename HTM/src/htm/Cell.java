package htm;

import java.util.ArrayList;
import java.util.Vector;

public class Cell {
	static final int MinThreshold=4;
	
	public Column parent;
	public ArrayList<Segment> distSegments=new ArrayList<Segment>();
	
	public boolean pActiveState=false;
	public boolean tActiveState=false;
	public boolean pPredictiveState=false;
	public boolean tPredictiveState=false;
	public boolean pLearnState=false;
	public boolean tLearnState=false;
	
	public ArrayList<SegmentUpdate> segmentUpdateList=new ArrayList<SegmentUpdate>();
	
	public boolean getLearnState(boolean timeT){
		if(timeT) return this.tLearnState;
		else return this.pLearnState;
	}
	
	public boolean getActiveState(boolean timeT){
		if(timeT) return this.tActiveState;
		else return this.pActiveState;
	}
	
	public boolean getPredictiveState(boolean timeT){
		if(timeT) return this.tPredictiveState;
		else return this.pPredictiveState;
	}
	
	public ArrayList<Segment> getActiveSegments() {
		ArrayList<Segment> activeSegments = new ArrayList<Segment>();
		for(Segment s : distSegments){
			if(s.segmentActive(false, true) == true)activeSegments.add(s);
		}
		return activeSegments;
	}
	
	public Segment getBestMatchingSegment() {
		boolean found=false;
		Segment bestMatchingSegment = new Segment();
		int max=MinThreshold;
		if(distSegments.size()>0){
			for(Segment seg : distSegments){
				//can't be the segment made this prediction
				if(seg.sequenceSegment==false && seg.segmentActive(false, true)==false){
					int count=0;
					for(Synapse s : seg.synapses){
						if(s.destRegion.columns[s.destCoor[0]][s.destCoor[1]].cells[s.destCoor[2]].pActiveState==true)count++;
					}
					//System.out.print("["+count+","+max+"]");
					if(count>max){
						found=true;
						max=count;
						bestMatchingSegment=seg;
					}
				}
			}	
		}
		if(found==true)return bestMatchingSegment;
		else return null;
	}
	
	public void calculatePredictiveState(Region destRegion){
		if(distSegments.size()>0){
			for(Segment seg : distSegments){
				if(seg.segmentActive(true, true) == true){
					System.out.print("P");
					tPredictiveState=true;
					seg.sequenceSegment=true;
					//update active synapses
					segmentUpdateList.add(new SegmentUpdate(seg,seg.getActiveSynapses(true,false,null)));
					//update a segment that could have predict this activation
					Segment bestMatchingSegment = this.getBestMatchingSegment();
					if(bestMatchingSegment != null){
						segmentUpdateList.add(new SegmentUpdate(bestMatchingSegment,bestMatchingSegment.getActiveSynapses(false,true, destRegion)));
					//}else{
						//speed up the learning to add new segment here
						//bestMatchingSegment = new Segment();
						//bestMatchingSegment.synapses=bestMatchingSegment.getActiveSynapses(false, true, destRegion);
						//distSegments.add(bestMatchingSegment);
						//segmentUpdateList.add(new SegmentUpdate(bestMatchingSegment,bestMatchingSegment.synapses));
					}
				}
			}
		}
	}
	
	public void adaptSegments(boolean positiveReinforcement){
		//NO WAY to remove synapses added to segment
		//find a method to replace low perm synapses with new comer
		if(segmentUpdateList.size()>0){
			for(SegmentUpdate sUpdate : segmentUpdateList){
				//System.out.print(sUpdate.updateSegment.synapses.size()+"-"+sUpdate.updateSynapsesList.size()+";");
				if(positiveReinforcement){
					if(sUpdate.updateSegment.newSegment==true){
						this.distSegments.add(sUpdate.updateSegment);
						sUpdate.updateSegment.newSegment=false;
					}
					for(Synapse s : sUpdate.updateSynapsesList){
						s.permanenceInc();
						s.updated=true;
					}
					
					for(Synapse s : sUpdate.updateSegment.synapses){
						if(s.updated==false) s.permanenceDec();
						else s.updated=false;
					}
				}else{
					for(Synapse s : sUpdate.updateSynapsesList){
						s.permanenceDec();
					}
				}
			}
		}
	}
	
	public String toString(){
		String str ="distSegment: "+distSegments.size()+"\n";
		str+="Active State ("+pActiveState+", "+tActiveState+")\n";
		str+="Predictive State ("+pPredictiveState+", "+tPredictiveState+")\n";
		str+="Learn State ("+pLearnState+", "+tLearnState+")\n";
		
		if(this.distSegments.size()>0){
			int row = this.parent.parentRegion.row;
			int column = this.parent.parentRegion.column;
			int layer = this.parent.cells.length;
			double[][][] outputMatrix=new double[row][column][layer];
			
			for(Synapse s : this.distSegments.get(0).synapses){
				if(outputMatrix[s.destCoor[0]][s.destCoor[1]][s.destCoor[2]]>0) str+="Duplicate Synapse!";
				else if(s.isConnected()) outputMatrix[s.destCoor[0]][s.destCoor[1]][s.destCoor[2]]=s.permanence;
				else outputMatrix[s.destCoor[0]][s.destCoor[1]][s.destCoor[2]]=-1;
			}
			for(int i=0; i< row;i++){
				for(int j=0; j< column; j++){
					str+="(";
					for(int k=0; k<layer; k++){
						if(outputMatrix[i][j][k]>0)str+=String.format("%.1f", outputMatrix[i][j][k]);
						else if(outputMatrix[i][j][k]==-1) str+="-";
						else str+=" ";
					}
					str+=")";
				}
				str+="\n";
			}
		}
		return str;
	}
	
	public Cell(Column col){
		this.parent=col;
	}
	
	public static void main(String[] args) {
		

	}





}
