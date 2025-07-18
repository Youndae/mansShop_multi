package com.example.modulecommon.fixture;

import com.example.modulecommon.model.entity.Classification;

import java.util.ArrayList;
import java.util.List;

public class ClassificationFixture {

    public static List<Classification> createClassifications() {
        List<String> classificationIds = List.of(
                "OUTER",
                "TOP",
                "BAGS",
                "PANTS",
                "SHOES"
        );
        List<Classification> result = new ArrayList<>();
        int stepCount = 1;
        for(String name : classificationIds) {
            result.add(
                    Classification.builder()
                            .id(name)
                            .classificationStep(stepCount++)
                            .build()
            );
        }

        return result;
    }
}
