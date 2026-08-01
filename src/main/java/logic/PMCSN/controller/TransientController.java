package logic.PMCSN.controller;

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
import java.util.List;

import logic.PMCSN.libraries.Rngs;
import logic.PMCSN.model.ClubNode;
import logic.PMCSN.model.LoginNode;
import logic.PMCSN.model.StagioniNode;
import logic.PMCSN.model.TransientStats;
import logic.PMCSN.model.UltimateTeamNode;
import logic.PMCSN.utils.Rvms;

import static logic.PMCSN.model.Constants.*;

public class TransientController {
	
	static double START = 0.0; //tempo d'inizio della simulazione
    static double sarrival = START; //ultimo tempo in cui è stato generato un arrivo
    //static double STOP = 172800;
    static double STOP = 21600;
    static int INTERVAL_DATA = 20;
    static double COLUMNS = (STOP/INTERVAL_DATA) + 1;
	
	public void startAnalysis() {
		String filenameLogin = "transientLogin.csv";
		String filenameUT = "transientUT.csv";
		String filenameStagioni = "transientStagioni.csv";
		String filenameClub = "transientClub.csv";
		long[] seeds = new long[1024];
		seeds[0] = 123456789;
		Rngs r = new Rngs();
		
		//for (int i = 0; i < 10; i++) {
		for (int i = 0; i < 150; i++) {
			System.out.println("ITERAZIONE: " + i);
			TransientStats ts = new TransientStats();//va inizializzato dentro al ciclo perché ad ogni nuova run raccolgo nuove statistiche da 0
			sarrival = START;
			seeds[i+1] = finiteHorizonSimulation(seeds[i], r, ts);
			writeCsv(ts.getTransientStatsLogin(), seeds[i], filenameLogin);
			writeCsv(ts.getTransientStatsUT(), seeds[i], filenameUT);
			writeCsv(ts.getTransientStatsStagioni(), seeds[i], filenameStagioni);
			writeCsv(ts.getTransientStatsClub(), seeds[i], filenameClub);
			
		}
	}
	
	private long finiteHorizonSimulation(long seed, Rngs rng, TransientStats ts) {
		
		LoginNode loginNode = new LoginNode();
		StagioniNode StagioniNode = new StagioniNode();
		ClubNode clubNode = new ClubNode();
		UltimateTeamNode UTnode = new UltimateTeamNode();
		
		
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
		
		rng.plantSeeds(seed);
		
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
        
        //inizializzazione clock
        MsqT t = new MsqT();
        t.current = START;  
        
        events[0].t = getArrival(rng, loginNode.getStreamIndex(), t.current);
        events[0].x = 1;
        
        //EVENTO SAVE_STAT
        //System.out.println("\n------------GENERAZIONE SAVE_STAT----------");        
        events[ALL_EVENTS].t = INTERVAL_DATA;
        events[ALL_EVENTS].x = 1;

        
        for (int i = 0; i < ALL_EVENTS; i++) {
        	if ((events[i].t != 0) && (events[i].x != 1)) {
        		events[i].t = START;
                events[i].x = 0;
                sum[i].service = 0.0;
                sum[i].served = 0;
        	}
        } 
                
        /* === INIZIO ITERAZIONE === */
        //System.out.println("\n\n\n----INIZIA LA SIMULAZIONE------");
        //System.out.println("La simulazione andrà avanti fino al numero di job prefissati");
        
        int iter = 0;
        ts.getTransientStatsLogin().add(0.0);
    	ts.getTransientStatsUT().add(0.0);
    	ts.getTransientStatsStagioni().add(0.0);
    	ts.getTransientStatsClub().add(0.0);
    	
        
        
        while(events[0].x != 0 || totalJobsInLogin+totalJobsInUltimateTeam+totalJobsInStagioni+totalJobsInClub != 0) {
        	
        	iter++;
        	System.out.println("\n\n-------LA SIMULAZIONE VA AVANTI, QUINDI NUOVA ITERAZIONE, è LA NUMERO: " + iter);	
        	     
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
            //System.out.println("Siamo all'istante: " + t.current);
            //System.out.println("Il prossimo evento (che è quello appena trovato) avverrà all'istante: " + t.next);

    		//Node area
    		nodeAreaLogin += (t.next - t.current)*totalJobsInLogin;
    		nodeAreaUltimateTeam += (t.next - t.current)*totalJobsInUltimateTeam;
    		nodeAreaStagioni += (t.next - t.current)*totalJobsInStagioni;
    		nodeAreaClub += (t.next - t.current)*totalJobsInClub;
    		
    		//System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.current = t.next;
            System.out.println("Siamo all'istante: " + t.current);
            //System.out.println("L'evento successivo doveva avvenire all'istante: " + t.next);
            //System.out.println("I due tempi coincidono, quindi andiamo a processare l'evento " + e);

            if (e == ALL_EVENTS) {
            	System.out.println("\n---------L'EVENTO è il SALVATAGGIO STATISTICHE TRANSITORIO-------------");
            	double responseTimeLogin = nodeAreaLogin/totalLoginCheck;
            	double responseTimeUT = nodeAreaUltimateTeam/totalUltimateTeamCheck;
            	double responseTimeStagioni = nodeAreaStagioni/totalStagioniCheck;
            	double responseTimeClub = nodeAreaClub/totalClubCheck;
            	ts.getTransientStatsLogin().add(responseTimeLogin);
            	ts.getTransientStatsUT().add(responseTimeUT);
            	ts.getTransientStatsStagioni().add(responseTimeStagioni);
            	ts.getTransientStatsClub().add(responseTimeClub);
            	events[ALL_EVENTS].t += INTERVAL_DATA;
            	if (events[ALL_EVENTS].t > STOP) {
            		events[ALL_EVENTS].x = 0;
            	}
            	//System.out.println("Prossimo evento di SAVE_STAT: " + events[ALL_EVENTS].t);
            } else if (e == INDEX_ARRIVAL_LOGIN) { //e == 0
            	System.out.println("\n---------L'EVENTO è UN NUOVO ARRIVO NEL LOGIN-------------");
            	totalJobsInLogin++;
            	/*System.out.println("Job nel nodo Login: " + totalJobsInLogin);
            	System.out.println("Numero di serventi della coda Login: " + SERVERS_LOGIN);
            	
            	System.out.println("------(Intanto pianifico il nuovo evento di arrivo, che sarà alla coda Login)");*/
            	events[INDEX_ARRIVAL_LOGIN].t = getArrival(rng, loginNode.getStreamIndex(), t.current);
            	//System.out.println("--------(Sarà un arrivo in coda Login, all'istante: " + events[0].t + ")");
            	if (events[INDEX_ARRIVAL_LOGIN].t > STOP) {
            		events[INDEX_ARRIVAL_LOGIN].x = 0;
            	} 
            	            	
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

        int i = INDEX_FIRST_SERVER_CLUB; 

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
	private double exponential(double mean, Rngs r) {
        return (-mean * Math.log(1.0 - r.random()));
    }
	
	//funzione per generare il prossimo arrivo
	private double getArrival(Rngs r, int streamIndex, double currentTime) {
		r.selectStream(1 + streamIndex);
        sarrival+= exponential(1/LAMBDA, r);

        return (sarrival);
    }
	
	private int nextEvent(MsqEvent[] event) {
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
	    return (e);   
	}
	
}