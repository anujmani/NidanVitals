package com.example.vitalsapplication.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.example.vitalsapplication.model.VitalsObservationEntity;
import com.example.vitalsapplication.repo.VitalsObservationRepo;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
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
        Observation obs = parser.parseResource(Observation.class, data);
        if (!"85353-1".equals(obs.getCode().getCodingFirstRep().getCode())) {
            throw new IllegalArgumentException("Invalid LOINC code");
        }
        double height = 0, weight = 0, bmi = 0;
        boolean hasBmi = false;
        for (Observation.ObservationComponentComponent comp : obs.getComponent()) {
            String code = comp.getCode().getCodingFirstRep().getCode();
            double value = ((Quantity) comp.getValue()).getValue().doubleValue();
            if ("8302-2".equals(code)) height = value;
            else if ("29463-7".equals(code)) weight = value;
            else if ("39156-5".equals(code)) {
                bmi = value;
                hasBmi = true;
            }
        }
        if (!hasBmi && height > 0 && weight > 0) {
            bmiCalculator(obs,height,weight);
        }


        // Create and save the entity
        VitalsObservationEntity entity = new VitalsObservationEntity();
        entity.setPatientId(patientId);
        entity.setData(parser.encodeResourceToString(obs));  // Store updated FHIR JSON
        return vitalsObservationRepo.save(entity);  // Save and return the entity


    }

    private String getBmiCategory(Double bmi) {
        if (bmi == null) return "Unknown";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }
    private Observation bmiCalculator(Observation obs,double height, double weight){
        double bmi = weight / Math.pow(height / 100, 2);
        Quantity bmiQuantity = new Quantity().setValue(bmi).setUnit("kg/m2").setSystem("http://unitsofmeasure" +
                ".org").setCode("kg/m2");
        Observation.ObservationComponentComponent bmiComp = new Observation.ObservationComponentComponent();

        Coding bmiCoding = new Coding()
                .setSystem("http://loinc.org")
                .setCode("39156-5")
                .setDisplay("BMI");
        CodeableConcept bmiCodeable = new CodeableConcept()
                .addCoding(bmiCoding);
        bmiComp.setCode(bmiCodeable);
        bmiComp.setValue(bmiQuantity);
        obs.addComponent(bmiComp);
        return obs;
    }

}

