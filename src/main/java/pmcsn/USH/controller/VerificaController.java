package pmcsn.USH.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pmcsn.USH.libraries.Msq;
import pmcsn.USH.libraries.Rngs;

import pmcsn.USH.model.MarioKartNode;
import pmcsn.USH.model.BiglietteriaFisicaNode;
import pmcsn.USH.model.ControlloBigliettiNode;
import pmcsn.USH.model.ControlloSicurezzaNode;
import pmcsn.USH.model.HarryPotterNode;
import pmcsn.USH.utils.Estimate;
import pmcsn.USH.model.Node;

import static pmcsn.USH.model.Constants.*;

import static pmcsn.USH.model.Events.*;



public class VerificaController {
	
	static double START = 0.0; //tempo d'inizio della simulazione
    static double sarrival = START; //ultimo tempo in cui è stato generato un arrivo
	
	private BiglietteriaFisicaNode biglietteria;
	private ControlloSicurezzaNode sicurezza;
	private ControlloBigliettiNode controlli;
	private MarioKartNode mario_kart;
	private HarryPotterNode harry_potter;
	
	public void startAnalysis() {
		
		int batchsize = 750; //B da aumentare
		int numBatches = 500; //K
		
		int intervalLength = 480;
		
		//primo istante del nuovo batch ed ogni primo arrivo
	    double currentBatchStartTime = 0;
	    double currentFirstArrivalTimeSicurezza = 0;
	    double currentFirstArrivalTimeBiglietteria = 0;
	    double currentFirstArrivalTimeControlli = 0;
	    double currentFirstArrivalTimeMario = 0;
	    double currentFirstArrivalTimeHP= 0;
	    
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
		
		List<Double> dropoutsLoginQueue = new ArrayList<>();
		int dropoutsLogin = 0;
		List<Double> dropoutsUltimateTeamQueue = new ArrayList<>();
		int dropoutsUltimateTeam = 0;
		List<Double> dropoutsStagioniQueue = new ArrayList<>();
		int dropoutsStagioni = 0;
		List<Double> dropoutsClubQueue = new ArrayList<>();
		int dropoutsClub = 0;
		
		biglietteria = new BiglietteriaFisicaNode();
		sicurezza = new ControlloSicurezzaNode();
		controlli = new ControlloBigliettiNode();
		mario_kart = new MarioKartNode();
		harry_potter = new HarryPotterNode();
		
        //Setup generatore RNG
		Rngs rng = new Rngs();
		long seed = 123456789;
		rng.plantSeeds(seed);
        
        MsqEvent[] events = new MsqEvent[ALL_EVENTS_WITH_SAVE_STAT_VERIFICA];
        MsqSum[] sum = new MsqSum[ALL_EVENTS_VERIFICA];
        
        for (int i = 0; i < ALL_EVENTS_WITH_SAVE_STAT_VERIFICA; i++) {
            events[i] = new MsqEvent();
        }
        for (int i = 0; i < ALL_EVENTS_VERIFICA; i++) {
            sum[i] = new MsqSum();
        }
        
        //inizializzazione clock
        MsqT t = new MsqT();
        t.current = START;  
        
        events[0].t = getArrival(rng, sicurezza.getStreamIndex(), t.current);
        events[0].x = 1;
                
        events[ALL_EVENTS_VERIFICA].t = intervalLength;
        events[ALL_EVENTS_VERIFICA].x = 1;
        
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
        
        int iter = 0;
        
        int[] batchCounter = new int[NUMBER_OF_CENTERS];
        for (int i = 0; i < NUMBER_OF_CENTERS; i++) {
        	batchCounter[i] = 0;
        }
        
        while(events[0].x != 0) {
        	
        	iter++;
        	System.out.println("\n\n-------LA SIMULAZIONE VA AVANTI, QUINDI NUOVA ITERAZIONE, è LA NUMERO: " + iter);
        	
        	if (totalSicurezzaCheck != 0 && totalSicurezzaCheck % batchsize == 0 && batchCounter[0] < numBatches) {
    			batchCounter[0]++;
    			statsBatch(sicurezza, nodeAreaSicurezza, t.current, totalSicurezzaCheck, INDEX_FIRST_SERVER_SICUREZZA, INDEX_LAST_SERVER_SICUREZZA, sum, events[INDEX_ARRIVAL_SICUREZZA].t);
    			nodeAreaSicurezza = 0.0;
    			for (int i = INDEX_FIRST_SERVER_SICUREZZA; i <= INDEX_LAST_SERVER_SICUREZZA; i++) {
    		        sum[i].service = 0;
    		        sum[i].served = 0;
    		    }
    			totalSicurezzaCheck = 0;
    			//dropoutsLogin = 0;
    			
    			currentFirstArrivalTimeSicurezza = events[INDEX_ARRIVAL_SICUREZZA].t;
    			sicurezza.setCurrentFirstArrivalTime(currentFirstArrivalTimeSicurezza);
    			
    			currentBatchStartTime = t.current;
    			sicurezza.setCurrentStartTimeBatch(currentBatchStartTime);
    		}
    		//System.out.println("Job serviti nel batch Sicurezza: " + totalSicurezzaCheck);
            if (totalBiglietteriaCheck != 0 && totalBiglietteriaCheck % batchsize == 0 && batchCounter[1] < numBatches) {
    			
    			batchCounter[1]++;
    			statsBatch(biglietteria, nodeAreaBiglietteria, t.current, totalBiglietteriaCheck, INDEX_FIRST_SERVER_BIGLIETTERIA, INDEX_LAST_SERVER_BIGLIETTERIA, sum, events[INDEX_ARRIVAL_BIGLIETTERIA].t);
    			nodeAreaBiglietteria = 0.0;
    			for (int i = INDEX_FIRST_SERVER_BIGLIETTERIA; i <= INDEX_LAST_SERVER_BIGLIETTERIA; i++) {
    		        sum[i].service = 0;
    		        sum[i].served = 0;
    		    }
    			totalBiglietteriaCheck = 0;
    			//dropoutsUltimateTeam = 0;
    			
    			currentFirstArrivalTimeBiglietteria = events[INDEX_ARRIVAL_BIGLIETTERIA].t;
    			biglietteria.setCurrentFirstArrivalTime(currentFirstArrivalTimeBiglietteria);
    			
    			currentBatchStartTime = t.current;
    			biglietteria.setCurrentStartTimeBatch(currentBatchStartTime);
    		}
            //System.out.println("Job serviti nel batch Biglietteria: " + totalBiglietteriaCheck);
            if (totalControlliCheck != 0 && totalControlliCheck % batchsize == 0 && batchCounter[2] < numBatches) {
    			batchCounter[2]++;
    			statsBatch(controlli, nodeAreaControlli, t.current, totalControlliCheck, INDEX_FIRST_SERVER_CONTROLLI, INDEX_LAST_SERVER_CONTROLLI, sum, events[INDEX_ARRIVAL_CONTROLLI].t);
    			nodeAreaControlli = 0.0;
    			for (int i = INDEX_FIRST_SERVER_CONTROLLI; i <= INDEX_LAST_SERVER_CONTROLLI; i++) {
    		        sum[i].service = 0;
    		        sum[i].served = 0;
    		    }
    			totalControlliCheck = 0;
    			//dropoutsStagioni = 0;
    			
    			currentFirstArrivalTimeControlli = events[INDEX_ARRIVAL_CONTROLLI].t;
    			controlli.setCurrentFirstArrivalTime(currentFirstArrivalTimeControlli);
    			
    			currentBatchStartTime = t.current;
    			controlli.setCurrentStartTimeBatch(currentBatchStartTime);
    		}
            //System.out.println("Job serviti nel batch Controlli: " + totalControlliCheck);
            if (totalMarioCheck != 0 && totalMarioCheck % batchsize == 0 && batchCounter[3] < numBatches) {
    			batchCounter[3]++;
    			statsBatch(mario_kart, nodeAreaMario, t.current, totalMarioCheck, INDEX_FIRST_SERVER_MARIO_VERIFICA, INDEX_LAST_SERVER_MARIO_VERIFICA, sum, events[INDEX_ARRIVAL_MARIO].t);
    			nodeAreaMario = 0.0;
    			for (int i = INDEX_FIRST_SERVER_MARIO_VERIFICA; i <= INDEX_LAST_SERVER_MARIO_VERIFICA; i++) {
    		        sum[i].service = 0;
    		        sum[i].served = 0;
    		    }
    			totalMarioCheck = 0;
    			//dropoutsClub = 0;
    			
    			currentFirstArrivalTimeMario = events[INDEX_ARRIVAL_MARIO].t;
    			mario_kart.setCurrentFirstArrivalTime(currentFirstArrivalTimeMario);
    			
    			currentBatchStartTime = t.current;
    			mario_kart.setCurrentStartTimeBatch(currentBatchStartTime);
    		}
            
            if (totalHPCheck != 0 && totalHPCheck % batchsize == 0 && batchCounter[4] < numBatches) {
    			batchCounter[4]++;
    			statsBatch(harry_potter, nodeAreaHP, t.current, totalHPCheck, INDEX_FIRST_SERVER_HP_VERIFICA, INDEX_LAST_SERVER_HP_VERIFICA, sum, events[INDEX_ARRIVAL_HP].t);
    			nodeAreaHP = 0.0;
    			for (int i = INDEX_FIRST_SERVER_HP_VERIFICA; i <= INDEX_LAST_SERVER_HP_VERIFICA; i++) {
    		        sum[i].service = 0;
    		        sum[i].served = 0;
    		    }
    			totalHPCheck = 0;
    			
    			currentFirstArrivalTimeHP = events[INDEX_ARRIVAL_HP].t;
    			harry_potter.setCurrentFirstArrivalTime(currentFirstArrivalTimeHP);
    			
    			currentBatchStartTime = t.current;
    			harry_potter.setCurrentStartTimeBatch(currentBatchStartTime);
    		}
        	
        	//System.out.println("---Aggiornamento batch completati:");
        	int checkBatchCounter = 1;
        	for (int i = 0; i < NUMBER_OF_CENTERS; i++) {
        		System.out.println("Counter " + i + ": " + batchCounter[i]);
        		if (batchCounter[i] < numBatches) {
        			checkBatchCounter = 0;
        		}
        	}	
        
        	if (checkBatchCounter == 1) {
        		System.out.println("-----------SIMULAZIONE FINITA---------------");
        		break;
        	}
        	
        	
            // Trova evento più imminente
            e = nextEvent(events);
            //System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.next = events[e].t;
            /*System.out.println("Siamo all'istante: " + t.current);
            System.out.println("Il prossimo evento (che è quello appena trovato) avverrà all'istante: " + t.next);*/

    		//Node area
            nodeAreaSicurezza += (t.next - t.current)*totalJobsInSicurezza;
    	    nodeAreaBiglietteria += (t.next - t.current)*totalJobsInBiglietteria;
    	    nodeAreaControlli += (t.next - t.current)*totalJobsInControlli;
    	    nodeAreaMario += (t.next - t.current)*totalJobsInMario;
    	    nodeAreaHP += (t.next - t.current)*totalJobsInHP;
    	    
    		
    		//System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.current = t.next;
            /*System.out.println("Siamo all'istante: " + t.current);
            System.out.println("L'evento successivo doveva avvenire all'istante: " + t.next);
            System.out.println("I due tempi coincidono, quindi andiamo a processare l'evento " + e);*/

            if (e == ALL_EVENTS_VERIFICA) {
            	events[ALL_EVENTS_VERIFICA].t += intervalLength;
            } else if (e == INDEX_ARRIVAL_SICUREZZA) { //e == 0
            	System.out.println("\n---------L'EVENTO è UN NUOVO ARRIVO NEL CENTRO SICUREZZA-------------");
            	totalJobsInSicurezza++;
            	/*System.out.println("Job nel nodo Login: " + totalJobsInLogin);
            	System.out.println("Numero di serventi della coda Login: " + SERVERS_LOGIN);
            	
            	System.out.println("------(Intanto pianifico il nuovo evento di arrivo, che sarà alla coda Login)");*/
            	events[INDEX_ARRIVAL_SICUREZZA].t = getArrival(rng, sicurezza.getStreamIndex(), t.current);
            	//System.out.println("--------(Sarà un arrivo in coda Login, all'istante: " + events[0].t + ")");
            	            	
            	if (totalJobsInSicurezza <= SERVERS_SICUREZZA) {
            		//System.out.println("Ci sono meno utenti nel centro di quanti server totali");
            		service = getService(rng, sicurezza.getStreamIndex(), sicurezza.getServiceTime());
            		//System.out.println("Si cerca un server libero");
            		s = findSicurezzaServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            
            } else if (e == INDEX_ARRIVAL_BIGLIETTERIA) { //e == 14, cioè l'arrivo ad Ultimate Team
            	
            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DELLA BIGLIETTERIA----------");
            	totalJobsInBiglietteria++;
            	/*System.out.println("Elementi nel centro Ultimate Team: " + totalJobsInUltimateTeam);
            	System.out.println("Numero di server della coda Ultimate Team: " + SERVERS_ULTIMATE_TEAM);*/
            	
            	if (totalJobsInBiglietteria <= SERVERS_BIGLIETTERIA) { //verifico se posso essere servito subito
            		service = getService(rng, biglietteria.getStreamIndex(), biglietteria.getServiceTime());
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_ULTIMATE_TEAM);
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
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DEI CONTROLLI----------");
            	totalJobsInControlli++;
            	/*System.out.println("Elementi nel centro Stagioni: " + totalJobsInStagioni);
            	System.out.println("Numero di server della coda Stagioni: " + SERVERS_STAGIONI);*/
            	
            	if (totalJobsInControlli <= SERVERS_CONTROLLI) { //verifico se posso essere servito subito
            		service = getService(rng, controlli.getStreamIndex(), controlli.getServiceTime());
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_STAGIONI);
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
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA STANDARD DI MARIO KART----------");
            	totalJobsInMario++;
            	/*System.out.println("Elementi nel centro Pro Club: " + totalJobsInClub);
            	System.out.println("Numero di server della coda Pro Club: " + SERVERS_CLUB);*/
            	
            	if (totalJobsInMario <= SERVERS_MARIO) { //verifico se posso essere servito subito
            		
            		service = getService(rng, mario_kart.getStreamIndex(), mario_kart.getServiceTime());
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_CLUB);
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
                events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA STANDARD DI HARRY POTTER----------");
            	totalJobsInHP++;
            	/*System.out.println("Elementi nel centro Pro Club: " + totalJobsInClub);
            	System.out.println("Numero di server della coda Pro Club: " + SERVERS_CLUB);*/
            	
            	if (totalJobsInHP <= SERVERS_HP) { //verifico se posso essere servito subito
            		
            		service = getService(rng, harry_potter.getStreamIndex(), harry_potter.getServiceTime());
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_CLUB);
            		s = findHarryPotterServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    
                    //System.out.println("AUMENTO DI 1 SERVED");
                    events[s].t = t.current + service;
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if ((e >= INDEX_FIRST_SERVER_SICUREZZA) && (e <= INDEX_LAST_SERVER_SICUREZZA)) { //eventi dei server di Login, 1 e 12
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER AL CENTRO SICUREZZA------------------");
            	
            	if (firstCompletionSicurezza == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionSicurezza = t.current; 
            	}
            	boolean abandon = false;
            	if (abandon == true) { //se l'utente non supera i controlli
            		//System.out.println("L'utente non ha superato i controlli del Login");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		//dropoutsLoginQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
            	}
            	else {
            		int percorsi = generateBiglietteriaDestination(rng, sicurezza.getStreamIndex());
            		totalJobsInSicurezza--;//diminuisco di 1 il numero di utenti in questo centro
            		totalSicurezzaCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nel Login: " + totalLoginCheck);
                	System.out.println("Utenti ancora nel Login: " + totalJobsInLogin);*/
                	
                	//int percorsi = generateDestination(rng, loginNode.getStreamIndex());
            		/*if (percorsi == -1) { //se l'utente non supera i controlli
                		//System.out.println("L'utente non ha superato i controlli del Login");
                		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
                		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
                		//dropoutsLoginQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
                	}*/
                	               	
            		if (percorsi == 0) {
                		//System.out.println("L'utente andrà in coda Ultimate Team");            		
                	    events[INDEX_ARRIVAL_BIGLIETTERIA].t = t.current; //aggiunto un evento alla coda Biglietteria
                		events[INDEX_ARRIVAL_BIGLIETTERIA].x = 1; //attivazione dell'evento
                	} else if (percorsi == 1) {
                		//System.out.println("L'utente andrà in coda Club");            		
                	    events[INDEX_ARRIVAL_CONTROLLI].t = t.current; //aggiunto un evento alla coda Controlli
                		events[INDEX_ARRIVAL_CONTROLLI].x = 1; //attivazione dell'evento	
                	}
                	
                	s = e;
                	
                	if (totalJobsInSicurezza >= SERVERS_SICUREZZA) {//ci sono ancora elementi in coda
                		//System.out.println("Ci sono degli elementi in coda Login da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, sicurezza.getStreamIndex(), sicurezza.getServiceTime());
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda Login, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
        	} else if ((e >= INDEX_FIRST_SERVER_BIGLIETTERIA) && (e <= INDEX_LAST_SERVER_BIGLIETTERIA)) { //eventi dei server di Ultimate Team, 15 E 27
        		System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVENTE DELLA BIGLIETTERIA------------------");
            	if (firstCompletionBiglietteria == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionBiglietteria = t.current; 
            	}
            	
            	boolean abandon = generateAbandon(rng, biglietteria.getStreamIndex(), not_P5);//qua si decide se l'utente abbandona oppure supera i controlli
            	abandon = false;
            	if (abandon) { //se l'utente non supera i controlli
            		//System.out.println("L'utente non ha superato i controlli di Ultimate Team");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		//dropoutsUltimateTeamQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
            	}
            	else {
            		totalJobsInBiglietteria--;//diminuisco di 1 il numero di utenti in coda in questo centro
            		totalBiglietteriaCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nella coda Ultimate Team: " + totalUltimateTeamCheck);
                	System.out.println("Utenti ancora in UltimateTeam: " + totalJobsInUltimateTeam);*/
            		events[INDEX_ARRIVAL_CONTROLLI].t = t.current; //aggiunto un evento alla coda Controlli
            		events[INDEX_ARRIVAL_CONTROLLI].x = 1;
            		
            		s = e;
                	
                	if (totalJobsInBiglietteria >= SERVERS_BIGLIETTERIA) {//ci sono ancora elementi in coda
                		//System.out.println("Ci sono degli elementi in coda Ultimate Teame da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, biglietteria.getStreamIndex(), biglietteria.getServiceTime());
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda Ultimate Team, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= INDEX_FIRST_SERVER_CONTROLLI) && (e <= INDEX_LAST_SERVER_CONTROLLI)) { //eventi dei server di Stagioni, 30 e 33
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVENTE DEL CONTROLLO BIGLIETTI------------------");
            	if (firstCompletionControlli == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionControlli = t.current; 
            	}
            	
            	boolean abandon = generateAbandon(rng, controlli.getStreamIndex(), not_P6);//qua si decide se l'utente abbandona oppure supera i controlli
            	abandon = false;
            	if (abandon) { //se l'utente non supera i controlli
            		//System.out.println("L'utente non ha superato i controlli di Stagioni");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		//dropoutsStagioniQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
            	}
            	else {
            		totalJobsInControlli--;//diminuisco di 1 il numero di utenti in coda in questo centro
            		totalControlliCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nella coda di Stagioni: " + totalStagioniCheck);
                	System.out.println("Utenti ancora in Stagioni: " + totalJobsInStagioni);*/
            		
            		int percorsi = generateAttractionsDestination(rng, controlli.getStreamIndex());
                	if (percorsi == 0) {
                		events[INDEX_ARRIVAL_MARIO].t = t.current; //aggiunto un evento alla coda Ultimate Team
                		events[INDEX_ARRIVAL_MARIO].x = 1; //attivazione dell'evento
                		//System.out.println("L'utente andrà Mario Kart");	
                	} else if (percorsi == 1) {
                		//System.out.prinln("L'utente andrà in Harry Potter");
                		events[INDEX_ARRIVAL_HP].t = t.current; //aggiunto un evento alla coda Ultimate Team
                		events[INDEX_ARRIVAL_HP].x = 1; //attivazione dell'evento	
                	}
            		
                	s = e;
                	
                	if (totalJobsInControlli >= SERVERS_CONTROLLI) {//ci sono ancora elementi in coda
                		//System.out.println("Ci sono degli elementi in coda Stagioni da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, controlli.getStreamIndex(), controlli.getServiceTime());
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda Stagioni, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= INDEX_FIRST_SERVER_MARIO_VERIFICA) && (e <= INDEX_LAST_SERVER_MARIO_VERIFICA)) { //eventi dei server di Club, 36 e 40
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DEL SERVER DI MARIO------------------");
            	if (firstCompletionMario == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionMario = t.current; 
            	}
            	
            	//boolean abandon = generateAbandon(rng, clubNode.getStreamIndex(), not_P7);//qua si decide se l'utente abbandona oppure supera i controlli
            	boolean abandon = false;
            	if (abandon) { //se l'utente non supera i controlli
            		//System.out.println("L'utente non ha superato i controlli di Club");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		//dropoutsClubQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
            	}
            	else {
            		totalJobsInMario--;//diminuisco di 1 il numero di utenti in questo centro
            		totalMarioCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nel Club: " + totalClubCheck);
                	System.out.println("Utenti ancora nel Club: " + totalJobsInClub);*/
                	
                	s = e;
                	
                	if (totalJobsInMario >= SERVERS_MARIO) {//ci sono ancora elementi in una delle due code di Mario
                		//System.out.println("Ci sono degli elementi Club da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, mario_kart.getStreamIndex(), mario_kart.getServiceTime());
                		sum[s].service += service;
                        sum[s].served++;
                        
                        events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda ProClub, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= INDEX_FIRST_SERVER_HP_VERIFICA) && (e <= INDEX_LAST_SERVER_HP_VERIFICA)) {
            	
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DEL SERVENTE DI HARRY POTTER------------------");
            	if (firstCompletionHP == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionHP = t.current; 
            	}
            	
            	//boolean abandon = generateAbandon(rng, harry_potter.getStreamIndex(), not_P7);//qua si decide se l'utente abbandona oppure supera i controlli
            	boolean abandon = false;
            	if (abandon) { //se l'utente non supera i controlli
            		//System.out.println("L'utente non ha superato i controlli di Club");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		//dropoutsClubQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
            	}
            	else {
            		totalJobsInHP--;//diminuisco di 1 il numero di utenti in questo centro
            		totalHPCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nel Club: " + totalClubCheck);
                	System.out.println("Utenti ancora nel Club: " + totalJobsInClub);*/
                	
                	s = e;
                	
                	if (totalJobsInHP >= SERVERS_HP) {//ci sono ancora elementi in coda
                		service = getService(rng, harry_potter.getStreamIndex(), harry_potter.getServiceTime());
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda ProClub, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            } /*else if (e == INDEX_DROPOUT_LOGIN) { //e == 13
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA LOGIN------------");
            	dropoutsLogin++;
            	dropoutsLoginQueue.remove(0);	
            } else if (e == INDEX_DROPOUT_ULTIMATE_TEAM) { //e == 28
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA ULTIMATE TEAM------------");
            	dropoutsUltimateTeam++;
            	dropoutsUltimateTeamQueue.remove(0);
            	
            } else if (e == INDEX_DROPOUT_STAGIONI) { //e == 34
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA STAGIONI------------");
            	dropoutsStagioni++;
            	dropoutsStagioniQueue.remove(0);
            	
            } else if (e == INDEX_DROPOUT_CLUB) { //e == 41
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA CLUB------------");
            	dropoutsClub++;
            	dropoutsClubQueue.remove(0);
            }*/
        }
        
        removeWarmUp(sicurezza.getPopolazioneDellaCodaBatch());
        removeWarmUp(sicurezza.getPopolazioneDelSistemaBatch());
        removeWarmUp(sicurezza.getTempiDiServizioBatch());
        removeWarmUp(sicurezza.getTempiMediDiRispostaBatch());
        removeWarmUp(sicurezza.getTempiMediInCodaBatch());
        removeWarmUp(sicurezza.getInterarriviBatch());
        removeWarmUp(sicurezza.getUtilizzazioneBatch());
        
        removeWarmUp(biglietteria.getPopolazioneDellaCodaBatch());
        removeWarmUp(biglietteria.getPopolazioneDelSistemaBatch());
        removeWarmUp(biglietteria.getTempiDiServizioBatch());
        removeWarmUp(biglietteria.getTempiMediDiRispostaBatch());
        removeWarmUp(biglietteria.getTempiMediInCodaBatch());
        removeWarmUp(biglietteria.getInterarriviBatch());
        removeWarmUp(biglietteria.getUtilizzazioneBatch());
        
        removeWarmUp(controlli.getPopolazioneDellaCodaBatch());
        removeWarmUp(controlli.getPopolazioneDelSistemaBatch());
        removeWarmUp(controlli.getTempiDiServizioBatch());
        removeWarmUp(controlli.getTempiMediDiRispostaBatch());
        removeWarmUp(controlli.getTempiMediInCodaBatch());
        removeWarmUp(controlli.getInterarriviBatch());
        removeWarmUp(controlli.getUtilizzazioneBatch());
        
        removeWarmUp(mario_kart.getPopolazioneDellaCodaBatch());
        removeWarmUp(mario_kart.getPopolazioneDelSistemaBatch());
        removeWarmUp(mario_kart.getTempiDiServizioBatch());
        removeWarmUp(mario_kart.getTempiMediDiRispostaBatch());
        removeWarmUp(mario_kart.getTempiMediInCodaBatch());
        removeWarmUp(mario_kart.getInterarriviBatch());
        removeWarmUp(mario_kart.getUtilizzazioneBatch());
        
        removeWarmUp(harry_potter.getPopolazioneDellaCodaBatch());
        removeWarmUp(harry_potter.getPopolazioneDelSistemaBatch());
        removeWarmUp(harry_potter.getTempiDiServizioBatch());
        removeWarmUp(harry_potter.getTempiMediDiRispostaBatch());
        removeWarmUp(harry_potter.getTempiMediInCodaBatch());
        removeWarmUp(harry_potter.getInterarriviBatch());
        removeWarmUp(harry_potter.getUtilizzazioneBatch());
        
        //CENTRO DI SICUREZZA
        writeFile(sicurezza.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_sicurezza");
        writeFile(sicurezza.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_sicurezza");
        writeFile(sicurezza.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_sicurezza");
        writeFile(sicurezza.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_sicurezza");
        writeFile(sicurezza.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_sicurezza");
        //writeFile(sicurezza.getInterarriviBatch(),"batch_reports", "interarrivi_sicurezza");
        writeFile(sicurezza.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_sicurezza");
        
        //BIGLIETTERIA FISICA
        writeFile(biglietteria.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_biglietteria");
        writeFile(biglietteria.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_biglietteria");
        writeFile(biglietteria.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_biglietteria");
        writeFile(biglietteria.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_biglietteria");
        writeFile(biglietteria.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_biglietteria");
        //writeFile(biglietteria.getInterarriviBatch(),"batch_reports", "interarrivi_biglietteria");
        writeFile(biglietteria.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_biglietteria");
        
        //CONTROLLI BIGLIETTI
        writeFile(controlli.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_controlli");
        writeFile(controlli.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_controlli");
        writeFile(controlli.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_controlli");
        writeFile(controlli.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_controlli");
        writeFile(controlli.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_controlli");
        //writeFile(controlli.getInterarriviBatch(),"batch_reports", "interarrivi_controlli");
        writeFile(controlli.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_controlli");
        
        //CODA STANDARD MARIO KART
        writeFile(mario_kart.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_Mario");
        writeFile(mario_kart.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_Mario");
        writeFile(mario_kart.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_Mario");
        writeFile(mario_kart.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_Mario");
        writeFile(mario_kart.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_Mario");
        //writeFile(clubNode.getInterarriviBatch(),"batch_reports", "interarrivi_MarioStandard");
        writeFile(mario_kart.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_Mario");
        
        //CODA STANDARD HARRY POTTER 
        writeFile(harry_potter.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_HP");
        writeFile(harry_potter.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_HP");
        writeFile(harry_potter.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_HP");
        writeFile(harry_potter.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_HP");
        writeFile(harry_potter.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_HP");
        //writeFile(harry_potter_standard.getInterarriviBatch(),"batch_reports", "interarrivi_HPStandard");
        writeFile(harry_potter.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_HP");
                
        Estimate estimate = new Estimate();

        List<String> filenames = Arrays.asList(
        		"popolazione_coda_sicurezza",
                "popolazione_sistema_sicurezza",
                "tempiDiservizio_sicurezza",
                "tempiDiRisposta_sicurezza",
                "tempi_in_coda_sicurezza",
                "utilizzazione_sicurezza", 
        		"popolazione_coda_biglietteria","popolazione_sistema_biglietteria", "tempiDiservizio_biglietteria","tempiDiRisposta_biglietteria",
                "tempi_in_coda_biglietteria","utilizzazione_biglietteria", 
                "popolazione_coda_controlli",
                "popolazione_sistema_controlli",
                "tempiDiservizio_controlli",
                "tempiDiRisposta_controlli",
                "tempi_in_coda_controlli",
                "utilizzazione_controlli",
                "popolazione_coda_Mario",
                "popolazione_sistema_Mario",
                "tempiDiservizio_Mario",
                "tempiDiRisposta_Mario",
                "tempi_in_coda_Mario",
                "utilizzazione_Mario",
                "popolazione_coda_HP",
                "popolazione_sistema_HP",
                "tempiDiservizio_HP",
                "tempiDiRisposta_HP",
                "tempi_in_coda_HP",
                "utilizzazione_HP");
        
        for (String filename : filenames) {
            estimate.createInterval("batch_reports", filename, 1);
        }     
    }
	
	//rimozione dei batch di warmup
	private void removeWarmUp(List<Double> list) {
		int warmUpBatches = 20;
		list.subList(0, warmUpBatches).clear();
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
	
	private void statsBatch(Node node, double nodeArea, double currentTime, double jobsServedPerBatch, int indexFirstServer, int indexLastServer, MsqSum[] sum, double eventTime) {
		double responseTime = nodeArea/jobsServedPerBatch;
		double interarrivals = (eventTime - node.getCurrentFirstArrivalTime())/jobsServedPerBatch;
		double abandons;
		
		double actualTime = currentTime - node.getCurrentStartTimeBatch();   
		double avgPopulations = nodeArea/actualTime;
		
		double queueArea = nodeArea;
		for (int i = indexFirstServer; i <= indexLastServer; i++) {
			queueArea -= sum[i].service;
		}
		//double delaysTime = Math.max(0, queueArea/jobsServedPerBatch);
		double avgQueuePopulations = queueArea/actualTime;
		
		double sumUtilizations = 0.0;
        double sumServices = 0.0;
        double sumServed = 0.0;
        for (int i = indexFirstServer; i <= indexLastServer; i++) {
			sumUtilizations += sum[i].service/actualTime;
			sumServices += sum[i].service;
			sumServed += sum[i].served;
		}
        
        int numServers = 0;
		for (int i = indexFirstServer; i <= indexLastServer; i++) {
			numServers++;
		}
        double utilization = sumUtilizations/numServers;
        double serviceTime = sumServices/sumServed;
        
        double delaysTime = avgQueuePopulations/(1.0/interarrivals);
        
		node.getTempiMediDiRispostaBatch().add(responseTime);
		node.getTempiMediInCodaBatch().add(delaysTime);
		node.getTempiDiServizioBatch().add(serviceTime);
		node.getPopolazioneDelSistemaBatch().add(avgPopulations);
		node.getPopolazioneDellaCodaBatch().add(avgQueuePopulations);
		node.getUtilizzazioneBatch().add(utilization);
		node.getInterarriviBatch().add(interarrivals);
	}   
	
	private boolean generateAbandon(Rngs rngs, int streamIndex, double percentage) {
        rngs.selectStream(2 + streamIndex);
        return rngs.random() <= percentage;
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
        int s;

        int i = INDEX_FIRST_SERVER_SICUREZZA;

        while (event[i].x == 1) 
            i++;                       
        s = i;
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
        while (i < INDEX_LAST_SERVER_HP_VERIFICA) {   
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
				
	double getService(Rngs r, int streamIndex, double meanServiceTime) {
        r.selectStream(streamIndex);
        return (exponential(meanServiceTime, r));
    }
	
	//funzione per generare tempi esponenziali
	double exponential(double mean, Rngs r) {
        return (-mean * Math.log(1.0 - r.random()));
    }
	
	//funzione per generare il prossimo arrivo
	double getArrival(Rngs r, int streamIndex, double currentTime) {
		r.selectStream(1 + streamIndex);
        sarrival += exponential(1.0/LAMBDA5, r);

        return (sarrival);
    }
	
	int nextEvent(MsqEvent[] event) {
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
