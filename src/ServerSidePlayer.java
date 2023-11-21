

import java.io.*;
import java.net.Socket;
import java.util.Collections;

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

    public synchronized void endOfStateWait() throws InterruptedException {
        if (this.opponent.getState() == State.WAITING) {
            notifyAll();
        } else wait();
    }

    public void run() {

        String userAnswer;
        String pickedCategory = "";
        int rondnr = 0;


        try {

            // Quiz runda
            while (true) {


                output.writeObject(game.categories.get(0).getName() + " " + game.categories.get(1).getName());

                if (state == SELECT) {
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
                        game.categoryIsPicked = false;
                        output.writeObject("MESSAGE Other player is choosing category ");
                        output.writeObject("DISABLE");
                        state = ROUNDS;
                        game.legalMove(this);
                        while (!game.categoryIsPicked){Thread.sleep(20);}
                    }
                } else if (state == ROUNDS) {
                    while (game.getSelectedCategory() != null) {
                        output.writeObject(game.getSelectedCategory().getQuestions().get(rondnr));


                        if ((userAnswer = input.readLine()) != null) {
                            if (userAnswer.equals(game.getSelectedCategory().getQuestions().get(rondnr).getAnswer())) {
                                this.points++;

                            }
                        }
                        rondnr++;
                        if (rondnr == 2) {
                            state = ENDROUND;
                            break;
                        }
                    }
                } else if (state == ENDROUND) {
                    output.writeObject("POINTS" + "\n" + player + ": " + points + " \n" + opponent.player + ": " + opponent.getPoints());
                    Thread.sleep(2000);
                    if (!game.opponentIsWaiting) {
                        game.opponentIsWaiting = true;
                        output.writeObject("MESSAGE Waiting for opponent ");
                        while (game.waitForOpponent) {
                            Thread.sleep(20);
                        }
                    }
                    else if (game.opponentIsWaiting){
                        game.opponentIsWaiting = false;
                        game.waitForOpponent = false;
                    }
                    rondnr = 0;
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
