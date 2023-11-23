
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ServerListener {

    public static void main(String[] args) {
        GameLoader gameLoader = new GameLoader();
        ArrayList<Category> categories = gameLoader.loadGame();


        while (true) {
            ServerSideGame game = new ServerSideGame(categories);

            try (ServerSocket serverSocket = new ServerSocket(55565)) {

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
