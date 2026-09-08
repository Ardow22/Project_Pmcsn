package pmcsn.UniversalStudiosHollywood.model;

public class Events {
     
     /* TIPOLOGIE DI EVENTO IN BASE ALL'INDICE NELL'ARRAY EVENTS
      * 
      *
      * CONTROLLO SICUREZZA
      * 0 arrivo
      * 1-2-3-4-5-6-7-8-9-10-11-12-13-14-15-16-17-18-19-20 servizio
      * 
      * BIGLIETTERIA FISICA
      * 21 arrivo
      * 22-23-24-25-26-27-28-29-30-31-32-33-34-35-36-37 servizio
      * 
      * CONTROLLO BIGLIETTI
      * 38 arrivo
      * 39-40-41-42-43-44-45-46-47-48-49-50-51-52-53-54 servizio
      * 
      * SUPER MARIO
      * 55 arrivo standard
      * 56 arrivo express
      * 57 servizio
      * 
      * HARRY POTTER
      * 58 arrivo standard
      * 59 arrivo express
      * 60 servizio
      * 
      */
     
     
     
     public static int ARRIVAL_EVENT_SICUREZZA = 1;
     public static int SERVERS_SICUREZZA = 20; 
     
     public static int ARRIVAL_EVENT_BIGLIETTERIA = 1;
     public static int SERVERS_BIGLIETTERIA = 16; 
     
     public static int ARRIVAL_EVENT_CONTROLLI = 1;
     public static int SERVERS_CONTROLLI = 16; 
     
     public static int ARRIVAL_EVENT_MARIO_STANDARD = 1;
     public static int ARRIVAL_EVENT_MARIO_EXPRESS = 1;
     public static int SERVERS_MARIO = 1;
     //verifica
     public static int ARRIVAL_EVENT_MARIO = 1;
     
     public static int ARRIVAL_EVENT_HP_STANDARD = 1;
     public static int ARRIVAL_EVENT_HP_EXPRESS = 1;
     public static int SERVERS_HP = 1;
     //verifica
     public static int ARRIVAL_EVENT_HP = 1;
     
     public static int ALL_EVENTS_SICUREZZA = ARRIVAL_EVENT_SICUREZZA + SERVERS_SICUREZZA;
     public static int ALL_EVENTS_BIGLIETTERIA = ARRIVAL_EVENT_BIGLIETTERIA + SERVERS_BIGLIETTERIA;
     public static int ALL_EVENTS_CONTROLLI = ARRIVAL_EVENT_CONTROLLI + SERVERS_CONTROLLI;
     public static int ALL_EVENTS_MARIO = ARRIVAL_EVENT_MARIO_STANDARD + ARRIVAL_EVENT_MARIO_EXPRESS + SERVERS_MARIO;
     public static int ALL_EVENTS_HP = ARRIVAL_EVENT_HP_STANDARD + ARRIVAL_EVENT_HP_EXPRESS + SERVERS_HP;
     //verifica
     public static int ALL_EVENTS_MARIO_VERIFICA = ARRIVAL_EVENT_MARIO + SERVERS_MARIO;
     public static int ALL_EVENTS_HP_VERIFICA = ARRIVAL_EVENT_HP + SERVERS_HP;
     
     public static int ALL_EVENTS = ALL_EVENTS_SICUREZZA + ALL_EVENTS_BIGLIETTERIA + ALL_EVENTS_CONTROLLI + ALL_EVENTS_MARIO + ALL_EVENTS_HP;
     public static int ALL_EVENTS_WITH_SAVE_STAT = ALL_EVENTS + 1;
     //verifica
     public static int ALL_EVENTS_VERIFICA = ALL_EVENTS_SICUREZZA + ALL_EVENTS_BIGLIETTERIA + ALL_EVENTS_CONTROLLI + ALL_EVENTS_MARIO_VERIFICA + ALL_EVENTS_HP_VERIFICA;
     public static int ALL_EVENTS_WITH_SAVE_STAT_VERIFICA = ALL_EVENTS_VERIFICA + 1;
     public static int ALL_EVENTS_STAT_LAMBDA_VERIFICA = ALL_EVENTS_WITH_SAVE_STAT_VERIFICA + 1; 
     
     public static int INDEX_ARRIVAL_SICUREZZA = 0;
     public static int INDEX_FIRST_SERVER_SICUREZZA = 1;
     public static int INDEX_LAST_SERVER_SICUREZZA = 20; 
     
     public static int INDEX_ARRIVAL_BIGLIETTERIA = 21; 
     public static int INDEX_FIRST_SERVER_BIGLIETTERIA = 22; 
     public static int INDEX_LAST_SERVER_BIGLIETTERIA = 37; 
     
     public static int INDEX_ARRIVAL_CONTROLLI = 38; 
     public static int INDEX_FIRST_SERVER_CONTROLLI = 39;
     public static int INDEX_LAST_SERVER_CONTROLLI = 54;
     
     public static int INDEX_ARRIVAL_MARIO_STANDARD = 55;
     public static int INDEX_ARRIVAL_MARIO_EXPRESS = 56; 
     public static int INDEX_FIRST_SERVER_MARIO = 57; 
     public static int INDEX_LAST_SERVER_MARIO = 57;
     //verifica
     public static int INDEX_ARRIVAL_MARIO = 55; 
     public static int INDEX_FIRST_SERVER_MARIO_VERIFICA = 56; 
     public static int INDEX_LAST_SERVER_MARIO_VERIFICA = 56;
     
     public static int INDEX_ARRIVAL_HP_STANDARD = 58; 
     public static int INDEX_ARRIVAL_HP_EXPRESS = 59; 
     public static int INDEX_FIRST_SERVER_HP = 60; 
     public static int INDEX_LAST_SERVER_HP = 60; 
     //verifica
     public static int INDEX_ARRIVAL_HP = 57; 
     public static int INDEX_FIRST_SERVER_HP_VERIFICA = 58; 
     public static int INDEX_LAST_SERVER_HP_VERIFICA = 58;
     

     
     
    

 }
