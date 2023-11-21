import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ServerSidePlayer extends Thread {
    ServerSidePlayer opponent;
    Socket socket;
    BufferedReader input;
    ObjectOutputStream output;
    ServerSideGame game;
    String player;

    int points;
    private int roundPoints = 0;
    private int roundNumber = 0;
    private List<Integer> allRoundPoints = new ArrayList<>();


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

    public String getRoundPoints() {
        return String.valueOf(this.roundPoints);
    }

    public List<Integer> getAllRoundPoints() {
        return this.allRoundPoints;
    }

    public int getRoundNumber() {return this.roundNumber;}

    public void run() {
        roundNumber = allRoundPoints.size() + 1;
        String userAnswer;
        String pickedCategory = "";
        int rondnr = 0;


        try {

            // Quiz runda
            while (true) {
                this.roundNumber = allRoundPoints.size() + 1;
                output.writeObject(game.categories.get(0).getName() + " " + game.categories.get(1).getName());

                if (this.equals(game.currentPlayer)) {
                    output.writeObject("MESSAGE Select a category");
                    while ((pickedCategory = input.readLine()) != null) {
                        game.setSelectedCategory(pickedCategory);
                        Collections.shuffle(game.getSelectedCategory().getQuestions());
                        break;
                    }
                } else {
                    output.writeObject("MESSAGE Other player is choosing category ");
                    output.writeObject("DISABLE");
                }
                while (game.getSelectedCategory() != null) {
                    output.writeObject(game.getSelectedCategory().getQuestions().get(rondnr));



                    if ((userAnswer = input.readLine()) != null) {
                        if (userAnswer.equals(game.getSelectedCategory().getQuestions().get(rondnr).getAnswer())) {
                            this.points++;
                            this.roundPoints++;
                        }
                    }
                    game.legalMove(this);
                    rondnr++;
                    Thread.sleep(2000);
                    if (rondnr == 2) {
                        rondnr = 0;
                        this.allRoundPoints.add(this.roundPoints);
                        String pointMsg = "<html>POINTS  <br>" + player + ": " + points + "<br>" +
                                            opponent.player + ": " + opponent.getPoints() + "<br><br>" + getRoundNumber() + "</html>";
                        output.writeObject(pointMsg);
                        Thread.sleep(2000);
                        this.roundPoints = 0;
                        break;
                    }
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

        } catch (IOException e) {
            System.out.println("Player died: " + e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Encountered following error: " + e);
            }
        }
    }
}
