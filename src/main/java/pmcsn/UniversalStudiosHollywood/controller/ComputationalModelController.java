package pmcsn.UniversalStudiosHollywood.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pmcsn.UniversalStudiosHollywood.libraries.Msq;
import pmcsn.UniversalStudiosHollywood.libraries.Rngs;
import pmcsn.UniversalStudiosHollywood.utils.Rvms;
import pmcsn.UniversalStudiosHollywood.model.BiglietteriaFisicaNode;
import pmcsn.UniversalStudiosHollywood.model.ControlloBigliettiNode;
import pmcsn.UniversalStudiosHollywood.model.ControlloSicurezzaNode;
import pmcsn.UniversalStudiosHollywood.model.Events;
import pmcsn.UniversalStudiosHollywood.model.HarryPotterNode;
import pmcsn.UniversalStudiosHollywood.model.MarioKartNode;
import pmcsn.UniversalStudiosHollywood.model.Node;
import pmcsn.UniversalStudiosHollywood.utils.Estimate;

import static pmcsn.UniversalStudiosHollywood.model.Constants.*;
import static pmcsn.UniversalStudiosHollywood.model.Events.*;

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
 * 
 *
 * CONTROLLO SICUREZZA
 * 0 arrivo
 * 1-2-3-4-5-6-7-8-9-10-11-12-13-14-15-16 servizio
 * 
 * BIGLIETTERIA FISICA
 * 17 arrivo
 * 18-19-20-21-22-23-24-25-26-27-28-29-30-31-32-33 servizio
 * 
 * CONTROLLO BIGLIETTI
 * 34 arrivo
 * 35-36-37-38-39-40-41-42-43-44-45-46-47-48-49-50 servizio
 * 
 * SUPER MARIO
 * 51 arrivo standard
 * 52 arrivo express
 * 53 servizio
 * 
 * HARRY POTTER
 * 54 arrivo standard
 * 55 arrivo express
 * 56 servizio
 * 
 */

public class ComputationalModelController { 
		
	static double START = 0.0; //tempo d'inizio della simulazione
    static double STOP = 6 * 3600; //dopo quanto tempo termina la simulazione
    static double sarrival = START; //ultimo tempo in cui è stato generato un arrivo
    
    private BiglietteriaFisicaNode biglietteria;
	private ControlloSicurezzaNode sicurezza;
	private ControlloBigliettiNode controlli;
	private MarioKartNode mario_kart;
	private HarryPotterNode harry_potter;
    
    public void startSimulation() {
		
		int intervalLength = 480;  
		        
        //inizializzazione dei double per memorizzare il primo completamento delle varie code
        double totalJobsInSicurezza = 0;
	    double totalJobsInBiglietteria = 0;
	    double totalJobsInControlli = 0;
	    double totalJobsInMario = 0;
	    double totalJobsInMarioStandardQueue = 0;
	    double totalJobsInMarioExpressQueue = 0;
	    double totalJobsInHarryPotter = 0;
	    double totalJobsInHarryPotterStandardQueue = 0;
	    double totalJobsInHarryPotterExpressQueue = 0;
	    
	    double totalSicurezzaCheck = 0;
	    double totalBiglietteriaCheck = 0;
	    double totalControlliCheck = 0;
	    double totalMarioCheck = 0;
	    double totalHarryPotterCheck = 0;
	    
	    double nodeAreaSicurezza = 0.0;
	    double nodeAreaBiglietteria = 0.0;
	    double nodeAreaControlli = 0.0;
	    double nodeAreaMario = 0.0;
	    double nodeAreaHarryPotter = 0.0;
	    
	    double firstCompletionSicurezza = 0;
	    double firstCompletionBiglietteria = 0;
	    double firstCompletionControlli = 0;
	    double firstCompletionMario = 0;
	    double firstCompletionHarryPotter = 0;
	    
	    //true = il prossimo cliente è standard, false = il prossimo cliente è express
	    boolean nextMarioStandard = true; 
	    boolean nextHPstandard = true;
	    
	    //chi sta terminando adesso
	    boolean marioServingStandard = true;
	    boolean hpServingStandard = true;
        
		int e; //indice next event, cioè l'evento più imminente
		int s; //indice del server
		
		double service; //tempo di servizio
		
		biglietteria = new BiglietteriaFisicaNode();
		sicurezza = new ControlloSicurezzaNode();
		controlli = new ControlloBigliettiNode();
		mario_kart = new MarioKartNode();
		harry_potter = new HarryPotterNode();
		
        //System.out.println("\n-----------INIZIALIZZAZIONE EVENTI NELLA SIMULAZIONE-------------");
        //int sumDebug = ALL_EVENTS_WITH_SAVE_STAT;
        //System.out.println("Eventi totali previsti nella simulazione (INCLUSO SAVE_STAT): " + sumDebug);
        
		//Setup generatore RNG
		Rngs rng = new Rngs();
		long seed = 123456789;
		rng.plantSeeds(seed);
		        
		MsqEvent[] events = new MsqEvent[ALL_EVENTSS_WITH_SAVE_STAT];
		MsqSum[] sum = new MsqSum[ALL_EVENTSS];
		        
		for (int i = 0; i < ALL_EVENTSS_WITH_SAVE_STAT; i++) {
			events[i] = new MsqEvent();
		}
		for (int i = 0; i < ALL_EVENTSS; i++) {
			sum[i] = new MsqSum();
		}
		        
		        
	    //inizializzazione clock
		MsqT t = new MsqT();
		t.current = START;  
		        
		events[0].t = getArrival(rng, sicurezza.getStreamIndex(), t.current);
		events[0].x = 1;
		                
		events[ALL_EVENTSS].t = intervalLength;
		events[ALL_EVENTSS].x = 1;
		        
		for (int i = 0; i < ALL_EVENTSS; i++) {
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
        
        
        //while(events[0].x != 0) {
        while(iter <= 4000) {
        	
        	iter++;
        	/*System.out.println("\n\n-------LA SIMULAZIONE VA AVANTI, QUINDI NUOVA ITERAZIONE, è LA NUMERO: " + iter);	
        	System.out.println("SITUAZIONI DELLA LISTA DEGLI EVENTI: ");
            for (int i = 0; i < events.length; i++) {
            	System.out.println("Evento " + i + ", tempo in cui avverrà: " + events[i].t);
            	System.out.println("Evento " + i + ", stato dell'evento: " + events[i].x);
            	System.out.println(" ");
            }
            
            System.out.println("\n--- STATO CODE PRIMA DELL'EVENTO ---");

            System.out.println(
                "MARIO -> Totale: " + (int)totalJobsInMario +
                " | Standard: " + (int)totalJobsInMarioStandardQueue +
                " | Express: " + (int)totalJobsInMarioExpressQueue +
                " | Prossimo preferito: " +
                (nextMarioStandard ? "STANDARD" : "EXPRESS")
            );

            System.out.println(
                "HARRY POTTER -> Totale: " + (int)totalJobsInHarryPotter +
                " | Standard: " + (int)totalJobsInHarryPotterStandardQueue +
                " | Express: " + (int)totalJobsInHarryPotterExpressQueue +
                " | Prossimo preferito: " +
                (nextHPstandard ? "STANDARD" : "EXPRESS")
            );*/
        	
        	//System.out.println("\n========================================"); 
        	//System.out.println("ITERAZIONE NUMERO: " + iter); 
        	//System.out.println("========================================"); 
        	
        	
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
    	    nodeAreaHarryPotter += (t.next - t.current)*totalJobsInHarryPotter;
    	    
    		//System.out.println("\n----AGGIORNAMENTO DEL CLOCK------");
            t.current = t.next;
            /*System.out.println("Siamo all'istante: " + t.current);
            System.out.println("L'evento successivo doveva avvenire all'istante: " + t.next);
            System.out.println("I due tempi coincidono, quindi andiamo a processare l'evento " + e);*/

            /*System.out.println("\n========================================"); 
            System.out.println("EVENTO SCELTO: " + e); 
            System.out.println("TEMPO EVENTO: " + events[e].t); 
            System.out.println("CLOCK AGGIORNATO: " + t.current); 
            System.out.println("========================================");*/
            
            if (e == ALL_EVENTSS) {
            	//System.out.println("\n---------L'EVENTO è SAVE_STAT-------------");
            	events[ALL_EVENTSS].t += intervalLength;
            	if (events[ALL_EVENTSS].t > STOP) {
            		events[ALL_EVENTSS].x = 0;
            	}
            	//System.out.println("Prossimo evento di SAVE_STAT: " + events[ALL_EVENTS].t);
            } else if (e == INDEX_ARRIVAL_SICUREZZA) { //e == 0
            	//System.out.println("\n---------L'EVENTO è UN NUOVO ARRIVO NEI CONTROLLI SICUREZZA-------------");
            	totalJobsInSicurezza++;
            	/*System.out.println("Job nel nodo Login: " + totalJobsInLogin);
            	System.out.println("Numero di serventi della coda Login: " + SERVERS_LOGIN);*/
            	
            	//System.out.println("------(Intanto pianifico il nuovo evento di arrivo, che sarà alla coda Login)");
            	events[INDEX_ARRIVAL_SICUREZZA].t = getArrival(rng, sicurezza.getStreamIndex(), t.current);
            	//System.out.println("--------(Sarà un arrivo in coda Sicurezza, all'istante: " + events[0].t + ")");
            	if (events[0].t > STOP) {
        			//System.out.println("--------(però " + events[0].t + " è oltre " + STOP + "quindi non avverrà");
                    events[0].x = 0;
        		}
            	            	
            	if (totalJobsInSicurezza <= SERVERS_SICUREZZA) {
            		//System.out.println("Nel centro sicurezza ci sono meno utenti nel centro di quanti server totali");
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
            
            } else if (e == INDEX_ARRIVAL_BIGLIETTERIA) { //e == 14, cioè l'arrivo alla biglietteria fisica
            	
            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	//System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DELLA BIGLIETTERIA----------");
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
            
            } else if (e == INDEX_ARRIVAL_CONTROLLI) { // e == 29 arrivo alla coda Stagioni
            	
            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	//System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DEI CONTROLLI BIGLIETTI----------");
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
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI MARIO STANDARD----------");
            	totalJobsInMario++;
            	totalJobsInMarioStandardQueue++;
            	System.out.println("Elementi nel centro MarioKart: " + totalJobsInMario);
            	System.out.println("Elementi solo nella coda Mario Standard: " + totalJobsInMarioStandardQueue);
            	
            	if (totalJobsInMario <= SERVERS_MARIO) { //verifico se posso essere servito subito
            		System.out.println("ENTRA CLIENTE MARIO STANDARD");
            		marioServingStandard = true;
            		nextMarioStandard = false;
            		
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
            	
            } else if (e == INDEX_ARRIVAL_MARIO_EXPRESS) {
                events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI MARIO EXPRESS----------");
            	totalJobsInMario++;
            	totalJobsInMarioExpressQueue++;
            	System.out.println("Elementi nel centro Mario Kart: " + totalJobsInMario);
            	System.out.println("Elementi solo nella coda Mario express: " + totalJobsInMarioExpressQueue);
            	
            	if (totalJobsInMario <= SERVERS_MARIO) { //verifico se posso essere servito subito
            		System.out.println("ENTRA CLIENTE MARIO EXPRESS");
            		marioServingStandard = false;
            		nextMarioStandard = true;
            		
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
            	
            } else if (e == INDEX_ARRIVAL_HP_STANDARD) {

            	events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI HARRY POTTER STANDARD----------");
            	totalJobsInHarryPotter++;
            	totalJobsInHarryPotterStandardQueue++;
            	/*System.out.println("Elementi nel centro Pro Club: " + totalJobsInClub);
            	System.out.println("Numero di server della coda Pro Club: " + SERVERS_CLUB);*/
            	
            	if (totalJobsInHarryPotter <= SERVERS_HP) { //verifico se posso essere servito subito
            		
            		hpServingStandard = true;
            		nextHPstandard = false;
            		
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
            	
            } else if (e == INDEX_ARRIVAL_HP_EXPRESS) { 
                events[e].x = 0;//disattivazione dell'evento di arrivo
            	
            	System.out.println("\n--------------L'EVENTO è UN NUOVO ARRIVO ALLA CODA DI HARRY POTTER EXPRESS----------");
            	totalJobsInHarryPotter++;
            	totalJobsInHarryPotterExpressQueue++;
            	/*System.out.println("Elementi nel centro Pro Club: " + totalJobsInClub);
            	System.out.println("Numero di server della coda Pro Club: " + SERVERS_CLUB);*/
            	
            	if (totalJobsInHarryPotter <= SERVERS_HP) { //verifico se posso essere servito subito
            		
            		hpServingStandard = false;
            		nextHPstandard = true;
            		
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
            	
            } else if ((e >= INDEX_FIRST_SERVER_SICUREZZA) && (e <= INDEX_LAST_SERVER_SICUREZZA)) { //eventi dei server di Login
            	//System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVER ALLA SICUREZZA------------------");
            	
            	if (firstCompletionSicurezza == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionSicurezza = t.current; 
            	}
            	
            	//boolean abandon = generateAbandon(rng, sicurezza.getStreamIndex(), 0.2);//qua si decide se l'utente abbandona oppure supera i controlli
            	boolean abandon = false;
            	if (abandon) { //se l'utente non supera i controlli
            		//System.out.println("L'utente non ha superato i controlli del Login");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		//dropoutsLoginQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
            	}
            	else {
            		totalJobsInSicurezza--;//diminuisco di 1 il numero di utenti in questo centro
            		totalSicurezzaCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nel Login: " + totalLoginCheck);
                	System.out.println("Utenti ancora nel Login: " + totalJobsInLogin);*/
                	
                	int percorsi = generateBiglietteriaDestination(rng, sicurezza.getStreamIndex());
                	               	
                	if (percorsi == 0) {
                		//System.out.println("L'utente andrà in coda Ultimate Team");            		
                	    events[INDEX_ARRIVAL_BIGLIETTERIA].t = t.current; //aggiunto un evento alla coda Ultimate Team
                		events[INDEX_ARRIVAL_BIGLIETTERIA].x = 1; //attivazione dell'evento
                	} else if (percorsi == 1) {
                		//System.out.println("L'utente andrà in coda Club");            		
                	    events[INDEX_ARRIVAL_CONTROLLI].t = t.current; //aggiunto un evento alla coda Stagioni
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
        	} else if ((e >= INDEX_FIRST_SERVER_BIGLIETTERIA) && (e <= INDEX_LAST_SERVER_BIGLIETTERIA)) { //eventi dei serventi della biglietteria
        		//System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVENTE ALLA BIGLIETTERIA------------------");
            	if (firstCompletionBiglietteria == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionBiglietteria = t.current; 
            	}
            	
            	//boolean abandon = generateAbandon(rng, biglietteria.getStreamIndex(), not_P5);//qua si decide se l'utente abbandona oppure supera i controlli
            	boolean abandon = false;
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
                	s = e;
                	
                	if (totalJobsInBiglietteria >= SERVERS_BIGLIETTERIA) {//ci sono ancora elementi in coda
                		//System.out.println("Ci sono degli elementi in coda Biglietteria da servire, ma ora il servente " + s + " si è liberato");
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
            	
            } else if ((e >= INDEX_FIRST_SERVER_CONTROLLI) && (e <= INDEX_LAST_SERVER_CONTROLLI)) { //eventi dei server di Stagioni
            	//System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DI UN SERVENTE DI CONTROLLI------------------");
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
            	
            } else if ((e >= INDEX_FIRST_SERVER_MARIO) && (e <= INDEX_LAST_SERVER_MARIO)) { //eventi dei server di Pro Club
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DEL SERVER DI MARIO------------------");
            	if (firstCompletionMario == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionMario = t.current; 
            	}
            	
            	 System.out.println(
            		        "CLIENTE CHE HA FINITO: " +
            		        (marioServingStandard ? "STANDARD" : "EXPRESS")
            		    );
            	
            	boolean abandon = generateAbandon(rng, mario_kart.getStreamIndex(), not_P7);//qua si decide se l'utente abbandona oppure supera i controlli
            	abandon = false;
            	if (abandon) { //se l'utente non supera i controlli
            		//System.out.println("L'utente non ha superato i controlli di Club");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		//dropoutsClubQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
            	}
            	else { //questo else ci fa entrare nel ramo in cui il cliente ha terminato il servizio
            		
            		//il cliente ha terminato il servizio ed appartiene alla coda che era in servizio
            		if (marioServingStandard) {
            			totalJobsInMarioStandardQueue--;
            		}
            		else {
            			totalJobsInMarioExpressQueue--;
            		}
            		
            		totalJobsInMario--;//diminuisco di 1 il numero di utenti in questo centro
            		totalMarioCheck++;//aumento il numero di utenti serviti in questo centro
                	
                	
                	s = e;
                	
                	if (totalJobsInMario >= SERVERS_MARIO) {//controlliamo se ci sono ancora elementi in coda
                		
                		//CI SONO ANCORA ELEMENTI, SCEGLIAMO DA QUALE CODA PRENDERE
                		if (nextMarioStandard && totalJobsInMarioStandardQueue > 0) {
                			System.out.println("ENTRA MARIO STANDARD");
                			//si serve la coda standard
                			marioServingStandard = true;
                			nextMarioStandard = false;
                		} else if (!nextMarioStandard && totalJobsInMarioExpressQueue > 0) {
                            // SERVE EXPRESS
                			System.out.println("ENTRA MARIO EXPRESS");
                            marioServingStandard = false;
                            nextMarioStandard = true;

                        } else if (totalJobsInMarioStandardQueue > 0) {
                            // È VUOTA LA EXPRESS,
                            // QUINDI SERVIAMO STANDARD
                        	System.out.println("EXPRESS VUOTA QUINDI ENTRA MARIO STANDARD");
                            marioServingStandard = true;
                            nextMarioStandard = false;
                        } else if (totalJobsInMarioExpressQueue > 0) {
                        	System.out.println("STANDARD VUOTA QUINDI ENTRA MARIO EXPRESS");
                            // STANDARD È VUOTA,
                            // QUINDI SERVIAMO EXPRESS
                            marioServingStandard = false;
                            nextMarioStandard = true;
                        }
                		                			
                		service = getService(rng, mario_kart.getStreamIndex(), mario_kart.getServiceTime());
                		sum[s].service += service;
                        sum[s].served++;
                        events[s].t = t.current + service;
                        //System.out.println("Il server " + s + " concluderà all'istante " + events[s].t);
                	} else { //altrimenti, se non ci sono persone in coda
                		//System.out.println("Non ci sono altri elementi in coda ProClub, il server " + s + " diventa disponibile");
                      	events[s].x = 0; //non ci sono altri clienti, il server è marcato come libero
                	}
            	}
            	
            } else if ((e >= INDEX_FIRST_SERVER_HP) && (e <= INDEX_LAST_SERVER_HP)) {
            	System.out.println("\n------L'EVENTO è IL COMPLETAMENTO DEL SERVENTE DI HARRY POTTER------------------");
            	if (firstCompletionHarryPotter == 0) { //salviamo il primo completamento per le statistiche 
            		firstCompletionHarryPotter = t.current; 
            	}
            	
            	boolean abandon = generateAbandon(rng, harry_potter.getStreamIndex(), not_P7);//qua si decide se l'utente abbandona oppure supera i controlli
            	abandon = false;
            	if (abandon) { //se l'utente non supera i controlli
            		//System.out.println("L'utente non ha superato i controlli di Club");
            		double abandonTime = t.current + 0.01;//si aggiunge 0.01 per realizzare l'evento il prima possibile
            		//System.out.println("Prossimo evento di abbandono: " + abandonTime);
            		//dropoutsClubQueue.add(abandonTime); //si aggiunge l'abbandono alla lista di abbandoni	
            	}
            	else {
            		
            		//il cliente ha terminato il servizio ed appartiene alla coda che era in servizio
            		if (hpServingStandard) {
            			totalJobsInHarryPotterStandardQueue--;
            		}
            		else {
            			totalJobsInHarryPotterExpressQueue--;
            		}
            		
            		totalJobsInHarryPotter--;//diminuisco di 1 il numero di utenti in questo centro
            		totalHarryPotterCheck++;//aumento il numero di utenti serviti in questo centro
                	/*System.out.println("Utenti serviti nel Club: " + totalClubCheck);
                	System.out.println("Utenti ancora nel Club: " + totalJobsInClub);*/
                	
                	s = e;
                	
                	if (totalJobsInHarryPotter >= SERVERS_HP) {//ci sono ancora elementi in coda
                		
                		//CI SONO ANCORA ELEMENTI, SCEGLIAMO DA QUALE CODA PRENDERE
                		if (nextHPstandard && totalJobsInHarryPotterStandardQueue > 0) {
                			//si serve la coda standard
                			hpServingStandard = true;
                			nextHPstandard = false;
                		} else if (!nextHPstandard && totalJobsInHarryPotterExpressQueue > 0) {
                            // SERVE EXPRESS
                            hpServingStandard = false;
                            nextHPstandard = true;

                        } else if (totalJobsInHarryPotterStandardQueue > 0) {
                            // È VUOTA LA EXPRESS,
                            // QUINDI SERVIAMO STANDARD
                            hpServingStandard = true;
                            nextHPstandard = false;
                        } else if (totalJobsInHarryPotterExpressQueue > 0) {
                            // STANDARD È VUOTA,
                            // QUINDI SERVIAMO EXPRESS
                            hpServingStandard = false;
                            nextHPstandard = true;
                        }
                		
                		
                		
                		
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
            	
            } /*else if (e == 13) {
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
  	
            } */   
        }
        
        /*stats(loginNode, nodeAreaLogin, totalLoginCheck, 1, 12, events, firstCompletionLogin, sum, events[0].t);
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
        return rng.getSeed();*/
        
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
	
	static int generateBiglietteriaDestination(Rngs rngs, int streamIndex) {
	    rngs.selectStream(3 + streamIndex);
	    double r = rngs.random();

	    if (r < 0.30) {
	        return 0; // Biglietteria
	    } else {
	        return 1; // Controlli
	    }
	}
	
	static int generateAttractionsDestination(Rngs rngs, int streamIndex) {
		rngs.selectStream(4 + streamIndex);
	    double r = rngs.random();

	    if (r < 0.25) { //Mario Kart
	    	return 0; 
	    } else if (r < 0.40) { // Harry Potter
	    	return 1;
	    } else { 
	    	return 2; //altre attrazioni
	    }
	}
	
	static int generateQueueDestination(Rngs rngs, int streamIndex) {
		rngs.selectStream(5 + streamIndex);
	    double r = rngs.random();

	    if (r < 0.70) {
	        return 0; // Coda Standard
	    } else {
	        return 1; // Coda Express
	    }		
	}
	
		
	static int findSicurezzaServer(MsqEvent[] event) {
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
	
	static int findBiglietteriaServer(MsqEvent[] event) {
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
	
	static int findControlliServer(MsqEvent[] event) {
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
	
	static int findMarioKartServer(MsqEvent[] event) {
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
	
	static int findHarryPotterServer(MsqEvent[] event) {
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
				
	static double getService(Rngs r, int streamIndex, double meanServiceTime) {
        r.selectStream(streamIndex);
        return (exponential(meanServiceTime, r));
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
	    while (i < ALL_EVENTSS_WITH_SAVE_STAT -1) {
	    	i++;
	    	if ((event[i].x == 1) && (event[i].t < event[e].t)) {
	    		e = i;
	    	}
	    }
	    //System.out.println("Evento trovato con indice " + (e));
	    return (e);   
	}
}
