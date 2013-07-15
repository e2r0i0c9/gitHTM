package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NeighborMap {
	private HashMap<Integer,ArrayList<int[]>> neighborMap=new HashMap<Integer,ArrayList<int[]>>();
	
	private ArrayList<int[]> intNeighbor(int radius){
		ArrayList<int[]> intNeighborList=new ArrayList<int[]>();
		int rsq=radius*radius;
		if (radius>1){
			intNeighborList.add(new int[]{radius,0});
			intNeighborList.add(new int[]{-1*radius,0});
			intNeighborList.add(new int[]{0,radius});
			intNeighborList.add(new int[]{0,-1*radius});
			for(int r=-1*radius+1;r<radius;r++){
				for(int c=-1*radius+1;c<radius;c++){
					if(r*r+c*c<=rsq && !(r==0 && c==0)){
						intNeighborList.add(new int[]{r,c});
					}
				}
			}
			return intNeighborList;
		}else{
			intNeighborList.add(new int[]{1,0});
			intNeighborList.add(new int[]{0,1});
			intNeighborList.add(new int[]{0,-1});
			intNeighborList.add(new int[]{-1,0});	
		    return intNeighborList;
		}
	}
	
	private void constructNeighborMap(int maxRadius){
		for(int i=1;i<maxRadius;i++){
			neighborMap.put(i, intNeighbor(i));
		}
	}
	public HashMap<Integer,ArrayList<int[]>> get(){
		return neighborMap;
	}
	
	public String toString(int r){
		String s="";
		ArrayList<int[]> pointSet=neighborMap.get(r);
		char[][] output = new char[2*r+1][2*r+1];
		for(int i=0; i<output.length;i++){
			Arrays.fill(output[i], '-');
		}
		
		for(int[] point : pointSet){
			//if(point[0]<0)point[0]+=r;
			//if(point[1]<0)point[1]+=r;
			output[point[0]+r][point[1]+r]='X';
		}
		for(int i=0; i<output.length;i++){
			for(int j=0;j<output[i].length;j++){
				s+=output[i][j];
			}
			s+="\n";
		}
		return s;
	}
	public NeighborMap(int maxRadius){
		constructNeighborMap(maxRadius);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// 0 to (n-1)
		NeighborMap neighborMap = new NeighborMap(30);
		
		System.out.print(neighborMap.toString(29));
	}

}
