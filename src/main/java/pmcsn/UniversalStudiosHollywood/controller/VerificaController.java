package pmcsn.UniversalStudiosHollywood.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pmcsn.UniversalStudiosHollywood.libraries.Msq;
import pmcsn.UniversalStudiosHollywood.libraries.Rngs;

import pmcsn.UniversalStudiosHollywood.model.MarioKartNode;
import pmcsn.UniversalStudiosHollywood.model.BiglietteriaFisicaNode;
import pmcsn.UniversalStudiosHollywood.model.ControlloBigliettiNode;
import pmcsn.UniversalStudiosHollywood.model.ControlloSicurezzaNode;
import pmcsn.UniversalStudiosHollywood.model.HarryPotterNode;
import pmcsn.UniversalStudiosHollywood.utils.Estimate;
import pmcsn.UniversalStudiosHollywood.model.Node;

import static pmcsn.UniversalStudiosHollywood.model.Constants.*;

import static pmcsn.UniversalStudiosHollywood.model.Events.*;



public class VerificaController {
	
	static double START = 0.0; //tempo d'inizio della simulazione
    static double sarrival = START; //ultimo tempo in cui è stato generato un arrivo
	
	private BiglietteriaFisicaNode biglietteria;
	private ControlloSicurezzaNode sicurezza;
	private ControlloBigliettiNode controlli;
	private MarioKartNode mario_kart_standard;
	private MarioKartNode mario_kart_express;
	private HarryPotterNode harry_potter_standard;
	private HarryPotterNode harry_potter_express;
	
	public void startAnalysis() {
		
		int batchsize = 1088;
		int numBatches = 140;
		
		int intervalLength = 480;
		
		//primo istante del nuovo batch ed ogni primo arrivo
	    double currentBatchStartTime = 0;
	    double currentFirstArrivalTimeSicurezza = 0;
	    double currentFirstArrivalTimeBiglietteria = 0;
	    double currentFirstArrivalTimeControlli = 0;
	    double currentFirstArrivalTimeMarioStandard = 0;
	    double currentFirstArrivalTimeMarioExpress = 0;
	    double currentFirstArrivalTimeHPstandard = 0;
	    double currentFirstArrivalTimeHPexpress = 0;
	    
	    long totalJobsInSicurezza = 0;
	    long totalJobsInBiglietteria = 0;
	    long totalJobsInControlli = 0;
	    long totalJobsInMarioStandard = 0;
	    long totalJobsInMarioExpress = 0;
	    long totalJobsInHPstandard = 0;
	    long totalJobsInHPexpress = 0;
	    long totalJobsInMario = 0;
	    long totalJobsInHP = 0;
		
	    int totalSicurezzaCheck = 0;
	    int totalBiglietteriaCheck = 0;
	    int totalControlliCheck = 0;
	    int totalMarioStandardCheck = 0;
	    int totalMarioExpressCheck = 0;
	    int totalHPstandardCheck = 0;
	    int totalHPexpressCheck = 0;
	    int totalMarioCheck = 0;
	    int totalHPcheck = 0;
	    
		double nodeAreaSicurezza = 0.0; 
		double nodeAreaBiglietteria = 0.0; 
		double nodeAreaControlli = 0.0; 
		double nodeAreaMarioStandard = 0.0;
		double nodeAreaMarioExpress = 0.0;
		double nodeAreaHPstandard = 0.0;
		double nodeAreaHPexpress = 0.0;
		        
        //inizializzazione dei double per memorizzare il primo completamento delle varie code
		double firstCompletionSicurezza = 0;
	    double firstCompletionBiglietteria = 0;
	    double firstCompletionControlli = 0;
	    double firstCompletionMario = 0;
	    double firstCompletionHP = 0;
	    
	    //true = il prossimo cliente è standard, false = il prossimo cliente è express
	    boolean nextMarioStandard = true; 
	    boolean nextHPstandard = true;
	    
	    //chi sta terminando adesso
	    boolean marioServingStandard = true;
	    boolean hpServingStandard = true;
        
        
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
		mario_kart_standard = new MarioKartNode();
		mario_kart_express = new MarioKartNode();
		harry_potter_standard = new HarryPotterNode();
		harry_potter_express = new HarryPotterNode();
		
        //Setup generatore RNG
		Rngs rng = new Rngs();
		long seed = 123456789;
		rng.plantSeeds(seed);
        
        MsqEvent[] events = new MsqEvent[ALL_EVENTS_WITH_SAVE_STAT];
        MsqSum[] sum = new MsqSum[ALL_EVENTS];
        
        for (int i = 0; i < ALL_EVENTS_WITH_SAVE_STAT; i++) {
            events[i] = new MsqEvent();
        }
        for (int i = 0; i < ALL_EVENTS; i++) {
            sum[i] = new MsqSum();
        }
        
        //aggiunti per la gestione delle code Standard ed Express
        MsqSum[] sumMarioStandard = new MsqSum[ALL_EVENTS];
        MsqSum[] sumMarioExpress = new MsqSum[ALL_EVENTS];
        MsqSum[] sumHPStandard = new MsqSum[ALL_EVENTS];
        MsqSum[] sumHPExpress = new MsqSum[ALL_EVENTS];
        for (int i = 0; i < ALL_EVENTS; i++) {
            sumMarioStandard[i] = new MsqSum();
            sumMarioExpress[i] = new MsqSum();
            sumHPStandard[i] = new MsqSum();
            sumHPExpress[i] = new MsqSum();
        }
        
        
        //inizializzazione clock
        MsqT t = new MsqT();
        t.current = START;  
        
        events[0].t = getArrival(rng, sicurezza.getStreamIndex(), t.current);
        events[0].x = 1;
                
        events[ALL_EVENTS].t = intervalLength;
        events[ALL_EVENTS].x = 1;
        
        for (int i = 0; i < ALL_EVENTS; i++) {
        	if ((events[i].t != 0) && (events[i].x != 1)) {
        		events[i].t = START;
                events[i].x = 0;
                sum[i].service = 0.0;
                sum[i].served = 0;
                
                //per le code standard ed express
                sumMarioStandard[i].service = 0.0;
                sumMarioStandard[i].served = 0;
                sumMarioExpress[i].service = 0.0;
                sumMarioExpress[i].served = 0;
                sumHPStandard[i].service = 0.0;
                sumHPStandard[i].served = 0;
                sumHPExpress[i].service = 0.0;
                sumHPExpress[i].served = 0;
        	}
        } 
        
        /* === INIZIO ITERAZIONE === */
        System.out.println("\n\n\n----INIZIA LA SIMULAZIONE------");
        
        int iter = 0;
        
        int[] batchCounter = new int[NUMBER_OF_QUEUES];
        for (int i = 0; i < NUMBER_OF_QUEUES; i++) {
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
            if (totalMarioStandardCheck != 0 && totalMarioStandardCheck % batchsize == 0 && batchCounter[3] < numBatches) {
    			batchCounter[3]++;
    			statsBatch(mario_kart_standard, nodeAreaMarioStandard, t.current, totalMarioStandardCheck, INDEX_FIRST_SERVER_MARIO, INDEX_LAST_SERVER_MARIO, sumMarioStandard, events[INDEX_ARRIVAL_MARIO_STANDARD].t);
    			nodeAreaMarioStandard = 0.0;
    			for (int i = INDEX_FIRST_SERVER_MARIO; i <= INDEX_LAST_SERVER_MARIO; i++) {
    		        sumMarioStandard[i].service = 0;
    		        sumMarioStandard[i].served = 0;
    		    }
    			totalMarioStandardCheck = 0;
    			//dropoutsClub = 0;
    			
    			currentFirstArrivalTimeMarioStandard = events[INDEX_ARRIVAL_MARIO_STANDARD].t;
    			mario_kart_standard.setCurrentFirstArrivalTime(currentFirstArrivalTimeMarioStandard);
    			
    			currentBatchStartTime = t.current;
    			mario_kart_standard.setCurrentStartTimeBatch(currentBatchStartTime);
    		}
            
            if (totalMarioExpressCheck != 0 && totalMarioExpressCheck % batchsize == 0 && batchCounter[4] < numBatches) {
    			batchCounter[4]++;
    			statsBatch(mario_kart_express, nodeAreaMarioExpress, t.current, totalMarioExpressCheck, INDEX_FIRST_SERVER_MARIO, INDEX_LAST_SERVER_MARIO, sumMarioExpress, events[INDEX_ARRIVAL_MARIO_EXPRESS].t);
    			nodeAreaMarioExpress = 0.0;
    			for (int i = INDEX_FIRST_SERVER_MARIO; i <= INDEX_LAST_SERVER_MARIO; i++) {
    		        sumMarioExpress[i].service = 0;
    		        sumMarioExpress[i].served = 0;
    		    }
    			totalMarioExpressCheck = 0;
    			//dropoutsClub = 0;
    			
    			currentFirstArrivalTimeMarioExpress = events[INDEX_ARRIVAL_MARIO_EXPRESS].t;
    			mario_kart_express.setCurrentFirstArrivalTime(currentFirstArrivalTimeMarioExpress);
    			
    			currentBatchStartTime = t.current;
    			mario_kart_express.setCurrentStartTimeBatch(currentBatchStartTime);
    		}
            
            if (totalHPstandardCheck != 0 && totalHPstandardCheck % batchsize == 0 && batchCounter[5] < numBatches) {
    			batchCounter[5]++;
    			statsBatch(harry_potter_standard, nodeAreaHPstandard, t.current, totalHPstandardCheck, INDEX_FIRST_SERVER_HP, INDEX_LAST_SERVER_HP, sumHPStandard, events[INDEX_ARRIVAL_HP_STANDARD].t);
    			nodeAreaHPstandard = 0.0;
    			for (int i = INDEX_FIRST_SERVER_HP; i <= INDEX_LAST_SERVER_HP; i++) {
    		        sumHPStandard[i].service = 0;
    		        sumHPStandard[i].served = 0;
    		    }
    			totalHPstandardCheck = 0;
    			//dropoutsClub = 0;
    			
    			currentFirstArrivalTimeHPstandard = events[INDEX_ARRIVAL_HP_STANDARD].t;
    			harry_potter_standard.setCurrentFirstArrivalTime(currentFirstArrivalTimeHPstandard);
    			
    			currentBatchStartTime = t.current;
    			harry_potter_standard.setCurrentStartTimeBatch(currentBatchStartTime);
    		}
            
            if (totalHPexpressCheck != 0 && totalHPexpressCheck % batchsize == 0 && batchCounter[6] < numBatches) {
    			batchCounter[6]++;
    			statsBatch(harry_potter_express, nodeAreaHPexpress, t.current, totalHPexpressCheck, INDEX_FIRST_SERVER_HP, INDEX_LAST_SERVER_HP, sumHPExpress, events[INDEX_ARRIVAL_HP_EXPRESS].t);
    			nodeAreaHPexpress = 0.0;
    			for (int i = INDEX_FIRST_SERVER_HP; i <= INDEX_LAST_SERVER_HP; i++) {
    		        sumHPExpress[i].service = 0;
    		        sumHPExpress[i].served = 0;
    		    }
    			totalHPexpressCheck = 0;
    			//dropoutsClub = 0;
    			
    			currentFirstArrivalTimeHPexpress = events[INDEX_ARRIVAL_HP_EXPRESS].t;
    			harry_potter_express.setCurrentFirstArrivalTime(currentFirstArrivalTimeHPexpress);
    			
    			currentBatchStartTime = t.current;
    			harry_potter_express.setCurrentStartTimeBatch(currentBatchStartTime);
    		}
        	
        	//System.out.println("---Aggiornamento batch completati:");
        	int checkBatchCounter = 1;
        	for (int i = 0; i < NUMBER_OF_QUEUES; i++) {
        		System.out.println("Counter " + i + ": " + batchCounter[i]);
        		if (batchCounter[i] < numBatches) {
        			checkBatchCounter = 0;
        		}
        	}	
        
        	if (checkBatchCounter == 1) {
        		System.out.println("-----------SIMULAZIONE FINITA---------------");
        		break;
        	}
        	       	
            /*if(!dropoutsLoginQueue.isEmpty()) {
        		events[INDEX_DROPOUT_LOGIN].t = dropoutsLoginQueue.get(0);
        		events[INDEX_DROPOUT_LOGIN].x = 1; //attivo l'evento di abbandono
        		
        	}
        	else {
        		//System.out.println("La lista di abbandoni del Login è vuota, l'evento viene disattivato");
        		events[INDEX_DROPOUT_LOGIN].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	if(!dropoutsUltimateTeamQueue.isEmpty()) {
        		events[INDEX_DROPOUT_ULTIMATE_TEAM].t = dropoutsUltimateTeamQueue.get(0);
        		events[INDEX_DROPOUT_ULTIMATE_TEAM].x = 1; //attivo l'evento di abbandono
        	}
        	else {
        		//System.out.println("La lista di abbandoni della coda di Ultimate Team è vuota, l'evento viene disattivato");
        		events[INDEX_DROPOUT_ULTIMATE_TEAM].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	if(!dropoutsStagioniQueue.isEmpty()) {
        		//System.out.println("La lista di abbandoni della coda delle Stagioni non è vuota");
        		events[INDEX_DROPOUT_STAGIONI].t = dropoutsStagioniQueue.get(0);
        		//System.out.println("L'evento di abbandono avverrà della coda delle Stagioni all'istante " + events[32].t);
        		events[INDEX_DROPOUT_STAGIONI].x = 1; //attivo l'evento di abbandono
        		
        	}
        	else {
        		//System.out.println("La lista di abbandoni della coda delle Stagioni è vuota, l'evento viene disattivato");
        		events[INDEX_DROPOUT_STAGIONI].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	if(!dropoutsClubQueue.isEmpty()) {
        		//System.out.println("La lista di abbandoni della coda di Club non è vuota");
        		events[INDEX_DROPOUT_CLUB].t = dropoutsClubQueue.get(0);
        		events[INDEX_DROPOUT_CLUB].x = 1; //attivo l'evento di abbandono
        		
        	}
        	else {
        		//System.out.println("La lista di abbandoni della coda di Club è vuota, l'evento viene disattivato");
        		events[INDEX_DROPOUT_CLUB].x = 0; //disattivo l'evento di abbandono
        	}*/
        	
        	
        	
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
    	    nodeAreaMarioStandard += (t.next - t.current)*totalJobsInMarioStandard;
    	    nodeAreaMarioExpress += (t.next - t.current)*totalJobsInMarioExpress;
    	    nodeAreaHPstandard += (t.next - t.current)*totalJobsInHPstandard;
    	    nodeAreaHPexpress += (t.next - t.current)*totalJobsInHPexpress;
    		
    		//System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.current = t.next;
            /*System.out.println("Siamo all'istante: " + t.current);
            System.out.println("L'evento successivo doveva avvenire all'istante: " + t.next);
            System.out.println("I due tempi coincidono, quindi andiamo a processare l'evento " + e);*/

            if (e == ALL_EVENTS) {
            	events[ALL_EVENTS].t += intervalLength;
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
            	
            } else if (e == INDEX_ARRIVAL_MARIO_STANDARD) { 
            	
            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA STANDARD DI MARIO KART----------");
            	totalJobsInMario++;
            	totalJobsInMarioStandard++;
            	/*System.out.println("Elementi nel centro Pro Club: " + totalJobsInClub);
            	System.out.println("Numero di server della coda Pro Club: " + SERVERS_CLUB);*/
            	
            	if (totalJobsInMario <= SERVERS_MARIO) { //verifico se posso essere servito subito
            		
            		marioServingStandard = true;
            		nextMarioStandard = false;
            		
            		service = getService(rng, mario_kart_standard.getStreamIndex(), mario_kart_standard.getServiceTime());
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_CLUB);
            		s = findMarioKartServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    
                    //per la coda Standard
                    sumMarioStandard[s].service += service;
                    sumMarioStandard[s].served++;
                    
                    //System.out.println("AUMENTO DI 1 SERVED");
                    events[s].t = t.current + service;
                    
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if (e == INDEX_ARRIVAL_MARIO_EXPRESS) {
                events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA EXPRESS DI MARIO KART----------");
            	totalJobsInMario++;
            	totalJobsInMarioExpress++;
            	/*System.out.println("Elementi nel centro Pro Club: " + totalJobsInClub);
            	System.out.println("Numero di server della coda Pro Club: " + SERVERS_CLUB);*/
            	
            	if (totalJobsInMario <= SERVERS_MARIO) { //verifico se posso essere servito subito
            		
            		marioServingStandard = false;
            		nextMarioStandard = true;
            		
            		service = getService(rng, mario_kart_express.getStreamIndex(), mario_kart_express.getServiceTime());
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_CLUB);
            		s = findMarioKartServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    
                    //aggiunta per la coda Express
                    sumMarioExpress[s].service += service;
                    sumMarioExpress[s].served++;
                    
                    //System.out.println("AUMENTO DI 1 SERVED");
                    events[s].t = t.current + service;
                    
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if (e == INDEX_ARRIVAL_HP_STANDARD) {
                events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA STANDARD DI HARRY POTTER----------");
            	totalJobsInHP++;
            	totalJobsInHPstandard++;
            	/*System.out.println("Elementi nel centro Pro Club: " + totalJobsInClub);
            	System.out.println("Numero di server della coda Pro Club: " + SERVERS_CLUB);*/
            	
            	if (totalJobsInHP <= SERVERS_HP) { //verifico se posso essere servito subito
            		
            		hpServingStandard = true;
            		nextHPstandard = false;
            		
            		service = getService(rng, harry_potter_standard.getStreamIndex(), harry_potter_standard.getServiceTime());
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_CLUB);
            		s = findHarryPotterServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    
                    //aggiunta per la coda standard
                    sumHPStandard[s].service += service;
                    sumHPStandard[s].served++;
                    
                    //System.out.println("AUMENTO DI 1 SERVED");
                    events[s].t = t.current + service;
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if (e == INDEX_ARRIVAL_HP_EXPRESS) {
                events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA EXPRESS DI HARRY POTTER----------");
            	totalJobsInHP++;
            	totalJobsInHPexpress++;
            	/*System.out.println("Elementi nel centro Pro Club: " + totalJobsInClub);
            	System.out.println("Numero di server della coda Pro Club: " + SERVERS_CLUB);*/
            	
            	if (totalJobsInHP <= SERVERS_HP) { //verifico se posso essere servito subito
            		
            		hpServingStandard = false;
            		nextHPstandard = true;
            		
            		service = getService(rng, harry_potter_express.getStreamIndex(), harry_potter_express.getServiceTime());
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_CLUB);
            		s = findHarryPotterServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    //System.out.println("AUMENTO DI 1 SERVED");
                    
                    //aggiunta per la coda standard
                    sumHPExpress[s].service += service;
                    sumHPExpress[s].served++;
                    
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
            		
            		int primi_percorsi = generateAttractionsDestination(rng, controlli.getStreamIndex());
	               	int secondi_percorsi;
                	if (primi_percorsi == 0) {
                		//System.out.println("L'utente andrà Mario Kart");
                		secondi_percorsi = generateQueueDestination(rng, controlli.getStreamIndex());
                		if (secondi_percorsi == 0) {
                			events[INDEX_ARRIVAL_MARIO_STANDARD].t = t.current; //aggiunto un evento alla coda Ultimate Team
                    		events[INDEX_ARRIVAL_MARIO_STANDARD].x = 1; //attivazione dell'evento
                		} else {
                			events[INDEX_ARRIVAL_MARIO_EXPRESS].t = t.current; //aggiunto un evento alla coda Ultimate Team
                    		events[INDEX_ARRIVAL_MARIO_EXPRESS].x = 1; //attivazione dell'evento
                		}
                		
                	} else if (primi_percorsi == 1) {
                		//System.out.prinln("L'utente andrà in Harry Potter");
                		secondi_percorsi = generateQueueDestination(rng, controlli.getStreamIndex());
                		if (secondi_percorsi == 0) {
                			events[INDEX_ARRIVAL_HP_STANDARD].t = t.current; //aggiunto un evento alla coda Ultimate Team
                    		events[INDEX_ARRIVAL_HP_STANDARD].x = 1; //attivazione dell'evento
                		} else {
                			events[INDEX_ARRIVAL_HP_EXPRESS].t = t.current; //aggiunto un evento alla coda Ultimate Team
                    		events[INDEX_ARRIVAL_HP_EXPRESS].x = 1; //attivazione dell'evento
                		}	
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
            	
            } else if ((e >= INDEX_FIRST_SERVER_MARIO) && (e <= INDEX_LAST_SERVER_MARIO)) { //eventi dei server di Club, 36 e 40
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
            		
            		//il cliente ha terminato il servizio ed appartiene alla coda che era in servizio
            		if (marioServingStandard) {
            			totalJobsInMarioStandard--;
            			totalMarioStandardCheck++;
            		}
            		else {
            			totalJobsInMarioExpress--;
            			totalMarioExpressCheck++;
            		}
            		
            		
            		totalJobsInMario--;//diminuisco di 1 il numero di utenti in questo centro
            		totalMarioCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nel Club: " + totalClubCheck);
                	System.out.println("Utenti ancora nel Club: " + totalJobsInClub);*/
                	
                	s = e;
                	
                	int choiceMario = 0;
                	
                	if (totalJobsInMario >= SERVERS_MARIO) {//ci sono ancora elementi in una delle due code di Mario
                		
                		//CI SONO ANCORA ELEMENTI, SCEGLIAMO DA QUALE CODA PRENDERE
                		if (nextMarioStandard && totalJobsInMarioStandard > 0) {
                			//System.out.println("ENTRA MARIO STANDARD");
                			//si serve la coda standard
                			marioServingStandard = true;
                			nextMarioStandard = false;
                			choiceMario = 0;
                		} else if (!nextMarioStandard && totalJobsInMarioExpress > 0) {
                            // SERVE EXPRESS
                			//System.out.println("ENTRA MARIO EXPRESS");
                            marioServingStandard = false;
                            nextMarioStandard = true;
                            choiceMario = 1;
                        } else if (totalJobsInMarioStandard > 0) {
                            // È VUOTA LA EXPRESS,
                            // QUINDI SERVIAMO STANDARD
                        	//System.out.println("EXPRESS VUOTA QUINDI ENTRA MARIO STANDARD");
                            marioServingStandard = true;
                            nextMarioStandard = false;
                            choiceMario = 0;
                        } else if (totalJobsInMarioExpress > 0) {
                        	//System.out.println("STANDARD VUOTA QUINDI ENTRA MARIO EXPRESS");
                            // STANDARD È VUOTA,
                            // QUINDI SERVIAMO EXPRESS
                            marioServingStandard = false;
                            nextMarioStandard = true;
                            choiceMario = 1;
                        }

                		//System.out.println("Ci sono degli elementi Club da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, mario_kart_standard.getStreamIndex(), mario_kart_standard.getServiceTime());
                		sum[s].service += service;
                        sum[s].served++;
                        
                        if (choiceMario == 0) {
                        	sumMarioStandard[s].service += service;
                            sumMarioStandard[s].served++;
                        }
                        else {
                        	sumMarioExpress[s].service += service;
                            sumMarioExpress[s].served++;
                        }
                        
                        events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda ProClub, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= INDEX_FIRST_SERVER_HP) && (e <= INDEX_LAST_SERVER_HP)) {
            	
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
            		
            		//il cliente ha terminato il servizio ed appartiene alla coda che era in servizio
            		if (hpServingStandard) {
            			totalJobsInHPstandard--;
            			totalHPstandardCheck++;
            		}
            		else {
            			totalJobsInHPexpress--;
            			totalHPexpressCheck++;
            		}
            		
            		totalJobsInHP--;//diminuisco di 1 il numero di utenti in questo centro
            		totalHPcheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nel Club: " + totalClubCheck);
                	System.out.println("Utenti ancora nel Club: " + totalJobsInClub);*/
                	
                	s = e;
                	
                	int choiceHP = 0;
                	
                	if (totalJobsInHP >= SERVERS_HP) {//ci sono ancora elementi in coda
                		
                		//CI SONO ANCORA ELEMENTI, SCEGLIAMO DA QUALE CODA PRENDERE
                		if (nextHPstandard && totalJobsInHPstandard > 0) {
                			//si serve la coda standard
                			hpServingStandard = true;
                			nextHPstandard = false;
                			choiceHP = 0;
                		} else if (!nextHPstandard && totalJobsInHPexpress > 0) {
                            // SERVE EXPRESS
                            hpServingStandard = false;
                            nextHPstandard = true;
                            choiceHP = 1;
                        } else if (totalJobsInHPstandard > 0) {
                            // È VUOTA LA EXPRESS,
                            // QUINDI SERVIAMO STANDARD
                            hpServingStandard = true;
                            nextHPstandard = false;
                            choiceHP = 0;
                        } else if (totalJobsInHPexpress > 0) {
                            // STANDARD È VUOTA,
                            // QUINDI SERVIAMO EXPRESS
                            hpServingStandard = false;
                            nextHPstandard = true;
                            choiceHP = 1;
                        }
                		
                		service = getService(rng, harry_potter_standard.getStreamIndex(), harry_potter_standard.getServiceTime());
                		sum[s].service += service;
                        sum[s].served++;
                        
                        if (choiceHP == 0) {
                        	sumHPStandard[s].service += service;
                            sumHPStandard[s].served++;
                        }
                        else {
                        	sumHPExpress[s].service += service;
                            sumHPExpress[s].served++;
                        }
                        
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
        
        removeWarmUp(mario_kart_standard.getPopolazioneDellaCodaBatch());
        removeWarmUp(mario_kart_standard.getPopolazioneDelSistemaBatch());
        removeWarmUp(mario_kart_standard.getTempiDiServizioBatch());
        removeWarmUp(mario_kart_standard.getTempiMediDiRispostaBatch());
        removeWarmUp(mario_kart_standard.getTempiMediInCodaBatch());
        removeWarmUp(mario_kart_standard.getInterarriviBatch());
        removeWarmUp(mario_kart_standard.getUtilizzazioneBatch());
        
        removeWarmUp(mario_kart_express.getPopolazioneDellaCodaBatch());
        removeWarmUp(mario_kart_express.getPopolazioneDelSistemaBatch());
        removeWarmUp(mario_kart_express.getTempiDiServizioBatch());
        removeWarmUp(mario_kart_express.getTempiMediDiRispostaBatch());
        removeWarmUp(mario_kart_express.getTempiMediInCodaBatch());
        removeWarmUp(mario_kart_express.getInterarriviBatch());
        removeWarmUp(mario_kart_express.getUtilizzazioneBatch());
        
        removeWarmUp(harry_potter_standard.getPopolazioneDellaCodaBatch());
        removeWarmUp(harry_potter_standard.getPopolazioneDelSistemaBatch());
        removeWarmUp(harry_potter_standard.getTempiDiServizioBatch());
        removeWarmUp(harry_potter_standard.getTempiMediDiRispostaBatch());
        removeWarmUp(harry_potter_standard.getTempiMediInCodaBatch());
        removeWarmUp(harry_potter_standard.getInterarriviBatch());
        removeWarmUp(harry_potter_standard.getUtilizzazioneBatch());
        
        removeWarmUp(harry_potter_express.getPopolazioneDellaCodaBatch());
        removeWarmUp(harry_potter_express.getPopolazioneDelSistemaBatch());
        removeWarmUp(harry_potter_express.getTempiDiServizioBatch());
        removeWarmUp(harry_potter_express.getTempiMediDiRispostaBatch());
        removeWarmUp(harry_potter_express.getTempiMediInCodaBatch());
        removeWarmUp(harry_potter_express.getInterarriviBatch());
        removeWarmUp(harry_potter_express.getUtilizzazioneBatch());
        
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
        writeFile(mario_kart_standard.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_MarioStandard");
        writeFile(mario_kart_standard.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_MarioStandard");
        writeFile(mario_kart_standard.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_MarioStandard");
        writeFile(mario_kart_standard.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_MarioStandard");
        writeFile(mario_kart_standard.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_MarioStandard");
        //writeFile(clubNode.getInterarriviBatch(),"batch_reports", "interarrivi_MarioStandard");
        writeFile(mario_kart_standard.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_MarioStandard");
        
        //CODA EXPRESS MARIO KART
        writeFile(mario_kart_express.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_MarioExpress");
        writeFile(mario_kart_express.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_MarioExpress");
        writeFile(mario_kart_express.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_MarioExpress");
        writeFile(mario_kart_express.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_MarioExpress");
        writeFile(mario_kart_express.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_MarioExpress");
        //writeFile(mario_kart_express.getInterarriviBatch(),"batch_reports", "interarrivi_MarioExpress");
        writeFile(mario_kart_express.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_MarioExpress");
        
        //CODA STANDARD HARRY POTTER 
        writeFile(harry_potter_standard.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_HPStandard");
        writeFile(harry_potter_standard.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_HPStandard");
        writeFile(harry_potter_standard.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_HPStandard");
        writeFile(harry_potter_standard.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_HPStandard");
        writeFile(harry_potter_standard.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_HPStandard");
        //writeFile(harry_potter_standard.getInterarriviBatch(),"batch_reports", "interarrivi_HPStandard");
        writeFile(harry_potter_standard.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_HPStandard");
        
        //CODA EXPRESS HARRY POTTER
        writeFile(harry_potter_express.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_HPExpress");
        writeFile(harry_potter_express.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_HPExpress");
        writeFile(harry_potter_express.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_HPExpress");
        writeFile(harry_potter_express.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_HPExpress");
        writeFile(harry_potter_express.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_HPExpress");
        //writeFile(harry_potter_express.getInterarriviBatch(),"batch_reports", "interarrivi_HPExpress");
        writeFile(harry_potter_express.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_HPExpress");
        
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
                "popolazione_coda_MarioStandard",
                "popolazione_sistema_MarioStandard",
                "tempiDiservizio_MarioStandard",
                "tempiDiRisposta_MarioStandard",
                "tempi_in_coda_MarioStandard",
                "utilizzazione_MarioStandard",
                "popolazione_coda_MarioExpress",
                "popolazione_sistema_MarioExpress",
                "tempiDiservizio_MarioExpress",
                "tempiDiRisposta_MarioExpress",
                "tempi_in_coda_MarioExpress",
                "utilizzazione_MarioExpress",
                "popolazione_coda_HPStandard",
                "popolazione_sistema_HPStandard",
                "tempiDiservizio_HPStandard",
                "tempiDiRisposta_HPStandard",
                "tempi_in_coda_HPStandard",
                "utilizzazione_HPStandard",
                "popolazione_coda_HPExpress",
                "popolazione_sistema_HPExpress",
                "tempiDiservizio_HPExpress",
                "tempiDiRisposta_HPExpress",
                "tempi_in_coda_HPExpress",
                "utilizzazione_HPExpress"
                	);
        
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
		double delaysTime = Math.max(0, queueArea/jobsServedPerBatch);
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

	    if (r < 0.10184) { //Mario Kart 
	    	return 0; 
	    } else if (r < 0.10184+0.3) { // Harry Potter 
	    	return 1;
	    } else { 
	    	return 2; //altre attrazioni 
	    }
	}
	
	private int generateQueueDestination(Rngs rngs, int streamIndex) {
		rngs.selectStream(5 + streamIndex);
	    double r = rngs.random();

	    if (r < 0.8) {
	        return 0; // Coda Standard
	    } else {
	        return 1; // Coda Express
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

        int i = INDEX_FIRST_SERVER_MARIO;

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < INDEX_LAST_SERVER_MARIO) { //i < 38, perché i server di Club sono da 34 a 38 ma si entra già facendo i++ quindi deve essere minore stretto di 14  
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

        int i = INDEX_FIRST_SERVER_HP;

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < INDEX_LAST_SERVER_HP) {   
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
		//System.out.println("Ricerca in corso del prossimo evento da elaborare...");
	    int e;
	    int i = 0;
	    while (event[i].x == 0) 
	    	i++;
	    e = i;
	    while (i < ALL_EVENTS_WITH_SAVE_STAT -1) {
	    	i++;
	    	if ((event[i].x == 1) && (event[i].t < event[e].t)) {
	    		e = i;
	    	}
	    }
	    //System.out.println("Evento trovato con indice " + (e));
	    return (e);   
	}
}
