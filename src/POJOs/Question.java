package POJOs;

import java.io.Serializable;
import java.util.ArrayList;

public class Question implements Serializable { //Klass för frågor. Innehåller en lista över svarsalternativ med ett korrekt svar och tre felaktiga.

    String question;
    ArrayList<String> alternatives = new ArrayList<>();
    String answer;

    public Question(String question, String answer, String wrongAnswer1, String wrongAnswer2, String wrongAnswer3) {
        this.question = question;
        this.answer = answer;
        alternatives.add(answer);
        alternatives.add(wrongAnswer1);
        alternatives.add(wrongAnswer2);
        alternatives.add(wrongAnswer3);
    }

    public String getQuestion() {
        return question;
    }

    public ArrayList<String> getAlternatives() {
        return alternatives;
    }


    public String getAnswer() {
        return answer;
    }

}
