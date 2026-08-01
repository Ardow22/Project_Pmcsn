package logic.PMCSN.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import logic.PMCSN.libraries.Msq;
import logic.PMCSN.libraries.Rngs;
import logic.PMCSN.utils.Rvms;
import logic.PMCSN.model.ClubNode;
import logic.PMCSN.model.Events;
import logic.PMCSN.model.LoginNode;
import logic.PMCSN.model.Node;
import logic.PMCSN.model.StagioniNode;
import logic.PMCSN.model.UltimateTeamNode;
import logic.PMCSN.utils.Estimate;

import static logic.PMCSN.model.Constants.*;
import static logic.PMCSN.model.Events.*;

//classe per tener traccia del tempo
class MsqT {
    double current;  //tempo corrente                
    double next;     //tempo del prossimo evento               
}

//classe di supporto ad accumulare le statistiche di un singolo server
class MsqSum {                      
    double service;  //tempo di servizio totale impiegato                
    long served;    //numero clienti serviti in totale  
}

//classe per modellare un evento della simulazione
class MsqEvent {                    
    double t;   //tempo in cui avverrà l'evento
    int x;      //stato dell'evento: 1 è attivo, 0 è inattivo
}


/*ComputationalModelController è solo una classe di prova per sperimentare il funzionamento del sistema
 * con parametri casuali
 * 
 */




/* TIPOLOGIE DI EVENTO IN BASE ALL'INDICE NELL'ARRAY EVENTS
/* (PROVA)
 * LOGIN
 * 0 arrivo
 * 1-2-3-4-5-6-7-8-9-10-11-12 servizio
 * 13 abbandono
 * 
 * ULTIMATE TEAM
 * 14 arrivo
 * 15-16-17-18-19-20-21-22-23-24-25-26-27 servizio
 * 28 abbandono
 * 
 * STAGIONI
 * 29 arrivo
 * 30-31 servizio
 * 32 abbandono
 * 
 * CLUB
 * 33 arrivo
 * 34-35-36-37-38 servizio
 * 39 abbandono
 * 
 */



public class ComputationalModelController { 
		
	static double START = 0.0; //tempo d'inizio della simulazione
    static double STOP = 6 * 3600; //dopo quanto tempo termina la simulazione
    static double sarrival = START; //ultimo tempo in cui è stato generato un arrivo
	
    
    public long startSimulation(long seed, Rngs rng, Node loginNode, Node StagioniNode, Node UTnode, Node clubNode) {
		
    	sarrival = START;
		int intervalLength = 480;
		
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
		
        
        //System.out.println("\n-----------INIZIALIZZAZIONE EVENTI NELLA SIMULAZIONE-------------");
        //int sumDebug = ALL_EVENTS_WITH_SAVE_STAT;
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
        /*for (int i = 0; i < events.length; i++) {
        	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
        	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
        	System.out.println(" ");
        }*/
        
        //inizializzazione clock
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
        
        
        while(events[0].x != 0 || totalJobsInLogin+totalJobsInUltimateTeam+totalJobsInStagioni+totalJobsInClub != 0) {
        	
        	iter++;
        	System.out.println("\n\n-------LA SIMULAZIONE VA AVANTI, QUINDI NUOVA ITERAZIONE, è LA NUMERO: " + iter);	
        	/*System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI: ");
            for (int i = 0; i < events.length; i++) {
            	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
            	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
            	System.out.println(" ");
            }*/
            
            //System.out.println("SITUAZIONE DEL NUMERO DI JOB NEI CENTRI: ");
            //System.out.println("totalJobsInLogin vale " + totalJobsInLogin);
            //System.out.println("totalJobsInUltimateTeam vale " + totalJobsInUltimateTeam);
            //System.out.println("totalJobsInStagioni vale " + totalJobsInStagioni);
            //System.out.println("totalJobsInClub vale " + totalJobsInClub);
            
        	//System.out.println("\n----SITUAZIONE ABBANDONI-----------");
        	/*System.out.println("ABBANDONI LOGIN: ");
        	for (double info: dropoutsLoginQueue) {
        		System.out.println(info);
        	}
        	System.out.println("ABBANDONI ULTIMATE TEAM: ");
        	for (double info: dropoutsUltimateTeamQueue) {
        		System.out.println(info);
        	}
        	System.out.println("ABBANDONI STAGIONI: ");
        	for (double info: dropoutsStagioniQueue) {
        		System.out.println(info);
        	}
        	System.out.println("ABBANDONI CLUB: ");
        	for (double info: dropoutsClubQueue) {
        		System.out.println(info);
        	}*/
        	
        	       	
            if(!dropoutsLoginQueue.isEmpty()) {
        		//System.out.println("La lista di abbandoni del Login non è vuota");
        		events[13].t = dropoutsLoginQueue.get(0);
        		//System.out.println("L'evento di abbandono del Login avverrà all'istante " + events[13].t);
        		events[13].x = 1; //attivo l'evento di abbandono
        		
        		/*System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI AGGIUNGENDO L'ABBANDONO DEL LOGIN: ");
                for (int i = 0; i < events.length; i++) {
                	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
                	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
                	System.out.println(" ");
                }*/
        	}
        	else {
        		//System.out.println("La lista di abbandoni del Login è vuota, l'evento viene disattivato");
        		events[13].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	if(!dropoutsUltimateTeamQueue.isEmpty()) {
        		//System.out.println("La lista di abbandoni della coda di Ultimate Team non è vuota");
        		events[28].t = dropoutsUltimateTeamQueue.get(0);
        		//System.out.println("L'evento di abbandono della coda di Ultimate Team avverrà all'istante " + events[28].t);
        		events[28].x = 1; //attivo l'evento di abbandono
        		
        		/*System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI AGGIUNGENDO L'ABBANDONO Di ULTIMATE TEAM: ");
                for (int i = 0; i < events.length; i++) {
                	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
                	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
                	System.out.println(" ");
                }*/
        	}
        	else {
        		//System.out.println("La lista di abbandoni della coda di Ultimate Team è vuota, l'evento viene disattivato");
        		events[28].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	if(!dropoutsStagioniQueue.isEmpty()) {
        		//System.out.println("La lista di abbandoni della coda delle Stagioni non è vuota");
        		events[32].t = dropoutsStagioniQueue.get(0);
        		//System.out.println("L'evento di abbandono avverrà della coda delle Stagioni all'istante " + events[32].t);
        		events[32].x = 1; //attivo l'evento di abbandono
        		
        		/*System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI AGGIUNGENDO L'ABBANDONO DELLE STAGIONI: ");
                for (int i = 0; i < events.length; i++) {
                	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
                	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
                	System.out.println(" ");
                }*/
        	}
        	else {
        		//System.out.println("La lista di abbandoni della coda delle Stagioni è vuota, l'evento viene disattivato");
        		events[32].x = 0; //disattivo l'evento di abbandono
        	}
        	
        	if(!dropoutsClubQueue.isEmpty()) {
        		//System.out.println("La lista di abbandoni della coda di Club non è vuota");
        		events[39].t = dropoutsClubQueue.get(0);
        		//System.out.println("L'evento di abbandono avverrà all'istante " + events[39].t);
        		events[39].x = 1; //attivo l'evento di abbandono
        		
        		/*System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI AGGIUNGENDO L'ABBANDONO DI CLUB: ");
                for (int i = 0; i < events.length; i++) {
                	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
                	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
                	System.out.println(" ");
                }*/
        	}
        	else {
        		//System.out.println("La lista di abbandoni della coda di Club è vuota, l'evento viene disattivato");
        		events[39].x = 0; //disattivo l'evento di abbandono
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
            	//System.out.println("\n---------L'EVENTO è SAVE_STAT-------------");
            	events[ALL_EVENTS].t += intervalLength;
            	if (events[ALL_EVENTS].t > STOP) {
            		events[ALL_EVENTS].x = 0;
            	}
            	//System.out.println("Prossimo evento di SAVE_STAT: " + events[ALL_EVENTS].t);
            } else if (e == 0) { //e == 0
            	System.out.println("\n---------L'EVENTO è UN NUOVO ARRIVO NEL LOGIN-------------");
            	totalJobsInLogin++;
            	/*System.out.println("Job nel nodo Login: " + totalJobsInLogin);
            	System.out.println("Numero di serventi della coda Login: " + SERVERS_LOGIN);
            	
            	System.out.println("------(Intanto pianifico il nuovo evento di arrivo, che sarà alla coda Login)");*/
            	events[0].t = getArrival(rng, loginNode.getStreamIndex(), t.current);
            	//System.out.println("--------(Sarà un arrivo in coda Login, all'istante: " + events[0].t + ")");
            	if (events[0].t > STOP) {
        			//System.out.println("--------(però " + events[0].t + " è oltre " + STOP + "quindi non avverrà");
                    events[0].x = 0;
        		}
            	            	
            	if (totalJobsInLogin <= SERVERS_LOGIN) {
            		//System.out.println("Ci sono meno utenti nel centro di quanti server totali");
            		service = getService(rng, loginNode.getStreamIndex(), loginNode.getServiceTime());
            		//System.out.println("Si cerca un server libero");
            		s = findLoginServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            
            } else if (e == 14) { //e == 14, cioè l'arrivo ad Ultimate Team
            	
            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	//System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI ULTIMATE TEAM----------");
            	totalJobsInUltimateTeam++;
            	/*System.out.println("Elementi nel centro Ultimate Team: " + totalJobsInUltimateTeam);
            	System.out.println("Numero di server della coda Ultimate Team: " + SERVERS_ULTIMATE_TEAM);*/
            	
            	if (totalJobsInUltimateTeam <= SERVERS_ULTIMATE_TEAM) { //verifico se posso essere servito subito
            		service = getService(rng, UTnode.getStreamIndex(), UTnode.getServiceTime());
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_ULTIMATE_TEAM);
            		s = findUltimateTeamServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                   //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}	
            
            } else if (e == 29) { // e == 29 arrivo alla coda Stagioni
            	
            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI STAGIONI----------");
            	totalJobsInStagioni++;
            	/*System.out.println("Elementi nel centro Stagioni: " + totalJobsInStagioni);
            	System.out.println("Numero di server della coda Stagioni: " + SERVERS_STAGIONI);*/
            	
            	if (totalJobsInStagioni <= SERVERS_STAGIONI) { //verifico se posso essere servito subito
            		service = getService(rng, StagioniNode.getStreamIndex(), StagioniNode.getServiceTime());
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_STAGIONI);
            		s = findStagioniServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    events[s].t = t.current + service;
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if (e == 33) { //e == 33 arrivo alla coda Club
            	
            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI PRO CLUB----------");
            	totalJobsInClub++;
            	/*System.out.println("Elementi nel centro Pro Club: " + totalJobsInClub);
            	System.out.println("Numero di server della coda Pro Club: " + SERVERS_CLUB);*/
            	
            	if (totalJobsInClub <= SERVERS_CLUB) { //verifico se posso essere servito subito
            		service = getService(rng, clubNode.getStreamIndex(), clubNode.getServiceTime());
            		//System.out.println("Si cerca un server libero tra i " + SERVERS_CLUB);
            		s = findProClubServer(events);
            		//System.out.println("Abbiamo trovato il server numero " + s);
            		sum[s].service += service;
                    sum[s].served++;
                    //System.out.println("AUMENTO DI 1 SERVED");
                    events[s].t = t.current + service;
                    
                    //System.out.println("Il server " + s + " avrà completato all'istante " + events[s].t);
                    events[s].x = 1;
            	}
            	
            } else if ((e >= 1) && (e <= 12)) { //eventi dei server di Login
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER AL LOGIN------------------");
            	
            	if (firstCompletionLogin == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionLogin = t.current; 
            	}
            	
            	boolean abandon = generateAbandon(rng, loginNode.getStreamIndex(), 0.2);//qua si decide se l'utente abbandona oppure supera i controlli
            	if (abandon) { //se l'utente non supera i controlli
            		//System.out.println("L'utente non ha superato i controlli del Login");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		dropoutsLoginQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
            	}
            	else {
            		totalJobsInLogin--;//diminuisco di 1 il numero di utenti in questo centro
            		totalLoginCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nel Login: " + totalLoginCheck);
                	System.out.println("Utenti ancora nel Login: " + totalJobsInLogin);*/
                	
                	int percorsi = generateDestination(rng, loginNode.getStreamIndex());
                	               	
                	if (percorsi == 0) {
                		//System.out.println("L'utente andrà in coda Ultimate Team");            		
                	    events[14].t = t.current; //aggiunto un evento alla coda Ultimate Team
                		events[14].x = 1; //attivazione dell'evento
                	} else if (percorsi == 1) {
                		//System.out.println("L'utente andrà in coda Club");            		
                	    events[33].t = t.current; //aggiunto un evento alla coda Stagioni
                		events[33].x = 1; //attivazione dell'evento	
                	} else if (percorsi == 2) {
                		//System.out.println("L'utente andrà in coda Stagioni");
                		events[29].t = t.current; //aggiunto un evento alla coda Pro Club
                		events[29].x = 1;//attivazione dell'evento
                	}
                	
                	s = e;
                	
                	if (totalJobsInLogin >= SERVERS_LOGIN) {//ci sono ancora elementi in coda
                		//System.out.println("Ci sono degli elementi in coda Login da servire, ma ora il server " + s + " si è liberato");
                		service = getService(rng, loginNode.getStreamIndex(), loginNode.getServiceTime());
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda Login, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
        	} else if ((e >= 15) && (e <= 27)) { //eventi dei server di Ultimate Team
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
                		service = getService(rng, UTnode.getStreamIndex(), UTnode.getServiceTime());
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda Ultimate Team, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= 30) && (e <= 31)) { //eventi dei server di Stagioni
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
                		service = getService(rng, StagioniNode.getStreamIndex(), StagioniNode.getServiceTime());
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service; 
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda Stagioni, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if ((e >= 34) && (e <= 38)) { //eventi dei server di Pro Club
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
                		service = getService(rng, clubNode.getStreamIndex(), clubNode.getServiceTime());
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda ProClub, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //il server diventa libero	
                	}
            	}
            	
            } else if (e == 13) {
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA LOGIN------------");
            	dropoutsLogin++;
            	dropoutsLoginQueue.remove(0);	
            } else if (e == 28) {
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA ULTIMATE TEAM------------");
            	dropoutsUltimateTeam++;
            	dropoutsUltimateTeamQueue.remove(0);
            	
            } else if (e == 32) {
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA STAGIONI------------");
            	dropoutsStagioni++;
            	dropoutsStagioniQueue.remove(0);
            	
            } else if (e == 39) {
            	System.out.println("\n------L'EVENTO è L'ABBANDONO DELLA CODA CLUB------------");
            	dropoutsClub++;
            	dropoutsClubQueue.remove(0);
  	
            }   
        }
        
        stats(loginNode, nodeAreaLogin, totalLoginCheck, 1, 12, events, firstCompletionLogin, sum, events[0].t);
        stats(UTnode, nodeAreaUltimateTeam, totalUltimateTeamCheck, 15, 27, events, firstCompletionUltimateTeam, sum, events[14].t);
        stats(StagioniNode, nodeAreaStagioni, totalStagioniCheck, 30 , 31, events, firstCompletionStagioni, sum, events[29].t);
        stats(clubNode, nodeAreaClub, totalClubCheck, 34, 38, events, firstCompletionClub, sum, events[33].t);
        
        //LOGIN
        writeFile(loginNode.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_login");
        writeFile(loginNode.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_login");
        writeFile(loginNode.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_login");
        writeFile(loginNode.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_login");
        writeFile(loginNode.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_login");
        writeFile(loginNode.getInterarriviBatch(),"batch_reports", "interarrivi_login");
        writeFile(loginNode.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_login");
        
        //ULTIMATE TEAM
        writeFile(UTnode.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_UT");
        writeFile(UTnode.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_UT");
        writeFile(UTnode.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_UT");
        writeFile(UTnode.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_UT");
        writeFile(UTnode.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_UT");
        writeFile(UTnode.getInterarriviBatch(),"batch_reports", "interarrivi_UT");
        writeFile(UTnode.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_UT");
        
        //STAGIONI
        writeFile(StagioniNode.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_Stagioni");
        writeFile(StagioniNode.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_Stagioni");
        writeFile(StagioniNode.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_Stagioni");
        writeFile(StagioniNode.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_Stagioni");
        writeFile(StagioniNode.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_Stagioni");
        writeFile(StagioniNode.getInterarriviBatch(),"batch_reports", "interarrivi_Stagioni");
        writeFile(StagioniNode.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_Stagioni");
        
        //CLUB
        writeFile(clubNode.getPopolazioneDellaCodaBatch(), "batch_reports", "popolazione_coda_Club");
        writeFile(clubNode.getPopolazioneDelSistemaBatch(), "batch_reports","popolazione_sistema_Club");
        writeFile(clubNode.getTempiDiServizioBatch(), "batch_reports", "tempiDiservizio_Club");
        writeFile(clubNode.getTempiMediDiRispostaBatch(), "batch_reports","tempiDiRisposta_Club");
        writeFile(clubNode.getTempiMediInCodaBatch(), "batch_reports", "tempi_in_coda_Club");
        writeFile(clubNode.getInterarriviBatch(),"batch_reports", "interarrivi_Club");
        writeFile(clubNode.getUtilizzazioneBatch(),"batch_reports", "utilizzazione_Club");
        
        Estimate estimate = new Estimate();

        List<String> filenames = Arrays.asList(
        		"popolazione_coda_login",
                "popolazione_sistema_login",
                "tempiDiservizio_login",
                "tempiDiRisposta_login",
                "tempi_in_coda_login",
                "interarrivi_login",
                "utilizzazione_login", 
        		"popolazione_coda_UT","popolazione_sistema_UT", "tempiDiservizio_UT","tempiDiRisposta_UT",
                "tempi_in_coda_UT","interarrivi_UT","utilizzazione_UT", 
                "popolazione_coda_Stagioni",
                "popolazione_sistema_Stagioni",
                "tempiDiservizio_Stagioni",
                "tempiDiRisposta_Stagioni",
                "tempi_in_coda_Stagioni",
                "interarrivi_Stagioni",
                "utilizzazione_Stagioni",
                "popolazione_coda_Club",
                "popolazione_sistema_Club",
                "tempiDiservizio_Club",
                "tempiDiRisposta_Club",
                "tempi_in_coda_Club",
                "interarrivi_Club",
                "utilizzazione_Club"
                	);
        
        for (String filename : filenames) {
            estimate.createInterval("batch_reports", filename);
        }
        
        rng.selectStream(255);
        return rng.getSeed();
        
    }
    
    private static void stats(Node node, double nodeArea, double jobsServed, int indexFirstServer, int indexLastServer, MsqEvent[] events, double firstCompletion, MsqSum[] sum, double eventTime) {
		double responseTime = nodeArea/jobsServed;
		double interarrivals = eventTime/jobsServed;
		double abandons;
		
		double finalTime = 0;
		for (int s = indexFirstServer; s <= indexLastServer; s++) {
			if (events[s].t > finalTime) {
				finalTime = events[s].t;
			}
		}
		
		double actualTime = finalTime - firstCompletion;
		double avgPopulations = nodeArea/actualTime;
		
		double queueArea = nodeArea;
		for (int i = indexFirstServer; i <= indexLastServer; i++) {
			queueArea -= sum[i].service;
		}
		double delaysTime = queueArea/jobsServed;
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
	
	static boolean generateAbandon(Rngs rngs, int streamIndex, double percentage) {
        rngs.selectStream(2 + streamIndex);
        return rngs.random() <= percentage;
    }
	
	static int generateDestination(Rngs rngs, int streamIndex) {
	    rngs.selectStream(3 + streamIndex);
	    double r = rngs.random();

	    if (r < 0.75) {
	        return 0; // Ultimate Team
	    } else if (r < 0.95) {
	        return 1; // Club
	    } else {
	        return 2; // Stagioni
	    }
	}
	
		
	static int findLoginServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		//System.out.println("CERCHIAMO IL SERVER PER L'INFOPOINT");
        int s;

        int i = 1; //i server Login iniziano dall'indice 1 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < 12) { //i < 12, perché i server login sono da 1 a 12 ma si entra già facendo i++ quindi deve essere minore stretto di 12  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	static int findUltimateTeamServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
        int s;

        int i = 15; //i server di Ultimate Team iniziano dall'indice 15 in events

        while (event[i].x == 1)  
            i++;                  
        s = i;
        while (i < 27) { //i < 27, perché i server di Ultimate Team sono da 15 a 27 
        	i++;                                           
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	static int findStagioniServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		//System.out.println("CERCHIAMO IL SERVER PER L'INFOPOINT");
        int s;

        int i = 30; //i server delle Stagioni iniziano dall'indice 30 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < 31) { //i < 31, perché i server delle Stagioni sono da 30 a 31 ma si entra già facendo i++ quindi deve essere minore stretto di 31  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
	static int findProClubServer(MsqEvent[] event) {
        /* -----------------------------------------------------
         * return the index of the available server idle longest
         * -----------------------------------------------------
         */
		//System.out.println("CERCHIAMO IL SERVER PER L'INFOPOINT");
        int s;

        int i = 34; //i server infopoint iniziano dall'indice 13 in events

        while (event[i].x == 1) 
            i++;                       
        s = i;
        //System.out.println("Un servente candidato è il servente " + s);
        while (i < 38) { //i < 38, perché i server di Club sono da 34 a 38 ma si entra già facendo i++ quindi deve essere minore stretto di 14  
            i++;                                             
            if ((event[i].x == 0) && (event[i].t < event[s].t))
                s = i;
        }
        return (s);
    }
	
				
	static double getService(Rngs r, int streamIndex, double meanServiceTime) {
        r.selectStream(streamIndex);
        return (exponential(meanServiceTime, r));
    }
	
	static double NormalTruncated(double m, double s, double a, double b, Rngs r, Rvms rvms) throws Exception {
        // Genera un numero casuale dalla distribuzione normale standard
        // m indica la media e s la deviazione standard

        if (a >= b) {
            throw new Exception("Il valore di a deve essere minore di b");
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
	static double exponential(double mean, Rngs r) {
        return (-mean * Math.log(1.0 - r.random()));
    }
	
	//funzione per generare il prossimo arrivo in base allo slot orario
	static double getArrival(Rngs r, int streamIndex, double currentTime) {
        //System.out.println("----CALCOLO DELL'ARRIVO----");
        //System.out.println("Ultimo istante in cui è stato generato un arrivo è: " + sarrival);
		r.selectStream(1 + streamIndex);
        sarrival+= exponential(1/24.0, r);
        //System.out.println("Quindi ora l'ultimo istante in cui è stato generato un arrivo è: " + (sarrival));

        return (sarrival);
    }
	
	static int nextEvent(MsqEvent[] event) {
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
