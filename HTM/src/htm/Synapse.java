package htm;

public class Synapse {
	static final float connectedPerm = (float)0.2;
	static final double initialRange = 0.3;
	static final float permanenceIncStep = connectedPerm/11;
	static final float permanenceDecStep = connectedPerm/11;
	static final double PermBoostFactor=0.1;
	
	
	
	public float permanence;
	public Region destRegion;
	public int[] destCoor=new int[3];
	public boolean update=false;
	
	
	public boolean isConnected(){
		if(permanence>connectedPerm) return true;
		else return false;
	}
	
	public boolean equals(Synapse s){
		if(s.destCoor[0]==this.destCoor[0] && s.destCoor[1]==this.destCoor[1] && s.destCoor[2]==this.destCoor[2] && s.destRegion==this.destRegion) return true;
		return false;
	}
	
	public void permanenceInc(){
		permanence+=permanenceIncStep;
		permanence=(float) Math.min(1.0,permanence);
	}
	
	public void permanenceDec(){
		permanence-=permanenceDecStep;
		permanence=(float) Math.max(0.0,permanence);
	}
	
	public void boostPermanence(){
		permanence += connectedPerm*PermBoostFactor;
		permanence = (float) Math.min(1.0,permanence);
	}
	
	public String toString(){
		return ("["+destCoor[0]+", "+destCoor[1]+destCoor[2]+"]"+permanence);
	}
	
	public Synapse(Region reg, int r, int c,int l){
		this.destRegion=reg;
		this.destCoor[0]=r;
		this.destCoor[1]=c;
		this.destCoor[2]=l;
		//non bias
		this.permanence=(float)(connectedPerm-initialRange/2*connectedPerm+Math.random()*initialRange*connectedPerm);
	}
	public Synapse(Region reg, int r, int c, double bias){
		this.destRegion=reg;
		this.destCoor[0]=r;
		this.destCoor[1]=c;
		//bias
		this.permanence=(float)((1-initialRange*0.75+(Math.random()+Math.random()*bias)*initialRange)*connectedPerm);
		
		
		//non bias
		//this.permanence=(float)(connectedPerm-initialRange/2*connectedPerm+Math.random()*initialRange*connectedPerm);
		//test set all synapse connected
		//this.permanence=(float)(connectedPerm+initialRange/2*connectedPerm+Math.random()*initialRange*connectedPerm);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
