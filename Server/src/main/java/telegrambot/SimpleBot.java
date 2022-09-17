package telegrambot;

import data.Data;
import mining.KNN;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;
import utility.DataUtility;
import utility.KNNUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

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
    private class MyListener implements Listener {

        final String LOCALPATH = "src/main/Testfile/";
        final String TXTEXT = ".dat";
        final String BINEXT = ".dmp";

        @Override
        public void onCommandMessageReceived(CommandMessageReceivedEvent event) {
            String filename ="";
            try {
                filename = event.getArgs()[0];
            } catch (NullPointerException e){
                e.printStackTrace();
            }
            Data trainingSet;
            KNN knn = null;
            try {
                switch (event.getCommand().toLowerCase(Locale.ROOT)) {
                    case "loadknnfromfile":
                        trainingSet = DataUtility.getTrainingSetFromDat(addMissingExtention(filename, TXTEXT));
                        reply(event,trainingSet.toString());
                        new KNN(trainingSet).salva(LOCALPATH + filename + BINEXT); //Save KNN to binary
                        break;
                    case "loadknnfrombinary":
                        knn = KNNUtility.loadKNNFromBin(addMissingExtention(filename, BINEXT));
                        reply(event,knn.toString());
                        break;
                    case "loadknnfromdb":
                        trainingSet = DataUtility.getTrainingSetFromDB(filename);
                        reply(event,trainingSet.toString());
                        new KNN(trainingSet).salva(LOCALPATH+filename+"DB"+BINEXT); //Save KNN to binary
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void reply(TextMessageReceivedEvent e, String s){
            e.getChat().sendMessage(SendableTextMessage.builder().
                    message(s).replyTo(e.getMessage()).build());
        }
    }
}



