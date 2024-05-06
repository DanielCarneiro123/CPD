import javax.swing.*;
import java.awt.event.*;

// Classe para o menu inicial
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
