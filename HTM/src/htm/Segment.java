package htm;

import java.util.ArrayList;

public class Segment {
	static final int ActivationThreshold=0;
	static final int NewSynapseCount=50;
	
	
	//add a parent but it can be a cell or a column, how to solve this?
	public ArrayList<Synapse> synapses = new ArrayList<Synapse>();
	
	public boolean sequenceSegment=false;
	
	public boolean activeState=false;
	public boolean learnState=false;
	
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
		
	public ArrayList<Synapse> getActiveSynapses(boolean timeT, boolean addNewSynapses, Region destRegion){
		ArrayList<Synapse> activeSynapses = new ArrayList<Synapse>();
		if(synapses.size()>0){
			if(timeT){
				for(Synapse s : synapses){
					if(s.destRegion.columns[s.destCoor[0]][s.destCoor[1]].cells[s.destCoor[2]].tActiveState==true)activeSynapses.add(s);
				}
			}else{//t-1 previous time step
				for(Synapse s : synapses){
					if(s.destRegion.columns[s.destCoor[0]][s.destCoor[1]].cells[s.destCoor[2]].pActiveState==true)activeSynapses.add(s);
				}
			}
		}
		//System.out.print(activeSynapses.size()+",");
		if(addNewSynapses && NewSynapseCount > activeSynapses.size()){
			if(destRegion.newSynapsesSpace.size()==0){
				destRegion.newSynapsesSpace();
			}
			
			if(destRegion.newSynapsesSpace.size() > 0){
				//deep clone the list with a construction method in synapse
				ArrayList<Synapse> newSynapsesSpace=new ArrayList<Synapse>();
				for(Synapse s : destRegion.newSynapsesSpace){
					newSynapsesSpace.add(new Synapse(s));
				}
				if(newSynapsesSpace.size()+activeSynapses.size() <= NewSynapseCount)activeSynapses.addAll(newSynapsesSpace);
				else{
					double threshold = (double) (NewSynapseCount - activeSynapses.size())/newSynapsesSpace.size();
					//System.out.print("\""+threshold+"\"");
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
	
	public String toString(){
		String s="Synapses :"+synapses.size();
		s+="\nSequnece segment:" + sequenceSegment;
		s+="\nActive: "+ activeState;
		s+="\nLearn: " + learnState;
		s+="\n"+synapses.toString();
		return s;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}




}
