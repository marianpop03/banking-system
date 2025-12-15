package com.bank.auditservice.audit.repository;


import com.bank.auditservice.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findBySourceAccountId(Long accountId);
}