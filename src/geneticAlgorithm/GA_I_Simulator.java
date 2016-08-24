package geneticAlgorithm;

import java.awt.Canvas;

public interface GA_I_Simulator {

    public Canvas getDisplay();

    public float testCreature(GA_Creature creature);

    public GA_Creature createStartingCreature();

}
