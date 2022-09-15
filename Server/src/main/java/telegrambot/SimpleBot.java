package telegrambot;

import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;

public class SimpleBot {
    public SimpleBot(){
        TelegramBot tb = TelegramBot.login("5626538714:AAGD9-PIbiYA7eJcRI2ecI5vP4y2IDk3NEk");
        //The API key was invalid, an error will have also been printed into the console.
        if(tb == null) System.exit(-1);

        tb.getEventsManager().register(new MyListener());

        //This will tell the API to start polling the servers for updates
        //If you specify true as the argument you will receive any previous messages before the bot started.
        //If you specify false the API will discard any messages from before the bot was started.
        tb.startUpdates(false);
        System.out.println("Bot Started");

        //Thread would die, do something to keep it alive.
    }

    //Listener class
    private class MyListener implements Listener {

        public void onTextMessageReceived(TextMessageReceivedEvent event) {

            event.getChat().sendMessage(SendableTextMessage.builder().message("You sent me a text based message!").replyTo(event.getMessage()).build());
        }
    }
}



