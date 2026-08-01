package logic.PMCSN.model;

public class Constants {
	
	public static final int NUMBER_OF_CENTERS = 4;
	
	//----- TRUNCATED_NORMAL ------
	//----- LOGIN -------
	public static final double DEV_ST_LOGIN = 0.20;
	public static final double LOWER_B_LOGIN = 2.70;
	public static final double UPPER_B_LOGIN = 4.50;
	
	//per validazione
	/*public static final double DEV_ST_LOGIN = 0.40;
	public static final double LOWER_B_LOGIN = 5.40;
	public static final double UPPER_B_LOGIN = 9.00;
	
	public static final double DEV_ST_LOGIN = 0.10;
	public static final double LOWER_B_LOGIN = 1.35;
	public static final double UPPER_B_LOGIN = 2.25;*/
	
	
	
	//------ ULTIMATE TEAM ------
	public static final double DEV_ST_UT = 0.25;
	public static final double LOWER_B_UT = 3.00;
	public static final double UPPER_B_UT = 5.00;
	
	// ------ STAGIONI -------
	public static final double DEV_ST_STA = 0.50;
	public static final double LOWER_B_STA = 5.50;
	public static final double UPPER_B_STA = 10.0;
	
	// ------ CLUB --------
	public static final double DEV_ST_CLUB = 0.60;
	public static final double LOWER_B_CLUB = 6.00;
	public static final double UPPER_B_CLUB = 12.00;
	

    // ---- ABANDON PROB ----
    //probabilità di abbandonare il sistema dopo i controlli Ultimate Team
    public static final double not_P5 = 0.03;
    
    //probabilità di abbandonare il sistema dopo i controlli Stagioni
    public static final double not_P6 = 0.01;
    
    //probabilità di abbandonare il sistema dopo i controlli Club
    public static final double not_P7 = 0.02;
    

    // ---- TASSI DI ARRIVO [req/sec]----    
    public static final double LAMBDA = 1.53;
    //public static final double LAMBDA = 3.06;
    
}
