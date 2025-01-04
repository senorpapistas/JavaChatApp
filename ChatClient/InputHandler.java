import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JFrame;


public class InputHandler {
    private final PrintWriter out;
    private final Socket client;
    private final JFrame window;
    CustomWindowListener windowListener;

    
    public InputHandler(PrintWriter out, Socket client, JFrame window) {
        this.out = out;
        this.client = client;
        this.window = window;
        windowListener = new CustomWindowListener(this.client);
        this.window.addWindowListener(windowListener);
    }

    public void sendMsg(String userInput) {
        this.out.println(userInput);
        if (this.out.checkError()) {
            System.out.println("Error sending message");
        }
    }
}

class CustomWindowListener implements WindowListener {
    private final Socket client;

    public CustomWindowListener(Socket client) {
        this.client = client;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            this.client.close(); // closes print thread and exits main
        }
        catch (IOException err) {}
    }

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosed(WindowEvent e) {}

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}
};