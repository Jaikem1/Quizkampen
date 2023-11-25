
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class User extends JFrame implements ActionListener {

    private final ImageIcon buttonIconSelect = new ImageIcon("src/Resources/purple_love.png");
    private final ImageIcon buttonIconWrong = new ImageIcon("src/Resources/red_love.png");
    private final ImageIcon buttonIconRight = new ImageIcon("src/Resources/green_love.png");
    private final ImageIcon starsLeft = new ImageIcon("src/Resources/Three stars left.png");
    private final ImageIcon starsRight = new ImageIcon("src/Resources/Three stars right.png");

    Color backgroundColor = new Color(106, 90, 205);
    Color messageColor = new Color(72, 61, 139);
    JPanel buttonBoard = new JPanel(new GridLayout(2, 2));
    JPanel categoryBoard = new JPanel(new GridLayout(1,3));
    JLabel left = new JLabel();
    JLabel right = new JLabel();
    JLabel text = new JLabel("Frågan som ställs står här");
    JLabel category = new JLabel("Welcome");
    JButton a = new JButton("alt 1");
    JButton b = new JButton("alt 2");
    JButton c = new JButton("alt 3");
    JButton d = new JButton("alt 4");
    PrintWriter out;
    ObjectInputStream in;
    Question question;
    Boolean questionMode = false;
    ArrayList<JButton> buttons = new ArrayList<>();


    public User() {

        setTitle("Quiz Game");
        getContentPane().setBackground(backgroundColor);
        categoryBoard.setBackground(backgroundColor);
        category.setFont(new Font(Font.DIALOG, Font.BOLD,14));

        left.setIcon(starsLeft);
        right.setIcon(starsRight);
        left.setHorizontalAlignment(SwingConstants.CENTER);
        category.setHorizontalAlignment(SwingConstants.CENTER);
        right.setHorizontalAlignment(SwingConstants.CENTER);
        categoryBoard.add(left);
        categoryBoard.add(category);
        categoryBoard.add(right);
        add(categoryBoard, BorderLayout.NORTH);

        add(text);
        text.setHorizontalAlignment(SwingConstants.CENTER);
        text.setForeground(Color.WHITE);

        buttons.add(a);
        buttons.add(b);
        buttons.add(c);
        buttons.add(d);

        add(buttonBoard, BorderLayout.SOUTH);

        for (JButton button : buttons) {
            button.addActionListener(this);
            buttonBoard.add(button);
        }

        designButtons();
        hideButtons();

        setSize(500, 350);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        String message;
        Question question;

        try {
            Socket socket = new Socket("127.0.0.1", 55565);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new ObjectInputStream(socket.getInputStream());
            Object obj;
            while ((obj = in.readObject()) != null) {
                questionMode = false;

                if (obj instanceof Question) {
                    question = (Question) obj;
                    this.question = question;
                    paintQuestion();
                    questionMode = true;
                    showButtons();
                    resetButtonColors();

                } else {
                    category.setForeground(backgroundColor);// Osynlig text => Samma färg som bakgrund. Text behövs för att label ska målas ut korrekt.
                    message = (String) obj;
                    if (message.startsWith("MESSAGE")) {
                        text.setText(message.substring(8));
                    } else if (message.startsWith("DISABLE")) {
                        hideButtons();
                    } else if (message.startsWith("<html>MESSAGE")) {
                        hideButtons();
                        text.setText("<html>" + message.substring(14));
                    } else if (message.startsWith("CATEGORY")) {
                        category.setText(message.substring(8).toUpperCase());
                        category.setForeground(Color.ORANGE);
                    } else {
                        String[] categories = message.split(" ");
                        resetButtonColors();
                        showButtons();
                        a.setText(categories[0]);
                        b.setText(categories[1]);
                        c.setText(categories[2]);
                        d.setText(categories[3]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {

        JButton button = (JButton) e.getSource();
        out.println(e.getActionCommand());

        if (questionMode) {
            if (e.getActionCommand().equals(question.getAnswer())) {
                button.setIcon(buttonIconRight);
            } else {
                button.setIcon(buttonIconWrong);
            }
        }
    }

    public void paintQuestion() {
        text.setText(question.getQuestion());
        a.setText(question.getAlternatives().get(0));
        b.setText(question.getAlternatives().get(1));
        c.setText(question.getAlternatives().get(2));
        d.setText(question.getAlternatives().get(3));
    }

    public void resetButtonColors() {
        for (JButton button : buttons) {
            button.setIcon(buttonIconSelect);
        }
    }

    public void hideButtons() {
        for (JButton button : buttons) {
            button.setVisible(false);
        }
    }

    public void showButtons() {
        for (JButton button : buttons) {
            button.setVisible(true);
        }
    }

    public void designButtons() {

        for (JButton button : buttons) {
            //centrerar texten
            button.setIcon(buttonIconSelect);
            button.setHorizontalTextPosition(JButton.CENTER);
            //hanterar färgen
            button.setForeground(Color.white);
            button.setBackground(backgroundColor); //utfyllnad runt knapp samma som bakgrund
            button.getParent().setBackground(backgroundColor); //Gör så det inte blir vitt mellan rundorna
            button.setFocusable(false);//Målar inte ut en ram när man står över knapp
            button.setBorderPainted(false);//Gör så det inte blir vitt mellan knapparna
            button.setContentAreaFilled(false);
            button.setOpaque(true);
        }
    }


    public static void main(String[] args) {
        User user = new User();
    }
}