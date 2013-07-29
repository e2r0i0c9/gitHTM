package htm;

import java.util.ArrayList;
import java.util.LinkedList;

public class Column {
	//parameters
	static final int Cycle=100;
	static final int Layer=5;
	static final int MinOverlap=3;
	static final double MinOverlapRatio=0.005;
	static final double DesiredLocalActivityRatio=0.05;
	static final int DesiredLocalActivity=10;
	static final double ActiveDutyCycleFraction=0.3;
	static final double OverlapDutyCycleFraction=0.3;
	static final int DutyCycle=100;
	static final double BoostStep=2.0;
	//min threshold for to consider a segment as best matching segment
	static final int MinThreshold=2;
	
	public int posRow;
	public int posColumn;
	public Region parentRegion;
	public Cell[] cells=new Cell[Layer];
	public Segment proxSegment=new Segment();
	public ArrayList<Column> neighbor= new ArrayList<Column>();
	public int overlap=0;
	public LinkedList<Integer> activeQueue = new LinkedList<Integer>();
	public boolean active;
	public LinkedList<Integer> overlapQueue=new LinkedList<Integer>();
	
	public double boost=1.0;
	//public int inhibitionRadius;
	
	
	public void setNeighbor(Region region,ArrayList<int[]> neighborCoor){
		this.neighbor = new ArrayList<Column>();
		for(int[] coor : neighborCoor){
			int r = Math.max(Math.min(region.row-1,posRow+coor[0]),0);
			int c = Math.max(Math.min(region.column-1,posColumn+coor[1]),0);		
			if(!(r==posRow && c==posColumn))
					this.neighbor.add(region.columns[r][c]);
		}
	}
	
	public void calculateOverlap(boolean[][] input, int t){
		//reset
		overlap=0;
		active=false;
		for(Synapse s : proxSegment.synapses){
			if(s.isConnected() && input[s.destCoor[0]][s.destCoor[1]]){
				overlap+=1;					
			}
		}
		//if(overlap >= (int)ReceptiveField.size()*MinOverlapRatio){
		if(overlap >= MinOverlap){
			overlap = (int)(overlap*boost);
			overlapQueue.offer(t);
		}else if(overlap>0) overlap=-1;//special tag for below threshold overlap
	}

	public boolean isActive(int t){
		//this function finds out whether the overlap score of this column 
		//is among the top DesiredLocalActivity-percent of its neighbor
		//if it is set it active, if not remain inactive
		if(overlap>0){
			int tie=1;
			int ahead=0;
			for(Column c : neighbor){
				if(c.overlap > this.overlap){
					ahead++;
					continue;
				}
				else if(c.overlap == this.overlap) tie++;
			}
			int maxLocalRank= DesiredLocalActivity;
			//int maxLocalRank= (int) Math.round(neighbor.size()*DesiredLocalActivityRatio);
			//test display
			//System.out.print(maxLocalRank);
			//System.out.print("["+this.posRow+","+this.posColumn+"];");
			
			if(ahead+tie <= maxLocalRank){
				active=true;
				activeQueue.offer(t);
				return true;
			}else if(ahead < maxLocalRank){
				if(Math.random() < (double)1/tie){
					active=true;
					activeQueue.offer(t);
					return true;
				}
			}
		}
		return false;
	}
	
	private int maxActiveDutyCycle(){
		int max=0;
		for(Column c : neighbor){
			if(c.activeQueue.size() > max){
				max=c.activeQueue.size();
			}
		}
		return max;
	}
	
	private int maxOverlapDutyCycle() {
		int max=0;
		for(Column c : neighbor){
			if(c.overlapQueue.size()>max){
				max=c.overlapQueue.size();
			}
		}
		return max;
	}
	
	private int grandOverlap(){
		//calculate overlap for all synapses in the receptive field
		int grandOverlap=0;
		for(Synapse s : proxSegment.synapses){
			if(parentRegion.inputMatrix[s.destCoor[0]][s.destCoor[1]]){
				grandOverlap+=1;					
			}
		}
		return grandOverlap;
	}
	
	public void adjustBoost(int t){
		//after 100 iteration
		if(t > DutyCycle){
			//Boost Column overlap
			double minActiveDutyCycle = ActiveDutyCycleFraction*this.maxActiveDutyCycle();
			/*
			//Test display
			System.out.print(maxActiveDutyCycle()+",");
			if(minActiveDutyCycle>0){
				System.out.print("-"+minActiveDutyCycle+"-");
			}
			*/
			//update activeQueue
			if(activeQueue.size() > 0 && activeQueue.get(0) < t-DutyCycle) activeQueue.poll();
			//boost overlap value
			if (activeQueue.size() >= minActiveDutyCycle) this.boost=1.0;
			else if(overlap!=0) boost += (minActiveDutyCycle-activeQueue.size())/minActiveDutyCycle*BoostStep;
			
			//Boost  permanence
			//update overlapQueue
			double minOverlapDutyCycle=OverlapDutyCycleFraction*maxOverlapDutyCycle();
			/*
			//test Display
			System.out.print(maxOverlapDutyCycle()+";");
			if(minOverlapDutyCycle>0){
				System.out.print("("+minOverlapDutyCycle+")");
			}
			*/
			if(overlapQueue.size() > 0 && overlapQueue.get(0) < t-DutyCycle) overlapQueue.poll();
			//boost all synapses in the receptive field
			if(grandOverlap()>0 && overlapQueue.size() < minOverlapDutyCycle){
				for(Synapse s : proxSegment.synapses){
					s.boostPermanence();
				}
			}
		}
		
	}

	public double connectedRFSize(double rowFactor, double columnFactor){
		double size=0;
		for(Synapse s : proxSegment.synapses){
			if(s.isConnected()){
				double dist=(s.destCoor[0]*rowFactor-posRow)*(s.destCoor[0]*rowFactor-posRow)+
						(s.destCoor[1]*columnFactor-posColumn)*(s.destCoor[1]*columnFactor-posColumn);
				dist=Math.sqrt(dist);
				if(dist>size) size=dist;
			}
		}
		//System.out.print(size);
		return size;
	}
	
	public Cell getBestMatchingCell() {
		int max=MinThreshold;
		Segment bestMatchingSegment=new Segment();
		Cell bestMatchingCell = new Cell();
		boolean found=false;
		for(Cell cell : cells){
			int count=0;
			if(cell.distSegments.size() > 0){
				for(Segment seg : cell.distSegments){
					for(Synapse s : seg.synapses){
						if(s.destRegion.columns[s.destCoor[0]][s.destCoor[1]].cells[s.destCoor[2]].pActiveState == true){
							count++;
						}
					}
					if(count>max){
						found=true;
						max=count;
						bestMatchingSegment=seg;
						bestMatchingCell=cell;
					}
				}
			}	
		}
		
		if(found){
			bestMatchingCell.segmentUpdateList.add(new SegmentUpdate(bestMatchingSegment,bestMatchingSegment.getActiveSynapses(false,true, parentRegion)));
		}else{
			int min = cells[0].distSegments.size();
			bestMatchingCell=cells[0];
			for(Cell cell : cells){
				if(cell.distSegments.size() < min){
					min=cell.distSegments.size();
					bestMatchingCell=cell;
				}
			}
			bestMatchingSegment.synapses=bestMatchingSegment.getActiveSynapses(false,true, parentRegion);
			if(bestMatchingSegment.synapses.size()>0){
				bestMatchingCell.distSegments.add(bestMatchingSegment);
				bestMatchingCell.segmentUpdateList.add(new SegmentUpdate(bestMatchingSegment,bestMatchingSegment.synapses));
			}
		}
		return bestMatchingCell;
	}
	
	
	public String toString(){
		String output="\n";
		output+="["+posRow+", "+posColumn+"]";
		if(active) output+="\tActive\n";
		else output+="\tInactive\n";
		output+="ovelapCycle: ";
		for(int i=0;i<overlapQueue.size();i++){
			output+=(overlapQueue.get(i)+";");
		}
		output+="\nactiveCycle: ";
		for(int i=0;i<activeQueue.size();i++){
			output+=(activeQueue.get(i)+";");
		}
		output+="\n";
		//receptive field
		
		output+="Receptive Field size: "+proxSegment.synapses.size()+"\n";
		int row=parentRegion.inputRegion.row;
		int column=parentRegion.inputRegion.column;
		boolean[][] outputMatrix=new boolean[row][column];
		int countConnectedSynapses=0;
		for(Synapse s: proxSegment.synapses){
			//print only connected Synapses
			if(s.isConnected()){
				outputMatrix[s.destCoor[0]][s.destCoor[1]]=true;
				countConnectedSynapses++;
			}
			//print all synapses
			//outputMatrix[s.destCoor[0]][s.destCoor[1]]=true;
		}
		output+="Connected Synapses: "+countConnectedSynapses+"\n";
		/*
		//print out all synapses
		for(int i=0; i< row;i++){
			for(int j=0; j< column; j++){
				if(outputMatrix[i][j]==true)output+="1";
				else output+="0";
			}
			output+="\n";
		}
		*/
		
		//Neighbor
		output+="Neighbor size: "+neighbor.size()+"\n";
		/*
		//print out all neighbor
		boolean[][] neighborMatrix=new boolean[region.row][region.column];
		for(Column c : neighbor){
			neighborMatrix[c.posRow][c.posColumn]=true;
		}
		for(int i=0; i< region.row;i++){
			for(int j=0; j< region.column; j++){
				if(neighborMatrix[i][j]==true)output+="1";
				else output+="0";
			}
			output+="\n";
		}
		*/
		for(int i=0; i<Layer;i++){
			if(cells[i].tLearnState==true){
				output+="\nLearning Cell "+i+";\n"+cells[i].toString();
			}
		}
		return(output);
	}
	
	public Column(int r,int c,Region re){
		posRow=r;
		posColumn=c;
		parentRegion=re;
		//inhibitionRadius=i;
		for(int j=0; j<Layer; j++){
			cells[j]=new Cell();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}


}
