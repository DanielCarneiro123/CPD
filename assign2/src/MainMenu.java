import java.util.Scanner;
import javafx.util.Pair;


class MainMenu {
    
    Authentication auth = new Authentication();

    public Integer MainMenu() {
           
        while(true){

        System.out.println("Welcome to ChessCHAMP!");
        System.out.println("");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Reconnect")
        System.out.println("4. Exit");
        System.out.println("Please select an option: ");
        System.out.println("");

        Scanner scanner = new Scanner(System.in);
        Integer option = scanner.nextInt();

     
        switch (option) {
            case 1:
                return 1;
                break;
            case 2:
                return 2;
                break;
            case 3:
                return 3;
                break
            case 4:
                System.out.println("Goodbye!");
                return 4;
                break;
            default:
                System.out.println("Invalid option\n");
                break;
            }
        }
        return -1;
    }

    

    public Pair<String,String> LoginMenu() {
        
        System.out.println("Enter your username: ");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        System.out.println("Enter your password: ");
        String password = scanner.nextLine();
        
        return new Pair<String,String>(username, password);
    }
    

    public Pair<String,String> RegisterMenu() {
        System.out.println("Enter your username: ");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        System.out.println("Enter your password: ");
        String password = scanner.nextLine();
     
        return new Pair<String,String>(username, password);
    }
}
