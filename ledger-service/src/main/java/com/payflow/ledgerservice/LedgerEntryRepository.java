package com.payflow.ledgerservice;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByJournalId(UUID journalId);
    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(String accountId);
    List<LedgerEntry> findTop100ByOrderByCreatedAtDesc();
}
