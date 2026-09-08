package pmcsn.UniversalStudiosHollywood.utils;

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

public void createInterval(String directory, String filename, int type) {

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
	    if (type == 1) {
            printFormulario(filename, mean, w, df);
        } 
        else if (type == 2) {
            printFormulario2(filename, mean, w, df);
        	saveToCsv2(filename, mean, w, df);
        }


	}
	else {
	    System.out.print("ERROR - insufficient data\n");
	}
  }

    private void printFormulario(String filename, double mean, double w, DecimalFormat df) {
    	    System.out.println("\n");
    	    String fname = filename.toLowerCase();
    	    String centro = null;
    	    String misura = null;

    	    /* ===================== IDENTIFICAZIONE CENTRO ===================== */
    	    if (fname.contains("sicurezza")) {
    	        centro = "Sicurezza";
    	    } else if (fname.contains("biglietteria")) {
    	        centro = "Biglietteria";
    	    } else if (fname.contains("controlli")) {
    	        centro = "Controlli";
    	    } else if (fname.contains("mariostandard")) {
    	        centro = "Mario Kart Standard";
    	    } else if (fname.contains("marioexpress")) {
    	    	centro = "Mario Kart Express";
    	    } else if (fname.contains("mario")) {
    	    	centro = "Mario Kart";
    	    } else if (fname.contains("hpstandard")) {
    	    	centro = "Harry Potter Standard";
    	    } else if (fname.contains("hpexpress")) {
    	    	centro = "Harry Potter Express";
    	    } else if (fname.contains("hp")) {
    	    	centro = "Harry Potter";
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
    
    private void printFormulario2(String filename, double mean, double w, DecimalFormat df) {

        System.out.println("\n");

        String fname = filename.toLowerCase();

        String centro = null;
        String misura = "E(Tq)";

        // Identificazione del centro
        if (fname.contains("sicurezza")) {
            centro = "Sicurezza";
        } 
        else if (fname.contains("biglietteria")) {
            centro = "Biglietteria";
        } 
        else if (fname.contains("controlli")) {
            centro = "Controlli";
        } 
        else if (fname.contains("mariostandard")) {
            centro = "Mario Kart Standard";
        } 
        else if (fname.contains("marioexpress")) {
            centro = "Mario Kart Express";
        } 
        else if (fname.contains("hpstandard")) {
            centro = "Harry Potter Standard";
        } 
        else if (fname.contains("hpexpress")) {
            centro = "Harry Potter Express";
        }

        if (centro != null && misura != null) {
            System.out.println("Centro: " + centro);
            System.out.println(misura + ": " + df.format(mean) + " +/- " + df.format(w));
        }
    }
    
    private void saveToCsv2(String filename, double mean, double w, DecimalFormat df) {
        String fname = filename.toLowerCase();

        String centro = null;
        String misura = "E(Tq)";

        // Identificazione del centro
        if (fname.contains("sicurezza")) {
            centro = "Sicurezza";
        } else if (fname.contains("biglietteria")) {
            centro = "Biglietteria";
        } else if (fname.contains("controlli")) {
            centro = "Controlli";
        } else if (fname.contains("mariostandard")) {
            centro = "Mario Kart Standard";
        } else if (fname.contains("marioexpress")) {
            centro = "Mario Kart Express";
        } else if (fname.contains("hpstandard")) {
            centro = "Harry Potter Standard";
        } else if (fname.contains("hpexpress")) {
            centro = "Harry Potter Express";
        }

        if (centro != null) {
            File csvFile = new File("Medie_campionarie_tq.csv");
            boolean fileExists = csvFile.exists();

            // Usiamo il secondo parametro 'true' in FileWriter per abilitare la modalità APPEND
            try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile, true))) {
                
                // Scrive l'intestazione solo se il file viene creato per la prima volta
                if (!fileExists) {
                    pw.println("Centro,Misura,Media,HalfWidth");
                }

                // Scrive la riga con i dati formattati
                // Nota: Usiamo la virgola o il punto e virgola ';' per separare le colonne
                pw.println(centro + ";" + misura + ";" + df.format(mean) + ";" + df.format(w));

            } catch (IOException e) {
                System.err.println("Errore durante la scrittura del file CSV: " + e.getMessage());
            }
        }
    }


}