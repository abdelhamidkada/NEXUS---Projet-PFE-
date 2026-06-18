package com.dyxia.nexuserp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.formula.Formula;
import smile.data.type.DataType;
import smile.data.type.StructField;
import smile.data.type.StructType;
import smile.data.vector.DoubleVector;
import smile.data.vector.IntVector;

import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class AttritionPredictionService {

    private RandomForest model;
    private StructType schema;

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing AttritionPredictionService and training RandomForest model...");
            DataFrame trainingData = generateTrainingData();
            model = trainModel(trainingData);
            
            // Define the schema for prediction (features used in training, plus the target variable as a dummy)
            schema = new StructType(
                new StructField("age", DataType.of(Integer.class)),
                new StructField("absencesCount", DataType.of(Integer.class)),
                new StructField("overtimeHours", DataType.of(Double.class)),
                new StructField("performanceScore", DataType.of(Integer.class)),
                new StructField("attritionRisk", DataType.of(Integer.class))
            );
            log.info("RandomForest model successfully trained and prediction schema initialized.");
        } catch (Exception e) {
            log.error("Failed to train Attrition prediction model", e);
        }
    }

    public DataFrame generateTrainingData() {
        int numRows = 100;
        int[] ages = new int[numRows];
        int[] absences = new int[numRows];
        double[] overtimes = new double[numRows];
        int[] performances = new int[numRows];
        int[] risks = new int[numRows];

        for (int i = 0; i < numRows; i++) {
            // Generate ages: 20 to 60
            int age = 20 + (i % 41);
            
            // Generate absencesCount: 0 to 20
            int absence = i % 21;
            
            // Generate overtimeHours: 0 to 50
            double overtime = (i % 11) * 5.0;
            
            // Generate performanceScore: 30 to 100
            int performance = 30 + (i % 71);

            int risk = 0;
            // Correlated logic:
            // 1. High absences and low performance score correlates with attritionRisk = 1
            if (absence > 10 && performance < 60) {
                risk = 1;
            }
            // 2. High overtimeHours and low age correlates with attritionRisk = 1
            else if (overtime > 35 && age < 30) {
                risk = 1;
            }
            // 3. Very low performance score correlates with attritionRisk = 1
            else if (performance < 40) {
                risk = 1;
            }

            ages[i] = age;
            absences[i] = absence;
            overtimes[i] = overtime;
            performances[i] = performance;
            risks[i] = risk;
        }

        return DataFrame.of(
            IntVector.of("age", ages),
            IntVector.of("absencesCount", absences),
            DoubleVector.of("overtimeHours", overtimes),
            IntVector.of("performanceScore", performances),
            IntVector.of("attritionRisk", risks)
        );
    }

    public RandomForest trainModel(DataFrame trainingDf) {
        Formula formula = Formula.lhs("attritionRisk");
        return RandomForest.fit(formula, trainingDf);
    }

    public String predictRisk(int age, int absencesCount, double overtimeHours, int performanceScore) {
        if (model == null) {
            log.warn("Model not trained, returning default LOW_RISK");
            return "LOW_RISK";
        }
        
        Object[] row = new Object[]{age, absencesCount, overtimeHours, performanceScore, 0};
        Tuple tuple = Tuple.of(row, schema);
        
        int prediction = model.predict(tuple);
        return prediction == 1 ? "HIGH_RISK" : "LOW_RISK";
    }
}
