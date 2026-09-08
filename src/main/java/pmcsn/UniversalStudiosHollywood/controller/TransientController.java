package pmcsn.UniversalStudiosHollywood.controller;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import pmcsn.UniversalStudiosHollywood.libraries.Rngs;
import pmcsn.UniversalStudiosHollywood.model.BiglietteriaFisicaNode;
import pmcsn.UniversalStudiosHollywood.model.ControlloBigliettiNode;
import pmcsn.UniversalStudiosHollywood.model.ControlloSicurezzaNode;
import pmcsn.UniversalStudiosHollywood.model.HarryPotterNode;
import pmcsn.UniversalStudiosHollywood.model.MarioKartNode;
import pmcsn.UniversalStudiosHollywood.model.TransientStats;
import pmcsn.UniversalStudiosHollywood.utils.Rvgs;
import pmcsn.UniversalStudiosHollywood.utils.Rvms;

import static pmcsn.UniversalStudiosHollywood.model.Constants.*;
import static pmcsn.UniversalStudiosHollywood.model.Events.*;

public class TransientController {
	
	static double START = 0.0; //tempo d'inizio della simulazione
    static double sarrival = START; //ultimo tempo in cui è stato generato un arrivo
    static double STOP = 18000;
    static int INTERVAL_DATA = 20;
    static double COLUMNS = (STOP/INTERVAL_DATA) + 1;
	
	public void startAnalysis() {
		String filenameSicurezza = "transientSicurezza.csv";
		String filenameBiglietteria = "transientBiglietteria.csv";
		String filenameControlli = "transientControlli.csv";
		String filenameMario = "transientMario.csv";
		String filenameHP = "transientHP.csv";
		long[] seeds = new long[1024];
		seeds[0] = 123456789;
		Rngs r = new Rngs();
		
		//for (int i = 0; i < 10; i++) {
		for (int i = 0; i < 150; i++) {
			System.out.println("ITERAZIONE: " + i);
			TransientStats ts = new TransientStats();//va inizializzato dentro al ciclo perché ad ogni nuova run raccolgo nuove statistiche da 0
			sarrival = START;
			seeds[i+1] = finiteHorizonSimulation(seeds[i], r, ts);
			writeCsv(ts.getTransientStatsSicurezza(), seeds[i], filenameSicurezza);
			writeCsv(ts.getTransientStatsBiglietteria(), seeds[i], filenameBiglietteria);
			writeCsv(ts.getTransientStatsControlli(), seeds[i], filenameControlli);
			writeCsv(ts.getTransientStatsMario(), seeds[i], filenameMario);
			writeCsv(ts.getTransientStatsHP(), seeds[i], filenameHP);	
		}
	}
	
	private long finiteHorizonSimulation(long seed, Rngs rng, TransientStats ts) {
		
		ControlloSicurezzaNode sicurezza = new ControlloSicurezzaNode();
		BiglietteriaFisicaNode biglietteria = new BiglietteriaFisicaNode();
		ControlloBigliettiNode controlli = new ControlloBigliettiNode();
		MarioKartNode mario_kart = new MarioKartNode();
		HarryPotterNode harry_potter = new HarryPotterNode();
	    
	    long totalJobsInSicurezza = 0;
	    long totalJobsInBiglietteria = 0;
	    long totalJobsInControlli = 0;
	    long totalJobsInMario = 0;
	    long totalJobsInHP = 0;
		
	    int totalSicurezzaCheck = 0;
	    int totalBiglietteriaCheck = 0;
	    int totalControlliCheck = 0;
	    int totalMarioCheck = 0;
	    int totalHPCheck = 0;
	    
	    double nodeAreaSicurezza = 0.0; 
		double nodeAreaBiglietteria = 0.0; 
		double nodeAreaControlli = 0.0; 
		double nodeAreaMario = 0.0;
		double nodeAreaHP= 0.0;  
		        
        //inizializzazione dei double per memorizzare il primo completamento delle varie code
		double firstCompletionSicurezza = 0;
	    double firstCompletionBiglietteria = 0;
	    double firstCompletionControlli = 0;
	    double firstCompletionMario = 0;
	    double firstCompletionHP = 0;
        
		int e; //indice next event, cioè l'evento più imminente
		int s; //indice del server
		
		double service; //tempo di servizio
		
		rng.plantSeeds(seed);
		
        MsqEvent[] events = new MsqEvent[ALL_EVENTS_STAT_LAMBDA_VERIFICA];
        MsqSum[] sum = new MsqSum[ALL_EVENTS_VERIFICA];
        Rvms rvms = new Rvms();
        
        //System.out.println("Lista eventi (incluso SAVE_STAT e LAMBDA): ");
        for (int i = 0; i < ALL_EVENTS_STAT_LAMBDA_VERIFICA; i++) {
            events[i] = new MsqEvent();
        }
        for (int i = 0; i < ALL_EVENTS_VERIFICA; i++) {
            sum[i] = new MsqSum();
        }
        
        //inizializzazione clock
        MsqT t = new MsqT();
        t.current = START;  
        
     // --- INIZIALIZZAZIONE FASI DI ARRIVO ---
        int currentPhase = 1;
        double currentLambda = LAMBDA1;
     
        
        //EVENTO SAVE_STAT
        //System.out.println("\n------------GENERAZIONE SAVE_STAT----------");        
        events[ALL_EVENTS_VERIFICA].t = INTERVAL_DATA;
        events[ALL_EVENTS_VERIFICA].x = 1;
        events[ALL_EVENTS_WITH_SAVE_STAT_VERIFICA].t = 3600.0; //l'ultimo elemento è ALL_EVENTS_VERIFICA + 1
        events[ALL_EVENTS_WITH_SAVE_STAT_VERIFICA].x = 1;
        
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
    	ts.getTransientStatsMario().add(0.0);
    	ts.getTransientStatsHP().add(0.0);
    	
        while(events[0].x != 0 || totalJobsInSicurezza+totalJobsInBiglietteria+totalJobsInControlli+totalJobsInMario+totalJobsInHP != 0) {
        	
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
    	    nodeAreaMario += (t.next - t.current)*totalJobsInMario;
    	    nodeAreaHP += (t.next - t.current)*totalJobsInHP;
    		
    		//System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.current = t.next;
            System.out.println("Siamo all'istante: " + t.current);
            //System.out.println("L'evento successivo doveva avvenire all'istante: " + t.next);
            //System.out.println("I due tempi coincidono, quindi andiamo a processare l'evento " + e);
            if (e == ALL_EVENTS_WITH_SAVE_STAT_VERIFICA) {
            	System.out.println("\n--------- EVENTO: CAMBIO TASSO DI ARRIVO -------------");
                
                double oldLambda = currentLambda;
                currentPhase++; // Passa alla fase/fascia successiva
                
                // 1. Aggiorna il valore di lambda e schedula il cambio successivo
                if (currentPhase == 2) { 
                    currentLambda = LAMBDA2; 
                    events[ALL_EVENTS_WITH_SAVE_STAT_VERIFICA].t = 7200.0; 
                } else if (currentPhase == 3) { 
                    currentLambda = LAMBDA3; 
                    events[ALL_EVENTS_WITH_SAVE_STAT_VERIFICA].t = 10800.0; 
                } else if (currentPhase == 4) { 
                    currentLambda = LAMBDA4; 
                    events[ALL_EVENTS_WITH_SAVE_STAT_VERIFICA].t = 14400.0; 
                } else if (currentPhase == 5) { 
                    currentLambda = LAMBDA5; 
                    events[ALL_EVENTS_WITH_SAVE_STAT_VERIFICA].x = 0; // Disattiva gli eventi di cambio tasso (fase finale)
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
            } else if (e == ALL_EVENTS_VERIFICA) {
            	System.out.println("\n---------L'EVENTO è il SALVATAGGIO STATISTICHE TRANSITORIO-------------");
            	/*double responseTimeSicurezza = nodeAreaSicurezza/totalSicurezzaCheck;
            	double responseTimeBiglietteria = nodeAreaBiglietteria/totalBiglietteriaCheck;
            	double responseTimeControlli = nodeAreaControlli/totalControlliCheck;
            	double responseTimeMario = nodeAreaMario/totalMarioCheck;
            	double responseTimeHP = nodeAreaHP/totalHPCheck;*/
            	
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

                double queueAreaMario = nodeAreaMario;
                for (int i = INDEX_FIRST_SERVER_MARIO_VERIFICA; i <= INDEX_LAST_SERVER_MARIO_VERIFICA; i++) {
                    queueAreaMario -= sum[i].service;
                }

                double queueAreaHP = nodeAreaHP;
                for (int i = INDEX_FIRST_SERVER_HP_VERIFICA; i <= INDEX_LAST_SERVER_HP_VERIFICA; i++) {
                    queueAreaHP -= sum[i].service;
                }
                
                // --- 3. Calcolo dei soli Tempi Medi di Attesa in Coda (E[Tq]) ---
                double queueTimeSicurezza = Math.max(0, queueAreaSicurezza/totalSicurezzaCheck);
                double queueTimeBiglietteria = Math.max(0, queueAreaBiglietteria/totalBiglietteriaCheck);
                double queueTimeControlli = Math.max(0, queueAreaControlli/totalControlliCheck);
                double queueTimeMario = Math.max(0, queueAreaMario/totalMarioCheck); 
                double queueTimeHP = Math.max(0, queueAreaHP/totalHPCheck);
            	
            	ts.getTransientStatsSicurezza().add(queueTimeSicurezza);
            	ts.getTransientStatsBiglietteria().add(queueTimeBiglietteria);
            	ts.getTransientStatsControlli().add(queueTimeControlli);
            	ts.getTransientStatsMario().add(queueTimeMario);
            	ts.getTransientStatsHP().add(queueTimeHP);
            	events[ALL_EVENTS_VERIFICA].t += INTERVAL_DATA;
            	if (events[ALL_EVENTS_VERIFICA].t > STOP) {
            		events[ALL_EVENTS_VERIFICA].x = 0;
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
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI MARIO----------");
            	totalJobsInMario++;
            	
            	if (totalJobsInMario <= SERVERS_MARIO) { //verifico se posso essere servito subito
            		service = getServiceMario(rng, mario_kart.getStreamIndex(), mario_kart.getServiceTime(), rvms);
            		
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
            	totalJobsInHP++;
            	
            	if (totalJobsInHP <= SERVERS_HP) { //verifico se posso essere servito subito
            		service = getServiceHP(rng, harry_potter.getStreamIndex(), harry_potter.getServiceTime(), rvms);
            		
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
                		            		
                	    events[INDEX_ARRIVAL_BIGLIETTERIA].t = t.current; 
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
            	if (prova) { 
            		double provaTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            			
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
                		//System.out.println("Non ci sono altri elementi in coda Ultimate Team, il server " + s + " diventa disponibile");
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
                		//System.out.println("Ci sono degli elementi in coda Stagioni da servire, ma ora il server " + s + " si è liberato");
                		service = getServiceControlli(rng, controlli.getStreamIndex(), controlli.getServiceTime(), rvms);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= INDEX_FIRST_SERVER_MARIO_VERIFICA) && (e <= INDEX_LAST_SERVER_MARIO_VERIFICA)) {
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DEL SERVENTE DI MARIO KART------------------");
            	if (firstCompletionMario == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionMario = t.current; 
            	}
            	
            	boolean prova = false;
            	if (prova) { //se l'utente non supera i controlli
            		
            		double provaTime = t.current + 0.01;
            			
            	}
            	else {
            		totalJobsInMario--;//diminuisco di 1 il numero di utenti in questo centro
            		totalMarioCheck++;//aumento il numero di utenti serviti in questo centro
                	
                	
                	s = e;
                	
                	if (totalJobsInMario >= SERVERS_MARIO) {//ci sono ancora elementi in coda
                		
                		service = getServiceMario(rng, mario_kart.getStreamIndex(), mario_kart.getServiceTime(), rvms);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= INDEX_FIRST_SERVER_HP_VERIFICA) && (e <= INDEX_LAST_SERVER_HP_VERIFICA)) { 
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DEL SERVENTE DI HARRY POTTER------------------");
            	if (firstCompletionHP == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionHP = t.current; 
            	}
            	
            	boolean prova = false;
            	if (prova) { 
            		double provaTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            			
            	}
            	else {
            		totalJobsInHP--;//diminuisco di 1 il numero di utenti in questo centro
            		totalHPCheck++;//aumento il numero di utenti serviti in questo centro
                	
                	s = e;
                	
                	if (totalJobsInHP >= SERVERS_HP) {//ci sono ancora elementi in coda
                		//System.out.println("Ci sono degli elementi Club da servire, ma ora il server " + s + " si è liberato");
                		service = getServiceHP(rng, harry_potter.getStreamIndex(), harry_potter.getServiceTime(), rvms);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}	
            } 
        }
        
		rng.selectStream(255);
		return rng.getSeed();
	}
	
	private void writeCsv(List<Double> list, long seed, String filepath) {
		File file = new File(filepath);
		boolean fileExists = file.exists();

		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
			if (!fileExists) {
				//writer.write("seed, valore_1, valore_2, valore_3");
				StringBuilder header = new StringBuilder("seed");
	            for (int i = 0; i <= COLUMNS; i++) {
	            	header.append(",tempo_").append(i*INTERVAL_DATA);
	            }
	            writer.write(header.toString());
				writer.newLine();
			}
			
			StringBuilder row = new StringBuilder();
			row.append(seed);
			for (double d: list) {
				row.append(",").append(d);
			}
			
			writer.write(row.toString());
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
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
        while (i < INDEX_LAST_SERVER_SICUREZZA) {   
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
        while (i < INDEX_LAST_SERVER_BIGLIETTERIA) { 
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
        while (i < INDEX_LAST_SERVER_MARIO_VERIFICA) {   
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
	    while (i < ALL_EVENTS_STAT_LAMBDA_VERIFICA -1) {
	    	i++;
	    	if ((event[i].x == 1) && (event[i].t < event[e].t)) {
	    		e = i;
	    	}
	    }
	    return (e);   
	}
	
}