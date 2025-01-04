import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.Box;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Main{
    public static void main(String args[]) {
        Frontend window = new Frontend();
        int argsSize = args.length;
        if (argsSize != 1) {
            System.out.println("Invalid parameters, run as: java ChatClient.Main [serverHostName]");
            return;
        }
        
        try (Socket client = new Socket(args[0], 8888)) {
            PrintWriter serverOut = new PrintWriter(client.getOutputStream(), true);
            BufferedReader serverResponse = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inputHandler = new InputHandler(serverOut, client, window.window);
            window.connectHandler(inputHandler);
            window.setUpGUI();

            System.out.println("Enter a display name to join the chat:");
            Scanner scanner = new Scanner(System.in);
            String userInput = scanner.nextLine();
            serverOut.println(userInput);
            String msg;
            while (!(msg = serverResponse.readLine()).equals(new String(userInput+" accepted"))) {
                System.out.println(msg);
                userInput = scanner.nextLine();
                serverOut.println(userInput);
            }
            window.printMsg(msg);
            scanner.close();

            window.setVisible();
            
            Thread printThread = new Thread(new PrintThread(serverResponse, window.printContainer, window.customFont));
            printThread.start();

            printThread.join();
            System.out.println("Main closed");
        } catch (IOException | InterruptedException | RuntimeException err) {
            System.out.println("Main closed with msg: " + err);
        }
    }

    
}

class PrintThread implements Runnable {
    final BufferedReader in;
    Container printContainer;
    public Font customFont;

    public PrintThread(BufferedReader in, Container printContainer, Font customFont) {
        this.in = in;
        this.printContainer = printContainer;
        this.customFont = customFont;
    }

    public void run() {
        try {
            String msg;
            while ((msg = this.in.readLine()) != null) {
                final String msgText = new String(msg);
                SwingUtilities.invokeLater(() -> {
                    JTextArea textArea = new JTextArea();
                    textArea.setEditable(false);
                    textArea.setText(msgText);
                    textArea.setEditable(false);
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(false);
                    textArea.setFont(customFont);
                    if (msgText.indexOf("[SERVER]") == 0 || msgText.indexOf("[ERR]") == 0) {
                        textArea.setForeground(Color.WHITE);
                        textArea.setBackground(Color.GRAY);
                    }
                    else if (msgText.indexOf("(private)") == 0) {
                        textArea.setBackground(Color.PINK);
                    }
                    textArea.setSize(400, textArea.getPreferredSize().height);
                    textArea.setAlignmentX(Component.LEFT_ALIGNMENT);

                    printContainer.add(Box.createVerticalStrut(10)); // add gap before adding textArea
                    printContainer.add(textArea);
                    printContainer.revalidate();
                    printContainer.repaint();
                });
            }
        } catch (IOException err) {
            System.out.println("PrintThread closed with msg: " + err);
        }
    }
}