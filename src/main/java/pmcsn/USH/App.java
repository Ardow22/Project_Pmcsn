package pmcsn.USH;


import java.io.IOException;
import java.util.Scanner;


import pmcsn.USH.controller.ExperimentsController;
import pmcsn.USH.controller.TransientController;
import pmcsn.USH.controller.VerificaController;
import pmcsn.USH.utils.Autocorrelation;

public class App {
	
    public static void main( String[] args ) {
        Scanner input = new Scanner(System.in);
        System.out.println("Benvenuto nel simulatore PMCSN!");
        System.out.println("Puoi scegliere tra le seguenti opzioni: ");
        System.out.println("1 - Verifica");        
        System.out.println("2 - Analisi transiente");
        System.out.println("3 - Esperimenti");
        
        System.out.println("Digita un numero: ");
        String choice = input.nextLine();
        
        switch(choice) {
        case "1":
        	System.out.println("Hai scelto la verifica");
        	VerificaController vc = new VerificaController();
        	vc.startAnalysis();
        	break;
        
        case "2":
        	System.out.println("Hai scelto l'analisi transiente");
        	TransientController tc = new TransientController();
        	tc.startAnalysis();
        	break;
        
        case "3":
        	System.out.println("Hai scelto gli esperimenti");
        	ExperimentsController exc = new ExperimentsController();
        	exc.startAnalysis();
        	break;
        	
        default:
        	System.out.println("Non hai scelto nulla, chiusura del programma");
        	System.exit(0);
        }
    	
    	        
    }
}
