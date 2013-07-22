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
	
	
	public int timeStep=0;
	
	
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
	
	public void setNeighbor(NeighborMap neighborMap,int Radius){
		for(int r=0;r<row;r++){
			for(int c=0;c<column;c++){
				//initiate neighbor
				this.columns[r][c].setNeighbor(this,neighborMap.get().get(Radius));
			}
		}
	}
	
	public void setCellActiveState() {
		for(Column col : activeColumns){
			boolean buPredicted=false;
			boolean lcChosen=false;
			
			for(Cell cell : col.cells){
				if(cell.pPredictiveState==true){
					ArrayList<Segment> activeSegments = cell.getActiveSegments();
					if(activeSegments.size()>0){
						for(Segment seg : activeSegments){
							if(seg.sequenceSegment==true){
								buPredicted=true;
								cell.tActiveState=true;
								//This segment is active due to learning cell?
								if(seg.segmentActive(false)){
									lcChosen=true;
									cell.tLearnState=true;
								}
							}
						}
					}
				}
			}
			
			if(buPredicted==false){
				for(Cell cell : col.cells){
					cell.tActiveState=true;
				}
			}
			
			if(lcChosen==false){
				Cell bestMatchingCell = col.getBestMatchingCell();
				bestMatchingCell.tLearnState=true;
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
					//cell.temporalLearning();
					if(cell.tLearnState==true){
						
					}
					else if(){
						
					}
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
		for(int i= 0; 8+i<Math.min(column,8+time) ;i++){
			output.add(i, new ArrayList<int[]>());
			output.get(i).add(new int[]{0,0+i});
			output.get(i).add(new int[]{1,1+i});
			output.get(i).add(new int[]{2,2+i});
			output.get(i).add(new int[]{3,3+i});
			output.get(i).add(new int[]{4,4+i});
			output.get(i).add(new int[]{5,5+i});
			output.get(i).add(new int[]{6,6+i});
			output.get(i).add(new int[]{7,7+i});
			output.get(i).add(new int[]{8,8+i});
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
				this.columns[r][c] = new Column(r,c,this,DefaultInhibitionRadius);
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

	public static void main(String[] args) {
		int totalTime = 120;
		//Initialize input region
		Region inputRegion = new Region(100,150,totalTime);
		//print region output at specific time stamp
		//inputRegion.print(142);
		
		NeighborMap neighborMap = new NeighborMap(40);
		//Initialize HTM region
		
		Region htmRegion = new Region(50,50,inputRegion,neighborMap);
		
		for(int time=0; time<totalTime; time++){
			if(time==100){
				System.out.print("!");
			}
				
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
			htmRegion.setCellActiveState();
			htmRegion.calculatePredictiveState();
			//htmRegion.temporalLearning();
			
			System.out.print(" t="+time+";\n");
		}
		
		System.out.print("finish");

	}

	

}
