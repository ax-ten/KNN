package telegrambot;

import data.Data;
import example.ExampleSizeException;
import mining.KNN;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;
import utility.DataUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static java.util.Objects.isNull;
import static utility.DataUtility.addMissingExtention;

public class SimpleBot {
    public SimpleBot(){
        Scanner s = null;
        try {
            s = new Scanner(new File("src/main/java/server/AUTHKEY.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        final String KEY = s.nextLine();

        TelegramBot tb = TelegramBot.login(KEY);

        //The API key was invalid, an error will have also been printed into the console.
        try{ convalidateAPIKey(tb);
        } catch (InvalidBotException e) {e.printStackTrace();}

        //Register listeners
        tb.getEventsManager().register(new MyListener());

        //This will tell the API to start polling the servers for updates
        //If you specify true as the argument you will receive any previous messages before the bot started.
        //If you specify false the API will discard any messages from before the bot was started.
        tb.startUpdates(false);
        System.out.println("Bot Started");

        //Thread would die, do something to keep it alive.
    }

    private void convalidateAPIKey(TelegramBot tb) throws InvalidBotException {
        if (tb == null){
            throw new InvalidBotException();
        }
    }

    //Listener class
    public static class MyListener implements Listener {
        final String LOCALPATH = "src/main/Testfile/";
        final String BINEXT = ".dmp";
        private HashMap<String, KNN> userData = new LinkedHashMap<>();
        //String: Chatid / Data: trainingset salvato

        @Override
        public void onTextMessageReceived(TextMessageReceivedEvent event) {
            if (event.getMessage().toString().charAt(0)=='/'){return;}
            String chatID = event.getChat().getId();
            KNN knn;
            Double prediction;
            if (userData.containsKey(chatID)){
                knn = userData.get(chatID);
                try {
                    prediction = knn.predict(event.getContent().getContent());
                    reply(event,"Prediction: "+prediction+ "\n"+exampleFormatBuilder(knn));
                } catch (ExampleSizeException e) {
                    e.printStackTrace();
                    reply(event,"Formato non valido.\n" +exampleFormatBuilder(knn));
                }
            } else
                event.getChat().sendMessage("Carica un trainingset per procedere.");
        }

        @Override
        public void onCommandMessageReceived(CommandMessageReceivedEvent event) {
            String filename ="", chatID = event.getChat().getId();
            try {
                filename = event.getArgs()[0];
            } catch (NullPointerException e){
                e.printStackTrace();
            }
            Data trainingSet;
            KNN knn = null;
            boolean flag =true;
            try {
                switch (event.getCommand().toLowerCase(Locale.ROOT)) {
                    case "loadknnfromfile":
                        trainingSet = DataUtility.getTrainingSetFromDat(filename);
                        knn = new KNN(trainingSet); //Save KNN to binary
                        knn.salva(LOCALPATH + filename + BINEXT);
                        reply(event,knn.getData().toString());
                        userData.put(chatID,knn);
                        break;
                    case "loadknnfromdb":
                        trainingSet = DataUtility.getTrainingSetFromDB(filename);
                        knn = new KNN(trainingSet); //Save KNN to binary
                        knn.salva(LOCALPATH + filename + BINEXT);
                        reply(event,knn.getData().toString());
                        userData.put(chatID,knn);
                        break;
                    case "loadknnfrombinary":
                        knn = DataUtility.loadKNNFromBin(filename);
                        reply(event,knn.getData().toString());
                        userData.put(chatID,knn);
                        break;
                    default:
                        reply(event,"Comando non riconosciuto");
                        flag = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // se c'è un knn salvato, (se comando riconosciuto) manda il mex [C] [D] sul knn salvato
            if (flag){
                event.getChat().sendMessage(exampleFormatBuilder(knn));
            }
        }

        private void reply(TextMessageReceivedEvent e, String s){
            e.getChat().sendMessage(SendableTextMessage.builder().
                    message(s).replyTo(e.getMessage()).build());
        }

        private String exampleFormatBuilder(KNN knn){
            return "Per procedere scrivere l'example in formato: \n"+
                    knn.getData().explanatorySetStringBuilder() +
                    "\n[C]: Attributo continuo\n[D]: Attributo discreto\n[K]: Example target" +
                    "\nOppure caricare un nuovo training set utilizzando gli appositi comandi.";
        }
    }
}



