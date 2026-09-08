package pmcsn.UniversalStudiosHollywood.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pmcsn.UniversalStudiosHollywood.libraries.Rngs;
import pmcsn.UniversalStudiosHollywood.model.BiglietteriaFisicaNode;
import pmcsn.UniversalStudiosHollywood.model.ControlloBigliettiNode;
import pmcsn.UniversalStudiosHollywood.model.ControlloSicurezzaNode;
import pmcsn.UniversalStudiosHollywood.model.HarryPotterNode;
import pmcsn.UniversalStudiosHollywood.model.MarioKartNode;
import pmcsn.UniversalStudiosHollywood.model.TransientStats;
//import pmcsn.UniversalStudiosHollywood.model.Welford; 
import pmcsn.UniversalStudiosHollywood.utils.Estimate;
import pmcsn.UniversalStudiosHollywood.utils.Rvgs;
import pmcsn.UniversalStudiosHollywood.utils.Rvms;

import static pmcsn.UniversalStudiosHollywood.model.Constants.*;
import static pmcsn.UniversalStudiosHollywood.model.Events.*;

public class ExperimentsController {
	
	static double START = 0.0; //tempo d'inizio della simulazione
    static double sarrival = START; //ultimo tempo in cui è stato generato un arrivo
    static double STOP = 18000;
    static int INTERVAL_DATA = 3600;
    static int INDEX_CHANGE_LAMBDA = ALL_EVENTS_VERIFICA;
    //static double COLUMNS = (STOP/INTERVAL_DATA) + 1;
 
    // Rapporti di pescaggio configurabili (es. 2 Standard : 1 Express)
    private static final int MARIO_RATIO_STANDARD = 3;
    private static final int MARIO_RATIO_EXPRESS = 1;

    private static final int HP_RATIO_STANDARD = 3;
    private static final int HP_RATIO_EXPRESS = 1;
	
	public void startAnalysis() {
		String filenameExperiments = "esperimenti_prestazioni.csv";
		long[] seeds = new long[1024];
		seeds[0] = 123456789;
		Rngs r = new Rngs();
		
		// Intestazione del file CSV di esperimento
	    initCsvExperimentsHeader(filenameExperiments);
	    
	    /*Welford wSicurezza = new Welford();
	    Welford wBiglietteria = new Welford();
	    Welford wControlli = new Welford();
	    Welford wMarioStandard = new Welford();
	    Welford wMarioExpress = new Welford();
	    Welford wHPStandard = new Welford();
	    Welford wHPExpress = new Welford();*/

		List<Double> meanSicurezza = new ArrayList<>();
		List<Double> meanBiglietteria = new ArrayList<>();
		List<Double> meanControlli = new ArrayList<>();
		List<Double> meanMarioStandard = new ArrayList<>();
		List<Double> meanMarioExpress = new ArrayList<>();
		List<Double> meanHpStandard = new ArrayList<>();
		List<Double> meanHpExpress = new ArrayList<>();
		
		
		//for (int i = 0; i < 5; i++) {
		for (int i = 0; i < 150; i++) {
			System.out.println("ITERAZIONE: " + i);
			TransientStats ts = new TransientStats();//va inizializzato dentro al ciclo perché ad ogni nuova run raccolgo nuove statistiche da 0
			sarrival = START;
			//seeds[i+1] = finiteHorizonSimulation(seeds[i], r, ts, filenameExperiments);
			
			// Esegui la simulazione della singola run e ottieni l'array dei tempi medi
	        double[] runMeans = finiteHorizonSimulation(seeds[i], r, ts, filenameExperiments);
	        
	        // Passa al seed successivo per l'iterazione i+1
	        seeds[i+1] = (long) runMeans[0]; // Salviamo il nuovo seed generato
			
			/*wSicurezza.add(runMeans[1]);
	        wBiglietteria.add(runMeans[2]);
	        wControlli.add(runMeans[3]);
	        wMarioStandard.add(runMeans[4]);
	        wMarioExpress.add(runMeans[5]);
	        wHPStandard.add(runMeans[6]);
	        wHPExpress.add(runMeans[7]);*/
	        
	     // Liste per Estimate
		    meanSicurezza.add(runMeans[1]);
		    meanBiglietteria.add(runMeans[2]);
		    meanControlli.add(runMeans[3]);
		    meanMarioStandard.add(runMeans[4]);
		    meanMarioExpress.add(runMeans[5]);
		    meanHpStandard.add(runMeans[6]);
		    meanHpExpress.add(runMeans[7]);
	        
		}
		
		System.out.println("===== MEDIE CAMPIONARIE =====");

		/*System.out.println("Sicurezza: " + wSicurezza.getMean());
		System.out.println("Biglietteria: " + wBiglietteria.getMean());
		System.out.println("Controlli: " + wControlli.getMean());
		System.out.println("Mario Kart Standard: " + wMarioStandard.getMean());
		System.out.println("Mario Kart Express: " + wMarioExpress.getMean());
		System.out.println("Harry Potter Standard: " + wHPStandard.getMean());
		System.out.println("Harry Potter Express: " + wHPExpress.getMean());*/
		
		String directory = "experiments_output";
		writeFile(meanSicurezza, directory, "sicurezza_attesa_coda");
        writeFile(meanBiglietteria, directory,"biglietteria_attesa_coda");
		writeFile(meanControlli, directory,"controlli_attesa_coda");
		writeFile(meanMarioStandard, directory,"mariostandard_attesa_coda");
		writeFile(meanMarioExpress, directory,"marioexpress_attesa_coda");
		writeFile(meanHpStandard, directory,"hpstandard_attesa_coda");
		writeFile(meanHpExpress, directory,"hpexpress_attesa_coda");
		
		Estimate estimate = new Estimate();

		List<String> filenames = Arrays.asList(
		    "sicurezza_attesa_coda",
		    "biglietteria_attesa_coda",
		    "controlli_attesa_coda",
		    "mariostandard_attesa_coda",
		    "marioexpress_attesa_coda",
		    "hpstandard_attesa_coda",
		    "hpexpress_attesa_coda"
		);

		for (String filename : filenames) {
		    estimate.createInterval(directory, filename, 2);
		}
	}
	
	private double[] finiteHorizonSimulation(long seed, Rngs rng, TransientStats ts, String expFilename) {
		
		ControlloSicurezzaNode sicurezza = new ControlloSicurezzaNode();
		BiglietteriaFisicaNode biglietteria = new BiglietteriaFisicaNode();
		ControlloBigliettiNode controlli = new ControlloBigliettiNode();
		MarioKartNode mario_kart = new MarioKartNode();
		HarryPotterNode harry_potter = new HarryPotterNode();
	    
	    long totalJobsInSicurezza = 0;
	    long totalJobsInBiglietteria = 0;
	    long totalJobsInControlli = 0;
	    long totalJobsMario = 0;
	    long totalJobsHP = 0;
	 // --- MARIO KART: Sdoppiamento presenze ---
	    long totalJobsInMarioStandard = 0;
	    long totalJobsInMarioExpress = 0;
	    int totalMarioStandardCheck = 0;
	    int totalMarioExpressCheck = 0;
	    
	 // --- HARRY POTTER: Sdoppiamento presenze ---
	    long totalJobsInHPStandard = 0;
	    long totalJobsInHPExpress = 0;
	    int totalHPStandardCheck = 0;
	    int totalHPExpressCheck = 0;
		
	    int totalSicurezzaCheck = 0;
	    int totalBiglietteriaCheck = 0;
	    int totalControlliCheck = 0;
	    
	    double nodeAreaSicurezza = 0.0; 
		double nodeAreaBiglietteria = 0.0; 
		double nodeAreaControlli = 0.0; 
		
		double nodeAreaMarioStandard = 0.0;
	    double nodeAreaMarioExpress = 0.0;
	    double nodeAreaHPStandard = 0.0;
	    double nodeAreaHPExpress = 0.0;  
		        
        //inizializzazione dei double per memorizzare il primo completamento delle varie code
		double firstCompletionSicurezza = 0;
	    double firstCompletionBiglietteria = 0;
	    double firstCompletionControlli = 0;
	    double firstCompletionMario = 0;
	    double firstCompletionHP = 0;
	    
	 // --- Stato Servente Singolo e Pescaggio MARIO KART ---
	    boolean marioServingStandard = true;
	    int marioConsecutiveServed = 0;
	    
	    // --- Stato Servente Singolo e Pescaggio HARRY POTTER ---
	    boolean hpServingStandard = true;
	    int hpConsecutiveServed = 0;
	    double serviceHPStandardSum = 0.0;    // accumulatore servizio Standard
	    double serviceHPExpressSum = 0.0;     // accumulatore servizio Express
	    double serviceMarioStandardSum = 0.0;    // accumulatore servizio Standard
	    double serviceMarioExpressSum = 0.0;     // accumulatore servizio Express
        
        
		int e; //indice next event, cioè l'evento più imminente
		int s; //indice del server
		
		double service; //tempo di servizio
		
		rng.plantSeeds(seed);
		
        MsqEvent[] events = new MsqEvent[ALL_EVENTS_WITH_SAVE_STAT_VERIFICA];
        MsqSum[] sum = new MsqSum[ALL_EVENTS_VERIFICA];
        Rvms rvms = new Rvms();
        
        //System.out.println("Lista eventi (incluso SAVE_STAT): ");
        for (int i = 0; i < ALL_EVENTS_WITH_SAVE_STAT_VERIFICA; i++) {
            events[i] = new MsqEvent();
        }
        for (int i = 0; i < ALL_EVENTS_VERIFICA; i++) {
            sum[i] = new MsqSum();
        }
        
        //inizializzazione clock
        MsqT t = new MsqT();
        t.current = START;  
        
        /*events[0].t = getArrival(rng, sicurezza.getStreamIndex(), t.current);
        events[0].x = 1;*/
        
     // --- INIZIALIZZAZIONE FASI DI ARRIVO ---
        int currentPhase = 1;
        double currentLambda = LAMBDA1;
              
     // Configura il primo cambio di tasso a t = 3600 secondi
        events[INDEX_CHANGE_LAMBDA].t = 3600.0;
        events[INDEX_CHANGE_LAMBDA].x = 1; // Attiva l'evento
        
        events[0].t = getArrival(rng, sicurezza.getStreamIndex(), currentLambda);
        events[0].x = 1;

        
        for (int i = 0; i < ALL_EVENTS_VERIFICA; i++) {
        	if ((events[i].t != 0) && (events[i].x != 1)) {
        		events[i].t = START;
                events[i].x = 0;
                sum[i].service = 0.0;
                sum[i].served = 0;
        	}
        } 
                
        /* === INIZIO ITERAZIONE === */
        System.out.println("\n\n\n----INIZIA LA SIMULAZIONE------");
        //System.out.println("La simulazione andrà avanti fino al numero di job prefissati");
        
        int iter = 0;
        ts.getTransientStatsSicurezza().add(0.0);
    	ts.getTransientStatsBiglietteria().add(0.0);
    	ts.getTransientStatsControlli().add(0.0);
    	
    	ts.getTransientStatsMarioStandard().add(0.0);
        ts.getTransientStatsMarioExpress().add(0.0);
        ts.getTransientStatsHPStandard().add(0.0);
        ts.getTransientStatsHPExpress().add(0.0);
    	
        while(events[0].x != 0 || totalJobsInSicurezza+totalJobsInBiglietteria+totalJobsInControlli+totalJobsMario+totalJobsHP != 0) {
        	
        	iter++;
        	System.out.println("\n\n-------LA SIMULAZIONE VA AVANTI, QUINDI NUOVA ITERAZIONE, è LA NUMERO: " + iter);	
        	     
        	
        	
        	
            // Trova evento più imminente
            e = nextEvent(events);
            //System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.next = events[e].t;
            //System.out.println("Siamo all'istante: " + t.current);
            //System.out.println("Il prossimo evento (che è quello appena trovato) avverrà all'istante: " + t.next);

    		//Node area
            nodeAreaSicurezza += (t.next - t.current)*totalJobsInSicurezza;
    	    nodeAreaBiglietteria += (t.next - t.current)*totalJobsInBiglietteria;
    	    nodeAreaControlli += (t.next - t.current)*totalJobsInControlli;
    	    
    	 // Node area sdoppiate per le attrazioni
    	    nodeAreaMarioStandard += (t.next - t.current)*totalJobsInMarioStandard;
    	    nodeAreaMarioExpress += (t.next - t.current)*totalJobsInMarioExpress;
    	    nodeAreaHPStandard += (t.next - t.current)*totalJobsInHPStandard;
    	    nodeAreaHPExpress += (t.next - t.current)*totalJobsInHPExpress;
    		
    		//System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.current = t.next;
            System.out.println("Siamo all'istante: " + t.current);
            //System.out.println("L'evento successivo doveva avvenire all'istante: " + t.next);
            //System.out.println("I due tempi coincidono, quindi andiamo a processare l'evento " + e);

            if (e == INDEX_CHANGE_LAMBDA) {
            	System.out.println("\n--------- EVENTO: CAMBIO TASSO DI ARRIVO -------------");
                
                double oldLambda = currentLambda;
                currentPhase++; // Passa alla fase/fascia successiva
                
                // 1. Aggiorna il valore di lambda e schedula il cambio successivo
                if (currentPhase == 2) { 
                    currentLambda = LAMBDA2; 
                    events[INDEX_CHANGE_LAMBDA].t = 7200.0; 
                } else if (currentPhase == 3) { 
                    currentLambda = LAMBDA3; 
                    events[INDEX_CHANGE_LAMBDA].t = 10800.0; 
                } else if (currentPhase == 4) { 
                    currentLambda = LAMBDA4; 
                    events[INDEX_CHANGE_LAMBDA].t = 14400.0; 
                } else if (currentPhase == 5) { 
                    currentLambda = LAMBDA5; 
                    events[INDEX_CHANGE_LAMBDA].x = 0; // Disattiva gli eventi di cambio tasso (fase finale)
                }

                // 2. RISCHEDULAZIONE DELL'ARRIVO PENDENTE (Riscalamento del tempo residuo)
                if (events[INDEX_ARRIVAL_SICUREZZA].x == 1) {
                    // Calcola il tempo residuo d'attesa secondo il vecchio tasso
                    double tempoResiduoVecchio = events[INDEX_ARRIVAL_SICUREZZA].t - t.current;
                    
                    // Riscala il tempo residuo in proporzione al cambio di tasso
                    double tempoResiduoNuovo = tempoResiduoVecchio * (oldLambda / currentLambda);
                    
                    // Aggiorna la variabile d'arrivo globale e il tempo nel calendario eventi
                    sarrival = t.current + tempoResiduoNuovo;
                    events[INDEX_ARRIVAL_SICUREZZA].t = sarrival;
                    
                    // Disattiva l'arrivo se supera il tempo limite di STOP
                    if (events[INDEX_ARRIVAL_SICUREZZA].t > STOP) {
                        events[INDEX_ARRIVAL_SICUREZZA].x = 0;
                    }
                }
            	//System.out.println("Prossimo evento di SAVE_STAT: " + events[ALL_EVENTS].t);
            } else if (e == INDEX_ARRIVAL_SICUREZZA) { //e == 0
            	System.out.println("\n---------L'EVENTO è UN NUOVO ARRIVO NEL CENTRO SICUREZZA-------------");
            	totalJobsInSicurezza++;
            
            	events[INDEX_ARRIVAL_SICUREZZA].t = getArrival(rng, sicurezza.getStreamIndex(), currentLambda);
            
            	if (events[INDEX_ARRIVAL_SICUREZZA].t > STOP) {
            		events[INDEX_ARRIVAL_SICUREZZA].x = 0;
            	} 
            	            	
            	if (totalJobsInSicurezza <= SERVERS_SICUREZZA) {
            		//System.out.println("Ci sono meno utenti nel centro di quanti server totali");
            		service = getServiceSicurezza(rng, sicurezza.getStreamIndex(), sicurezza.getServiceTime(), rvms);
            		//System.out.println("Si cerca un server libero");
            		s = findSicurezzaServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            
            } else if (e == INDEX_ARRIVAL_BIGLIETTERIA) { 
            	System.out.println("\n---------L'EVENTO è UN NUOVO ARRIVO NEL CENTRO BIGLIETTERIA-------------");
            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	totalJobsInBiglietteria++;
            	
            	if (totalJobsInBiglietteria <= SERVERS_BIGLIETTERIA) { //verifico se posso essere servito subito
            		service = getServiceBiglietteria(rng, biglietteria.getStreamIndex(), biglietteria.getServiceTime(), rvms);
            		s = findBiglietteriaServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                   //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}	
            
            } else if (e == INDEX_ARRIVAL_CONTROLLI) {
            	
            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI CONTROLLI----------");
            	totalJobsInControlli++;
           
            	
            	if (totalJobsInControlli <= SERVERS_CONTROLLI) { //verifico se posso essere servito subito
            		service = getServiceControlli(rng, controlli.getStreamIndex(), controlli.getServiceTime(), rvms);
            		s = findControlliServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if (e == INDEX_ARRIVAL_MARIO) { 
            	
            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	// Esempio: smistamento in base al tipo di biglietto (80% Standard, 20% Express)
                //boolean isExpress = (rng.random() < 0.20); 
            	boolean isExpress = generateQueueDestination(rng, mario_kart.getStreamIndex());
                if (isExpress) {
                    totalJobsInMarioExpress++;
                } else {
                    totalJobsInMarioStandard++;
                }
                
                totalJobsMario = totalJobsInMarioStandard + totalJobsInMarioExpress;
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI MARIO----------");
            	
            	if (totalJobsMario <= SERVERS_MARIO) { //verifico se posso essere servito subito
            		marioServingStandard = !isExpress;
            		
                    marioConsecutiveServed = 1;
            		service = getServiceMario(rng, mario_kart.getStreamIndex(), mario_kart.getServiceTime(), rvms);
            		
            		// Accumulo del tempo di servizio distinto
            		if (marioServingStandard) {
            			serviceMarioStandardSum += service;
            		} else {
            			serviceMarioExpressSum += service;
            		}
            		
            		
            		s = findMarioKartServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    //System.out.println("AUMENTO DI 1 SERVED");
                    events[s].t = t.current + service;
                    
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if (e == INDEX_ARRIVAL_HP) {
            	events[e].x = 0;
            	System.out.println("\n----------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI HARRY POTTER");
            	//boolean isExpress = (rng.random() < 0.20);
            	boolean isExpress = generateQueueDestination(rng, harry_potter.getStreamIndex());
                if (isExpress) {
                    totalJobsInHPExpress++;
                } else {
                    totalJobsInHPStandard++;
                }
                totalJobsHP = totalJobsInHPStandard + totalJobsInHPExpress;
            	
            	if (totalJobsHP <= SERVERS_HP) { //verifico se posso essere servito subito
            		hpServingStandard = !isExpress;
            		
                    hpConsecutiveServed = 1;
            		service = getServiceHP(rng, harry_potter.getStreamIndex(), harry_potter.getServiceTime(), rvms);
            		
            		// Accumulo del tempo di servizio distinto
            		if (hpServingStandard) {
            			serviceHPStandardSum += service;
            		} else {
            			serviceHPExpressSum += service;
            		}
            		
            		
            		s = findHarryPotterServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    //System.out.println("AUMENTO DI 1 SERVED");
                    events[s].t = t.current + service;
                    
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            } else if ((e >= INDEX_FIRST_SERVER_SICUREZZA) && (e <= INDEX_LAST_SERVER_SICUREZZA)) {
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVENTE AL CENTRO SICUREZZA------------------");
            	
            	if (firstCompletionSicurezza == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionSicurezza = t.current; 
            	}
            	boolean prova = false;
            	if (prova == true) { 
            		double provaTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile	
            	}
            	else {
            		int percorsi = generateBiglietteriaDestination(rng, sicurezza.getStreamIndex());
            		totalJobsInSicurezza--;//diminuisco di 1 il numero di utenti in questo centro
            		totalSicurezzaCheck++;//aumento il numero di utenti serviti in questo centro
                	
                	
                	               	
            		if (percorsi == 0) {
                		            		
                	    events[INDEX_ARRIVAL_BIGLIETTERIA].t = t.current; //aggiunto un evento alla coda Biglietteria
                		events[INDEX_ARRIVAL_BIGLIETTERIA].x = 1; //attivazione dell'evento
                	} else if (percorsi == 1) {
                		           		
                	    events[INDEX_ARRIVAL_CONTROLLI].t = t.current; //aggiunto un evento alla coda Controlli
                		events[INDEX_ARRIVAL_CONTROLLI].x = 1; //attivazione dell'evento	
                	}
                	
                	s = e;
                	
                	if (totalJobsInSicurezza >= SERVERS_SICUREZZA) {//ci sono ancora elementi in coda
                		service = getServiceSicurezza(rng, sicurezza.getStreamIndex(), sicurezza.getServiceTime(), rvms);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
        	} else if ((e >= INDEX_FIRST_SERVER_BIGLIETTERIA) && (e <= INDEX_LAST_SERVER_BIGLIETTERIA)) {
        		System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVENTE ALLA BIGLIETTERIA------------------");
            	if (firstCompletionBiglietteria == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionBiglietteria = t.current; 
            	}
            	
            	
            	boolean prova = false;
            	if (prova) { //se l'utente non supera i controlli
            		
            		double provaTime = t.current + 0.01;
            	}
            	else {
            		totalJobsInBiglietteria--;//diminuisco di 1 il numero di utenti in coda in questo centro
            		totalBiglietteriaCheck++;//aumento il numero di utenti serviti in questo centro
                	
                	
            		events[INDEX_ARRIVAL_CONTROLLI].t = t.current; //aggiunto un evento alla coda Controlli
            		events[INDEX_ARRIVAL_CONTROLLI].x = 1;
                	s = e;
                	
                	if (totalJobsInBiglietteria >= SERVERS_BIGLIETTERIA) {//ci sono ancora elementi in coda
                		
                		service = getServiceBiglietteria(rng, biglietteria.getStreamIndex(), biglietteria.getServiceTime(), rvms);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= INDEX_FIRST_SERVER_CONTROLLI) && (e <= INDEX_LAST_SERVER_CONTROLLI)) { 
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVENTE DI CONTROLLI------------------");
            	if (firstCompletionControlli == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionControlli = t.current; 
            	}
            	
            	boolean prova = false;
            	if (prova) { 
            		double provaTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile	
            	}
            	else {
            		totalJobsInControlli--;//diminuisco di 1 il numero di utenti in coda in questo centro
            		totalControlliCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nella coda di Stagioni: " + totalStagioniCheck);
                	System.out.println("Utenti ancora in Stagioni: " + totalJobsInStagioni);*/
                    
            		int percorsi = generateAttractionsDestination(rng, controlli.getStreamIndex());
                	if (percorsi == 0) {
                		events[INDEX_ARRIVAL_MARIO].t = t.current; 
                		events[INDEX_ARRIVAL_MARIO].x = 1; //attivazione dell'evento
                		//System.out.println("L'utente andrà Mario Kart");	
                	} else if (percorsi == 1) {
                		//System.out.prinln("L'utente andrà in Harry Potter");
                		events[INDEX_ARRIVAL_HP].t = t.current; 
                		events[INDEX_ARRIVAL_HP].x = 1; //attivazione dell'evento	
                	}
            		
            		s = e;
                	
                	if (totalJobsInControlli >= SERVERS_CONTROLLI) {//ci sono ancora elementi in coda
                		
                		service = getServiceControlli(rng, controlli.getStreamIndex(), controlli.getServiceTime(), rvms);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= INDEX_FIRST_SERVER_MARIO_VERIFICA) && (e <= INDEX_LAST_SERVER_MARIO_VERIFICA)) { //eventi dei server di Club, 36 e 40
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DEL SERVENTE DI MARIO KART------------------");
            	if (firstCompletionMario == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionMario = t.current; 
            	}
            	
            	// 1. Aggiorna contatori del cliente appena completato
                if (marioServingStandard) {
                    totalJobsInMarioStandard--;
                    totalMarioStandardCheck++;
                } else {
                    totalJobsInMarioExpress--;
                    totalMarioExpressCheck++;
                }

                totalJobsMario = totalJobsInMarioStandard + totalJobsInMarioExpress;
                	
                s = e;
                	
                if (totalJobsMario >= SERVERS_MARIO) {//ci sono ancora elementi in coda
                	/*boolean nextIsStandard = selectNextQueue(
                            (int)totalJobsInMarioStandard, 
                            (int)totalJobsInMarioExpress, 
                            marioServingStandard, 
                            marioConsecutiveServed, 
                            MARIO_RATIO_STANDARD, 
                            MARIO_RATIO_EXPRESS
                        );

                        if (nextIsStandard == marioServingStandard) {
                            marioConsecutiveServed++;
                        } else {
                            marioServingStandard = nextIsStandard;
                            marioConsecutiveServed = 1;
                        }*/
                	
                	
                	if (totalJobsInMarioStandard > 0 && totalJobsInMarioExpress > 0) {
                        if (marioServingStandard) {
                            if (marioConsecutiveServed < MARIO_RATIO_STANDARD) {
                                marioConsecutiveServed++;
                            } else {
                                marioServingStandard = false; // Passa a Express
                                marioConsecutiveServed = 1;
                            }
                        } else {
                            if (marioConsecutiveServed < MARIO_RATIO_EXPRESS) {
                                marioConsecutiveServed++;
                            } else {
                                marioServingStandard = true; // Passa a Standard
                                marioConsecutiveServed = 1;
                            }
                        }
                    } else if (totalJobsInMarioStandard > 0) {
                        marioServingStandard = true;
                        marioConsecutiveServed = 1;
                    } else {
                        marioServingStandard = false;
                        marioConsecutiveServed = 1;
                    }
                	
                	service = getServiceMario(rng, mario_kart.getStreamIndex(), mario_kart.getServiceTime(), rvms);
                		
                	// Accumulo del tempo di servizio distinto
                	if (marioServingStandard) {
                		serviceMarioStandardSum += service;
                	} else {
                		serviceMarioExpressSum += service;
                	}
                		
                	sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                } else { //altrimenti, se non ci sono persone in coda
                      	events[s].x = 0; //il server diventa libero	
                      	marioConsecutiveServed = 0;
                }
            } else if ((e >= INDEX_FIRST_SERVER_HP_VERIFICA) && (e <= INDEX_LAST_SERVER_HP_VERIFICA)) { //eventi dei server di Club, 36 e 40
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DEL SERVENTE DI HARRY POTTER------------------");
            	if (firstCompletionHP == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionHP = t.current; 
            	}
            	
            	if (hpServingStandard) {
                    totalJobsInHPStandard--;
                    totalHPStandardCheck++;
                } else {
                    totalJobsInHPExpress--;
                    totalHPExpressCheck++;
                }

                totalJobsHP = totalJobsInHPStandard + totalJobsInHPExpress;                	
                s = e;
                	
                if (totalJobsHP >= SERVERS_HP) {//ci sono ancora elementi in coda
                	
                	
                	/*boolean nextIsStandard = selectNextQueue(
                            (int)totalJobsInHPStandard, 
                            (int)totalJobsInHPExpress, 
                            hpServingStandard, 
                            hpConsecutiveServed, 
                            HP_RATIO_STANDARD, 
                            HP_RATIO_EXPRESS
                        );

                        if (nextIsStandard == hpServingStandard) {
                            hpConsecutiveServed++;
                        } else {
                            hpServingStandard = nextIsStandard;
                            hpConsecutiveServed = 1;
                        }*/
                	
                	
                	if (totalJobsInHPStandard > 0 && totalJobsInHPExpress > 0) {
                        if (hpServingStandard) {
                            if (hpConsecutiveServed < HP_RATIO_STANDARD) {
                                hpConsecutiveServed++;
                            } else {
                                hpServingStandard = false; // Passa a Express
                                hpConsecutiveServed = 1;
                            }
                        } else {
                            if (hpConsecutiveServed < HP_RATIO_EXPRESS) {
                                hpConsecutiveServed++;
                            } else {
                                hpServingStandard = true; // Passa a Standard
                                hpConsecutiveServed = 1;
                            }
                        }
                    } else if (totalJobsInHPStandard > 0) {
                        hpServingStandard = true;
                        hpConsecutiveServed = 1;
                    } else {
                        hpServingStandard = false;
                        hpConsecutiveServed = 1;
                    }
                	
                	
                	
                	service = getServiceHP(rng, harry_potter.getStreamIndex(), harry_potter.getServiceTime(), rvms);
                	
                	// Accumulo del tempo di servizio distinto
            		if (hpServingStandard) {
            			serviceHPStandardSum += service;
            		} else {
            			serviceHPExpressSum += service;
            		}
                	
                	sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                      	events[s].x = 0; //il server diventa libero	
                      	hpConsecutiveServed = 0;
                	}
            }	
        }
        
             
     //--- 1. Accumulo del tempo di servizio totale erogato dai serventi per nodo ---
        double queueAreaSicurezza = nodeAreaSicurezza;
        for (int i = INDEX_FIRST_SERVER_SICUREZZA; i <= INDEX_LAST_SERVER_SICUREZZA; i++) {
            queueAreaSicurezza -= sum[i].service;
        }

        double queueAreaBiglietteria = nodeAreaBiglietteria;
        for (int i = INDEX_FIRST_SERVER_BIGLIETTERIA; i <= INDEX_LAST_SERVER_BIGLIETTERIA; i++) {
            queueAreaBiglietteria -= sum[i].service;
        }

        double queueAreaControlli = nodeAreaControlli;
        for (int i = INDEX_FIRST_SERVER_CONTROLLI; i <= INDEX_LAST_SERVER_CONTROLLI; i++) {
            queueAreaControlli -= sum[i].service;
        }
        
        double queueAreaMarioStandard = nodeAreaMarioStandard - serviceMarioStandardSum;
        double queueAreaMarioExpress = nodeAreaMarioExpress - serviceMarioExpressSum;

        double queueAreaHPStandard = nodeAreaHPStandard - serviceHPStandardSum;
        double queueAreaHPExpress = nodeAreaHPExpress - serviceHPExpressSum;

        
        // --- 3. Calcolo dei soli Tempi Medi di Attesa in Coda (E[Tq]) ---
        double avgSicurezza = Math.max(0, queueAreaSicurezza/totalSicurezzaCheck);
        double avgBiglietteria = Math.max(0, queueAreaBiglietteria/totalBiglietteriaCheck);
        double avgControlli = Math.max(0, queueAreaControlli/totalControlliCheck);
        double avgMarioStandard = Math.max(0, queueAreaMarioStandard/totalMarioStandardCheck);
        double avgMarioExpress = Math.max(0, queueAreaMarioExpress/totalMarioExpressCheck); 
        double avgHPStandard = Math.max(0, queueAreaHPStandard/totalHPStandardCheck);
        double avgHPExpress = Math.max(0, queueAreaHPExpress/totalHPExpressCheck);
        
        // Scrittura della riga sintetica dell'esperimento
        writeCsvExperimentsRow(expFilename, seed, avgSicurezza, avgBiglietteria, avgControlli, 
                               avgMarioStandard, avgMarioExpress, avgHPStandard, avgHPExpress);
        
		rng.selectStream(255);
		//return rng.getSeed();
		// Restituisce il nuovo seed + tutti i valori medi calcolati nella run
		long nextSeed = rng.getSeed();
        return new double[] { (double) nextSeed, avgSicurezza, avgBiglietteria, avgControlli, 
                              avgMarioStandard, avgMarioExpress, avgHPStandard, avgHPExpress };
	}
	
	// --- NUOVI METODI HELPER PER IL CSV DEGLI ESPERIMENTI ---

	private void initCsvExperimentsHeader(String filepath) {
	    File file = new File(filepath);
	    if (!file.exists()) {
	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
	            writer.write("seed,avg_sicurezza,avg_biglietteria,avg_controlli,avg_mario_std,avg_mario_exp,avg_hp_std,avg_hp_exp");
	            writer.newLine();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	private void writeCsvExperimentsRow(String filepath, long seed, double avgSic, double avgBig, 
            double avgCtrl, double avgMarStd, double avgMarExp, 
            double avgHPStd, double avgHPExp) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
			String row = String.format(java.util.Locale.US,"%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f", 
					seed, avgSic, avgBig, avgCtrl, avgMarStd, avgMarExp, avgHPStd, avgHPExp);
			writer.write(row);
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
		
	public static void writeFile(List<Double> list, String directoryName, String filename) {
        File directory = new File(directoryName);
        BufferedWriter bw = null;

        try {
            if (!directory.exists())
                directory.mkdirs();

            File file = new File(directory, filename + ".dat");

            if (!file.exists())
                file.createNewFile();

            FileWriter writer = new FileWriter(file);
            bw = new BufferedWriter(writer);


            for (int i = 0; i < list.size(); i++) {
                bw.append(String.valueOf(list.get(i)));
                bw.append("\n");
                bw.flush();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                bw.flush();
                bw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
	
	static boolean generateQueueDestination(Rngs rngs, int streamIndex) {
		rngs.selectStream(5 + streamIndex);
		double r = rngs.random(); 
		
		if (r < 0.20) {
			return  true;
		}
		else {
			return false;
		}
	}

	
	/*static boolean generateAbandon(Rngs rngs, int streamIndex, double percentage) {
        rngs.selectStream(2 + streamIndex);
        return rngs.random() <= percentage;
    }*/
	
	private int generateBiglietteriaDestination(Rngs rngs, int streamIndex) {
	    rngs.selectStream(3 + streamIndex);
	    double r = rngs.random();

	    if (r < 0.38) {
	        return 0; // Biglietteria
	    } else {
	        return 1; // Controlli
	    }
	}
	
	private int generateAttractionsDestination(Rngs rngs, int streamIndex) {
		rngs.selectStream(4 + streamIndex);
	    double r = rngs.random();

	    if (r < 0.25) { // Mario Kart 25%
	        return 0;   
	    } else if (r < 0.55) { // Harry Potter 30%
	        return 1;  
	    } else { // Altre attrazioni 45%
	        return 2;
	    } 
	}
		
	int findSicurezzaServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		//System.out.println("CERCHIAMO IL SERVER PER I CONTROLLI DI SICUREZZA");
        int s;

        int i = INDEX_FIRST_SERVER_SICUREZZA;

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < INDEX_LAST_SERVER_SICUREZZA) { //i < 12, perché i server login sono da 1 a 12 ma si entra già facendo i++ quindi deve essere minore stretto di 12  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findBiglietteriaServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;

        int i = INDEX_FIRST_SERVER_BIGLIETTERIA;

        while (event[i].x == 1)  
            i++;                  
        s = i;
        while (i < INDEX_LAST_SERVER_BIGLIETTERIA) { //i < 27, perché i server di Ultimate Team sono da 15 a 27 
        	i++;                                           
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findControlliServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;

        int i = INDEX_FIRST_SERVER_CONTROLLI;

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < INDEX_LAST_SERVER_CONTROLLI) { 
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findMarioKartServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;

        int i = INDEX_FIRST_SERVER_MARIO_VERIFICA;

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < INDEX_LAST_SERVER_MARIO_VERIFICA) { //i < 38, perché i server di Club sono da 34 a 38 ma si entra già facendo i++ quindi deve essere minore stretto di 14  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findHarryPotterServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;

        int i = INDEX_FIRST_SERVER_HP_VERIFICA;

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < INDEX_LAST_SERVER_HP_VERIFICA) {   
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	/**
	 * Funzione Helper per selezionare la prossima coda da servire (Quota + Fallback)
	 */
	/*private boolean selectNextQueue(int jobsStandard, int jobsExpress, boolean currentlyServingStandard, 
	                                int consecutiveServed, int ratioStd, int ratioExp) {
	    if (jobsStandard == 0) return false; // Coda Standard vuota -> Serve Express
	    if (jobsExpress == 0) return true;   // Coda Express vuota -> Serve Standard

	    if (currentlyServingStandard) {
	        return consecutiveServed < ratioStd;
	    } else {
	        return consecutiveServed >= ratioExp;
	    }
	}*/
	
	private double getServiceSicurezza(Rngs r, int streamIndex, double meanServiceTime, Rvms rvms) {
        r.selectStream(streamIndex);
        double variance = SIGMA_SICUREZZA*SIGMA_SICUREZZA;
        double b = Math.sqrt(Math.log(1 + (variance / (meanServiceTime * meanServiceTime))));
        double a = Math.log(meanServiceTime) - 0.5 * b * b;
        return logNormal(a, b, r);	
    }
	
	private double getServiceBiglietteria(Rngs r, int streamIndex, double meanServiceTime, Rvms rvms) {
        r.selectStream(streamIndex);
        double variance = SIGMA_BIGLIETTERIA*SIGMA_BIGLIETTERIA;
        double b = Math.sqrt(Math.log(1 + (variance / (meanServiceTime * meanServiceTime))));
        double a = Math.log(meanServiceTime) - 0.5 * b * b;
        return logNormal(a, b, r);
    }
	
	private double getServiceControlli(Rngs r, int streamIndex, double meanServiceTime, Rvms rvms) {
        r.selectStream(streamIndex);
        double variance = SIGMA_CONTROLLI*SIGMA_CONTROLLI;
        double b = Math.sqrt(Math.log(1 + (variance / (meanServiceTime * meanServiceTime))));
        double a = Math.log(meanServiceTime) - 0.5 * b * b;
        return logNormal(a, b, r);
    }
	
	private double getServiceMario(Rngs r, int streamIndex, double meanServiceTime, Rvms rvms) {
        r.selectStream(streamIndex);
        return (NormalTruncated(meanServiceTime, DEV_ST_MARIO, LOWER_B_MARIO, UPPER_B_MARIO, r, rvms));
    }
	
	private double getServiceHP(Rngs r, int streamIndex, double meanServiceTime, Rvms rvms) {
        r.selectStream(streamIndex);
        return (NormalTruncated(meanServiceTime, DEV_ST_HP, LOWER_B_HP, UPPER_B_HP, r, rvms));
    }
	
	private double NormalTruncated(double mu, double sigma, double min, double max, Rngs rngs, Rvms rvms) {
	    // 1. Calcola la CDF al limite inferiore e superiore
	    double cdfMin = rvms.cdfNormal(mu, sigma, min);
	    double cdfMax = rvms.cdfNormal(mu, sigma, max);
	    Rvgs rvgs = new Rvgs(rngs);

	    // 2. Genera un valore Uniforme nell'intervallo [cdfMin, cdfMax]
	    // Usando lo stream RNG passato come parametro
	    //double u = cdfMin + (cdfMax - cdfMin) * rngs.random();
	    double u = rvgs.uniform(cdfMin, cdfMax);

	    // 3. Applica l'inversa della CDF
	    return rvms.idfNormal(mu, sigma, u);
	}

	
	/*	
	private double truncatedLogNormal(double mu, double sigma, double truncationPoint, Rngs rngs) {
        Rvms rvms = new Rvms();
        Rvgs rvgs = new Rvgs(rngs);

        // Calculate 'a' and 'b' based on the given mean and variance
        double variance = sigma*sigma;
        double b = Math.sqrt(Math.log(1 + (variance / (mu * mu))));
        double a = Math.log(mu) - 0.5 * b * b;

        // Calculate alpha (CDF at the left tail)
        double alpha = rvms.cdfLogNormal(a,b,1e-20);
        // Calculate beta (1 - CDF at truncation point)
        double beta = 1.0 - rvms.cdfLogNormal(a,b,truncationPoint);

        // Generate a uniform value in the range [alpha, 1 - beta]
        double u = rvgs.uniform(alpha, 1.0 - beta);

        // Calculate the inverse distribution function
        return rvms.idfLogNormal(a, b, u);
    }*/
		
	//funzione per generare tempi esponenziali
	private double exponential(double mean, Rngs r) {
        return (-mean * Math.log(1.0 - r.random()));
    }
	
	private double logNormal(double a, double b, Rngs r) {
		return (Math.exp(a + b * normal(0.0, 1.0, r)));
	}
	
	private double normal(double m, double s, Rngs rngs)
	    { 
		final double p0 = 0.322232431088;     final double q0 = 0.099348462606;
		final double p1 = 1.0;                final double q1 = 0.588581570495;
		final double p2 = 0.342242088547;     final double q2 = 0.531103462366;
		final double p3 = 0.204231210245e-1;  final double q3 = 0.103537752850;
		final double p4 = 0.453642210148e-4;  final double q4 = 0.385607006340e-2;
		double u, t, p, q, z;
		
		u   = rngs.random();
		if (u < 0.5)
		    t = Math.sqrt(-2.0 * Math.log(u));
		else
		    t = Math.sqrt(-2.0 * Math.log(1.0 - u));
		p   = p0 + t * (p1 + t * (p2 + t * (p3 + t * p4)));
		q   = q0 + t * (q1 + t * (q2 + t * (q3 + t * q4)));
		if (u < 0.5)
		    z = (p / q) - t;
		else
		    z = t - (p / q);
		return (m + s * z);
	    }
	
	private double getArrival(Rngs r, int streamIndex, double currentLambda) {
	    r.selectStream(1 + streamIndex);
	    sarrival += exponential(1.0 / currentLambda, r);
	    return sarrival;
	}
	
	private int nextEvent(MsqEvent[] event) {
		//System.out.println("Ricerca in corso del prossimo evento da elaborare...");
	    int e;
	    int i = 0;
	    while (event[i].x == 0) 
	    	i++;
	    e = i;
	    while (i < ALL_EVENTS_WITH_SAVE_STAT_VERIFICA -1) {
	    	i++;
	    	if ((event[i].x == 1) && (event[i].t < event[e].t)) {
	    		e = i;
	    	}
	    }
	    return (e);   
	}
	
}