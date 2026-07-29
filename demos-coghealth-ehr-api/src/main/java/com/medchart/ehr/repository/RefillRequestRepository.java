package com.medchart.ehr.repository;

import com.medchart.ehr.domain.refill.RefillRequest;
import com.medchart.ehr.domain.refill.RefillRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefillRequestRepository extends JpaRepository<RefillRequest, Long> {

    @EntityGraph(attributePaths = {"patient", "medication"})
    List<RefillRequest> findByStatusOrderByRequestedDateDesc(RefillRequestStatus status);
}
