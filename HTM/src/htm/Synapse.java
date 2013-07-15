package htm;

public class Synapse {
	static final float connectedPerm = (float)0.2;
	static final double initialRange = 0.3;
	static final float permanenceIncStep = connectedPerm/11;
	static final float permanenceDecStep = connectedPerm/11;
	static final double PermBoostFactor=0.2;
	
	
	
	public float permanence;
	public Region region;
	public int[] destCoor=new int[3];
	
	
	public boolean isValid(){
		if(permanence>connectedPerm) return true;
		else return false;
	}
	
	public boolean equals(Synapse s){
		if(s.destCoor[0]==this.destCoor[0] && s.destCoor[1]==this.destCoor[1]) return true;
		else return false;
	}
	
	public void permanenceInc(){
		permanence+=permanenceIncStep;
		permanence=(float) Math.min(1.0,permanence);
	}
	
	public void permanenceDec(){
		permanence-=permanenceDecStep;
		permanence=(float) Math.max(0.0,permanence);
	}
	
	public void increasePermanence(){
		permanence+=(connectedPerm*PermBoostFactor);
		permanence=(float) Math.min(1.0,permanence);
	}
	
	public String toString(){
		return ("["+destCoor[0]+", "+destCoor[1]+"]"+permanence);
	}
	
	public Synapse(int r, int c){
		this.destCoor[0]=r;
		this.destCoor[1]=c;
		//non bias
		this.permanence=(float)(connectedPerm-initialRange/2*connectedPerm+Math.random()*initialRange*connectedPerm);
	}
	public Synapse(Region reg, int r, int c, double bias){
		this.region=reg;
		this.destCoor[0]=r;
		this.destCoor[1]=c;
		//bias
		this.permanence=(float)((1-initialRange*0.6+(Math.random()+Math.random()*bias)*initialRange)*connectedPerm);
		
		
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
