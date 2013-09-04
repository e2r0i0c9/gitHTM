package htm;

import java.util.ArrayList;
import java.util.Arrays;
//import java.util.HashMap;
import util.NeighborMap;

public class Region {
	static final int ReceptiveFieldRadius=5;
	static final int DefaultInhibitionRadius=5;
	
	public Region inputRegion;
	boolean[][] inputMatrix;
	public int row;
	public int column;
	public Column[][] columns;
	public ArrayList<Column> activeColumns=new ArrayList<Column>();
	public ArrayList<ArrayList<int[]>> output = new ArrayList<ArrayList<int[]>>();
	public ArrayList<Synapse> tNewSynapsesSpace = new ArrayList<Synapse>();
	public ArrayList<Synapse> pNewSynapsesSpace = new ArrayList<Synapse>();
	
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
							//seg.activeState=seg.segmentActive(true);
							//seg.learnState=seg.segmentActive(false);
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
								System.out.print("R");
								buPredicted = true;
								cell.tActiveState = true;
								//This segment is active due to learning cell?
								if(seg.segmentActive(false, false)){
									lcChosen = true;
									cell.tLearnState = true;
									cell.segmentUpdateList.add(new SegmentUpdate(seg,seg.getActiveSynapses(false, false, null)));
								}
							}
						}
					}
				}
				//Reset sequenceSegment flag when it did not predict this input
				if(cell.distSegments.size()>0){
					for(Segment seg : cell.distSegments){
						seg.sequenceSegment=false;
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
	
	public ArrayList<Synapse> getNewSynapsesSpace(boolean timeT){
		if(timeT) return this.tNewSynapsesSpace;
		else return this.pNewSynapsesSpace;
	}
	//for each time step there is a fix new synapses space
	public void newSynapsesSpace(boolean timeT){
		for(int i=0; i<row;i++){
			for(int j=0; j<column; j++){
				for(int k=0; k<columns[i][j].cells.length; k++){
					if(columns[i][j].cells[k].getLearnState(timeT) == true) this.getNewSynapsesSpace(timeT).add(new Synapse(this,i,j,k));
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
					else if(cell.tActiveState == false && cell.pPredictiveState == true){
						cell.adaptSegments(false);
						System.out.print("N");
					}
					//reset update list for all cells
					cell.segmentUpdateList=new ArrayList<SegmentUpdate>();
				}
			}
		}
	}
	
	private void updateState() {
		//before go to next time step move every tState to pState and tState to false
		pNewSynapsesSpace = tNewSynapsesSpace;
		tNewSynapsesSpace = new ArrayList<Synapse>();
		activeColumns=new ArrayList<Column>();
		for(int r=0; r<row;r++){
			for(int c=0; c<column; c++){
				columns[r][c].overlap=0;
				columns[r][c].active=false;
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
					if(outputMatrix[i][j]==true)System.out.print("<!>");
					else System.out.print("   ");
				}
				System.out.print("\n");
			}
		}
	}
 	
 	private void barInput(int time){
 		//draw a moving bar watch out for out of boundary
 		int length=8;
		
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
			
		//add anomaly
		for(int i=0;i<30;i++){
			for(int j=0;j<length;j++){
				//output.get(460+i).add(new int[]{j,i-j+length});
			}
		}
 	}
 	
	private void letterInput(int time){
		int[][] A = new int[][]{
			new int[]{0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0},
			new int[]{0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
			new int[]{0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0},
			new int[]{0,0,0,0,0,0,0,1,1,0,1,1,0,0,0,0,0,0,0,0},
			new int[]{0,0,0,0,0,0,1,1,0,0,0,1,1,0,0,0,0,0,0,0},
			new int[]{0,0,0,0,0,0,1,1,0,0,0,1,1,0,0,0,0,0,0,0},
			new int[]{0,0,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,0,0},
			new int[]{0,0,0,0,0,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0},
			new int[]{0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0},
			new int[]{0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0},
			new int[]{0,0,0,0,1,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0},
			new int[]{0,0,0,1,1,1,0,0,0,0,0,0,0,0,1,1,1,0,0,0},
			new int[]{0,0,0,1,1,1,0,0,0,0,0,0,0,0,1,1,1,0,0,0},
			new int[]{0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0},
			new int[]{0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0},
			new int[]{0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0},
			new int[]{0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0},
			new int[]{0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0},
			new int[]{1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1},
			new int[]{1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1}
		};
		
		int[][] B = new int[][]{
			new int[]{0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			new int[]{0,1,1,1,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0},
			new int[]{0,1,1,1,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0},
			new int[]{0,1,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0},
			new int[]{0,1,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0},
			new int[]{0,1,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0},
			new int[]{0,1,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0},
			new int[]{0,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
			new int[]{0,1,1,1,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0},
			new int[]{0,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0},
			new int[]{0,1,1,1,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0},
			new int[]{0,1,1,1,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0},
			new int[]{0,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0},
			new int[]{0,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0},
			new int[]{0,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0},
			new int[]{0,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0},
			new int[]{0,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0},
			new int[]{0,1,1,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0},
			new int[]{0,1,1,1,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0},
			new int[]{0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0}
			};
		
		int[][] C = new int[][]{
			new int[]{0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0},
			new int[]{0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0},
			new int[]{0,0,0,0,0,1,1,1,0,0,0,0,0,1,1,1,0,0,0,0},
			new int[]{0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			new int[]{0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			new int[]{0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			new int[]{0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			new int[]{1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			new int[]{1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			new int[]{1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			new int[]{1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			new int[]{1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			new int[]{1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			new int[]{0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
			new int[]{0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1},
			new int[]{0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0},
			new int[]{0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0},
			new int[]{0,0,0,1,1,1,0,0,0,0,0,0,0,0,1,1,1,0,0,0},
			new int[]{0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0},
			new int[]{0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0}
			};
			
	    ArrayList<int[]> letterA = new ArrayList<int[]>();
		for(int i=0; i<A.length;i++){
			for(int j=0; j<A[i].length; j++){
				if(A[i][j]==1) letterA.add(new int[]{i,j});
			}
		}
		ArrayList<int[]> letterB = new ArrayList<int[]>();
		for(int i=0; i<B.length;i++){
			for(int j=0; j<B[i].length; j++){
				if(B[i][j]==1) letterB.add(new int[]{i,j});
			}
		}
		ArrayList<int[]> letterC = new ArrayList<int[]>();
		for(int i=0; i<C.length;i++){
			for(int j=0; j<C[i].length; j++){
				if(C[i][j]==1) letterC.add(new int[]{i,j});
			}
		}
		/*
		for(int i=0; i<output.size();i++){
			if(i%3==0) output.set(i, letterA);
			else if(i%3==1) output.set(i, letterB);
			else if(i%3==2) output.set(i, letterA);
		}*/
		
		for(int i=0; i<output.size();i++){
			if(i%5==0) output.set(i, letterA);
			else if(i%5==1) output.set(i, letterB);
			else if(i%5==2) output.set(i, letterC);
			else if(i%5==3) output.set(i, letterA);
			else if(i%5==4) output.set(i, letterC);
		}
		/*
		for(int i=0; i<output.size();i++){
			if(i%10==0) output.set(i, letterA);
			else if(i%10==1) output.set(i, letterB);
			else if(i%10==2) output.set(i, letterC);
			else if(i%10==3) output.set(i, letterA);
			else if(i%10==4) output.set(i, letterB);
			else if(i%10==5) output.set(i, letterA);
			else if(i%10==6) output.set(i, letterA);
			else if(i%10==7) output.set(i, letterA);
			else if(i%10==8) output.set(i, letterC);
			else if(i%10==9) output.set(i, letterB);
		}*/
	}
 	
	public Region(int m,int n, int time){
		this.row=m;
		this.column=n;
		for(int i= 0; i<time ;i++){
			output.add(i, new ArrayList<int[]>());
		}
		//draw something
		//barInput(time);
		letterInput(time);
		
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
	
	public Region(){
		
	}
	
	public String toString(){
		String str ="";
		
		str+="Active State:\n";
		for(int r=0; r<row;r++){
			for(int c=0; c<column; c++){
				if(columns[r][c].active == true)str+="1(";
				else str+="0(";
				for(Cell cell : columns[r][c].cells){
					if(cell.tActiveState==true)str+="1";else str+=" ";
					//if(cell.tPredictiveState==true)str+="1";else str+=" ";
					//if(cell.tLearnState==true)str+="1";else str+=" ";	
				}
				str+=")";
			}
			str+="\n";
		}
		str+="Active Column: ";
		for(Column col : this.activeColumns){
			str+="["+col.posRow+","+col.posColumn+"];";
		}
		int[][] prediction =new int[this.inputRegion.row][this.inputRegion.column];
		for(int r=0; r<row;r++){
			for(int c=0; c<column; c++){
				for(Cell cell : columns[r][c].cells){
					if(cell.tPredictiveState==true){
						for(Synapse s : columns[r][c].proxSegment.synapses){
							if(s.isConnected())prediction[s.destCoor[0]][s.destCoor[1]]++;
						}
					}
				}
			}
		}
		
		str+="\nNext Step Prediction:\n";
		for(int i=0;i<this.inputRegion.row;i++){
			for(int j=0; j<this.inputRegion.column;j++){
				if(prediction[i][j]>0)str+=prediction[i][j]+",";
				else str+=" ";
			}
			str+="\n";
		}
		return str;
	}
	
	public static void main(String[] args) {
		int totalTime = 2000;
		//Initialize input region
		Region inputRegion = new Region(20,20,totalTime);
		//print region output at specific time stamp
		//inputRegion.print(3);
		
		NeighborMap neighborMap = new NeighborMap(40);
		//Initialize HTM region
		
		Region htmRegion = new Region(20,20,inputRegion,neighborMap);
		
		//spatial pooler analyze
		Region spatialPoolA= new Region();
		Region spatialPoolB= new Region();
		
		for(int time=0; time<totalTime; time++){
			System.out.print("\nt="+time+";\n");
			
			if(time==1900){
				System.out.print("!");
			}
			//set time
			htmRegion.timeStep=time;
			//restore 2D array input
			boolean[][] input=new boolean[inputRegion.row][inputRegion.column];
			for(int[] coor : inputRegion.output.get(time)){
				input[coor[0]][coor[1]]=true;
			}
			//Spatial Pooling with learning
			if(time<300){
				htmRegion.overlap(input);
				htmRegion.inhibition();
				htmRegion.spatialLearning();
				//Adding too many(2000+) neighbor per column is the major time cost
				int inhibitionRadius = htmRegion.averageReceptiveFieldSize();
				System.out.print("New Inhibition Radius:"+ inhibitionRadius+"\n");
				//htmRegion.setNeighbor(neighborMap, inhibitionRadius);
			}else{
				//Spatial Pooling with learning turned OFF
				htmRegion.overlap(input);
				htmRegion.inhibition();
			}
			
			//spatial pooler analyze
			//how much time does it need to have a steady boost
			/*
			for(int i=0; i<htmRegion.row;i++){
				System.out.print("\n");
				for(int j=0; j<htmRegion.column;j++){
					System.out.print(String.format("%.2f", htmRegion.columns[i][j].boost)+";");
				}
			}*/
			System.out.print("\nabnormal boost");
			for(int r=0; r<htmRegion.row;r++){
				for(int c=0 ; c<htmRegion.column;c++){
					if(htmRegion.columns[r][c].boost>5){
						System.out.print("["+r+","+c+"]");
					}
				}
			}
			System.out.print("\n");
			
			//if(time>300 && !(time%3==1)){
			if(time>300 && (time%5==0 || time%5==3)){
				if(spatialPoolA.activeColumns.size()==0) spatialPoolA.activeColumns=htmRegion.activeColumns;
				else{
					for(Column col : htmRegion.activeColumns){
						if(!spatialPoolA.activeColumns.contains(col)){
							spatialPoolA.activeColumns.add(col);
						}
					}
				}
				System.out.print("active columnA%: "+(double)htmRegion.activeColumns.size()/spatialPoolA.activeColumns.size()+"\n");
			}
			
			//if(time>300 && time%3==1){
			if(time>300 && (time%5==2 || time%5==4)){
			//if(time>300 && time%5==1){
				if(spatialPoolB.activeColumns.size()==0) spatialPoolB.activeColumns=htmRegion.activeColumns;
				else{
					for(Column col : htmRegion.activeColumns){
						if(!spatialPoolB.activeColumns.contains(col)){
							spatialPoolB.activeColumns.add(col);
						}
					}
				}
				System.out.print("active columnB%: "+(double)htmRegion.activeColumns.size()/spatialPoolB.activeColumns.size()+"\n");
			}
			int count=0;
			ArrayList<Column> overlap = new ArrayList<Column>();
			for(Column col : spatialPoolA.activeColumns){
				if(spatialPoolB.activeColumns.contains(col)){
					overlap.add(col);
					count++;
				}
			}
			System.out.print("Overlap ["+spatialPoolA.activeColumns.size()+","+count+","+spatialPoolB.activeColumns.size()+"]");
			
			
			//Temporal Pooling
			if(time>=1000){
				//inputRegion.print(time);
				//for each column set cell activeState and chose a learning cell
				htmRegion.setCellState();
				System.out.print("\nNew Synapses Space t: "+htmRegion.tNewSynapsesSpace.size()+"\n");
				System.out.print("New Synapses Space t-1: "+htmRegion.pNewSynapsesSpace.size()+"\n");
				//based on input(t) set activeState and learnState for each segment
				//htmRegion.setSegmentState();
				htmRegion.calculatePredictiveState();
				System.out.print("\n");
				htmRegion.temporalLearning();
			}
					
			htmRegion.updateState();
		}
		
		System.out.print("finish");
		
	}

	

	

}
