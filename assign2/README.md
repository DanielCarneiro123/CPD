# Assignment 2: Game server

Group members:

1. Daniel Gago (up202108791@up.pt)
2. Daniel Moreira (up202108832@up.pt)
3. José Santos (up202108729@up.pt)

## Compilation and execution

Run the following command from the src directory to compile the source code:

```bash
javac *.java
```

Then, you can run the server with the following command:

```bash
java GameServer <port> <mode>
```

Where `<port>` is the port number where the server will be listening for incoming connections and `<mode>` is the mode of the server. The server can run in two modes: `simple` and `ranked`. In `simple` mode, the server will ignore the player's elo and will not update the leaderboard. In `ranked` mode, the server will use matchmaking to pair players with similar elo and will update the leaderboard after each game.

To run a client instance you can use the following command:

```bash
java GameClient <host> <port>
```

Where `<host>` is the IP address of the server (localhost if running locally) and `<port>` is the port number where the server is listening for incoming connections.

## Authentication and registration

When a client connects to the server, the server will ask the client to authenticate, register, reconnect, or exit.

```bash
Choose an option:
1. Login
2. Register
3. Reconnect
4. Exit
Enter choice: 
```

For testing purposes, there are 4 users already registered with the names `player1`, `player2`, `player3` and `player4`. The password for all users is `1234`.

### Login

If the client chooses to login, the server will ask for the username and password. If the credentials are correct, the client will be logged in and will be able to play the game.

```bash
Enter choice: 1
Enter your username:
player1
Enter your password:
1234
Authentication successful.
Waiting for the game to start...
```

### Register

If the client chooses to register, the server will ask for the username and password. If the username is not already taken, the client will be registered and will be able to play the game.

```bash
Enter choice: 2 
Enter your desired username:
daniel 
Enter your desired password:
1234
```

### Reconnect

If the client disconnects after loggin in, he can choose the option to reconnect. If the session occurs in the same server, the client will be able to return to its position in the waiting queue by writing the username when the server asks. If the token does not exist, the client session will be considered expired.

```bash 
Enter choice: 3
Enter your username:
daniel
Reconnected
```


## Playing the game

The game is a game of Pawn Chess. The game is played on a 8x8 board and follows normal chess rules apart from the fact that there are only pawns on the board. The goal of the game is to get a pawn to the other side of the board. The first player to do so wins the game. En passant does not exist in this version of the game.

The player must insert the move in algebraic notation. For example, to move a pawn from a2 to a4, the player must insert `a4`. To capture an enemy pawn on b4 with a pawn on c3, the player must insert `cxb4`.

```bash
Server: Current board:
Server: : : : : : : : : |8
Server: B B B : B B B B |7
Server: : : : : : : : : |6
Server: : : : B : : : : |5
Server: : : : : W : : : |4
Server: : : : : : : : : |3
Server: W W W W : W W W |2
Server: : : : : : : : : |1
Server: ----------------
Server: a b c d e f g h
Server: Your turn!
Server: Make your move in algebraic chess notation (e.g. a2, cxd5):
exd5
```
