package logic.PMCSN;

import java.io.IOException;
import java.util.Scanner;

import logic.PMCSN.controller.ExperimentsController;
import logic.PMCSN.controller.TransientController;
import logic.PMCSN.controller.VerificaController;
import logic.PMCSN.utils.Autocorrelation;

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
        
    	/*TransientController tc = new TransientController();
    	tc.startAnalysis();
        /*VerificaController vc = new VerificaController();
    	vc.startAnalysis();*/
    	/*Autocorrelation a = new Autocorrelation();
    	try {
			a.startCalculate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/  	
    	/*ExperimentsController exc = new ExperimentsController();
    	exc.startAnalysis();*/
    }
}
