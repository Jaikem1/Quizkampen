package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ServerListener {   //Lyssnar efter klienter och kopplar klient mot server

    public static void main(String[] args) {
        GameLoader gameLoader = new GameLoader();   //Läser in kategorier/frågor från filer
        ArrayList<Category> categories = gameLoader.loadGame(); //Lägger kategorier/frågor i en arraylist


        while (true) {
            ServerSideGame game = new ServerSideGame(categories); //Startar upp spel och skickar in kategorier/frågor

            try (ServerSocket serverSocket = new ServerSocket(55565)) { //matchar två klienter som player 1 och 2 och startar spelarnas trådar.

                ServerSidePlayer player1 = new ServerSidePlayer(serverSocket.accept(), game, "player1");
                ServerSidePlayer player2 = new ServerSidePlayer(serverSocket.accept(), game, "player2");

                player1.setOpponent(player2);
                player2.setOpponent(player1);

                game.currentPlayer = player1;
                player1.start();
                player2.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
