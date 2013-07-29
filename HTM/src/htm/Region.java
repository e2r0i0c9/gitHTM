package htm;

import java.util.ArrayList;
//import java.util.HashMap;
import util.NeighborMap;

public class Region {
	static final int ReceptiveFieldRadius=10;
	static final int DefaultInhibitionRadius=10;
	
	public Region inputRegion;
	boolean[][] inputMatrix;
	public int row;
	public int column;
	public Column[][] columns;
	public ArrayList<Column> activeColumns=new ArrayList<Column>();
	public ArrayList<ArrayList<int[]>> output = new ArrayList<ArrayList<int[]>>();
	public ArrayList<Synapse> newSynapsesSpace = new ArrayList<Synapse>();
	
	public int timeStep=0;
	
	public void setNeighbor(NeighborMap neighborMap,int Radius){
		for(int r=0;r<row;r++){
			for(int c=0;c<column;c++){
				//initiate neighbor
				this.columns[r][c].setNeighbor(this,neighborMap.get().get(Radius));
			}
		}
	}
	
	public void overlap(boolean[][] input){
		inputMatrix=input;
		//calculate overlap
		for(int i=0;i<row;i++){
			for(int j=0;j<column;j++){
				columns[i][j].calculateOverlap(inputMatrix,timeStep);
			}
		}
	}
	
	public void inhibition(){
		activeColumns=new ArrayList<Column>();
		for(int r=0;r<row;r++){
			for(int c=0;c<column;c++){
				if(columns[r][c].isActive(timeStep)){
					activeColumns.add(columns[r][c]);
				}
			}
		}
	}
	
	public void spatialLearning(){
		if(activeColumns.size()>0){
			for(Column c : activeColumns){
				if(c.overlap!=0){
					for(Synapse s : c.proxSegment.synapses){
						if(inputMatrix[s.destCoor[0]][s.destCoor[1]]) s.permanenceInc();
						else s.permanenceDec();
					}
				}
			}
		}
		
		for(int r=0;r<row;r++){
			for(int c=0;c<column;c++){
				columns[r][c].adjustBoost(timeStep);
			}
		}
	}
	
	public int averageReceptiveFieldSize(){
		double rowFactor = (double)row/inputRegion.row;
		double columnFactor = (double)column/inputRegion.column;
		double[][] receptiveFieldSize = new double[row][column];
		for(int r=0;r<row;r++){
			for(int c=0;c<column;c++){
				receptiveFieldSize[r][c]=columns[r][c].connectedRFSize(rowFactor, columnFactor);
			}
		}
		double average=0;
		for(int r=0;r<row;r++){
			double rowAverage=0;
			for(int c=0;c<column;c++){
				rowAverage+=receptiveFieldSize[r][c];
			}
			rowAverage/=column;
			average+=rowAverage;
		}
		average/=row;
		
		//System.out.print((int)average);
		
		return (int)average;
	}
	
	private void setSegmentState() {
		//segment activity only depend on previous time step, for time t only need to calculate once at the beginning
		for(int r=0; r<row;r++){
			for(int c=0; c<column; c++){
				for(Cell cell: columns[r][c].cells){
					if(cell.distSegments.size()>0){
						for(Segment seg : cell.distSegments){
							seg.activeState=seg.segmentActive(true);
							seg.learnState=seg.segmentActive(false);
						}
					}
				}
			}
		}
	}
	
	public void setCellState() {
		for(Column col : activeColumns){
			boolean buPredicted=false;//bottom up predicted
			boolean lcChosen=false;//learning cell chosen
			
			for(Cell cell : col.cells){
				if(cell.pPredictiveState == true){
					ArrayList<Segment> activeSegments = cell.getActiveSegments();
					if(activeSegments.size() > 0){
						for(Segment seg : activeSegments){
							if(seg.sequenceSegment == true){
								buPredicted = true;
								cell.tActiveState = true;
								//This segment is active due to learning cell?
								if(seg.learnState){
									lcChosen = true;
									cell.tLearnState = true;
								}
							}
						}
					}
				}
			}
			
			if(buPredicted == false){
				for(Cell cell : col.cells){
					cell.tActiveState=true;
				}
				System.out.print("L");
			}
			
			if(lcChosen == false){
				Cell bestMatchingCell = col.getBestMatchingCell();
				bestMatchingCell.tLearnState=true;
			}
		}
	}
	
	//for each time step there is a fix new synapses space
	public void newSynapsesSpace(){
		for(int i=0; i<row;i++){
			for(int j=0; j<column; j++){
				for(int k=0; k<columns[i][j].cells.length; k++){
					if(columns[i][j].cells[k].pLearnState == true){
						newSynapsesSpace.add(new Synapse(this,i,j,k));
					}
				}
			}
		}
	}
	
	public void calculatePredictiveState(){
		for(int r=0; r<row;r++){
			for(int c=0; c<column; c++){
				for(Cell cell: columns[r][c].cells){
					cell.calculatePredictiveState(this);
				}
			}
		}
	}
	
	public void temporalLearning(){
		for(int r=0; r<row;r++){
			for(int c=0; c<column; c++){
				for(Cell cell: columns[r][c].cells){
					if(cell.tLearnState==true){
						cell.adaptSegments(true);
						System.out.print("Y");
					}
					else if(cell.tPredictiveState == false && cell.pPredictiveState == true){
						cell.adaptSegments(false);
						System.out.print("N");
					}
				}
			}
		}
	}
	
	private void updateState() {
		//before go to next time step move every tState to pState and tState to false
		newSynapsesSpace = new ArrayList<Synapse>();
		for(int r=0; r<row;r++){
			for(int c=0; c<column; c++){
				for(Cell cell: columns[r][c].cells){
					cell.pActiveState=cell.tActiveState;
					cell.tActiveState=false;
					cell.pLearnState=cell.tLearnState;
					cell.tLearnState=false;
					cell.pPredictiveState=cell.tPredictiveState;
					cell.tPredictiveState=false;
				}
			}
		}
	}
	
  
 	public void print(int t){
		//System.out.print(output.size());
		if(t>output.size()-1){
			System.out.print("There are only "+output.size()+" input time step");
		}
		else{
			boolean[][] outputMatrix=new boolean[row][column];
			for(int[] coor : output.get(t)){
				outputMatrix[coor[0]][coor[1]]=true;
			}
			for(int i=0; i< row;i++){
				for(int j=0; j< column; j++){
					if(outputMatrix[i][j]==true)System.out.print(1);
					else System.out.print(0);
				}
				System.out.print("\n");
			}
		}
	}
	
	public Region(int m,int n, int time){
		this.row=m;
		this.column=n;
		//draw something
		//draw a moving bar watch out for out of boundary
		int length=8;
		for(int i= 0; i<time ;i++){
			output.add(i, new ArrayList<int[]>());
		}
		
		for(int bar=0; bar<time/20; bar++){
			int intercept=20*bar;
			for(int i=intercept;i<time;i++){
				for(int j=0;j<length;j++){
					if(i+j-intercept<column && i+j-intercept>=0){
						output.get(i).add(new int[]{j,i+j-intercept});
					}
				}
			}
		}
		
		for(int i=0;i<30;i++){
			for(int j=0;j<length;j++){
				output.get(460+i).add(new int[]{j,i-j+length});
			}
		}
		
	}
	
	public Region(int m,int n,Region input, NeighborMap neighborMap){ 
		this.inputRegion=input;
		this.row=m;
		this.column=n;
		//get a m*n array with null in it
		this.columns = new Column[m][n];
		//initialize columns and set default neighbors and FIXED Receptive Field space
		for(int r=0;r<row;r++){
			for(int c=0;c<column;c++){
				this.columns[r][c] = new Column(r,c,this);
				//add synapses with a bias towards natural center
				Double RFCenterRow=(double) (r*input.row/row);
				Double RFCenterColumn=(double) (c*input.column/column);
				for(int ir=0;ir<input.row;ir++){//ir input row, ic input column
					for(int ic=0;ic<input.column;ic++){
						double dist = 0.2*Math.sqrt((ir-RFCenterRow)*(ir-RFCenterRow)+(ic-RFCenterColumn)*(ic-RFCenterColumn)+0.01);
						double bias=Math.exp(-1*dist);
						if(bias>Math.random()){
							this.columns[r][c].proxSegment.addSynapse(input,ir,ic,bias);
						}
					}
				}
				/*
				int top = Math.max(RFCenterRow.intValue(),0);
				int bottom = Math.min(top+2*ReceptiveFieldRadius,input.row-1);
				int left = Math.max(RFCenterColumn.intValue(),0);
				int right = Math.min(left+2*ReceptiveFieldRadius,input.column-1);
				//Set Receptive field, add synapses of input (row and column)
				for(int ir=top;ir<=bottom;ir++){//ir input row, ic input column
					for(int ic=left;ic<=right;ic++){
						this.columns[r][c].addSynapse(ir,ic);
					}
				}
				*/
			}
		}
		//set inhibition radius
		this.setNeighbor(neighborMap, DefaultInhibitionRadius);
	}

	public String toString(){
		/*
		String s ="Active State:\n";
		
		for(int r=0; r<row;r++){
			for(int c=0; c<column; c++){
				if(columns[r][c].active==true){
					s+="1(";
				}else s+="0(";
				for(Cell cell : columns[r][c].cells){
					if(cell.tActiveState==true)s+="1";else s+="0";
					//if(cell.tLearnState==true)s+="1];";else s+="0];";	
				}
				s+=")";
			}
			s+="\n";
		}*/
		
		String s ="Predictive State:\n";
		
		for(int r=0; r<row;r++){
			for(int c=0; c<column; c++){
				if(columns[r][c].active==true){
					s+="1";
				}else s+="0";
				for(int k=0; k<columns[r][c].cells.length;k++){
					if(columns[r][c].cells[k].tPredictiveState==true){
						s+="["+k+",1]";
					}
					//if(cell.tLearnState==true)s+="1];";else s+="0];";	
				}
			}
			s+="\n";
		}
		/*
		s +="\nLearnState Column:\n";
		for(int r=0; r<row;r++){
			for(int c=0; c<column; c++){
				boolean found=false;
				for(Cell cell : columns[r][c].cells){
					if(cell.tLearnState==true){
						s+="1";
						found=true;
						break;
					}
				}
				if(found == false)s+="0";
			}
			s+="\n";
		}
		*/
		
		
		return s;
	}
	public static void main(String[] args) {
		int totalTime = 500;
		//Initialize input region
		Region inputRegion = new Region(200,150,totalTime);
		//print region output at specific time stamp
		//inputRegion.print(480);
		
		NeighborMap neighborMap = new NeighborMap(40);
		//Initialize HTM region
		
		Region htmRegion = new Region(50,50,inputRegion,neighborMap);
		
		for(int time=0; time<totalTime; time++){
			if(time==100){
				System.out.print("!");
			}
			//set time
			htmRegion.timeStep=time;
			//restore 2D array input
			boolean[][] input=new boolean[inputRegion.row][inputRegion.column];
			for(int[] coor : inputRegion.output.get(time)){
				input[coor[0]][coor[1]]=true;
			}
			
			//Spatial Pooling
			htmRegion.overlap(input);
			htmRegion.inhibition();
			htmRegion.spatialLearning();
			//Adding too many(2000+) neighbor per column is the major time cost
			htmRegion.setNeighbor(neighborMap, htmRegion.averageReceptiveFieldSize());
			
			//Temporal Pooling
			//based on input(t) set activeState and learnState for each segment
			htmRegion.setSegmentState();
			//for each column set cell activeState and chose a learning cell
			htmRegion.setCellState();
			System.out.print("\n");
			//
			htmRegion.calculatePredictiveState();
			System.out.print("\n");
			htmRegion.temporalLearning();		
			htmRegion.updateState();
			
			System.out.print("\nt="+time+";\n");
		}
		
		System.out.print("finish");

	}

	

	

}
