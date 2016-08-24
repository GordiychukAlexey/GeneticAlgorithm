package geneticAlgorithm;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
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
    //private int populationSize;
    //максимальная часть популяции, остающаяся при отборе. [0.0..1.0]
    //private double selectionSize;
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
        //generatePool(populationSize);
        //this.sim.createStartingCreature();////////test
    }

    /**
     * стартовая генерация существ
     */
    public void startingGeneratePool() {
        for (int i = 0; i < this.maxPopulationSize; i++) {
            this.pool.add(this.sim.createStartingCreature());
            //System.out.println(this.pool.get(i).getGene().size());
        }

        //test
//        runTest();
//                selection();
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

//        for (int i = this.pool.size() - 1; i >= 0; i--) {
//            if (this.pool.get(i).fitness > this.maxPopulationSize * 1.0) {//1.0 это максимальная длина ребра квадрата в который умещается любая модель
//                this.pool.remove(i);
//            }
//        }
//        for (GA_Creature creature : this.pool) {
//            if (creature.fitness > this.maxPopulationSize * 1.0) {//1.0 это максимальная длина ребра квадрата в который умещается любая модель
//                this.pool.remove(creature);
//            }
//        }
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

        //GA_Creature newCreature;
//        while (this.pool.size() < this.maxPopulationSize) {
//            newCreature = bestCreature.makeChild(null);
//
//            newCreature.mutate(mutationRate);
//            this.pool.add(newCreature);
//        }
    }

    public void testMethod() {
        //this.generatePool();
        for (int i = 1; i <= 100; i++) {
            fillPool();
            testAllCreaturesInPool();
            selection();

            this.sim.testCreature(this.pool.get(0));

            //this.saveSimulationImage();

            try {
                Thread.sleep(3000);
                //this.sim.testCreature(this.pool.get(this.pool.size() - 1));
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
                
//        Graphics2D gg = (Graphics2D) this.sim.getDisplay().getGraphics();
//        gg.setClip(0, 0, this.sim.getDisplay().getWidth(),
//                this.sim.getDisplay().getHeight());
//        Shape ss = gg.getClip();
//        ((Graphics2D) img.getGraphics()).draw(ss);

//        img = this.sim.getDisplay().createVolatileImage(this.sim.getDisplay().getWidth(),
//                this.sim.getDisplay().getHeight()).getSnapshot();

        Date curTime = new Date();
        DateFormat dtfrm = DateFormat.getTimeInstance();
        String dateTime = dtfrm.format(curTime);
        System.out.println(dateTime);
        dateTime = dateTime.replace(':', ',');
//            BufferedImage img = (BufferedImage) this.sim.getDisplay().createImage(this.sim.getDisplay().getWidth(), this.sim.getDisplay().getHeight());
        try {
            ImageIO.write(img, "JPEG", new File("imgs/foo" + dateTime + ".jpg"));
        } catch (IOException ex) {
            Logger.getLogger(GA_Population.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
