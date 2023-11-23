

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private int roundPoints = 0;
    private List<String> roundScores = new ArrayList<>();
    private StringBuilder pointsMessage = new StringBuilder();


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
     * Accepts notification of whom the opponent is.
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


    public synchronized void setPointsMessage(StringBuilder pointsMessage) {
        this.pointsMessage = new StringBuilder(pointsMessage);
    }

    public StringBuilder getPointsMessage() {
        return pointsMessage;
    }

    public int getRoundNumber() {return this.roundNumber;}

    public void run() {

        Properties p = new Properties();

        String userAnswer;
        String pickedCategory = "";

        int settingsQuestionsPerRound;
        int settingsNumberOfRounds;
        int currentQuestion = 0;
        boolean pointsMessageSent = false;


        try {
            p.load(new FileInputStream("src/Settings.properties"));
        } catch (IOException e) {
            System.out.println("Settings filen hittades ej!");;
        }

        settingsQuestionsPerRound = Integer.parseInt(p.getProperty("questionsPerRound", "1"));
        settingsNumberOfRounds = Integer.parseInt(p.getProperty("rounds", "3"));

        for(int i = 1; i <= settingsNumberOfRounds; i++) { roundScores.add("-"); }


        try {

            // Quiz runda
            while (true) {

                if (state == SELECT) {
                    currentQuestion = 0;
                    Collections.shuffle(game.categories);
                    output.writeObject(game.categories.get(0).getName() + " " + game.categories.get(1).getName());

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
                    output.writeObject("CATEGORY" + game.getSelectedCategory().getName());
                    while (game.getSelectedCategory() != null) {
                        Collections.shuffle(game.getSelectedCategory().getQuestions().get(currentQuestion).getAlternatives());
                        output.writeObject(game.getSelectedCategory().getQuestions().get(currentQuestion));


                        if ((userAnswer = input.readLine()) != null) {
                            if (userAnswer.equals(game.getSelectedCategory().getQuestions().get(currentQuestion).getAnswer())) {
                                this.points++;
                                this.roundPoints++;
                            }
                        }
                        Thread.sleep(500);
                        currentQuestion++;
                        if (currentQuestion == settingsQuestionsPerRound) {
                            state = ENDROUND;
                            break;
                        }
                    }
                } else if (state == ENDROUND) {
                    setPointsMessage(pointsMessage.delete(0, pointsMessage.length()));
                    opponent.setPointsMessage(opponent.pointsMessage.delete(0,opponent.pointsMessage.length()));
                    this.roundScores.set(roundNumber, String.valueOf(roundPoints));
                    this.roundNumber++;
                    this.roundPoints = 0;
                    for (int i = 0; i < settingsNumberOfRounds; i++) {
                        this.pointsMessage.append("<tr><td>").append(roundScores.get(i)).append("</td><td> Round ").append(i+1)
                                .append("</td><td>").append(opponent.roundScores.get(i)).append("</td>");
                    }
                    Thread.sleep(1000);
                    if (!game.opponentIsWaiting) {
                        game.switchCurrentPlayer();
                        game.opponentIsWaiting = true;
                        output.writeObject("<html>MESSAGE Waiting for opponent<br><br>" + "Points<br>"
                                            + points + " - " + opponent.points + "<br><br><table border=\"0\">"+ getPointsMessage()
                                            + "</table> </html>");
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
