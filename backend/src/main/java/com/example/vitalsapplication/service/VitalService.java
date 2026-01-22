package com.example.vitalsapplication.service;

import java.util.List;
import java.util.Map;

public interface VitalService {
    public List<Map<String, Object>> getObservations(String patientId, String riskFilter);
}
