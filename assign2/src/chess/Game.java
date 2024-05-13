package chess;

import java.util.Scanner;

public class Game {
    private static final int BOARD_SIZE = 8;

    private Scanner scanner = new Scanner(System.in);

    private Board board;
    private boolean isWhiteTurn;
    private boolean isGameOver;

    public Game() {
        board = new Board(BOARD_SIZE);
        isWhiteTurn = true;
        isGameOver = false;
    }

    public void start() {
        for (int i = 0; i < board.getBoardSize(); i++) {
            board.setPieceAt(i, 1, 1);
            board.setPieceAt(i, board.getBoardSize() - 2, 2);
        }
        gameloop();
    }

    private boolean validateAndExecuteMove(String move) {
        if (move.length() == 4 && move.charAt(1) == 'x') {
            int endX = move.charAt(2) - 'a';
            int endY = move.charAt(3) - '1';
            int startX = move.charAt(0) - 'a';
            int startY = isWhiteTurn ? endY - 1 : endY + 1;

            if (endX < 0 || endX >= board.getBoardSize() || endY < 0 || endY >= board.getBoardSize() || startX < 0
                    || startX >= board.getBoardSize() || startY < 0 || startY >= board.getBoardSize()) {
                return false;
            }

            if (board.getPieceAt(startX, startY) != 0 && board.hasWhitePiece(startX, startY) == isWhiteTurn
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

            if (endX < 0 || endX >= board.getBoardSize() || endY < 0 || endY >= board.getBoardSize() || startX < 0
                    || startX >= board.getBoardSize() || startY < 0 || startY >= board.getBoardSize()) {
                return false;
            }

            if (board.getPieceAt(startX, startY) != 0 && board.hasWhitePiece(startX, startY) == isWhiteTurn
                    && board.getPieceAt(endX, endY) == 0) {
                board.setPieceAt(endX, endY, board.getPieceAt(startX, startY));
                board.removePieceAt(startX, startY);
            } else {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean isGameOver() {
        for (int j = 0; j < board.getBoardSize(); j++) {
            if (board.getPieceAt(j, 0) != 0 || board.getPieceAt(j, board.getBoardSize() - 1) != 0) {
                return true;
            }
        }

        return false;
    }

    private void gameloop() {
        while (!isGameOver) {
            board.display();

            if (isWhiteTurn) {
                System.out.println("White's turn");
            } else {
                System.out.println("Black's turn");
            }
            System.out.print("Enter the move in algebraic chess notation: ");
            String move = scanner.nextLine();

            if (!validateAndExecuteMove(move)) {
                System.out.println("Invalid move. Try again.");
                continue;
            }

            if (isGameOver()) {
                isGameOver = true;
                if (isWhiteTurn) {
                    System.out.println("White wins!");
                } else {
                    System.out.println("Black wins!");
                }
            }

            isWhiteTurn = !isWhiteTurn;
        }

    }
}
