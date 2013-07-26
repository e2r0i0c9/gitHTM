package htm;

import java.util.ArrayList;

public class Segment {
	static final int ActivationThreshold=2;
	static final int NewSynapseCount=50;
	
	
	//add a parent but it can be a cell or a column, how to solve this?
	public ArrayList<Synapse> synapses = new ArrayList<Synapse>();
	
	public boolean sequenceSegment=false;
	
	public boolean pActiveState=false;
	public boolean tActiveState=false;
	
	
	public void addSynapse(Region input,int ir, int ic,double bias) {
		synapses.add(new Synapse(input, ir, ic, bias));
	}

	public boolean segmentActive(boolean activeState) {
		int count = 0;
		if(activeState == true){
			for(Synapse s : synapses){
				if(s.isConnected() && s.destRegion.columns[s.destCoor[0]][s.destCoor[1]].cells[s.destCoor[2]].pActiveState==true){
					count++;
				}
			}
			if(count>ActivationThreshold)return true;
			
		}else{
			for(Synapse s : synapses){
				if(s.isConnected() && s.destRegion.columns[s.destCoor[0]][s.destCoor[1]].cells[s.destCoor[2]].pLearnState==true){
					count++;
				}
			}
			if(count>ActivationThreshold)return true;
		}
		return false;
	}

	public ArrayList<Synapse> newSynapsesSpace(Region destRegion){
		ArrayList<Synapse> newSynapses=new ArrayList<Synapse>();
		for(int i=0; i<destRegion.row;i++){
			for(int j=0; j<destRegion.column; j++){
				for(int k=0; k<destRegion.columns[i][j].cells.length; k++){
					if(destRegion.columns[i][j].cells[k].pLearnState == true){
						newSynapses.add(new Synapse(destRegion,i,j,k));
					}
				}
			}
		}
		return newSynapses;
	}
	
	public ArrayList<Synapse> getActiveSynapses(boolean addNewSynapses, Region destRegion, boolean t){
		ArrayList<Synapse> activeSynapses = new ArrayList<Synapse>();
		if(synapses.size()>0){
			if(t){
				for(Synapse s : synapses){
					if(s.destRegion.columns[s.destCoor[0]][s.destCoor[1]].cells[s.destCoor[2]].tActiveState==true)activeSynapses.add(s);
				}
			}else{//t-1 previous time step
				for(Synapse s : synapses){
					if(s.destRegion.columns[s.destCoor[0]][s.destCoor[1]].cells[s.destCoor[2]].pActiveState==true)activeSynapses.add(s);
				}
			}
		}
		if(addNewSynapses && NewSynapseCount > activeSynapses.size()){
			ArrayList<Synapse> newSynapsesSpace = newSynapsesSpace(destRegion);
			if(newSynapsesSpace.size() > 0){
				if(newSynapsesSpace.size()+activeSynapses.size() <= NewSynapseCount)activeSynapses.addAll(newSynapsesSpace);
				else{
					double threshold = (double) (NewSynapseCount - activeSynapses.size())/newSynapsesSpace.size();
					for(Synapse s : newSynapsesSpace){
						if(!activeSynapses.contains(s) && Math.random()<threshold){
							activeSynapses.add(s);
						}
					}
				}
			}else{
				System.out.println("WARNING:no cell in learnState!!!");
			}
		}
		return activeSynapses;
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}




}
