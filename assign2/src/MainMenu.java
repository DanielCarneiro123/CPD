
class MainMenu {
    
    Authentication auth = new Authentication();

    public Integer MainMenu() {
        System.out.println("Welcome to ChessCHAMP!");
        System.out.println("");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.println("Please select an option: ");

        Scanner scanner = new Scanner(System.in);
        Integer option = scanner.nextInt();

        switch (option) {
            case 1:
                new LoginMenu();
                return option;
                break;
            case 2:
                new RegisterMenu();
                return option;
                break;
            case 3:
                System.exit(0);
                return -1;
                break;
            default:
                System.out.println("Invalid option");
                break;
        }
        return -1;
    }

    

    public void LoginMenu() {
        
        System.out.println("Enter your username: ");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        System.out.println("Enter your password: ");
        String password = scanner.nextLine();
        Player player = auth.login(username, password);
        if (player == null) {
            System.out.println("Invalid username or password");
            new MainMenu();
        } else {
            System.out.println("Welcome " + player.getUsername());
            System.out.println("Your elo is: " + player.getElo());
            System.out.println("1. Play");
            System.out.println("2. Logout");
            System.out.println("Please select an option: ");
            int option = scanner.nextInt();
            switch (option) {
                case 1:
                    new GameMenu(player);
                    break;
                case 2:
                    auth.logout(player);
                    new MainMenu();
                    break;
                default:
                    System.out.println("Invalid option");
                    break;
            }
        }
    }

    public  void RegisterMenu() {
        System.out.println("Enter your username: ");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        System.out.println("Enter your password: ");
        String password = scanner.nextLine();
        if (!auth.register(username, password)) {
            System.out.println("Username already taken");
            new MainMenu();
        } else {
            System.out.println("Welcome " + player.getUsername());
            System.out.println("Your elo is: " + player.getElo());
            System.out.println("1. Play");
            System.out.println("2. Logout");
            System.out.println("Please select an option: ");
            int option = scanner.nextInt();
            switch (option) {
                case 1:
                    new GameMenu(player);
                    break;
                case 2:
                    auth.logout(player);
                    new MainMenu();
                    break;
                default:
                    System.out.println("Invalid option");
                    break;
            }
        }
    }

    public void reconnect(boolean Token){
        
    }
}
