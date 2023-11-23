import java.io.*;
import java.util.ArrayList;

public class GameLoader {


    public ArrayList<Category> loadGame() {
        ArrayList<Category> categories = new ArrayList<>();

        String[] categoryNames = {"Film", "Geografi", "Musik", "Vetenskap", "Sport"};
        for (String categoryName : categoryNames) {
            Category category = readCategoryFromFile("src/QuestionFiles/" + categoryName);
            categories.add(category);
        }
        return categories;
    }

    private Category readCategoryFromFile(String fileName) {

        ArrayList<Question> questions = new ArrayList<>();
        String categoryName = new File(fileName).getName();
        Category category = new Category(categoryName, questions);

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(", ");

                if (parts.length >= 5) {
                    String questionText = parts[0];
                    String answer = parts[1];
                    String wrongAnswer1 = parts[2];
                    String wrongAnswer2 = parts[3];
                    String wrongAnswer3 = parts[4];

                    Question question = new Question(questionText, answer, wrongAnswer1, wrongAnswer2, wrongAnswer3);
                    category.addQuestion(question);
                } else {
                    System.out.println("Felaktig rad i filen: " + fileName);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return category;
    }
}
