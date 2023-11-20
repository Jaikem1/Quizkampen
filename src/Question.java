

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Question implements Serializable {

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
        Collections.shuffle(alternatives);
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
