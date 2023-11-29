package Server;

import POJOs.Category;

import java.util.ArrayList;

class ServerSideGame {  //Innehåller de metoder och variabler som båda ServerSidePlayer-trådar och klienter behöver ha tillgång till.

    ArrayList<Category> categories = new ArrayList<>();
    Category selectedCategory;
    ServerSidePlayer currentPlayer;
    public boolean waitForOpponent = true;
    public boolean opponentIsWaiting = false;
    public boolean categoryIsPicked = false;

    public ServerSideGame(ArrayList<Category> categories) {
        this.categories = categories;

    }

    public synchronized void switchCurrentPlayer() {    //byter aktuell spelare
            currentPlayer = currentPlayer.getOpponent();
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    } //returnera vald kategori

    public void setSelectedCategory(String categoryName) {  //välj kategori
        for (Category c : categories) {
            if (categoryName.equals(c.getName())) {
                selectedCategory = c;
            }
        }
    }
}
