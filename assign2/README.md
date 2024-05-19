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

When a client connects to the server, the server will ask the client to authenticate or register. If the client is already registered, the server will ask for their username and password.

```bash
Choose an option:
1. Login
2. Register
Enter choice: 1
Enter your username:
player1
Enter your password:
1234
```

For testing purposes, there are 4 users already registered with the names `player1`, `player2`, `player3` and `player4`. The password for all users is `1234`.

## Playing the game

The game is a simple game of heads or tails. Each game is composed of 3 rounds where the server will ask each player to choose between heads or tails.

```bash
Server: Make your guess (cara/coroa):
```

For each right guess, the player will receive 10 points and will be deducted 10 points for each wrong guess. The player with the most points at the end of the 3 rounds wins the game.

```bash
Server: Game over! Final scores:
Player player2: 20
Player player1: 10
```
