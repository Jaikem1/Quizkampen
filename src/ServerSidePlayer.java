

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

class ServerSidePlayer extends Thread { //innehåller serversidans spellogik för varje enskild spelare
    ServerSidePlayer opponent;
    Socket socket;
    BufferedReader input;
    ObjectOutputStream output;
    ServerSideGame game;
    String player;

    int points;                         //Spelarens totalpoäng
    private final int SELECT = 0;       //Spelets olika states
    private final int ROUNDS = 1;
    private final int ENDROUND = 2;
    private final int BETWEEN = 3;
    private final int ENDGAME = 4;
    private final int LANDING = 5;
    private final int NEW = 6;
    //private int state = SELECT;     //State-markör
    private int state = LANDING;
    private int roundNumber = 0;    //spelrondens ordningsnummer
    private int roundPoints = 0;    //Spelarens poäng för spelronden
    private List<String> roundScores = new ArrayList<>();   //Sparar de individuella rondernas poäng
    private StringBuilder pointsMessage = new StringBuilder();
    private String scoreOutput;
    boolean readyToStart = false;
    boolean confirmed = false;



    public ServerSidePlayer(Socket socket, ServerSideGame game, String player) {  //constructor med in/out-streams och initialt meddelande vid uppkoppling.

        this.socket = socket;
        this.game = game;
        this.player = player;

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new ObjectOutputStream(socket.getOutputStream());

            output.writeObject("MESSAGE Väntar på att motspelare ska ansluta");
        } catch (IOException e) {
            System.out.println("Player died: " + e);
        }
    }

    public void setOpponent(ServerSidePlayer opponent) {    //kopplar ihop spelaren med annan klient som motspelare
        this.opponent = opponent;
    }

    public ServerSidePlayer getOpponent() {
        return opponent;
    }   // returnerar motspelaren

    public synchronized void setPointsMessage(StringBuilder pointsMessage) {    //sätter poängmeddelandet
        this.pointsMessage = new StringBuilder(pointsMessage);
    }

    public StringBuilder getPointsMessage() {
        return pointsMessage;
    }   //returnerar poängmeddelandet
    public void run() { //Här sker spelronderna och övrig logik

        Properties p = new Properties();    //Skapar upp properties

        String userAnswer;
        String pickedCategory = "";

        int settingsQuestionsPerRound;
        int settingsNumberOfRounds;
        int currentQuestion = 0;

        try {   //läser in properties från fil och ställer in antal ronder och antal frågor per rond
            p.load(new FileInputStream("src/Settings.properties"));
        } catch (IOException e) {
            System.out.println("Settings filen hittades ej!");
        }

        settingsQuestionsPerRound = Integer.parseInt(p.getProperty("questionsPerRound", "1"));
        settingsNumberOfRounds = Integer.parseInt(p.getProperty("rounds", "3"));

        for(int i = 1; i <= settingsNumberOfRounds; i++) { roundScores.add("-"); }



        try {

            // Logik för quizronder med olika states. Spelet sker i huvudsak i denna loop.
            while (true) {
                if (state == LANDING) {
                    output.writeObject("LOBBY Välkommen");
                    String test = input.readLine();
                    if (test.equals("Starta")) {
                        readyToStart = true;
                        if (!opponent.readyToStart) {
                            output.writeObject("MESSAGE Väntar på motspelaren");
                            output.writeObject("DISABLE");
                            while (!opponent.readyToStart) {
                                Thread.sleep(100);
                            }
                        }
                        state = SELECT;
                    } else if (test.equals("Ändra bakgrundsfärg")) {
                        output.writeObject("SETTINGS");
                    }
                } else if (state == SELECT) {  //currentPlayer väljer kategori. Opponent väntar.
                    currentQuestion = 0;

                    if (this.equals(game.currentPlayer)) {
                        Collections.shuffle(game.categories);
                        output.writeObject(game.categories.get(0).getName() + " " + game.categories.get(1).getName() +
                                " " + game.categories.get(2).getName() + " " + game.categories.get(3).getName());
                        output.writeObject("MESSAGE Välj en kategori");
                        while ((pickedCategory = input.readLine()) != null) {
                            game.setSelectedCategory(pickedCategory);
                            Collections.shuffle(game.getSelectedCategory().getQuestions());
                            game.categoryIsPicked = true;
                            state = ROUNDS;
                            break;
                        }
                    } else {
                        output.writeObject("MESSAGE Din motspelare väljer kategori ");
                        output.writeObject("DISABLE");
                        while (!game.categoryIsPicked) {
                            Thread.sleep(100);
                        }
                        state = ROUNDS;
                    }
                } else if (state == ROUNDS) {   //Spelrondens frågor och poängräkning
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
                } else if (state == ENDROUND) { //Den spelare som först spelat klart ronden väntar här tills motspelaren avslutat sin rond.
                    setPointsMessage(pointsMessage.delete(0, pointsMessage.length()));
                    this.roundScores.set(roundNumber, String.valueOf(roundPoints));
                    this.roundNumber++; //nästa rond
                    this.roundPoints = 0; //rondens poäng nollställs


                    if (!game.opponentIsWaiting) {
                        game.switchCurrentPlayer();
                        game.opponentIsWaiting = true;

                        output.writeObject("<html>MESSAGE <body style='text-align: center;'> Du fick " + "<span style='color: orange;'>" + roundScores.get(this.roundNumber-1)+ "</span> poäng den här rundan."  +
                                                "<br><br>Väntar på att motståndaren ska att avsluta sin runda.</body></html>");
                        while (game.waitForOpponent) {
                            Thread.sleep(100);
                        }
                        game.waitForOpponent = true;
                    } else if (game.opponentIsWaiting) {
                        game.waitForOpponent = false;
                    }
                    game.categoryIsPicked = false;
                    game.opponentIsWaiting = false;
                    state = BETWEEN;
                }
                else if (state == BETWEEN) { //Spelarnas poäng för ronden visas för båda innan nästa rond påbörjas
                    scoreOutput = " <table border=\"1\"><tr style='font-size: 16px;'><td style='text-align: start;'>" + this.points +
                            "</td><td style='text-align: center;'>Totalt</td><td style='text-align: end;'>"+
                            this.opponent.points + "</td></tr>";
                    for (int i = 0; i < settingsNumberOfRounds; i++) {
                        this.pointsMessage.append("<tr><td style='text-align: start;'>").append(roundScores.get(i)).append("</td><td> Rond ").append(i+1)
                                .append("</td><td style='text-align: end;'>").append(opponent.roundScores.get(i)).append("</td></html>");
                    }
                    output.writeObject("<html>MESSAGE" + scoreOutput + getPointsMessage());
                    output.writeObject("CATEGORY Poäng");

                    if (roundNumber == settingsNumberOfRounds){
                        state = ENDGAME;
                    } else {
                        Thread.sleep(5000);
                        state = SELECT;
                    }
                }
                else if (state == ENDGAME) {    //Efter sista ronden är spelad. Resultatmeddelande visas.

                    if (points > opponent.points){
                        output.writeObject("<html>MESSAGE " + scoreOutput + getPointsMessage());
                        output.writeObject("CATEGORY Du vann!");
                    }
                    else if (points == opponent.points) {
                        output.writeObject("<html>MESSAGE " + scoreOutput + getPointsMessage());
                        output.writeObject("CATEGORY Oavgjort" );
                    }
                    else{
                        output.writeObject("<html>MESSAGE " + scoreOutput + getPointsMessage());
                        output.writeObject("CATEGORY Du förlorade :(" );
                    }
                    Thread.sleep(10000);
                    state = NEW;
                } else if (state == NEW) {
                    output.writeObject("END Vill du spela igen?");
                    String selection = input.readLine();
                    if (selection.equals("Nej")) {
                        socket.close();
                        System.exit(0);
                    } else if (selection.equals("Ja")) {
                        output.writeObject("PLAY");
                    }
                }
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
                e.printStackTrace();
            }
        }
    }
}
