package Server;

import POJOs.Question;

import java.io.Serializable;
import java.util.ArrayList;

public class Category implements Serializable { //Klass för kategorier. Innehåller en lista över frågor inom den kategorin.
    String name;
    ArrayList<Question> questions;

    public Category(String name, ArrayList<Question> questions) {
        this.name = name;
        this.questions = questions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }
}
