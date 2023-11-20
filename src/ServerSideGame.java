

import java.util.ArrayList;

class ServerSideGame {
    Question testQuestion1 = new Question("Vad heter inte Lilos mamma?", "Abuela", "Mamma gris", "Mamma Maria", "Lilos mamma");
    Question testQuestion2 = new Question("Vad heter inte Lilos pappa?", "Fernando", "Pappa gris", "Pappa Lukas", "Lilos pappa");
    ArrayList<Question> questionList1 = new ArrayList<>();

    Question testQuestion3 = new Question("Vad är 5+5", "10", "55", "15", "0");
    Question testQuestion4 = new Question("Vad är pi", "3,14", "31,4", "314", "0,314");
    ArrayList<Question> questionList2 = new ArrayList<>();
    ArrayList<Category> categories = new ArrayList<>();
    Category selectedCategory;
    int points = 0;
    int oponentpoints = 0;


    public ServerSideGame() {

        questionList1.add(testQuestion1);
        questionList1.add(testQuestion2);
        Category categoryNames = new Category("Names", questionList1);
        questionList2.add(testQuestion3);
        questionList2.add(testQuestion4);
        Category categoryMath = new Category("Math", questionList2);
        categories.add(categoryNames);
        categories.add(categoryMath);
    }

    /**
     * The current player.
     */
    ServerSidePlayer currentPlayer;


    public String getMyPoints() {
        return String.valueOf(points);
    }


    public synchronized void legalMove(ServerSidePlayer player) {
        if (player == currentPlayer) {
            currentPlayer = currentPlayer.getOpponent();

        }
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(String categoryName) {
        for (Category c : categories) {
            if (categoryName.equals(c.getName())) {
                selectedCategory = c;
            }
        }
    }
}
