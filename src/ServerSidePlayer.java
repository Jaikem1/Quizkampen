

import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.Properties;

class ServerSidePlayer extends Thread {
    ServerSidePlayer opponent;
    Socket socket;
    BufferedReader input;
    ObjectOutputStream output;
    ServerSideGame game;
    String player;

    int points;
    private final int SELECT = 0;
    private final int ROUNDS = 1;
    private final int ENDROUND = 2;
    private int state = SELECT;

    private int roundNumber = 0;


    public ServerSidePlayer(Socket socket, ServerSideGame game, String player) {

        this.socket = socket;
        this.game = game;
        this.player = player;

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject("MESSAGE Waiting for opponent to connect");
        } catch (IOException e) {
            System.out.println("Player died: " + e);
        }
    }

    /**
     * Accepts notification of who the opponent is.
     */
    public void setOpponent(ServerSidePlayer opponent) {
        this.opponent = opponent;
    }

    /**
     * Returns the opponent.
     */
    public ServerSidePlayer getOpponent() {
        return opponent;
    }

    public String getPoints() {
        return String.valueOf(points);
    }

    public int getRoundNumber() {return this.roundNumber;}

    public void run() {

        Properties p = new Properties();

        String userAnswer;
        String pickedCategory = "";

        int settingsQuestionsPerRound;
        int currentQuestion = 0;


        try {

            // Quiz runda
            while (true) {

                try {
                    p.load(new FileInputStream("src/Settings.properties"));
                } catch (IOException e) {
                    System.out.println("Settings filen hittades ej!");;
                }

                settingsQuestionsPerRound = Integer.parseInt(p.getProperty("questionsPerRound", "1"));

                output.writeObject(game.categories.get(0).getName() + " " + game.categories.get(1).getName());

                if (state == SELECT) {
                    currentQuestion = 0;

                    if (this.equals(game.currentPlayer)) {
                        output.writeObject("MESSAGE Select a category");
                        while ((pickedCategory = input.readLine()) != null) {
                            game.setSelectedCategory(pickedCategory);
                            Collections.shuffle(game.getSelectedCategory().getQuestions());
                            game.categoryIsPicked = true;
                            state = ROUNDS;
                            break;
                        }
                    } else {
                        output.writeObject("MESSAGE Other player is choosing category ");
                        output.writeObject("DISABLE");
                        while (!game.categoryIsPicked) {
                            Thread.sleep(100);
                        }
                        state = ROUNDS;
                    }
                } else if (state == ROUNDS) {
                    while (game.getSelectedCategory() != null) {
                        output.writeObject(game.getSelectedCategory().getQuestions().get(currentQuestion));


                        if ((userAnswer = input.readLine()) != null) {
                            if (userAnswer.equals(game.getSelectedCategory().getQuestions().get(currentQuestion).getAnswer())) {
                                this.points++;

                            }
                        }
                        currentQuestion++;
                        if (currentQuestion == settingsQuestionsPerRound) {
                            state = ENDROUND;
                            break;
                        }
                    }
                } else if (state == ENDROUND) {
                    String pointMsg = "<html>POINTS  <br>" + player + ": " + points + "<br>" +
                            opponent.player + ": " + opponent.getPoints() + "<br><br>" + getRoundNumber() + "</html>";
                    Thread.sleep(2000);
                    if (!game.opponentIsWaiting) {
                        game.switchCurrentPlayer();
                        game.opponentIsWaiting = true;
                        output.writeObject("<html>MESSAGE Waiting for opponent<br><br>"+ pointMsg + "</html>");
                        while (game.waitForOpponent) {
                            Thread.sleep(100);
                        }
                        game.waitForOpponent = true;
                    } else if (game.opponentIsWaiting) {
                        game.waitForOpponent = false;
                    }
                    game.categoryIsPicked = false;
                    game.opponentIsWaiting = false;
                    state = SELECT;
                }


                /*if (userAnswer.startsWith("MOVE")) {
                    int location = Integer.parseInt(userAnswer.substring(5));
                    if (game.legalMove(location, this)) {
                        output.println("VALID_MOVE");
                        output.println(game.hasWinner() ? "VICTORY"
                                : game.boardFilledUp() ? "TIE"
                                : "");
                    } else {
                        output.println("MESSAGE ?");
                    }
                } else if (userAnswer.startsWith("QUIT")) {
                    return;
                }*/
            }

        } catch (
                IOException e) {
            System.out.println("Player died: " + e);
        } catch (
                InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
