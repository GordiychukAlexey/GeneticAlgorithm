package geneticAlgorithm;

import algorithms.AL_MathFunctions;
import java.util.ArrayList;

public class GA_Creature implements Comparable<GA_Creature> {

    private ArrayList<ModelTransformParams> gene;
    public float fitness = 999999.0f;

    public GA_Creature(ArrayList<ModelTransformParams> gene) {
        this.gene = gene;
    }

    //породить потомка путём скрещивания
    public GA_Creature makeChild(GA_Creature partner) {
        return this.makeClone();
    }

    private GA_Creature makeClone() {
        ArrayList<ModelTransformParams> copyGene = new ArrayList<>();
        for (ModelTransformParams genePart : this.gene) {
            copyGene.add(new ModelTransformParams(genePart.posX, genePart.posY, genePart.angle));
        }
        GA_Creature clone = new GA_Creature(copyGene);
        return clone;
    }

    public void mutate(double mutationRate) {
        if (Math.random() < mutationRate) {
            int temp1Index = (int) (Math.random() * (this.gene.size()));
            ModelTransformParams temp1 = this.gene.get(temp1Index);
            int temp2Index = (int) (Math.random() * (this.gene.size()));
            this.gene.set(temp1Index, this.gene.get(temp2Index));
            this.gene.set(temp2Index, temp1);
        }

        for (ModelTransformParams genePart : this.gene) {
            if (Math.random() < mutationRate) {
                float inc = (float) AL_MathFunctions.degreesToRadians(((Math.random() * 2.0) -1.0) * 180.0f);
                genePart.angle += inc;

            }
        }
    }

    public ArrayList<ModelTransformParams> getGene() {
        return this.gene;
    }

    public ModelTransformParams createMTP(float posX, float posY, float angle) {
        return new ModelTransformParams(posX, posY, angle);
    }

    //сравнивает приспособленность существ
    @Override
    public int compareTo(GA_Creature o) {
        return (this.fitness > o.fitness ? 1 : -1);
    }

    public static class ModelTransformParams {

        public float posX;
        public float posY;
        public float angle;

        public ModelTransformParams() {
            this(0.0f, 0.0f, 0.0f);
        }

        public ModelTransformParams(float posX, float posY, float angle) {
            this.posX = posX;
            this.posY = posY;
            this.angle = angle;
        }
    }
}
