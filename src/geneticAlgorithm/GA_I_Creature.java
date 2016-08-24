package geneticAlgorithm;

import java.util.ArrayList;

public interface GA_I_Creature {
    public GA_Creature makeChild(GA_Creature partner);
    public void mutate(double mutationRate);
    public ArrayList getGene();
}
