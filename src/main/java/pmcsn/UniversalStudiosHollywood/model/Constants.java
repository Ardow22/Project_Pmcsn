package pmcsn.UniversalStudiosHollywood.model;

public class Constants {
	
	//----- TRUNCATED_NORMAL ------
	//----- Mario Kart -------
	/*public static final double DEV_ST_MARIO = 0.60;
	public static final double LOWER_B_MARIO = 2.00;
	public static final double UPPER_B_MARIO = 6.00;*/
	public static final double DEV_ST_MARIO = 1.0;
	public static final double LOWER_B_MARIO = 2.00;
	public static final double UPPER_B_MARIO = 6.00;
	
	public static final double DEV_ST_HP = 0.5;
	public static final double LOWER_B_HP = 1.00;
	public static final double UPPER_B_HP = 4.00;
	
	
		
	//------ Harry Potter ------
	/*public static final double DEV_ST_HP = 0.30;
	public static final double LOWER_B_HP = 1.00;
	public static final double UPPER_B_HP = 3.00;*/
	
	// LOGNORMAL
	// ------ SICUREZZA -------
	public static final double SIGMA_SICUREZZA = 5.5;
	
	// ------BIGLIETTERIA--------
	public static final double SIGMA_BIGLIETTERIA = 12.00;
	
	// ------CONTROLLI--------
	public static final double SIGMA_CONTROLLI = 5.0;
		
	

    // ---- ABANDON PROB ----
    //probabilità di abbandonare il sistema
    public static final double not_P5 = 0.03;
    
    //probabilità di abbandonare il sistema 
    public static final double not_P6 = 0.01;
    
    //probabilità di abbandonare il sistema dopo i controlli Club
    public static final double not_P7 = 0.02;
       

    // ---- TASSI DI ARRIVO [req/sec]----    
    public static final double LAMBDA1= 1.653;
    public static final double LAMBDA2 = 2.33;
    public static final double LAMBDA3 = 2.042;
    public static final double LAMBDA4 = 1.264;
    public static final double LAMBDA5 = 0.875;
    
    public static final double LAMBDA_VAL = 1.75;
    
    
    
    public static final int NUMBER_OF_QUEUES = 7;
    public static final int NUMBER_OF_CENTERS = 5;
    
}
