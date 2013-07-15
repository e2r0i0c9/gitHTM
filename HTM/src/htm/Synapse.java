package htm;

public class Synapse {
	static final float connectedPerm = (float)0.2;
	static final double initialRange = 0.3;
	static final float permanenceIncStep = connectedPerm/11;
	static final float permanenceDecStep = connectedPerm/11;
	static final double PermBoostFactor=0.2;
	
	
	
	public float permanence;
	public Region region;
	public int[] destinyCoor=new int[2];
	
	
	public boolean isValid(){
		if(permanence>connectedPerm) return true;
		else return false;
	}
	
	public boolean equals(Synapse s){
		if(s.destinyCoor[0]==this.destinyCoor[0] && s.destinyCoor[1]==this.destinyCoor[1]) return true;
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
		return ("["+destinyCoor[0]+", "+destinyCoor[1]+"]"+permanence);
	}
	
	public Synapse(int r, int c){
		this.destinyCoor[0]=r;
		this.destinyCoor[1]=c;
		//non bias
		this.permanence=(float)(connectedPerm-initialRange/2*connectedPerm+Math.random()*initialRange*connectedPerm);
	}
	public Synapse(Region reg, int r, int c){
		this.destinyCoor[0]=r;
		this.destinyCoor[1]=c;
		this.region=reg;
		//test set all synapse connected
		//this.permanence=(float)(connectedPerm+initialRange/2*connectedPerm+Math.random()*initialRange*connectedPerm);
		//non bias
		this.permanence=(float)(connectedPerm-initialRange/2*connectedPerm+Math.random()*initialRange*connectedPerm);
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
