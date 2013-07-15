package htm;

import java.util.ArrayList;
import java.util.LinkedList;

public class Column {
	//parameters
	static final int Cycle=100;
	static final int Layer=5;
	static final int MinOverlap=2;
	static final double MinOverlapRatio=0.005;
	static final double DesiredLocalActivity=0.05;
	static final double ActiveDutyCycleFraction=1.0;
	static final double OverlapDutyCycleFraction=1.0;
	static final int DutyCycle=100;
	static final double BoostStep=2.0;
	
	public int posRow;
	public int posColumn;
	public Region region;
	public Cell[] cells=new Cell[Layer];
	public ArrayList<Synapse> ReceptiveField=new ArrayList<Synapse>();
	public double boost=1.0;
	public int overlap=0;
	public ArrayList<Column> neighbor= new ArrayList<Column>();
	public LinkedList<Integer> activeQueue = new LinkedList<Integer>();
	public boolean activate;
	public LinkedList<Integer> overlapQueue=new LinkedList<Integer>();
	public int inhibitionRadius;
	
	
	public void addSynapse(int r,int c){
		Synapse s = new Synapse(r,c);
		if(!ReceptiveField.contains(s)){
			this.ReceptiveField.add(s);
			//System.out.println("add"+x+", "+y);
		}
	}
	
	public void addNeighbor(Region region,ArrayList<int[]> neighborCoor){
		this.neighbor = new ArrayList<Column>();
		for(int[] coor : neighborCoor){
			int r = Math.max(Math.min(region.row-1,posRow+coor[0]),0);
			int c = Math.max(Math.min(region.column-1,posColumn+coor[1]),0);		
			if(!this.neighbor.contains(region.columns[r][c]) && !(r==posRow && c==posColumn))
					this.neighbor.add(region.columns[r][c]);
		}
	}
	
	public void calculateOverlap(boolean[][] input, int t){
		//!!!!!reset those values here or at the end of temporal pooling
		overlap=0;
		activate=false;
		for(Synapse s : ReceptiveField){
			if(s.isValid() && input[s.destinyCoor[0]][s.destinyCoor[1]]){
				overlap+=1;							
			}
		}
		//if(overlap<MinOverlap){
		//System.out.print((int)ReceptiveField.size()*MinOverlapRatio);
		if(overlap >= (int)ReceptiveField.size()*MinOverlapRatio){
			overlap=(int)(overlap*boost);
			overlapQueue.offer(t);
		}else if(overlap>0) overlap=-1;
	}

	public boolean activated(){
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
			int maxLocalRank= (int) Math.round(neighbor.size()*DesiredLocalActivity);
			//test display
			//System.out.print(maxLocalRank);
			//System.out.print("["+this.posRow+","+this.posColumn+"];");
			
			if(ahead+tie<=maxLocalRank){
				activate=true;
				return true;
			}else if(ahead<maxLocalRank){
				if(Math.random()<(double)1/tie){
					activate=true;
					return true;
				}
			}
		}
		return false;
	}
	
	private int maxDutyCycle(){
		int max=0;
		for(Column c : neighbor){
			if(c.activeQueue.size()>max){
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
	
	public void adjustBoost(int t){
		//Boost Column overlap
		//update activeQueue
		int minActiveDutyCycle = (int)ActiveDutyCycleFraction*this.maxDutyCycle();
		if(t>DutyCycle && activeQueue.size()>0 && activeQueue.get(0)<t-DutyCycle) activeQueue.poll();
		if (activate) activeQueue.offer(t);
		//boost overlap value
		if (activeQueue.size() >= minActiveDutyCycle) this.boost=1.0;
		else if(overlap!=0) boost+=(minActiveDutyCycle-activeQueue.size())/minActiveDutyCycle*BoostStep;
		
		//Boost  permanence
		//update overlapQueue
		int minOverlapDutyCycle=(int)OverlapDutyCycleFraction*this.maxOverlapDutyCycle();
		if(t>DutyCycle && overlapQueue.size()>0 && overlapQueue.get(0)<t-DutyCycle) overlapQueue.poll();
		//boost all synapses in the receptive field
		if(overlapQueue.size() < minOverlapDutyCycle){
			for(Synapse s : ReceptiveField){
				s.increasePermanence();
			}
		}
	}

	public double connectedReceptiveSize(double rowFactor, double columnFactor){
		double size=0;
		for(Synapse s : ReceptiveField){
			if(s.isValid()){
				double dist=(s.destinyCoor[0]*rowFactor-posRow)*(s.destinyCoor[0]*rowFactor-posRow)+
						(s.destinyCoor[1]*columnFactor-posColumn)*(s.destinyCoor[1]*columnFactor-posColumn);
				dist=Math.sqrt(dist);
				if(dist>size) size=dist;
			}
		}
		//System.out.print(size);
		return size;
	}
	
	public String toString(){
		String output="\n";
		output+="["+posRow+", "+posColumn+"]";
		if(activate) output+="\tActive\n";
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
		
		output+="Receptive Field size: "+ReceptiveField.size()+"\n";
		for(Synapse s : ReceptiveField){
			output += "("+s.destinyCoor[0]+",";
			output += s.destinyCoor[1]+"),";
		}
		//Neighbor
		output+="\nNeighbor size: "+neighbor.size()+"\n";
		for(Column c : neighbor){
			output += "("+c.posRow+","+c.posColumn+"),";
		}
		
		return(output);
	}
	
	public Column(int r,int c,Region re,int i){
		posRow=r;
		posColumn=c;
		region=re;
		inhibitionRadius=i;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}

}
