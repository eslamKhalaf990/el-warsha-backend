package com.warsha.erp.repository;

import com.warsha.erp.dtos.BankAccountDTO;
import com.warsha.erp.dtos.TransactionCategoryDTO;
import com.warsha.erp.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    @Query("SELECT new com.warsha.erp.dtos.BankAccountDTO(b.id, b.name, b.accountType, b.currentBalance, b.createdAt) " +
            "FROM BankAccount b")
    List<BankAccountDTO> findAllAccounts();

    @Query("SELECT new com.warsha.erp.dtos.TransactionCategoryDTO(t.categoryID, t.categoryName, t.categoryType) FROM TransactionCategory t")
    List<TransactionCategoryDTO> findAllTransactionCategories();
}

