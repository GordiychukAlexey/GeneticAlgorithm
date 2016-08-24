package geneticAlgorithm;

import algorithms.AL_Tools;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import java.awt.Canvas;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ген - совокупность расположений объектов массива models основная задача -
 * запуск симуляции по гену
 */
public class GA_SimulatorScene extends ApplicationAdapter implements GA_I_Simulator{

    private final String modelsFilePath = "data/model4";

    private final float areaWidth;//ширина области размещения объектов

    private final long maximumWaitTime = 4000;//максимальное время ожидания прекращения движения
    
    private final LwjglAWTCanvas canvasAWT; //

    private Box2DDebugRenderer renderer;
    private World world;
    private OrthographicCamera cam;

    private float heightOfMeasuringBody = 0.1f;

    ArrayList<Body> borderBoxes;//элементы сцены, образующие границы области размещения объектов
    ArrayList<Body> models;
    Body measuringBody;

    public GA_SimulatorScene(double areaWidth) {
        this.areaWidth = (float) areaWidth;
        this.borderBoxes = new ArrayList<>();
        this.models = new ArrayList<>();
        
        this.canvasAWT = new LwjglAWTCanvas(this);
    }

    @Override
    public Canvas getDisplay() {
        return this.canvasAWT.getCanvas();
    }
    
    @Override
    public float testCreature(GA_Creature creature){
        return this.testGene(creature.getGene());
    }
    
    @Override
    public GA_Creature createStartingCreature() {
        return new GA_Creature(this.createStartingCreatureGene());
    }
    
    @Override
    public void create() {
        this.renderer = new Box2DDebugRenderer();
        this.world = new World(new Vector2(0.0f, -0.1f), true);
        this.cam = new OrthographicCamera();

        createWorld();
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    /**
     * Необходимо вызывать после изменения гена так как модели начинают
     * пересекаться. Модели размещаются согласно изначальному гену; выполняется
     * ожидание прекращения движений моделей; на основе получившегося размещения
     * строится результирующий ген.
     *
     * @param gene изначальный ген
     * @return уточнённый ген либо null в случае ошибки.
     */
    public ArrayList<GA_Creature.ModelTransformParams> clarifyGene(ArrayList<GA_Creature.ModelTransformParams> gene) {
        ArrayList<GA_Creature.ModelTransformParams> res = null;

        this.unfreezeModels();
        boolean errorFlag = this.waitWhileModelsStopped();
        this.freezeModels();

        if (errorFlag == false) {
            res = this.buildGene();
        }

        return res;
    }

    private void waitWhileWorldLocked() {
        while (this.world.isLocked()) {
            //System.err.println("world is locked");
        }
    }

    /**
     * Проводит вычисление высоты области, занимаемой моделями, размещёнными на
     * основе переданного гена.
     *
     * @param gene
     * @return
     */
    public float testGene(ArrayList<GA_Creature.ModelTransformParams> gene) {
        boolean testErrorFlag;
        testErrorFlag = false;

        //setup Models
        for (int i = 0; i < this.models.size(); i++) {
            GA_Creature.ModelTransformParams genePart = gene.get(i);
            this.waitWhileWorldLocked();
            this.models.get(i).setTransform(genePart.posX, genePart.posY, genePart.angle);
        }

        //для избежания зависания моделей в пространстве
        this.freezeModels();
        this.unfreezeModels();

        if (!this.waitWhileModelsStopped()) {
            System.err.println("error in SimulatorScene.testGene()");
        };

        //вычисление высоты расположения наивысшей модели
        float maxModelYPos = 0;
        for (Body model : this.models) {
            if (model.getPosition().y > maxModelYPos) {
                maxModelYPos = model.getPosition().y;
            }
        }
        this.waitWhileWorldLocked();
        this.measuringBody.setTransform(this.areaWidth / 2.0f, maxModelYPos + 1.5f, 0.0f);
        this.unfreezeMeasuringBody();

        long currentWaitTime = 0;

        while (!isMeasuringBodyColliseWithModel()) {
            if (currentWaitTime >= this.maximumWaitTime) {//случаться не должно
                System.err.println("превышен предел ожидания пересечения с измеряющим телом");
                testErrorFlag = true;
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(GA_SimulatorScene.class.getName()).log(Level.SEVERE, null, ex);
            }
            currentWaitTime += 100;
        }
        this.freezeModels();
        this.freezeMeasuringBody();

        float res;
        if (testErrorFlag == false) {
            res = this.measuringBody.getPosition().y - (this.heightOfMeasuringBody / 2.0f);
        } else {
            res = 99999.0f;
        }

        System.out.println(res);
        return res;
    }

    /**
     * Проверка пересечения измеряющего тела с моделями
     *
     * @return
     */
    private boolean isMeasuringBodyColliseWithModel() {
        boolean res = false;

        com.badlogic.gdx.utils.Array<Contact> contactList = this.world.getContactList();

        for (int i = 0; i < contactList.size; i++) {
            Contact contact = contactList.get(i);
            if (contact != null) {//иногда случается
                for (Body model : this.models) {

                    if (contact.isTouching()) {
                        if (contact.getFixtureA() == this.measuringBody.getFixtureList().get(0)
                                || contact.getFixtureB() == this.measuringBody.getFixtureList().get(0)) {
                            if (contact.getFixtureA() == model.getFixtureList().get(0)
                                    || contact.getFixtureB() == model.getFixtureList().get(0)) {
                                res = true;
                                break;
                            }
                        }
                    }

                }
            } else {
                System.err.println("contact == null");
            }

        }
        return res;
    }

    /**
     * Генерация гена для создания первичных существ. В случайном порядке
     * набрасывает модели друг на друга; ожидает перкрацения движения; строит
     * ген на основе расположения моделей.
     *
     * @return
     */
    public ArrayList<GA_Creature.ModelTransformParams> createStartingCreatureGene() {
        ArrayList<GA_Creature.ModelTransformParams> creatureGene = null;

        ArrayList<Integer> indexes = new ArrayList();
        for (int i = 0; i < this.models.size(); i++) {
            indexes.add(i);
        }
        AL_Tools.arrayShuffle(indexes);

        //do {
            for (int i = 0; i < this.models.size(); i++) {
                this.waitWhileWorldLocked();
                this.models.get(i).setTransform(areaWidth / 2.0f, (float) indexes.get(i), 0.0f);
                this.models.get(i).applyLinearImpulse(0.0f, -0.01f, 0.0f, 0.0f, true);
            }
            this.unfreezeModels();
            this.waitWhileModelsStopped();
            this.freezeModels();
            creatureGene =this.buildGene();

        return creatureGene;
    }

    /**
     * Построение гена на основе расположения можелей
     *
     * @return
     */
    private ArrayList<GA_Creature.ModelTransformParams> buildGene() {
        ArrayList<GA_Creature.ModelTransformParams> creatureGene = new ArrayList<>();

        Body model;
        for (int i = 0; i < GA_SimulatorScene.this.models.size(); i++) {
            model = GA_SimulatorScene.this.models.get(i);
            creatureGene.add(new GA_Creature.ModelTransformParams(
                    model.getPosition().x,
                    model.getPosition().y,
                    model.getAngle()));
        }

        return creatureGene;
    }

    /**
     * Ожидание окончания движений моделей.
     *
     * @return true в случае остановки всех моделей; false в случае превышения
     * времени ожидания.
     */
    private boolean waitWhileModelsStopped() {
        boolean res = true;

        long cycleWaitTime = 100;
        long currentWaitTime = 0;
        do {
            try {
                Thread.sleep(cycleWaitTime);
            } catch (InterruptedException ex) {
                Logger.getLogger(GA_SimulatorScene.class.getName()).log(Level.SEVERE, null, ex);
            }
            currentWaitTime += cycleWaitTime;
            if (currentWaitTime >= this.maximumWaitTime) {
                System.err.println("Models not stopped");
                res = false;
                break;
            }
        } while (!GA_SimulatorScene.this.isModelsStopped());

        return res;
    }

    /**
     * "Если модели не двигаются".
     *
     * @return true в случае полного отсутствия движений моделей; false если
     * двигается хотя бы одна модель.
     */
    private boolean isModelsStopped() {
        boolean res = true;
        for (Body model : this.models) {
            if (model.isAwake()) {
                res = false;
                break;
            }
        }
        return res;
    }



    public Body modelLoader(int modelID) {
        Body model = null;
        try {
            model = createModel(this.modelsFilePath, String.format("%s%d", "model", modelID));
        } catch (java.lang.RuntimeException ex) {
            //объкта с индексом modelID не существует
        }
        if (model != null) {
            this.models.add(model);
        }

        return model;
    }

    private void createWorld() {
        loadModels();

        float widthOfBB = this.areaWidth;
        float heightOfBB = widthOfBB * models.size();

        Body currentBorder;
        currentBorder = createBorderBox(widthOfBB, heightOfBB, 1.0f);//левая
        currentBorder.setTransform(-1.0f * (widthOfBB / 2.0f), heightOfBB / 2.0f, 0.0f);
        borderBoxes.add(currentBorder);
        currentBorder = createBorderBox(widthOfBB, heightOfBB, 1.0f);//правая
        currentBorder.setTransform(this.areaWidth + (widthOfBB / 2.0f), heightOfBB / 2.0f, 0.0f);
        borderBoxes.add(currentBorder);
        currentBorder = createBorderBox(this.areaWidth + (widthOfBB * 2.0f), widthOfBB, 1.0f);//нижняя
        currentBorder.setTransform(this.areaWidth / 2.0f, -1.0f * (widthOfBB / 2.0f), 0.0f);
        borderBoxes.add(currentBorder);

        measuringBody = this.createBox(BodyDef.BodyType.DynamicBody, widthOfBB, heightOfMeasuringBody, 1.0f);
        measuringBody.setActive(false);
        measuringBody.setFixedRotation(true);
        measuringBody.getFixtureList().get(0).setFriction(0.0f);
        measuringBody.setTransform(this.areaWidth / 2.0f, -1.0f * (heightOfMeasuringBody / 2.0f), 0.0f);
    }

    private void loadModels() {
        int modelID = 1;
        Body model;
        do {
            model = modelLoader(modelID);
            modelID++;
        } while (model != null);

        this.freezeModels();
    }

    private void freezeModels() {
        this.waitWhileWorldLocked();

        for (Body model : this.models) {
            model.setActive(false);
        }
    }

    private void unfreezeModels() {
        this.waitWhileWorldLocked();

        for (Body model : this.models) {
            model.setActive(true);
        }
    }

    private void freezeMeasuringBody() {
        this.waitWhileWorldLocked();

        this.measuringBody.setActive(false);
    }

    private void unfreezeMeasuringBody() {
        this.waitWhileWorldLocked();

        this.measuringBody.setActive(true);
    }

    private Body createBorderBox(float width, float height, float density) {
        return createBox(BodyDef.BodyType.StaticBody, width, height, density);
    }

    private Body createBox(BodyDef.BodyType type, float width, float height, float density) {
        BodyDef def = new BodyDef();
        def.type = type;
        Body box = world.createBody(def);
        PolygonShape poly = new PolygonShape();
        poly.setAsBox(width / 2.0f, height / 2.0f);
        box.createFixture(poly, density);
        poly.dispose();

        return box;
    }

    private Body createModel(String patch, String modelName) {
        BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal(patch));

        // 1. Create a BodyDef, as usual.
        BodyDef bd = new BodyDef();
        //bd.position.set(0.0f, 10.0f);
        bd.type = BodyDef.BodyType.DynamicBody;

        // 2. Create a FixtureDef, as usual.
        FixtureDef fd = new FixtureDef();
        fd.density = 1.0f;
        fd.friction = 0.1f;
        fd.restitution = 0.3f;

        // 3. Create a Body, as usual.
        Body body = world.createBody(bd);
        // 4. Create the body fixture automatically by using the loader.
        loader.attachFixture(body, modelName, fd, 1.0f);

        return body;
    }

    @Override
    public void render() {
        update();
        draw();

        //для уменьшения нагрузки на процессор. Отключать во время эволюции без отрисовки
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(GA_SimulatorScene.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        this.cam.update();//можно без этого (используется после изменения параметров камеры, таких как положение)
    }

    public void update() {
        this.measuringBody.setTransform(this.areaWidth / 2.0f,
                this.measuringBody.getPosition().y,
                this.measuringBody.getAngle());
        //https://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/World.html#step-float-int-int-
        this.world.step(0.1f, 30, 30);
    }

    public void removeModels() {
//        for(Body model: this.models){
//            world.destroyBody(model);
//        }
//        this.models.clear();

//        for (Body model : this.models) {
//            model.setTransform(0.0f, 0.0f, 0.0f);
//        }
    }

    public void draw() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        try {
            this.renderer.render(world, cam.combined);
        } catch (Exception ex) {
            System.err.println("Ошибка отрисовки!");
        }

    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        float viewportHeight = ((float) this.models.size() / this.areaWidth) + this.heightOfMeasuringBody + 2.0f;
        float viewportWidth = viewportHeight * ((float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight());
        this.cam.setToOrtho(false, viewportWidth, viewportHeight);

        SetCamera(areaWidth / 2.0f, viewportHeight / 2.0f - 1.0f);
    }

    public void SetCamera(float x, float y) {
        this.cam.position.set(x, y, 0.0f);
        this.cam.update();
    }

    @Override
    public void dispose() {
        this.world.dispose();
        this.renderer.dispose();
    }
}
