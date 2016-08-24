package geneticAlgorithm;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Осуществляет механизм естественного отбора над пулом существ. Не должен
 * ничего знать о природе существ
 *
 * @author AL
 */
public class GA_Population {

    //PARAMS>>>>
    private int selectionSize;

    private int maxPopulationSize;

    private double mutationRate = 0.2;
    //<<<<PARAMS

    private ArrayList<GA_Creature> pool;
    private GA_Creature bestCreature;
    private int generation;

    public GA_I_Simulator sim;

    public GA_Population() {
        this(4, 2);
    }

    /**
     *
     * @param populationSize количество одновременно существующих особей
     * @param selectionPercentage доля отбора наиболее приспособленных особей
     */
    public GA_Population(int populationSize, int selectionSize) {
        this.maxPopulationSize = populationSize;
        this.sim = new GA_SimulatorScene(2.0);

        this.selectionSize = selectionSize;;

        this.pool = new ArrayList<>();
    }

    /**
     * стартовая генерация существ
     */
    public void startingGeneratePool() {
        for (int i = 0; i < this.maxPopulationSize; i++) {
            this.pool.add(this.sim.createStartingCreature());
        }
    }

    public void testAllCreaturesInPool() {
        for (GA_Creature creature : this.pool) {
            creature.fitness = this.sim.testCreature(creature);
        }
    }

    public void selection() {
        pool.sort(null);
        for (int i = this.pool.size() - 1; i >= this.selectionSize; i--) {
            this.pool.remove(i);
        }
        this.bestCreature = this.pool.get(0);
        System.out.println("bestCreature fitness = " + this.bestCreature.fitness);
    }

    private void fillPool() {
        GA_Creature newCreature;
        int startPoolSize = this.pool.size();

        int nextParentIndex = 0;
        while (this.pool.size() < this.maxPopulationSize) {
            newCreature = this.pool.get(nextParentIndex).makeChild(null);
            newCreature.mutate(mutationRate);
            this.pool.add(newCreature);

            nextParentIndex++;
            if (nextParentIndex >= startPoolSize - 1) {
                nextParentIndex = 0;
            }
        }
    }

    public void testMethod() {
        for (int i = 1; i <= 100; i++) {
            fillPool();
            testAllCreaturesInPool();
            selection();

            this.sim.testCreature(this.pool.get(0));

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(GA_Population.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void saveSimulationImage() {
        BufferedImage img;
        img = new BufferedImage(
                this.sim.getDisplay().getWidth(),
                this.sim.getDisplay().getHeight(),
                BufferedImage.TYPE_INT_RGB);
        
        this.sim.getDisplay().paint(img.getGraphics());

        Date curTime = new Date();
        DateFormat dtfrm = DateFormat.getTimeInstance();
        String dateTime = dtfrm.format(curTime);
        System.out.println(dateTime);
        dateTime = dateTime.replace(':', ',');
        try {
            ImageIO.write(img, "JPEG", new File("imgs/foo" + dateTime + ".jpg"));
        } catch (IOException ex) {
            Logger.getLogger(GA_Population.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
