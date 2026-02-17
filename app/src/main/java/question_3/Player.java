package question_3;

// represents a player thread
public class Player implements Runnable {

    private Board board; // shared board object
    private int id; // player id (1 or 2)

    public Player(Board board, int id) {
        this.board = board;
        this.id = id;
    }

    @Override
    public void run() {

        // keep playing until interrupted
        while (true) {

            // attempt to play a turn
            board.playTurn(id);

            // if thread was interrupted externally, stop loop
            if (Thread.currentThread().isInterrupted())
                break;

            try {
                // small pause to simulate thinking time
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // preserve interrupt status
            }
        }
    }
}
