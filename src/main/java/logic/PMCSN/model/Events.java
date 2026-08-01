package logic.PMCSN.model;

public class Events {

    // === UTENTI -> LOGIN ===
    public static int ARRIVAL_EVENT_LOGIN = 1;
    public static int SERVERS_LOGIN = 7;
    public static int DROPOUT_EVENT_LOGIN = 1;

    // === UTENTI CHE GIOCANO AD ULTIMATE TEAM -> SERVER DI ACCESSO AD ULTIMATE TEAM ===
    public static int ARRIVAL_EVENT_ULTIMATE_TEAM = 1;
    public static int SERVERS_ULTIMATE_TEAM = 5;
    public static int DROPOUT_EVENT_ULTIMATE_TEAM = 1;
    
    
    // === UTENTI CHE GIOCANO A STAGIONI -> SERVER DI ACCESSO A STAGIONI ===
    public static int ARRIVAL_EVENT_STAGIONI = 1;
    public static int SERVERS_STAGIONI = 3;
    public static int DROPOUT_EVENT_STAGIONI = 1;
    
    // === UTENTI CHE GIOCANO A CLUB -> SERVER DI ACCESSO A CLUB ===
    public static int ARRIVAL_EVENT_CLUB = 1;
    public static int SERVERS_CLUB = 4;
    public static int DROPOUT_EVENT_CLUB = 1;
    
    // === TOTALE EVENTI PER BLOCCO ===
    public static int ALL_EVENTS_LOGIN = ARRIVAL_EVENT_LOGIN + SERVERS_LOGIN + DROPOUT_EVENT_LOGIN;
    public static int ALL_EVENTS_ULTIMATE_TEAM = ARRIVAL_EVENT_ULTIMATE_TEAM + SERVERS_ULTIMATE_TEAM + DROPOUT_EVENT_ULTIMATE_TEAM;
    public static int ALL_EVENTS_STAGIONI = ARRIVAL_EVENT_STAGIONI + SERVERS_STAGIONI + DROPOUT_EVENT_STAGIONI;
    public static int ALL_EVENTS_CLUB = ARRIVAL_EVENT_CLUB + SERVERS_CLUB + DROPOUT_EVENT_CLUB;
    
    public static int ALL_EVENTS = ALL_EVENTS_LOGIN + ALL_EVENTS_ULTIMATE_TEAM + ALL_EVENTS_STAGIONI + ALL_EVENTS_CLUB;
    public static int ALL_EVENTS_WITH_SAVE_STAT = ALL_EVENTS + 1; //l'ultimo evento è l'evento periodico SAVE_STAT per salvare le statisiche
    
    /* TIPOLOGIE DI EVENTO IN BASE ALL'INDICE NELL'ARRAY EVENTS
     * 
     *
     * LOGIN
     * 0 arrivo
     * 1-2-3-4-5-6 servizio
     * 7 abbandono
     * 
     * ULTIMATE TEAM
     * 8 arrivo
     * 9-10-11-12 servizio
     * 13 abbandono
     * 
     * STAGIONI
     * 14 arrivo
     * 15-16-17 servizio
     * 18 abbandono
     * 
     * CLUB
     * 19 arrivo
     * 20-21-22-23-24 servizio
     * 25 abbandono
     * 
     * 
     */
    
     public static int INDEX_ARRIVAL_LOGIN = 0;
     public static int INDEX_FIRST_SERVER_LOGIN = 1;
     public static int INDEX_LAST_SERVER_LOGIN = 7;
     public static int INDEX_DROPOUT_LOGIN = 8;
 
     public static int INDEX_ARRIVAL_ULTIMATE_TEAM = 9;
     public static int INDEX_FIRST_SERVER_ULTIMATE_TEAM = 10;
     public static int INDEX_LAST_SERVER_ULTIMATE_TEAM = 14;
     public static int INDEX_DROPOUT_ULTIMATE_TEAM = 15;
     
     public static int INDEX_ARRIVAL_STAGIONI = 16;
     public static int INDEX_FIRST_SERVER_STAGIONI = 17;
     public static int INDEX_LAST_SERVER_STAGIONI = 19;
     public static int INDEX_DROPOUT_STAGIONI = 20;
     
     public static int INDEX_ARRIVAL_CLUB = 21;
     public static int INDEX_FIRST_SERVER_CLUB = 22;
     public static int INDEX_LAST_SERVER_CLUB = 25;
     public static int INDEX_DROPOUT_CLUB = 26;

    

 }
