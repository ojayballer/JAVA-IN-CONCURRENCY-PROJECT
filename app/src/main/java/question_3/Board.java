package question_3;

// represents the shared game board
public class Board {

    public static void main(String[] args) {

        Board board = new Board(); // create shared board

        // create two player threads
        Thread player1 = new Thread(new Player(board, 1));
        Thread player2 = new Thread(new Player(board, 2));

        // start both threads
        player1.start();
        player2.start();
    }

    private int[] pits = new int[12]; // 12 pits total
    private int score1 = 0; // score for player 1
    private int score2 = 0; // score for player 2

    private int currentPlayer = 1; // track whose turn it is
    private boolean gameOver = false; // track game state

    public Board() {
        for (int i = 0; i < 12; i++) {
            pits[i] = 4;
        }
    }

    public synchronized void playTurn(int player) {

        // if not your turn, wait
        while (player != currentPlayer && !gameOver) {
            try {
                wait(); // release monitor and sleep
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // if game ended while waiting, exit
        if (gameOver)
            return;

        // choose first valid move
        int pit = chooseMove(player);

        // if no move available, end game
        if (pit == -1) {
            gameOver = true;
            notifyAll(); // wake other thread
            return;
        }

        // perform move logic
        makeMove(player, pit);

        // show updated board
        printBoard();

        // check if game finished
        if (isGameOver()) {
            collectRemaining();
            gameOver = true;
            notifyAll();
            return;
        }

        // switch turn
        currentPlayer = (currentPlayer == 1) ? 2 : 1;

        notifyAll(); // wake waiting thread
    }

    // choose first non-empty pit from player's side
    private int chooseMove(int player) {

        int start = (player == 1) ? 0 : 6;
        int end = (player == 1) ? 5 : 11;

        for (int i = start; i <= end; i++) {
            if (pits[i] > 0)
                return i;
        }

        return -1; // no valid move
    }

    private void makeMove(int player, int pit) {

        int seeds = pits[pit];
        pits[pit] = 0; // empty selected pit
        int index = pit;

        // SOW
        while (seeds > 0) {

            index = (index + 1) % 12; // wrap around

            if (index == pit)
                continue; // skip original pit

            pits[index]++;
            seeds--;
        }

        // CAPTURE
        while (isOpponentPit(player, index) &&
                (pits[index] == 2 || pits[index] == 3)) {

            if (player == 1)
                score1 += pits[index];
            else
                score2 += pits[index];

            pits[index] = 0; // remove captured seeds

            index = (index - 1 + 12) % 12; // move backward circularly
        }
    }

    // check if pit belongs to opponent
    private boolean isOpponentPit(int player, int index) {
        if (player == 1)
            return index >= 6;
        return index <= 5;
    }

    // game ends if one side is empty
    private boolean isGameOver() {

        boolean side1Empty = true;
        boolean side2Empty = true;

        for (int i = 0; i <= 5; i++)
            if (pits[i] > 0)
                side1Empty = false;

        for (int i = 6; i <= 11; i++)
            if (pits[i] > 0)
                side2Empty = false;

        return side1Empty || side2Empty;
    }

    // add remaining seeds to scores when game ends
    private void collectRemaining() {

        for (int i = 0; i <= 5; i++) {
            score1 += pits[i];
            pits[i] = 0;
        }

        for (int i = 6; i <= 11; i++) {
            score2 += pits[i];
            pits[i] = 0;
        }

        System.out.println("\nFINAL SCORES:");
        System.out.println("Player 1: " + score1);
        System.out.println("Player 2: " + score2);

        if (score1 > score2)
            System.out.println("Player 1 wins!");
        else if (score2 > score1)
            System.out.println("Player 2 wins!");
        else
            System.out.println("Draw!");
    }

    // print board state
    public void printBoard() {

        System.out.println("\nPlayer 2 side:");
        for (int i = 11; i >= 6; i--)
            System.out.print("[" + pits[i] + "] ");

        System.out.println();

        for (int i = 0; i <= 5; i++)
            System.out.print("[" + pits[i] + "] ");

        System.out.println("Player 1 side");
        System.out.println("Score P1: " + score1 + " | Score P2: " + score2);
    }
}
