package com.example.vitalsapplication.service;

import com.example.vitalsapplication.model.VitalsObservationEntity;

import java.util.List;
import java.util.Map;

public interface VitalService {
    public List<Map<String, Object>> getObservations(String patientId, String riskFilter);

    VitalsObservationEntity saveObservationFromJson(String patientId, String data);
}
