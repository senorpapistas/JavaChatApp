import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.FocusEvent;

public class Frontend {
    public JFrame window;
    private JTextArea inputBox;
    private String defaultText = "Send a message";
    private int inputBoxStatus; // =0 means empty input + unfocused text box (show default text) AND !=0 for all other scenarios 
    public JPanel printContainer;
    public InputHandler inputHandler;
    public Box.Filler boxFiller;
    public Color bgColor;
    public Font customFont;


    public Frontend() {
        this.window = new JFrame();
        this.window.setSize(500, 500);
        this.window.setTitle("chat noir");
        this.window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        this.bgColor = new Color(255, 255, 255);
        this.customFont = new Font("Serif", Font.PLAIN, 15);

        try {
            this.customFont = Font.createFont(Font.TRUETYPE_FONT, new File("./ComingSoon-Regular.ttf")).deriveFont(12f);
        }
        catch (FontFormatException | IOException err) {
            this.customFont = new Font("Serif", Font.PLAIN, 15);
        }
    }

    public void connectHandler(InputHandler ih) {
        inputHandler = ih;
    }

    public void setUpGUI() {
        Container contentPane = window.getContentPane();
        contentPane.setLayout(new BorderLayout());
        

        // Add message input box

        JPanel inputContainer = new JPanel();
        inputContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        inputContainer.setBackground(bgColor);

        JPanel innerInputContainer = new JPanel(); // for the purpose of having a round-borderd container that form-fits the scrollable textbox
        innerInputContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        innerInputContainer.setPreferredSize(new Dimension(375, 112));
        innerInputContainer.setBorder(new RoundedBorder());
        innerInputContainer.setBackground(Color.WHITE);
    
        inputBox = new JTextArea(defaultText, 5, 30);
        inputBox.setLineWrap(true);
        inputBox.setWrapStyleWord(true);
        inputBox.setFont(customFont);

        DocumentFilter inputListener = new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (inputBoxStatus == 0) { // case for when focusGained event calls: inputBox.setText("")
                    fb.replace(0, fb.getDocument().getLength(), "", null);
                    return;
                }

                if(text.charAt(0) == '\n' && text.length() == 1) {
                    String currInput = inputBox.getText().trim();
                    fb.replace(0, fb.getDocument().getLength(), "", null);
                    if (!currInput.equals("")) {
                        inputHandler.sendMsg(currInput);
                    }
                }
                else {
                    fb.replace(offset, 0, text, attrs);
                }
            }

        };
        AbstractDocument document = (AbstractDocument) inputBox.getDocument();
        document.setDocumentFilter(inputListener);

        FocusListener inputFocusListener = new FocusListener() {
            @Override
            public void focusGained(FocusEvent fe) {
                String currInput = inputBox.getText();
                if (currInput.equals(defaultText) && inputBoxStatus == 0) {
                    inputBox.setText(""); // note: halts for the Documentfilter replace event
                    inputBoxStatus = 1;
                }
            }
            public void focusLost(FocusEvent fe) {
                String currInput = inputBox.getText();
                if (currInput.equals("")) {
                    inputBox.setText(defaultText);
                    inputBoxStatus = 0;
                }
            }
        };
        inputBox.addFocusListener(inputFocusListener);
        JScrollPane inputScrollContainer = new JScrollPane(inputBox, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        inputScrollContainer.setBorder(null);
        innerInputContainer.add(inputScrollContainer);
        inputContainer.add(innerInputContainer);
        contentPane.add(inputContainer, BorderLayout.SOUTH);


        // Add message printing container

        printContainer = new JPanel();
        printContainer.setLayout(new BoxLayout(printContainer, BoxLayout.Y_AXIS));
        printContainer.setBackground(bgColor);
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new FlowLayout());
        mainContainer.setBackground(bgColor);
        mainContainer.add(printContainer);
        JScrollPane scrollContainer = new JScrollPane(mainContainer, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        contentPane.add(scrollContainer, BorderLayout.CENTER);
    }

    public void printMsg(String msgText) {
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
    }

    public void setVisible() {
        window.setVisible(true);
    }
}

class RoundedBorder extends AbstractBorder {
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(c.getForeground());
        g2d.drawRoundRect(x, y, width - 1, height - 1, 20, 20);
        g2d.dispose();
    }
}