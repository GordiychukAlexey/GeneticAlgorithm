package geneticAlgorithm;

public interface GA_I_Population {
    /**
     * стартовая генерация существ
     */
    public void startingGeneratePool();
    
    /**
     * тестирование существ для выяснения их приспособленности
     */
    public void testAllCreaturesInPool();
    
    /**
     * удаление из пула наименее приспособленных существ
     */
    public void selection();
    
    /**
     * заполнение пула после выборки
     */
    public void fillPool();
}
