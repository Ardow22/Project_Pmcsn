package logic.PMCSN.utils;

/* ----------------------------------------------------------------------
 * This program reads a data sample from a text file in the format
 *                         one data point per line 
 * and calculates an interval estimate for the mean of that (unknown) much 
 * larger set of data from which this sample was drawn.  The data can be 
 * either discrete or continuous.  A compiled version of this program 
 * supports redirection and can used just like program uvs.c. 
 * 
 * Name              : Estimate.java (Interval Estimation) 
 * Authors           : Steve Park & Dave Geyer 
 * Translated By     : Richard Dutton & Jun Wang
 * Language          : Java
 * Latest Revision   : 6-16-06 
 * ----------------------------------------------------------------------
 */

import java.lang.Math;
import java.io.*;
import java.text.*;
import java.util.StringTokenizer;

public class Estimate{
    
    static final double LOC = 0.95;    /* level of confidence,        */ 
                                       /* use 0.95 for 95% confidence */

public void createInterval(String directory, String filename) {

	long   n    = 0;                     /* counts data points */
	double sum  = 0.0;
	double mean = 0.0;
	double data;
	double stdev;
	double u, t, w;
	double diff;
	
	String line = "";
	
	Rvms rvms = new Rvms();

	BufferedReader br = null;
	File file = new File(directory + "/" + filename + ".dat");
		try {

			FileInputStream inputStream = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(inputStream));

		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	try{
	    line = br.readLine();
	    
	    while (line!=null) {         /* use Welford's one-pass method */
			StringTokenizer tokenizer = new StringTokenizer(line);
			if(tokenizer.hasMoreTokens()){
		    	data = Double.parseDouble(tokenizer.nextToken());
		    
		    n++;                 /* and standard deviation        */
		    diff  = data - mean;
		    sum  += diff * diff * (n - 1.0) / n;
		    mean += diff / n;
		}  
		
		line = br.readLine();
		
	    }
	}catch(IOException e){
	    System.err.println(e);
	    System.exit(1);
	}
	
	stdev  = Math.sqrt(sum / n);
	
	DecimalFormat df = new DecimalFormat("###0.00000");
	
	if (n > 1) {
	    u = 1.0 - 0.5 * (1.0 - LOC);              /* interval parameter  */
	    t = rvms.idfStudent(n - 1, u);            /* critical value of t */
	    w = t * stdev / Math.sqrt(n - 1);         /* interval half width */

		/*System.out.println(filename);
	    System.out.print("based upon " + n + " data points");
	    System.out.print(" and with " + (int) (100.0 * LOC + 0.5) + 
		"% confidence\n");
	    System.out.print("the expected value is in the interval ");
	    System.out.print( df.format(mean) + " +/- " + df.format(w) + "\n\n");*/
	    printFormulario(filename, mean, w, df);


	}
	else{
	    System.out.print("ERROR - insufficient data\n");
	}
    }

    private void printFormulario(String filename, double mean, double w, DecimalFormat df) {
    	    System.out.println("\n");
    	    String fname = filename.toLowerCase();
    	    String centro = null;
    	    String misura = null;

    	    /* ===================== IDENTIFICAZIONE CENTRO ===================== */
    	    if (fname.contains("login")) {
    	        centro = "Login";
    	    } else if (fname.contains("ultimate")) {
    	        centro = "Ultimate Team";
    	    } else if (fname.contains("stagioni")) {
    	        centro = "Stagioni";
    	    } else if (fname.contains("club")) {
    	        centro = "Club";
    	    }

    	    /* ===================== IDENTIFICAZIONE MISURA ===================== */
    	    if (fname.contains("popolazione_coda")) {
    	        misura = "E(Nq)";
    	    } else if (fname.contains("popolazione_sistema")) {
    	        misura = "E(Ns)";
    	    } else if (fname.contains("tempi_in_coda")) {
    	        misura = "E(Tq)";
    	    } else if (fname.contains("tempidirisposta")) {
    	        misura = "E(Ts)";
    	    } else if (fname.contains("tempidiservizio")) {
    	        misura = "E(Si)";
    	    } else if (fname.contains("utilizzazione")) {
    	        misura = "ρ";
    	    }

    	    /* ===================== STAMPA ===================== */
    	    if (centro != null && misura != null) {
    	        System.out.println("Centro: " + centro);
    	        System.out.println(misura + ": " + df.format(mean) + " +/- " + df.format(w));
    	    }
    }


    /*public void printFormulario(String filename, double mean, double w, DecimalFormat df) {
    	if (filename.toLowerCase().contains("login")) {
    	    System.out.println("Centro: Login");
    	    if (filename.toLowerCase().contains("popolazione_coda")) {
    	    	System.out.println("E(Nq): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("popolazione_sistema")) {
    	    	System.out.println("E(Ns): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("tempi_in_coda")) {
    	    	System.out.println("E(Tq): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("tempiDiRisposta")) {
    	    	System.out.println("E(Ts): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("tempiDiServizio")) {
    	    	System.out.println("E(S): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("utilizzazione")) {
    	    	System.out.println("ρ: " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    }  
    	} else if (filename.toLowerCase().contains("UT")) {
    	    System.out.println("Centro: Ultimate Team");
    	    if (filename.toLowerCase().contains("popolazione_coda")) {
    	    	System.out.println("E(Nq): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("popolazione_sistema")) {
    	    	System.out.println("E(Ns): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("tempi_in_coda")) {
    	    	System.out.println("E(Tq): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("tempiDiRisposta")) {
    	    	System.out.println("E(Ts): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("tempiDiServizio")) {
    	    	System.out.println("E(S): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("utilizzazione")) {
    	    	System.out.println("ρ: " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    }
    	} else if (filename.toLowerCase().contains("Stagioni")) {
    	    System.out.println("Centro: Stagioni");
    	    if (filename.toLowerCase().contains("popolazione_coda")) {
    	    	System.out.println("E(Nq): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("popolazione_sistema")) {
    	    	System.out.println("E(Ns): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("tempi_in_coda")) {
    	    	System.out.println("E(Tq): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("tempiDiRisposta")) {
    	    	System.out.println("E(Ts): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("tempiDiServizio")) {
    	    	System.out.println("E(S): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("utilizzazione")) {
    	    	System.out.println("ρ: " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    }
    	} else if (filename.toLowerCase().contains("Club")) {
    	    System.out.println("Centro: Club");
    	    if (filename.toLowerCase().contains("popolazione_coda")) {
    	    	System.out.println("E(Nq): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("popolazione_sistema")) {
    	    	System.out.println("E(Ns): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("tempi_in_coda")) {
    	    	System.out.println("E(Tq): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("tempiDiRisposta")) {
    	    	System.out.println("E(Ts): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("tempiDiServizio")) {
    	    	System.out.println("E(S): " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    } else if (filename.toLowerCase().contains("utilizzazione")) {
    	    	System.out.println("ρ: " + df.format(mean) + " +/- " + df.format(w) + "\n\n");
    	    }
    	}

    }*/
}