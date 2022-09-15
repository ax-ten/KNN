package telegrambot;

import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

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
        public void onTextMessageReceived(TextMessageReceivedEvent event) {
            event.getChat().sendMessage(SendableTextMessage.builder().message("You sent me a text based message!").replyTo(event.getMessage()).build());
        }
    }
}



