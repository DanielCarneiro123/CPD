public class Game {
    private static final int BOARD_SIZE = 8;

    private Board board;
    private boolean isWhiteTurn;

    public Game() {
        board = new Board(BOARD_SIZE);
        for (int i = 0; i < board.getBoardSize(); i++) {
            board.setPieceAt(i, 1, 1);
            board.setPieceAt(i, board.getBoardSize() - 2, 2);
        }

        isWhiteTurn = true;
    }

    public boolean validateAndExecuteMove(String move) {
        if (move.length() == 4 && move.charAt(1) == 'x') {
            int endX = move.charAt(2) - 'a';
            int endY = move.charAt(3) - '1';
            int startX = move.charAt(0) - 'a';
            int startY = isWhiteTurn ? endY - 1 : endY + 1;

            if (endX < 0
                    || endX >= board.getBoardSize()
                    || endY < 0
                    || endY >= board.getBoardSize()
                    || startX < 0
                    || startX >= board.getBoardSize()
                    || startY < 0
                    || startY >= board.getBoardSize()) {
                return false;
            }

            if (board.getPieceAt(startX, startY) != 0
                    && board.hasWhitePiece(startX, startY) == isWhiteTurn
                    && board.hasWhitePiece(endX, endY) != isWhiteTurn) {
                board.setPieceAt(endX, endY, board.getPieceAt(startX, startY));
                board.removePieceAt(startX, startY);
            } else {
                return false;
            }

        } else if (move.length() == 2) {
            int endX = move.charAt(0) - 'a';
            int endY = move.charAt(1) - '1';
            int startX = endX;
            int startY = isWhiteTurn ? endY - 1 : endY + 1;
            int altStartY = isWhiteTurn ? endY - 2 : endY + 2;

            if (endX < 0
                    || endX >= board.getBoardSize()
                    || endY < 0
                    || endY >= board.getBoardSize()
                    || startX < 0
                    || startX >= board.getBoardSize()
                    || startY < 0
                    || startY >= board.getBoardSize()) {
                return false;
            }

            if (board.getPieceAt(startX, startY) != 0
                    && board.hasWhitePiece(startX, startY) == isWhiteTurn
                    && board.getPieceAt(endX, endY) == 0) {
                board.setPieceAt(endX, endY, board.getPieceAt(startX, startY));
                board.removePieceAt(startX, startY);
            } else if (((endY == 3 && isWhiteTurn) || (endY == BOARD_SIZE - 4 && !isWhiteTurn))
                    && board.getPieceAt(startX, altStartY) != 0
                    && board.hasWhitePiece(startX, altStartY) == isWhiteTurn
                    && board.getPieceAt(startX, startY) == 0
                    && board.getPieceAt(endX, endY) == 0) {

                board.setPieceAt(endX, endY, board.getPieceAt(startX, altStartY));
                board.removePieceAt(startX, altStartY);
            } else {
                return false;
            }
        } else {
            return false;
        }

        isWhiteTurn = !isWhiteTurn;

        return true;
    }

    public String getBoardDisplay() {
        return board.getBoardDisplay();
    }

    public boolean isGameOver() {
        for (int j = 0; j < board.getBoardSize(); j++) {
            if (board.getPieceAt(j, 0) != 0 || board.getPieceAt(j, board.getBoardSize() - 1) != 0) {
                return true;
            }
        }

        return false;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }
}