/**
 * 
 */
package io;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Hongjie
 *
 */
public class Input {
	public int row;
	public int column;
	public int time;
	public ArrayList<boolean[][]> data;
	
	public boolean[][] get(int t){
		return data.get(t);
	}
	
	public String toString(int t){
		String s="";
		boolean[][] d=data.get(t);
		
		for(int i=0;i<row;i++){
			for(int j=0;j<column;j++){
				if(d[i][j]){
					s+="1";
				}
				else{
					s+="0";
				}
			}
			s+="\n";
		}
		return s;
	}
	
	public Input(int row, int column, int time){
		this.row=row;
		this.column=column;
		this.time=time;
		
		ArrayList<boolean[][]> inputData = new ArrayList<boolean[][]>(time);
		for(int i=0;i<time;i++){
			inputData.add(i, new boolean[row][column]);
		}
		//draw something
		//draw a moving bar watch out for out of boundary
		for(int i= 0; 8+i<Math.min(column,8+time) ;i++){
			inputData.get(i)[0][0+i]=true;
			inputData.get(i)[1][1+i]=true;
			inputData.get(i)[2][2+i]=true;
			inputData.get(i)[3][3+i]=true;
			inputData.get(i)[4][4+i]=true;
			inputData.get(i)[5][5+i]=true;
			inputData.get(i)[6][6+i]=true;
			inputData.get(i)[7][7+i]=true;
			inputData.get(i)[8][8+i]=true;
		}
		
		//save as input data
		this.data=inputData;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Input input = new Input(30,50,200);
		//System.out.println(Arrays.deepToString(input.data.get(0)));
		System.out.print(input.toString(5));
	}

}
