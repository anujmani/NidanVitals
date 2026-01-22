package com.example.vitalsapplication.repo;

import com.example.vitalsapplication.model.VitalsObservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VitalsObservationRepo extends JpaRepository<VitalsObservationEntity,Long> {
    List<VitalsObservationEntity> findByPatientId(String patientId);

}
