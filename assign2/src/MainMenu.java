import javax.swing.*;
import java.awt.event.*;

class MainMenu extends JFrame {
    JButton loginButton, registerButton, quitButton;

    public MainMenu() {
        setTitle("Chess Game");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        quitButton = new JButton("Quit");

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                LoginMenu loginMenu = new LoginMenu();
                loginMenu.show();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                RegisterMenu registerMenu = new RegisterMenu();
                registerMenu.show();
            }
        });

        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JPanel panel = new JPanel();
        panel.add(loginButton);
        panel.add(registerButton);
        panel.add(quitButton);

        add(panel);
    }
}

class LoginMenu extends JFrame {
    JButton loginButton, backButton;

    public LoginMenu() {
        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loginButton = new JButton("Login");
        backButton = new JButton("Back");

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Adicione aqui a lógica para o login
                // Por enquanto, vamos apenas fechar a janela de login e ir para o tabuleiro
                dispose();
                openBoard();
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                MainMenu mainMenu = new MainMenu();
                mainMenu.show();
            }
        });

        JPanel panel = new JPanel();
        panel.add(loginButton);
        panel.add(backButton);

        add(panel);
    }

    private void openBoard() {
        ChessBoard board = new ChessBoard();
        board.show();
    }
}

// Classe para o menu de registro
class RegisterMenu extends JFrame {
    JButton registerButton, backButton;

    public RegisterMenu() {
        setTitle("Register");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        registerButton = new JButton("Register");
        backButton = new JButton("Back");

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Adicione aqui a lógica para o registro
                // Por enquanto, vamos apenas fechar a janela de registro e ir para o tabuleiro
                dispose();
                openBoard();
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                MainMenu mainMenu = new MainMenu();
                mainMenu.show();
            }
        });

        JPanel panel = new JPanel();
        panel.add(registerButton);
        panel.add(backButton);

        add(panel);
    }

}
