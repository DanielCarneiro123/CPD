public class Board {

    private final int boardSize;
    private int board[][];

    public Board(int boardSize) {
        this.boardSize = boardSize;
        board = new int[boardSize][boardSize];
        /*
        * 0: empty
        * 1: white
        * 2: black
        */
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = 0;
            }
        }
    }

    public int getBoardSize() {
        return boardSize;
    }

    public int getPieceAt(int x, int y) {
        return board[y][x];
    }

    public void setPieceAt(int x, int y, int piece) {
        board[y][x] = piece;
    }

    public void removePieceAt(int x, int y) {
        board[y][x] = 0;
    }

    public boolean hasWhitePiece(int x, int y) {
        return board[y][x] == 1;
    }

    public String getBoardDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current board:\n");
        for (int i = boardSize - 1; i >= 0; i--) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] == 1) {
                    sb.append("W ");
                } else if (board[i][j] == 2) {
                    sb.append("B ");
                } else {
                    sb.append(": ");
                }
            }
            sb.append("|" + (char)('1' + i) + "\n");
        }
        for (int i = 0; i < boardSize; i++) {
            sb.append("--");
        }
        sb.append("\n");
        for (int i = 0; i < boardSize; i++) {
            sb.append((char)('a' + i) + " ");
        }
        return sb.toString();
    }
}
