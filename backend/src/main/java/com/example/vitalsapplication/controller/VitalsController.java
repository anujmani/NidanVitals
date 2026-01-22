package com.example.vitalsapplication.controller;

import com.example.vitalsapplication.service.VitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fhir/observation")
@CrossOrigin(origins = "*")
public class VitalsController {
    @Autowired
    private VitalService vitalService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getObservations(
            @RequestParam(required = false) String patient_id,
            @RequestParam(required = false) String risk) {
        List<Map<String, Object>> vitalsObservation = vitalService.getObservations(patient_id, risk);
        return ResponseEntity.ok(vitalsObservation);

    }
}
