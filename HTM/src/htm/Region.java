package htm;

import io.Input;
import java.util.ArrayList;
//import java.util.HashMap;
import util.NeighborMap;

public class Region {
	static final int ReceptiveFieldRadius=10;
	static final int DefaultInhibitionRadius=10;
	
	
	public int row;
	public int column;
	public Column[][] columns;
	public ArrayList<Column> activeColumns=new ArrayList<Column>();
	
	
	public void overlap(boolean[][] input,int t){
		for(int i=0;i<row;i++){
			for(int j=0;j<column;j++){
				columns[i][j].calculateOverlap(input,t);
			}
		}
	}
	
	public void inhibition(){
		activeColumns=new ArrayList<Column>();
		for(int r=0;r<row;r++){
			for(int c=0;c<column;c++){
				if(columns[r][c].activated()){
					activeColumns.add(columns[r][c]);
				}
			}
		}
	}
	
	public void learning(boolean[][] input,int t){
		if(activeColumns.size()>0){
			for(Column c : activeColumns){
				if(c.overlap!=0){
					for(Synapse s : c.ReceptiveField){
						if(input[s.destinyCoor[0]][s.destinyCoor[1]]) s.permanenceInc();
						else s.permanenceDec();
					}
				}
			}
		}
		
		
		for(int r=0;r<row;r++){
			for(int c=0;c<column;c++){
				columns[r][c].adjustBoost(t);
			}
		}
		
	}
	
	public int averageReceptiveFieldSize(double rowFactor, double columnFactor){
		double[][] receptiveFieldSize = new double[row][column];
		for(int r=0;r<row;r++){
			for(int c=0;c<column;c++){
				receptiveFieldSize[r][c]=columns[r][c].connectedReceptiveSize(rowFactor, columnFactor);
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
		
		return (int)average;
	}
	
	public void setNeighbor(NeighborMap neighborMap,int Radius){
		for(int r=0;r<row;r++){
			for(int c=0;c<column;c++){
				//initiate neighbor
				this.columns[r][c].addNeighbor(this,neighborMap.get().get(Radius));
			}
		}
	}
	
	public Region(int m,int n,Input input){ 
		this.row=m;
		this.column=n;
		//get a m*n array with null in it
		this.columns = new Column[m][n];
		//initialize columns and set default neighbors and FIXED Receptive Field space
		for(int r=0;r<row;r++){
			for(int c=0;c<column;c++){
				this.columns[r][c] = new Column(r,c,this,DefaultInhibitionRadius);
				Double RFCenterRow=(double) (r*(input.row-2*ReceptiveFieldRadius)/row);
				Double RFCenterColumn=(double) (c*(input.column-2*ReceptiveFieldRadius)/column);
				
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
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int time = 200;
		Input input = new Input(100,350,time);
		Region region = new Region(50,50,input);
		int t=10;
		System.out.print(input.toString(t));
		
		region.overlap(input.get(t),t);
		
		System.out.print("finish");

	}

}
