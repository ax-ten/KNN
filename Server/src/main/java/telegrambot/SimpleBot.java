package telegrambot;

import data.Data;
import data.TrainingDataException;
import database.DatabaseConnectionException;
import database.InsufficientColumnNumberException;
import database.NoValueException;
import example.ExampleSizeException;
import mining.KNN;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.DocumentMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.MessageReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;
import utility.DataUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Bot necessario ad interfacciare un utente tramite la piattaforma client di Telegram.
 * È in grado di gestire richieste su trainingset precedentemente caricati sulla macchina, aggiungere nuovi dataset,
 * risolvere prediction da parte dell'utente e in caso questo non sia possibile, segnalare l'errore, tutto via chat.
 *
 * Apre un thread che ricava autonomamente il suo socket in comunicazione coi server telegram, sui quali effettua
 * polling per ottenere nuovi eventi chat, che gestisce autonomamente.
 */
public class SimpleBot {
    /**
     * Crea istanza del bot telegram
     * Ottiene chiave di autenticazione dal file AUTHKEY.txt, la convalida, registra il listener e inizia il polling.
     */
    public SimpleBot(){
        Scanner s = null;
        try {
            s = new Scanner(new File("src/main/java/server/AUTHKEY.txt"));
        } catch (FileNotFoundException e) {e.printStackTrace();}

        final String KEY = s.nextLine();
        TelegramBot tb = TelegramBot.login(KEY);

        try{
            convalidateAPIKey(tb);
        } catch (InvalidBotException e)   {e.printStackTrace(); return;}

        //Register listeners
        tb.getEventsManager().register(new MyListener());

        //This will tell the API to start polling the servers for updates
        //If you specify true as the argument you will receive any previous messages before the bot started.
        //If you specify false the API will discard any messages from before the bot was started.
        tb.startUpdates(false);
        System.out.println("Bot Started");
    }

    /**
     * @param tb istanza del bot
     * @throws InvalidBotException se la chiave non è valida
     */
    private void convalidateAPIKey(TelegramBot tb) throws InvalidBotException {
        if (tb == null){
            throw new InvalidBotException("chiave di autenticazione non valida");
        }
    }

    /**
     * Modella il listener del bot
     */
    private static class MyListener implements Listener {
        final String LOCALPATH = "src/main/Testfile/";
        final String BINEXT = ".dmp";
        private HashMap<String, KNN> userData = new LinkedHashMap<>(); //String: Chatid / Data: trainingset
        // ad ogni utente è associato un trainingset, nel momento in cui ne caricano un altro questo viene aggiornato.

        /**
         *  Listener dei messaggi testuali
         * Elabora messaggi che non contengono comandi, ma solo query sul quale effettuare prediction.
         * Può farlo solo dopo che sia stata caricata una knn in userData.
         * @param event ricezione di un messaggio
         */
        @Override
        public void onTextMessageReceived(TextMessageReceivedEvent event) {
            String chatID = event.getChat().getId();
            KNN knn;
            Double prediction;

            // i comandi sono considerati textmessages, tutti i comandi iniziano per '/', per cui
            // escludiamo tutti i messaggi che iniziano così
            if (event.getMessage().toString().charAt(0)=='/'){return;}

            // se l'utente ha registrato dei knn nel suo userdata (associato al suo id), ottieni il knn da lì
            if (userData.containsKey(chatID)){
                knn = userData.get(chatID);
                try {
                    // predici, rispondi con la predizione e invia un messaggio che richiede nuovamente lo stesso format
                    // per un'altra predizione sullo stesso knn
                    prediction = knn.predict(event.getContent().getContent());
                    reply(event,"Prediction: "+prediction);
                    sendMessage(event.getChat(),exampleFormatBuilder(knn.getData()));
                } catch (ExampleSizeException | NumberFormatException e) {
                    // se il formato di predizione non è valido avvisa l'utente e richiede nuovamente lo stesso format
                    e.printStackTrace();
                    reply(event,"Formato non valido.\n" +exampleFormatBuilder(knn.getData()));
                }
            } else
                // altrimenti richiedi di caricare un file
                event.getChat().sendMessage("Carica un trainingset per procedere.");
        }

        /**
         * Listener dei messaggi con file allegato
         * Elabora file ricevuti via chat, li salva in userData e manda un messaggio con la query dell'example da ricevere
         * basato sul file ricevuto.
         * @param event ricezione di un documento
         */
        @Override
        public void onDocumentMessageReceived(DocumentMessageReceivedEvent event) {
            Data trainingSet;
            String filename = event.getContent().getContent().getFileName();
            event.getContent().getContent().
                    downloadFile(event.getMessage().getBotInstance(), new File(LOCALPATH + filename));
            try {
                // carica trainingset dal file, ci crea un KNN e mostralo tramite messaggio, mostrando un prompt per
                // richiedere anche un example di ingresso. salva poi il knn caricato
                trainingSet = DataUtility.getTrainingSetFromDat(filename);
                KNN knn = new KNN(trainingSet);
                reply(event,knn.getData().toTgMessage());
                sendMessage(event.getChat(),exampleFormatBuilder(knn.getData()));
                userData.put(event.getChat().getId(),knn);
            } catch (TrainingDataException e) {
                e.printStackTrace();
                event.getChat().sendMessage("File non valido, manda un file con estensione .dat per continuare");
            }
        }


        /**
         * Listener dei comandi
         * Gestisce i comandi impartiti dall'utente via chat:
         *      loadfile - carica un trainingset da un file .dat salvato sul server
         *      loadddb - carica un trainingset da un database salvato sul server
         *      loadbinary - carica un trainingset da un file .dat salvato sul server
         *      listfiles - elenca i file presenti sul server
         *      start - presenta all'utente le varie funzioni del bot
         * @param event ricezione di un comando
         */
        @Override
        public void onCommandMessageReceived(CommandMessageReceivedEvent event) {
            String
                filename = "",
                chatID = event.getChat().getId(),
                command = event.getCommand().toLowerCase(Locale.ROOT);
            boolean
                flag_save = false,
                flag_load = false;
            KNN knn = null;

            try {
                filename = event.getArgs()[0];
            } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                System.out.println(e);
            }

            try {
                switch (command) {
                    case "loadfile":
                        knn = new KNN(DataUtility.getTrainingSetFromDat(filename));
                        flag_save = true;
                        break;
                    case "loaddb":
                        knn = new KNN(DataUtility.getTrainingSetFromDB(filename));
                        flag_save = true;
                        break;
                    case "loadbinary":
                        knn =  new KNN(DataUtility.getTrainingSetFromDmp(filename));
                        flag_load = true;
                        break;
                    case "listfiles":
                        File folder = new File("src/main/Testfile/");
                        StringBuilder msg = new StringBuilder("Lista dei file salvati sul server:\n```");
                        for (File f: Objects.requireNonNull(folder.listFiles()))
                            msg.append(f.getName()).append("\n");
                        reply(event,msg.append("```").toString());
                        break;
                    case "start":
                        reply(event, "Benvenutx in KNNbot! \nPer cominciare ad utilizzare questo bot utilizza uno " +
                                "degli appositi comandi listati nel menù. Per i comandi di caricamento del KNN il bot " +
                                "si aspetta anche il nome del file da aprire subito dopo il comando. Altrimenti puoi"+
                                "caricare direttamente un file di testo (in formato .dat) e verrà automaticamente elaborato");
                        break;
                    default:
                        reply(event,"Comando non riconosciuto");
                }

            } catch (NullPointerException | ArrayIndexOutOfBoundsException | ClassNotFoundException e){
                e.printStackTrace();
                //flag_save = false;
            } catch (TrainingDataException | IOException e) {
                e.printStackTrace();
                if (Objects.equals(filename, ""))
                    reply(event, "Specifica il nome del file da caricare dopo il comando, ad esempio ` /loadfile provac`");
                else if (command.equals("loaddb")) {
                    reply(event, "Non ho trovato il database che cerchi");}
                else
                    reply(event, "Impossibile trovare il file specificato");
            } catch (NoValueException | SQLException | InsufficientColumnNumberException e) {
                e.printStackTrace();
                reply(event, "Specifica il nome del file da caricare dopo il comando, ad esempio ` /loadfile provac`");
            } catch (DatabaseConnectionException e) {
                e.printStackTrace();
                if (Objects.equals(filename, ""))
                    reply(event, "Specifica il nome del database da caricare dopo il comando, ad esempio ` /loaddb provac`");
                else
                    reply(event, "Impossibile connettersi al DB");
            }

            if (flag_load || flag_save) {
                reply(event, knn.getData().toTgMessage());
                sendMessage(event.getChat(),exampleFormatBuilder(knn.getData()) );
                userData.put(chatID, knn);
            }
            if (flag_save && !filename.equals("") ) {
                try {
                    knn.salva(LOCALPATH + filename + BINEXT);
                } catch (IOException e) {
                    e.printStackTrace();
                    reply(event, "C'è stato un imprevisto col file selezionato");
                }
            }
        }

        /**
         * @param e evento al quale rispondere
         * @param s lista di messaggi da inviare come risposta
         */
        private void reply(MessageReceivedEvent e, List<String> s){
            for (String message : s)
                reply(e, message);
        }

        /**
         * @param e evento al quale rispondere
         * @param s messaggio da inviare come risposta
         */
        private void reply(MessageReceivedEvent e, String s){
            e.getChat().sendMessage(SendableTextMessage.builder()
                    .parseMode(ParseMode.MARKDOWN)
                    .message(s)
                    .replyTo(e.getMessage())
                    .build());
        }

        /**
         * @param c chat nel quale mandare il messaggio
         * @param s messaggio da inviare
         */
        private void sendMessage(Chat c, String s){
            c.sendMessage(SendableTextMessage.builder()
                    .parseMode(ParseMode.MARKDOWN)
                    .message(s)
                    .build());
        }

        /**
         * Builder di una stringa di linguaggio ad alto livello per mostrare all'utente come
         * immettere correttamente i valori
         *
         * @param d trainingset sul quale creare il prototipo
         * @return stringa leggibile e formattata con markdown
         */
        private String exampleFormatBuilder(Data d){
            return "Per procedere scrivere l'example in formato: \n`"+
                    d.explanatorySetStringBuilder() +
                    "`\n`[C]`: Attributo continuo\n`[D]`: Attributo discreto\n`[K]`: Example target" +
                    "\nOppure caricare un nuovo training set utilizzando gli appositi comandi.";
        }
    }
}



