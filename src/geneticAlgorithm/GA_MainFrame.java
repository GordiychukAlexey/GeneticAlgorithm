package geneticAlgorithm;

import gui.AL_SimpleWindow;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class GA_MainFrame extends AL_SimpleWindow {

    JPanel poolListPanel;
    JPanel graphPanel;
    JPanel simulationViewPanel;

    GA_Population population;

    public GA_MainFrame() {
        super(800, 600, "Genetic algorithm", new GA_ApplicationGUI());
        this.simulationViewPanel = ((GA_ApplicationGUI) this.getContentPane()).simulationViewPanel;
        this.simulationViewPanel.setLayout(new BorderLayout());

        ((GA_ApplicationGUI) this.getContentPane()).b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        GA_MainFrame.this.population.startingGeneratePool();
                        GA_MainFrame.this.population.testMethod();
                    }
                }.start();
            }
        });
        
        ((GA_ApplicationGUI) this.getContentPane()).b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        GA_MainFrame.this.population.testMethod();
                    }
                }.start();
            }
        });

    }

    public void startApp() {
        this.population = new GA_Population();
        Canvas display = this.population.sim.getDisplay();
        this.simulationViewPanel.add(display);
        //this.simulationViewPanel.getComponent(1).setBackground(Color.red);
        
        //this.simulationViewPanel.getComponent(0).setPreferredSize(new Dimension(200, 200));
        //this.pack();
        System.out.println("");
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            GA_MainFrame application = new GA_MainFrame();
            application.startApp();
        });
    }
}
