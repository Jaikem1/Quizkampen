
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class User extends JFrame implements ActionListener {
    JPanel buttonBoard = new JPanel(new GridLayout(2, 2, 2, 2));
    JPanel categoryBoard = new JPanel();
    JLabel text = new JLabel("Frågan som ställs står här");
    JLabel category = new JLabel("Kategori");
    JButton a = new JButton("alt 1");
    JButton b = new JButton("alt 2");
    JButton c = new JButton("alt 3");
    JButton d = new JButton("alt 4");
    PrintWriter out;
    ObjectInputStream in;
    Question question;
    Boolean questionMode = false;
    Color normal = a.getBackground();
    ArrayList<JButton> buttons = new ArrayList<>();


    public User() {

        setTitle("Quiz Game");

        categoryBoard.add(category);
        add(categoryBoard, BorderLayout.NORTH);
        categoryBoard.setBackground(Color.ORANGE);

        add(text);
        text.setHorizontalAlignment(SwingConstants.CENTER);

        buttons.add(a);
        buttons.add(b);
        buttons.add(c);
        buttons.add(d);

        for (JButton button : buttons) {
            buttonBoard.add(button);
        }

        add(buttonBoard, BorderLayout.SOUTH);

        for (JButton button : buttons) {
            button.addActionListener(this);
        }


        setSize(400, 350);
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

                if (obj instanceof Question) {
                    question = (Question) obj;
                    this.question = question;
                    paintQuestion();
                    questionMode = true;
                    enableButtons();
                    resetButtonColors();

                } else {
                    message = (String) obj;
                    if (message.startsWith("MESSAGE")) {
                        text.setText(message);
                    } else if (message.startsWith("DISABLE")) {
                        disableButtons();
                    } else if (message.startsWith("<html>POINTS")) {
                        hideButtons();
                        text.setText(message);
                    } else {
                        String[] categories = message.split(" ");
                        resetButtonColors();
                        showButtons();
                        a.setText(categories[0]);
                        b.setText(categories[1]);
                        c.setText("");
                        d.setText("");

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {

        JButton button = (JButton) e.getSource();
        button.setOpaque(true);

        out.println(e.getActionCommand());
        if (questionMode) {
            if (e.getActionCommand().equals(question.getAnswer())) {
                button.setBackground(Color.GREEN);

            } else {
                button.setBackground(Color.PINK);
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
            button.setBackground(normal);
        }
    }

    public void enableButtons() {
        for (JButton button : buttons) {
            button.setEnabled(true);
        }
    }

    public void disableButtons() {
        for (JButton button : buttons) {
            button.setEnabled(false);
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


    public static void main(String[] args) {
        User user = new User();
    }
}
