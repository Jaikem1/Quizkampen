

import java.util.ArrayList;

class ServerSideGame {

    ArrayList<Category> categories = new ArrayList<>();
    Category selectedCategory;
    int points = 0;
    int oponentpoints = 0;


    public ServerSideGame(ArrayList<Category> categories) {
        this.categories = categories;

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
