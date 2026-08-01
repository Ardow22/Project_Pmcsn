package logic.PMCSN.controller;

import static logic.PMCSN.model.Constants.DEV_ST_CLUB;
import static logic.PMCSN.model.Constants.DEV_ST_LOGIN;
import static logic.PMCSN.model.Constants.DEV_ST_STA;
import static logic.PMCSN.model.Constants.DEV_ST_UT;
import static logic.PMCSN.model.Constants.LAMBDA;
import static logic.PMCSN.model.Constants.LOWER_B_CLUB;
import static logic.PMCSN.model.Constants.LOWER_B_LOGIN;
import static logic.PMCSN.model.Constants.LOWER_B_STA;
import static logic.PMCSN.model.Constants.LOWER_B_UT;
import static logic.PMCSN.model.Constants.NUMBER_OF_CENTERS;
import static logic.PMCSN.model.Constants.UPPER_B_CLUB;
import static logic.PMCSN.model.Constants.UPPER_B_LOGIN;
import static logic.PMCSN.model.Constants.UPPER_B_STA;
import static logic.PMCSN.model.Constants.UPPER_B_UT;
import static logic.PMCSN.model.Constants.not_P5;
import static logic.PMCSN.model.Constants.not_P6;
import static logic.PMCSN.model.Constants.not_P7;
import static logic.PMCSN.model.Events.ALL_EVENTS;
import static logic.PMCSN.model.Events.ALL_EVENTS_WITH_SAVE_STAT;
import static logic.PMCSN.model.Events.INDEX_ARRIVAL_CLUB;
import static logic.PMCSN.model.Events.INDEX_ARRIVAL_LOGIN;
import static logic.PMCSN.model.Events.INDEX_ARRIVAL_STAGIONI;
import static logic.PMCSN.model.Events.INDEX_ARRIVAL_ULTIMATE_TEAM;
import static logic.PMCSN.model.Events.INDEX_DROPOUT_CLUB;
import static logic.PMCSN.model.Events.INDEX_DROPOUT_LOGIN;
import static logic.PMCSN.model.Events.INDEX_DROPOUT_STAGIONI;
import static logic.PMCSN.model.Events.INDEX_DROPOUT_ULTIMATE_TEAM;
import static logic.PMCSN.model.Events.INDEX_FIRST_SERVER_CLUB;
import static logic.PMCSN.model.Events.INDEX_FIRST_SERVER_LOGIN;
import static logic.PMCSN.model.Events.INDEX_FIRST_SERVER_STAGIONI;
import static logic.PMCSN.model.Events.INDEX_FIRST_SERVER_ULTIMATE_TEAM;
import static logic.PMCSN.model.Events.INDEX_LAST_SERVER_CLUB;
import static logic.PMCSN.model.Events.INDEX_LAST_SERVER_LOGIN;
import static logic.PMCSN.model.Events.INDEX_LAST_SERVER_STAGIONI;
import static logic.PMCSN.model.Events.INDEX_LAST_SERVER_ULTIMATE_TEAM;
import static logic.PMCSN.model.Events.SERVERS_CLUB;
import static logic.PMCSN.model.Events.SERVERS_LOGIN;
import static logic.PMCSN.model.Events.SERVERS_STAGIONI;
import static logic.PMCSN.model.Events.SERVERS_ULTIMATE_TEAM;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import logic.PMCSN.libraries.Rngs;
import logic.PMCSN.model.ClubNode;
import logic.PMCSN.model.LoginNode;
import logic.PMCSN.model.Node;
import logic.PMCSN.model.StagioniNode;
import logic.PMCSN.model.UltimateTeamNode;
import logic.PMCSN.utils.Estimate;
import logic.PMCSN.utils.Rvms;

public class ExperimentsController {
	
	static double START = 0.0; //tempo d'inizio della simulazione
    static double sarrival = START; //ultimo tempo in cui è stato generato un arrivo

    private LoginNode loginNode;
	private StagioniNode StagioniNode;
	private ClubNode clubNode;
	private UltimateTeamNode UTnode;
	
	static int BATCHSIZE = 1088;
	static int NUMBATCHES = 140;
	
	public void startAnalysis() {
		
		String filenameLogin = "experimentsLogin.csv";
		String filenameUT = "experimentsUT.csv";
		String filenameStagioni = "experimentsStagioni.csv";
		String filenameClub = "experimentsClub.csv";
		
		int intervalLength = 480;
		
		//primo istante del nuovo batch ed ogni primo arrivo
	    double currentBatchStartTime = 0;
	    double currentFirstArrivalTimeLogin = 0;
	    double currentFirstArrivalTimeUT = 0;
	    double currentFirstArrivalTimeStagioni = 0;
	    double currentFirstArrivalTimeClub = 0;
	    
	    long totalJobsInLogin = 0;
		long totalJobsInUltimateTeam = 0;
	    long totalJobsInStagioni = 0;
	    long totalJobsInClub = 0;
		
	    int totalLoginCheck = 0;
	    int totalUltimateTeamCheck = 0;
	    int totalStagioniCheck = 0;
	    int totalClubCheck = 0;
	    
		double nodeAreaLogin = 0.0; 
		double nodeAreaUltimateTeam = 0.0; 
		double nodeAreaStagioni = 0.0; 
		double nodeAreaClub = 0.0;  
		        
        //inizializzazione dei double per memorizzare il primo completamento delle varie code
        double firstCompletionLogin = 0;
        double firstCompletionUltimateTeam = 0;
        double firstCompletionStagioni = 0;
        double firstCompletionClub = 0;
        
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
		
		loginNode = new LoginNode();
		StagioniNode = new StagioniNode();
		clubNode = new ClubNode();
		UTnode = new UltimateTeamNode();
		
        //Setup generatore RNG
		Rngs rng = new Rngs();
		long seed = 123456789;
		rng.plantSeeds(seed);
        
        //System.out.println("\n-----------INIZIALIZZAZIONE EVENTI NELLA SIMULAZIONE-------------");
        int sumDebug = ALL_EVENTS_WITH_SAVE_STAT;
        //System.out.println("Eventi totali previsti nella simulazione (INCLUSO SAVE_STAT): " + sumDebug);
        
        MsqEvent[] events = new MsqEvent[ALL_EVENTS_WITH_SAVE_STAT];
        MsqSum[] sum = new MsqSum[ALL_EVENTS];
        Rvms rvms = new Rvms();
        
        //System.out.println("Lista eventi (incluso SAVE_STAT): ");
        for (int i = 0; i < ALL_EVENTS_WITH_SAVE_STAT; i++) {
            events[i] = new MsqEvent();
        }
        for (int i = 0; i < ALL_EVENTS; i++) {
            sum[i] = new MsqSum();
        }
        
        MsqT t = new MsqT();
        t.current = START;  
        /*System.out.println("\n-----INIZIALIZZAZIONE DEL CLOCK------------");
        System.out.println("Siamo nell'istante: " + t.current); // DEBUG PRINT
        System.out.println("Il prossimo evento avverrà all'istante: " + t.next);*/
        
        //PRIMO ARRIVO NEL SISTEMA GENERICO
        ///System.out.println("\n-----------PRIMO ARRIVO AL SISTEMA--------");
        
        events[0].t = getArrival(rng, loginNode.getStreamIndex(), t.current);
        events[0].x = 1;
        
        //EVENTO SAVE_STAT
        //System.out.println("\n------------GENERAZIONE SAVE_STAT----------");        
        events[ALL_EVENTS].t = intervalLength;
        events[ALL_EVENTS].x = 1;
        
        
        /*System.out.println("Nuova lista eventi: ");
        for (int i = 0; i < events.length; i++) {
        	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
        	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
        	System.out.println(" ");
        }*/
        
        for (int i = 0; i < ALL_EVENTS; i++) {
        	if ((events[i].t != 0) && (events[i].x != 1)) {
        		events[i].t = START;
                events[i].x = 0;
                sum[i].service = 0.0;
                sum[i].served = 0;
        	}
        } 
                
        /*System.out.println("Inizializzazione completata. Pronto per l’iterazione.");
        System.out.printf("\nSIMULAZIONE DURERà ALL'INFINITO\n");*/
        
        /* === INIZIO ITERAZIONE === */
        System.out.println("\n\n\n----INIZIA LA SIMULAZIONE------");
        //System.out.println("La simulazione andrà avanti fino al numero di job prefissati");
        
        int iter = 0;
        
        int[] batchCounter = new int[NUMBER_OF_CENTERS];
        for (int i = 0; i < NUMBER_OF_CENTERS; i++) {
        	batchCounter[i] = 0;
        }
        
        //while (iter != 15) {
        while(events[0].x != 0) {
        	
        	iter++;
        	System.out.println("\n\n-------LA SIMULAZIONE VA AVANTI, QUINDI NUOVA ITERAZIONE, è LA NUMERO: " + iter);
        	
        	if (totalLoginCheck != 0 && totalLoginCheck % BATCHSIZE == 0 && batchCounter[0] < NUMBATCHES) {
    			batchCounter[0]++;
    			statsBatch(loginNode, nodeAreaLogin, t.current, totalLoginCheck, INDEX_FIRST_SERVER_LOGIN, INDEX_LAST_SERVER_LOGIN, sum, events[INDEX_ARRIVAL_LOGIN].t);
    			nodeAreaLogin = 0.0;
    			for (int i = INDEX_FIRST_SERVER_LOGIN; i <= INDEX_LAST_SERVER_LOGIN; i++) {
    		        sum[i].service = 0;
    		        sum[i].served = 0;
    		    }
    			totalLoginCheck = 0;
    			dropoutsLogin = 0;
    			
    			currentFirstArrivalTimeLogin = events[INDEX_ARRIVAL_LOGIN].t;
    			loginNode.setCurrentFirstArrivalTime(currentFirstArrivalTimeLogin);
    			
    			currentBatchStartTime = t.current;
    			loginNode.setCurrentStartTimeBatch(currentBatchStartTime);
    		}
    		//System.out.println("Job serviti nel batch UltimateTeam: " + totalUltimateTeamCheck);
            if (totalUltimateTeamCheck != 0 && totalUltimateTeamCheck % BATCHSIZE == 0 && batchCounter[1] < NUMBATCHES) {
    			
    			batchCounter[1]++;
    			statsBatch(UTnode, nodeAreaUltimateTeam, t.current, totalUltimateTeamCheck, INDEX_FIRST_SERVER_ULTIMATE_TEAM, INDEX_LAST_SERVER_ULTIMATE_TEAM, sum, events[INDEX_ARRIVAL_ULTIMATE_TEAM].t);
    			nodeAreaUltimateTeam = 0.0;
    			for (int i = INDEX_FIRST_SERVER_ULTIMATE_TEAM; i <= INDEX_LAST_SERVER_ULTIMATE_TEAM; i++) {
    		        sum[i].service = 0;
    		        sum[i].served = 0;
    		    }
    			totalUltimateTeamCheck = 0;
    			dropoutsUltimateTeam = 0;
    			
    			currentFirstArrivalTimeUT = events[INDEX_ARRIVAL_ULTIMATE_TEAM].t;
    			UTnode.setCurrentFirstArrivalTime(currentFirstArrivalTimeUT);
    			
    			currentBatchStartTime = t.current;
    			UTnode.setCurrentStartTimeBatch(currentBatchStartTime);
    		}
            //System.out.println("Job serviti nel batch Stagioni: " + totalStagioniCheck);
            if (totalStagioniCheck != 0 && totalStagioniCheck % BATCHSIZE == 0 && batchCounter[2] < NUMBATCHES) {
    			batchCounter[2]++;
    			statsBatch(StagioniNode, nodeAreaStagioni, t.current, totalStagioniCheck, INDEX_FIRST_SERVER_STAGIONI, INDEX_LAST_SERVER_STAGIONI, sum, events[INDEX_ARRIVAL_STAGIONI].t);
    			nodeAreaStagioni = 0.0;
    			for (int i = INDEX_FIRST_SERVER_STAGIONI; i <= INDEX_LAST_SERVER_STAGIONI; i++) {
    		        sum[i].service = 0;
    		        sum[i].served = 0;
    		    }
    			totalStagioniCheck = 0;
    			dropoutsStagioni = 0;
    			
    			currentFirstArrivalTimeStagioni = events[INDEX_ARRIVAL_STAGIONI].t;
    			StagioniNode.setCurrentFirstArrivalTime(currentFirstArrivalTimeStagioni);
    			
    			currentBatchStartTime = t.current;
    			StagioniNode.setCurrentStartTimeBatch(currentBatchStartTime);
    		}
            //System.out.println("Job serviti nel batch Club: " + totalClubCheck);
            if (totalClubCheck != 0 && totalClubCheck % BATCHSIZE == 0 && batchCounter[3] < NUMBATCHES) {
    			batchCounter[3]++;
    			statsBatch(clubNode, nodeAreaClub, t.current, totalClubCheck, INDEX_FIRST_SERVER_CLUB, INDEX_LAST_SERVER_CLUB, sum, events[INDEX_ARRIVAL_CLUB].t);
    			nodeAreaClub = 0.0;
    			for (int i = INDEX_FIRST_SERVER_CLUB; i <= INDEX_LAST_SERVER_CLUB; i++) {
    		        sum[i].service = 0;
    		        sum[i].served = 0;
    		    }
    			totalClubCheck = 0;
    			dropoutsClub = 0;
    			
    			currentFirstArrivalTimeClub = events[INDEX_ARRIVAL_CLUB].t;
    			clubNode.setCurrentFirstArrivalTime(currentFirstArrivalTimeClub);
    			
    			currentBatchStartTime = t.current;
    			clubNode.setCurrentStartTimeBatch(currentBatchStartTime);
    		}
        	
        	//System.out.println("---Aggiornamento batch completati:");
        	int checkBatchCounter = 1;
        	for (int i = 0; i < NUMBER_OF_CENTERS; i++) {
        		System.out.println("Counter " + i + ": " + batchCounter[i]);
        		if (batchCounter[i] < NUMBATCHES) {
        			checkBatchCounter = 0;
        		}
        	}	
        
        	if (checkBatchCounter == 1) {
        		System.out.println("-----------SIMULAZIONE FINITA---------------");
        		break;
        	}
        	
        	
        	       	
            if(!dropoutsLoginQueue.isEmpty()) {
        		//System.out.println("La lista di abbandoni del Login non è vuota");
        		events[INDEX_DROPOUT_LOGIN].t = dropoutsLoginQueue.get(0);
        		//System.out.println("L'evento di abbandono del Login avverrà all'istante " + events[13].t);
        		events[INDEX_DROPOUT_LOGIN].x = 1; //attivo l'evento di abbandono
        	}
        	else {
        		//System.out.println("La lista di abbandoni del Login è vuota, l'evento viene disattivato");
        		events[INDEX_DROPOUT_LOGIN].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	if(!dropoutsUltimateTeamQueue.isEmpty()) {
        		//System.out.println("La lista di abbandoni della coda di Ultimate Team non è vuota");
        		events[INDEX_DROPOUT_ULTIMATE_TEAM].t = dropoutsUltimateTeamQueue.get(0);
        		//System.out.println("L'evento di abbandono della coda di Ultimate Team avverrà all'istante " + events[28].t);
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
        		//System.out.println("L'evento di abbandono avverrà all'istante " + events[39].t);
        		events[INDEX_DROPOUT_CLUB].x = 1; //attivo l'evento di abbandono
        		
        	}
        	else {
        		//System.out.println("La lista di abbandoni della coda di Club è vuota, l'evento viene disattivato");
        		events[INDEX_DROPOUT_CLUB].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	
        	
            // Trova evento più imminente
            e = nextEvent(events);
            //System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.next = events[e].t;
            /*System.out.println("Siamo all'istante: " + t.current);
            System.out.println("Il prossimo evento (che è quello appena trovato) avverrà all'istante: " + t.next);*/

    		//Node area
    		nodeAreaLogin += (t.next - t.current)*totalJobsInLogin;
    		nodeAreaUltimateTeam += (t.next - t.current)*totalJobsInUltimateTeam;
    		nodeAreaStagioni += (t.next - t.current)*totalJobsInStagioni;
    		nodeAreaClub += (t.next - t.current)*totalJobsInClub;
    		
    		//System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.current = t.next;
            /*System.out.println("Siamo all'istante: " + t.current);
            System.out.println("L'evento successivo doveva avvenire all'istante: " + t.next);
            System.out.println("I due tempi coincidono, quindi andiamo a processare l'evento " + e);*/

            if (e == ALL_EVENTS) {
            	events[ALL_EVENTS].t += intervalLength;
            	//System.out.println("Prossimo evento di SAVE_STAT: " + events[ALL_EVENTS].t);
            } else if (e == INDEX_ARRIVAL_LOGIN) { //e == 0
            	System.out.println("\n---------L'EVENTO è UN NUOVO ARRIVO NEL LOGIN-------------");
            	totalJobsInLogin++;
            	/*System.out.println("Job nel nodo Login: " + totalJobsInLogin);
            	System.out.println("Numero di serventi della coda Login: " + SERVERS_LOGIN);
            	
            	System.out.println("------(Intanto pianifico il nuovo evento di arrivo, che sarà alla coda Login)");*/
            	events[INDEX_ARRIVAL_LOGIN].t = getArrival(rng, loginNode.getStreamIndex(), t.current);
            	//System.out.println("--------(Sarà un arrivo in coda Login, all'istante: " + events[0].t + ")");
            	            	
            	if (totalJobsInLogin <= SERVERS_LOGIN) {
            		//System.out.println("Ci sono meno utenti nel centro di quanti server totali");
            		service = getServiceLogin(rng, loginNode.getStreamIndex(), loginNode.getServiceTime(), rvms);
            		//System.out.println("Si cerca un server libero");
            		s = findLoginServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            
            } else if (e == INDEX_ARRIVAL_ULTIMATE_TEAM) { //e == 14, cioè l'arrivo ad Ultimate Team
            	
            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	//System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI ULTIMATE TEAM----------");
            	totalJobsInUltimateTeam++;
            	/*System.out.println("Elementi nel centro Ultimate Team: " + totalJobsInUltimateTeam);
            	System.out.println("Numero di server della coda Ultimate Team: " + SERVERS_ULTIMATE_TEAM);*/
            	
            	if (totalJobsInUltimateTeam <= SERVERS_ULTIMATE_TEAM) { //verifico se posso essere servito subito
            		service = getServiceUT(rng, UTnode.getStreamIndex(), UTnode.getServiceTime(), rvms);
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_ULTIMATE_TEAM);
            		s = findUltimateTeamServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                   //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}	
            
            } else if (e == INDEX_ARRIVAL_STAGIONI) { // e == 29 arrivo alla coda Stagioni
            	
            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI STAGIONI----------");
            	totalJobsInStagioni++;
            	/*System.out.println("Elementi nel centro Stagioni: " + totalJobsInStagioni);
            	System.out.println("Numero di server della coda Stagioni: " + SERVERS_STAGIONI);*/
            	
            	if (totalJobsInStagioni <= SERVERS_STAGIONI) { //verifico se posso essere servito subito
            		service = getServiceStagioni(rng, StagioniNode.getStreamIndex(), StagioniNode.getServiceTime(), rvms);
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_STAGIONI);
            		s = findStagioniServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if (e == INDEX_ARRIVAL_CLUB) { //e == 35 arrivo alla coda Club
            	
            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI PRO CLUB----------");
            	totalJobsInClub++;
            	/*System.out.println("Elementi nel centro Pro Club: " + totalJobsInClub);
            	System.out.println("Numero di server della coda Pro Club: " + SERVERS_CLUB);*/
            	
            	if (totalJobsInClub <= SERVERS_CLUB) { //verifico se posso essere servito subito
            		service = getServiceClub(rng, clubNode.getStreamIndex(), clubNode.getServiceTime(), rvms);
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_CLUB);
            		s = findClubServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    //System.out.println("AUMENTO DI 1 SERVED");
                    events[s].t = t.current + service;
                    
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if ((e >= INDEX_FIRST_SERVER_LOGIN) && (e <= INDEX_LAST_SERVER_LOGIN)) { //eventi dei server di Login, 1 e 12
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER AL LOGIN------------------");
            	
            	if (firstCompletionLogin == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionLogin = t.current; 
            	}
            	boolean abandon = false;
            	//int percorsi = generateDestination(rng, loginNode.getStreamIndex());
            	//boolean abandon = generateAbandon(rng, loginNode.getStreamIndex(), not_P1);//qua si decide se l'utente abbandona oppure supera i controlli
            	if (abandon == true) { //se l'utente non supera i controlli
            		//System.out.println("L'utente non ha superato i controlli del Login");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		dropoutsLoginQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
            	}
            	else {
            		int percorsi = generateDestination(rng, loginNode.getStreamIndex());
            		totalJobsInLogin--;//diminuisco di 1 il numero di utenti in questo centro
            		totalLoginCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nel Login: " + totalLoginCheck);
                	System.out.println("Utenti ancora nel Login: " + totalJobsInLogin);*/
                	
                	//int percorsi = generateDestination(rng, loginNode.getStreamIndex());
            		if (percorsi == -1) { //se l'utente non supera i controlli
                		//System.out.println("L'utente non ha superato i controlli del Login");
                		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
                		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
                		dropoutsLoginQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
                	}
                	               	
            		else if (percorsi == 0) {
                		//System.out.println("L'utente andrà in coda Ultimate Team");            		
                	    events[INDEX_ARRIVAL_ULTIMATE_TEAM].t = t.current; //aggiunto un evento alla coda Ultimate Team, cioè l'evento 14
                		events[INDEX_ARRIVAL_ULTIMATE_TEAM].x = 1; //attivazione dell'evento 14
                	} else if (percorsi == 1) {
                		//System.out.println("L'utente andrà in coda Club");            		
                	    events[INDEX_ARRIVAL_CLUB].t = t.current; //aggiunto un evento alla coda Club, cioè l'evento 35
                		events[INDEX_ARRIVAL_CLUB].x = 1; //attivazione dell'evento 35
                	} else if (percorsi == 2) {
                		//System.out.println("L'utente andrà in coda Stagioni");
                		events[INDEX_ARRIVAL_STAGIONI].t = t.current; //aggiunto un evento alla coda Stagioni, cioè l'evento 29
                		events[INDEX_ARRIVAL_STAGIONI].x = 1;//attivazione dell'evento 29
                	}
                	
                	s = e;
                	
                	if (totalJobsInLogin >= SERVERS_LOGIN) {//ci sono ancora elementi in coda
                		//System.out.println("Ci sono degli elementi in coda Login da servire, ma ora il server " + s + " si è liberato");
                		service = getServiceLogin(rng, loginNode.getStreamIndex(), loginNode.getServiceTime(), rvms);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda Login, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
        	} else if ((e >= INDEX_FIRST_SERVER_ULTIMATE_TEAM) && (e <= INDEX_LAST_SERVER_ULTIMATE_TEAM)) { //eventi dei server di Ultimate Team, 15 E 27
        		//System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER AD ULTIMATE TEAM------------------");
            	if (firstCompletionUltimateTeam == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionUltimateTeam = t.current; 
            	}
            	
            	boolean abandon = generateAbandon(rng, UTnode.getStreamIndex(), not_P5);//qua si decide se l'utente abbandona oppure supera i controlli
            	if (abandon) { //se l'utente non supera i controlli
            		//System.out.println("L'utente non ha superato i controlli di Ultimate Team");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		dropoutsUltimateTeamQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
            	}
            	else {
            		totalJobsInUltimateTeam--;//diminuisco di 1 il numero di utenti in coda in questo centro
            		totalUltimateTeamCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nella coda Ultimate Team: " + totalUltimateTeamCheck);
                	System.out.println("Utenti ancora in UltimateTeam: " + totalJobsInUltimateTeam);*/
                	s = e;
                	
                	if (totalJobsInUltimateTeam >= SERVERS_ULTIMATE_TEAM) {//ci sono ancora elementi in coda
                		//System.out.println("Ci sono degli elementi in coda Ultimate Teame da servire, ma ora il server " + s + " si è liberato");
                		service = getServiceUT(rng, UTnode.getStreamIndex(), UTnode.getServiceTime(), rvms);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda Ultimate Team, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= INDEX_FIRST_SERVER_STAGIONI) && (e <= INDEX_LAST_SERVER_STAGIONI)) { //eventi dei server di Stagioni, 30 e 33
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER DI STAGIONI------------------");
            	if (firstCompletionStagioni == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionStagioni = t.current; 
            	}
            	
            	boolean abandon = generateAbandon(rng, StagioniNode.getStreamIndex(), not_P6);//qua si decide se l'utente abbandona oppure supera i controlli
            	if (abandon) { //se l'utente non supera i controlli
            		//System.out.println("L'utente non ha superato i controlli di Stagioni");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		dropoutsStagioniQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
            	}
            	else {
            		totalJobsInStagioni--;//diminuisco di 1 il numero di utenti in coda in questo centro
            		totalStagioniCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nella coda di Stagioni: " + totalStagioniCheck);
                	System.out.println("Utenti ancora in Stagioni: " + totalJobsInStagioni);*/
                
                	s = e;
                	
                	if (totalJobsInStagioni >= SERVERS_STAGIONI) {//ci sono ancora elementi in coda
                		//System.out.println("Ci sono degli elementi in coda Stagioni da servire, ma ora il server " + s + " si è liberato");
                		service = getServiceStagioni(rng, StagioniNode.getStreamIndex(), StagioniNode.getServiceTime(), rvms);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda Stagioni, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= INDEX_FIRST_SERVER_CLUB) && (e <= INDEX_LAST_SERVER_CLUB)) { //eventi dei server di Club, 36 e 40
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER DI CLUB------------------");
            	if (firstCompletionClub == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionClub = t.current; 
            	}
            	
            	boolean abandon = generateAbandon(rng, clubNode.getStreamIndex(), not_P7);//qua si decide se l'utente abbandona oppure supera i controlli
            	if (abandon) { //se l'utente non supera i controlli
            		//System.out.println("L'utente non ha superato i controlli di Club");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		dropoutsClubQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
            	}
            	else {
            		totalJobsInClub--;//diminuisco di 1 il numero di utenti in questo centro
            		totalClubCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nel Club: " + totalClubCheck);
                	System.out.println("Utenti ancora nel Club: " + totalJobsInClub);*/
                	
                	s = e;
                	
                	if (totalJobsInClub >= SERVERS_CLUB) {//ci sono ancora elementi in coda
                		//System.out.println("Ci sono degli elementi Club da servire, ma ora il server " + s + " si è liberato");
                		service = getServiceClub(rng, clubNode.getStreamIndex(), clubNode.getServiceTime(), rvms);
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda ProClub, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if (e == INDEX_DROPOUT_LOGIN) { //e == 13
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
  	
            }
        }
        
        //LOGIN
        writeCsv(loginNode.getTempiMediDiRispostaBatch(), seed, filenameLogin);
        //ULTIMATE TEAM
        writeCsv(UTnode.getTempiMediDiRispostaBatch(), seed, filenameUT);
        //STAGIONI
        writeCsv(StagioniNode.getTempiMediDiRispostaBatch(), seed, filenameStagioni);
        //CLUB
        writeCsv(clubNode.getTempiMediDiRispostaBatch(), seed, filenameClub);
        
        removeWarmUp(loginNode.getPopolazioneDellaCodaBatch());
        removeWarmUp(loginNode.getPopolazioneDelSistemaBatch());
        removeWarmUp(loginNode.getTempiDiServizioBatch());
        removeWarmUp(loginNode.getTempiMediDiRispostaBatch());
        removeWarmUp(loginNode.getTempiMediInCodaBatch());
        removeWarmUp(loginNode.getInterarriviBatch());
        removeWarmUp(loginNode.getUtilizzazioneBatch());
        
        removeWarmUp(UTnode.getPopolazioneDellaCodaBatch());
        removeWarmUp(UTnode.getPopolazioneDelSistemaBatch());
        removeWarmUp(UTnode.getTempiDiServizioBatch());
        removeWarmUp(UTnode.getTempiMediDiRispostaBatch());
        removeWarmUp(UTnode.getTempiMediInCodaBatch());
        removeWarmUp(UTnode.getInterarriviBatch());
        removeWarmUp(UTnode.getUtilizzazioneBatch());
        
        removeWarmUp(StagioniNode.getPopolazioneDellaCodaBatch());
        removeWarmUp(StagioniNode.getPopolazioneDelSistemaBatch());
        removeWarmUp(StagioniNode.getTempiDiServizioBatch());
        removeWarmUp(StagioniNode.getTempiMediDiRispostaBatch());
        removeWarmUp(StagioniNode.getTempiMediInCodaBatch());
        removeWarmUp(StagioniNode.getInterarriviBatch());
        removeWarmUp(StagioniNode.getUtilizzazioneBatch());
        
        removeWarmUp(clubNode.getPopolazioneDellaCodaBatch());
        removeWarmUp(clubNode.getPopolazioneDelSistemaBatch());
        removeWarmUp(clubNode.getTempiDiServizioBatch());
        removeWarmUp(clubNode.getTempiMediDiRispostaBatch());
        removeWarmUp(clubNode.getTempiMediInCodaBatch());
        removeWarmUp(clubNode.getInterarriviBatch());
        removeWarmUp(clubNode.getUtilizzazioneBatch());
        
      //LOGIN
        writeFile(loginNode.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_login");
        writeFile(loginNode.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_login");
        writeFile(loginNode.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_login");
        writeFile(loginNode.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_login");
        writeFile(loginNode.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_login");
        //writeFile(loginNode.getInterarriviBatch(),"batch_reports", "interarrivi_login");
        writeFile(loginNode.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_login");
        
        //ULTIMATE TEAM
        writeFile(UTnode.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_UltimateTeam");
        writeFile(UTnode.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_UltimateTeam");
        writeFile(UTnode.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_UltimateTeam");
        writeFile(UTnode.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_UltimateTeam");
        writeFile(UTnode.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_UltimateTeam");
        //writeFile(UTnode.getInterarriviBatch(),"batch_reports", "interarrivi_UT");
        writeFile(UTnode.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_UltimateTeam");
        
        //STAGIONI
        writeFile(StagioniNode.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_Stagioni");
        writeFile(StagioniNode.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_Stagioni");
        writeFile(StagioniNode.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_Stagioni");
        writeFile(StagioniNode.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_Stagioni");
        writeFile(StagioniNode.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_Stagioni");
        //writeFile(StagioniNode.getInterarriviBatch(),"batch_reports", "interarrivi_Stagioni");
        writeFile(StagioniNode.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_Stagioni");
        
        //CLUB
        writeFile(clubNode.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_Club");
        writeFile(clubNode.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_Club");
        writeFile(clubNode.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_Club");
        writeFile(clubNode.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_Club");
        writeFile(clubNode.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_Club");
        //writeFile(clubNode.getInterarriviBatch(),"batch_reports", "interarrivi_Club");
        writeFile(clubNode.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_Club");
        
        Estimate estimate = new Estimate();

        List<String> filenames = Arrays.asList(
        		"popolazione_coda_login",
                "popolazione_sistema_login",
                "tempiDiservizio_login",
                "tempiDiRisposta_login",
                "tempi_in_coda_login",
                "utilizzazione_login", 
        		"popolazione_coda_UltimateTeam","popolazione_sistema_UltimateTeam", "tempiDiservizio_UltimateTeam","tempiDiRisposta_UltimateTeam",
                "tempi_in_coda_UltimateTeam","utilizzazione_UltimateTeam", 
                "popolazione_coda_Stagioni",
                "popolazione_sistema_Stagioni",
                "tempiDiservizio_Stagioni",
                "tempiDiRisposta_Stagioni",
                "tempi_in_coda_Stagioni",
                "utilizzazione_Stagioni",
                "popolazione_coda_Club",
                "popolazione_sistema_Club",
                "tempiDiservizio_Club",
                "tempiDiRisposta_Club",
                "tempi_in_coda_Club",
                "utilizzazione_Club"
                	);
        
        for (String filename : filenames) {
            estimate.createInterval("batch_reports", filename);
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

	
	private void writeCsv(List<Double> list, long seed, String filepath) {
	    File file = new File(filepath);
	    boolean fileExists = file.exists();

	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {

	        // Scrive l'intestazione solo se il file non esiste
	        if (!fileExists) {
	            writer.write("seed,response_time");
	            writer.newLine();
	        }

	        //Prendiamo solo tanti valori quanti sono il numero di batch
	        int limit = Math.min(list.size(), NUMBATCHES);

	        for (int i = 0; i < limit; i++) {
	            writer.write(seed + "," + list.get(i));
	            writer.newLine();
	        }


	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	
	private void removeWarmUp(List<Double> list) {
		int warmUpBatches = 20;
		list.subList(0, warmUpBatches).clear();
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
	
	static boolean generateAbandon(Rngs rngs, int streamIndex, double percentage) {
        rngs.selectStream(2 + streamIndex);
        return rngs.random() <= percentage;
    }
	
	static int generateDestination(Rngs rngs, int streamIndex) {
	    rngs.selectStream(3 + streamIndex);
	    double r = rngs.random();
	    
	    if (r < 0.6) { 
	        return 0; // Ultimate Team
	    } else if (r < 0.8) {
	        return 1; // Club
	    } else if (r < 0.9) {
	        return 2; // Stagioni
	    } else return -1;//abbandono
	    
	}
	
		
	int findLoginServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		
        int s;

        int i = INDEX_FIRST_SERVER_LOGIN; //i server Login iniziano dall'indice 1 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < INDEX_LAST_SERVER_LOGIN) { //i < 12, perché i server login sono da 1 a 12 ma si entra già facendo i++ quindi deve essere minore stretto di 12  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findUltimateTeamServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;

        int i = INDEX_FIRST_SERVER_ULTIMATE_TEAM; //i server di Ultimate Team iniziano dall'indice 15 in events

        while (event[i].x == 1)  
            i++;                  
        s = i;
        while (i < INDEX_LAST_SERVER_ULTIMATE_TEAM) { //i < 27, perché i server di Ultimate Team sono da 15 a 27 
        	i++;                                           
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findStagioniServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		
        int s;

        int i = INDEX_FIRST_SERVER_STAGIONI; //i server delle Stagioni iniziano dall'indice 30 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < INDEX_LAST_SERVER_STAGIONI) { //i < 33, perché i server delle Stagioni sono da 30 a 33 ma si entra già facendo i++ quindi deve essere minore stretto di 33  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	int findClubServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		
        int s;

        int i = INDEX_FIRST_SERVER_CLUB; //i server infopoint iniziano dall'indice 36 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < INDEX_LAST_SERVER_CLUB) { //i < 38, perché i server di Club sono da 34 a 38 ma si entra già facendo i++ quindi deve essere minore stretto di 14  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	private double getServiceLogin(Rngs r, int streamIndex, double meanServiceTime, Rvms rvms) {
        r.selectStream(streamIndex);
	    return (NormalTruncated(meanServiceTime, DEV_ST_LOGIN, LOWER_B_LOGIN, UPPER_B_LOGIN, r, rvms));
		
    }
	
	private double getServiceUT(Rngs r, int streamIndex, double meanServiceTime, Rvms rvms) {
        r.selectStream(streamIndex);
        return (NormalTruncated(meanServiceTime, DEV_ST_UT, LOWER_B_UT, UPPER_B_UT, r, rvms));
    }
	
	private double getServiceStagioni(Rngs r, int streamIndex, double meanServiceTime, Rvms rvms) {
        r.selectStream(streamIndex);
        return (NormalTruncated(meanServiceTime, DEV_ST_STA, LOWER_B_STA, UPPER_B_STA, r, rvms));
    }
	
	private double getServiceClub(Rngs r, int streamIndex, double meanServiceTime, Rvms rvms) {
        r.selectStream(streamIndex);
        return (NormalTruncated(meanServiceTime, DEV_ST_CLUB, LOWER_B_CLUB, UPPER_B_CLUB, r, rvms));
    }
	
	private double NormalTruncated(double m, double s, double a, double b, Rngs r, Rvms rvms) {
        // Genera un numero casuale dalla distribuzione normale standard
        // m indica la media e s la deviazione standard

        if (a >= b) {
            System.out.println("Il valore di a deve essere minore di b");
        }

        double u;
        double z;

        while(true) {
            u = r.random();
            z = rvms.idfNormal(m, s, u);

            if (z >= a && z <= b){
                return z;
            }
        }
        // Scala e trasla il numero secondo la media e la deviazione standard
        // Verifica se il numero è all'interno dell'intervallo desiderato
        
    }
	
	//funzione per generare tempi esponenziali
	double exponential(double mean, Rngs r) {
        return (-mean * Math.log(1.0 - r.random()));
    }
	
	//funzione per generare il prossimo arrivo in base allo slot orario
	double getArrival(Rngs r, int streamIndex, double currentTime) {
        //System.out.println("----CALCOLO DELL'ARRIVO----");
        //System.out.println("Ultimo istante in cui è stato generato un arrivo è: " + sarrival);
		r.selectStream(1 + streamIndex);
        sarrival += exponential(1.0/LAMBDA, r);
        //System.out.println("Quindi ora l'ultimo istante in cui è stato generato un arrivo è: " + (sarrival));

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
