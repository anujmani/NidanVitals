package com.example.vitalsapplication.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.example.vitalsapplication.model.VitalsObservationEntity;
import com.example.vitalsapplication.repo.VitalsObservationRepo;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VitalServiceImpl implements VitalService {
    @Autowired
    private VitalsObservationRepo vitalsObservationRepo;

    private final FhirContext ctx = FhirContext.forR4();
    private final IParser parser = ctx.newJsonParser();

    @Override
    public List<Map<String, Object>> getObservations(String patientId, String riskFilter) { //Data for Dashboard
        List<VitalsObservationEntity> entities = patientId != null ?
                vitalsObservationRepo.findByPatientId(patientId) : vitalsObservationRepo.findAll();
        List<Map<String, Object>> results = new ArrayList<>();
        for (VitalsObservationEntity entity : entities) {
            Observation obs = parser.parseResource(Observation.class, entity.getData());
            Double bmi = null;
            Double systolic = null;
            Double diastolic = null;
            for (Observation.ObservationComponentComponent comp : obs.getComponent()) {
                String code = comp.getCode().getCodingFirstRep().getCode();
                if ("39156-5".equals(code)) { // BMI
                    bmi = comp.getValueQuantity().getValue().doubleValue();
                } else if ("8480-6".equals(code)) { // Systolic BP
                    systolic = comp.getValueQuantity().getValue().doubleValue();
                } else if ("8462-4".equals(code)) { // Diastolic BP
                    diastolic = comp.getValueQuantity().getValue().doubleValue();
                }
            }
            // Apply risk filter
            if (riskFilter != null && bmi != null) {
                if ("normal".equals(riskFilter) && !(18.5 <= bmi && bmi < 25)) continue;
                if ("overweight".equals(riskFilter) && !(25 <= bmi && bmi < 30)) continue;
                if ("obese".equals(riskFilter) && bmi < 30) continue;
            }
            Map<String, Object> result = Map.of(
                    "patient_id", entity.getPatientId(),
                    "bmi", bmi,
                    "blood_pressure", systolic != null && diastolic != null ? systolic + "/" + diastolic : null,
                    "status", getBmiCategory(bmi)
            );
            results.add(result);
        }
        return results;
    }

    @Override
    public VitalsObservationEntity saveObservationFromJson(String patientId, String data) {
        return null;
    }

    private String getBmiCategory(Double bmi) {
        if (bmi == null) return "Unknown";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

}

