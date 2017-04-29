import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class PSOGUI {

    // set point num
    int pointNum = 14;

    // set the path of data file
    String tspData = System.getProperty("user.dir") + "/resources/att48.txt";

    int[] bestTour; // best path
    int bestLength; // shortest length
    private int[] x = new int[pointNum]; // matrix of X
    private int[] y = new int[pointNum]; // matrix of Y

    // set attributes on UI
    private JButton start;
    private JPanel jPanel;
    private JPanel displayPanel;
    private JLabel particleNum;
    private JLabel generation;
    private JLabel weightFactor;
    private JLabel bestLengthLabel;
    private JTextField particleNumText;
    private JTextField generationText;
    private JTextField wValue;
    private JLabel beta;
    private JTextField startPoint;
    private JTextField raValue;
    private JTextField rbValue;

    // check if "start" button has been clicked
    private Boolean isStarted = false;

    private GUICanvas guiCanvas;

    public PSOGUI() {
        // initialize panel
        displayPanel = new JPanel();
        displayPanel.setLayout(new BorderLayout());

        // initialize canvas and add it to panel
        guiCanvas = new GUICanvas();
        displayPanel.add(guiCanvas);

        // get matrix of X&Y from data file
        try {
            x = ReadFile.getX(pointNum, tspData);
            y = ReadFile.getY(pointNum, tspData);
            for (int i = 0; i < pointNum; i++) {
                x[i] += 30;
                y[i] += 200;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // if click "start", run following actions
        start.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    // get value from input box
                    int particleNum = Integer.parseInt(particleNumText.getText().trim());
                    int generation = Integer.parseInt(generationText.getText().trim());
                    float weight = Float.parseFloat(wValue.getText().trim());
                    int beta = Integer.parseInt(startPoint.getText().trim());

                    // pso
                    PSO pso = new PSO(pointNum, generation, particleNum, weight, beta-1);
                    pso.init(tspData);
                    pso.solve();
                    bestTour = pso.getPgd();
                    bestLength = pso.getvPgd();

                    bestLengthLabel.setText("Shortest Path: " + bestLength);

                    displayPanel.add(guiCanvas);

                    isStarted = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    class GUICanvas extends Canvas {
        public GUICanvas() {
            setBackground(Color.WHITE);
        }

        public void paint(Graphics graphics) {
            try {
                graphics.setColor(Color.RED);
                for (int i = 0; i < pointNum; i++) {
                    graphics.fillOval(x[i] / 10, y[i] / 10, 5, 5);
                    graphics.drawString(String.valueOf(i + 1), x[i] / 10, y[i] / 10);
                }

                if (isStarted == true) {
                    graphics.setColor(Color.CYAN);
                    for (int j = 0; j < pointNum - 1; j++) {
                        try {
                            graphics.drawLine(x[bestTour[j]] / 10, y[bestTour[j]] / 10, x[bestTour[j + 1]] / 10, y[bestTour[j + 1]] / 10);
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    graphics.drawLine(x[bestTour[0]]/10,y[bestTour[0]] / 10, x[bestTour[pointNum - 1]] / 10, y[bestTour[pointNum - 1]] / 10);
                    graphics.setColor(Color.GREEN);
                    graphics.fillOval(x[bestTour[0]] / 10, y[bestTour[0]] / 10, 6, 6);
                    graphics.setColor(Color.RED);
                    graphics.fillOval(x[bestTour[pointNum - 1]] / 10, y[bestTour[pointNum - 1]] / 10, 6, 6);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Distribution Route");
        PSOGUI myGUI = new PSOGUI();

        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());
        container.add(myGUI.jPanel, BorderLayout.EAST);
        container.add(myGUI.displayPanel, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(1024, 600);
        frame.setVisible(true);
    }
}
