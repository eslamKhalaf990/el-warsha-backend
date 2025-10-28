package com.warsha.erp.services;

import com.warsha.erp.dtos.BankAccountDTO;
import com.warsha.erp.dtos.BankSummaryDTO;
import com.warsha.erp.dtos.BankTransactionDTO;
import com.warsha.erp.dtos.TransactionCategoryDTO;
import com.warsha.erp.entities.BankAccount;
import com.warsha.erp.entities.BankTransaction;
import com.warsha.erp.entities.TransactionCategory;
import com.warsha.erp.repository.BankAccountRepository;
import com.warsha.erp.repository.BankTransactionRepository;
import com.warsha.erp.repository.TransactionCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BankTransactionService {

    @Autowired
    private BankTransactionRepository transactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionCategoryRepository categoryRepository;

    public BankTransaction createTransaction(BankTransactionDTO dto) {
        BankTransaction transaction = new BankTransaction();
        BankAccount account = bankAccountRepository.findById(dto.getBankAccountId())
                .orElseThrow(() -> new RuntimeException("Bank account not found"));
        transaction.setBankAccount(account);

        if (dto.getCategoryId() != null) {
            TransactionCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            transaction.setCategory(category);
        }

        transaction.setTransactionType(dto.getTransactionType());
        transaction.setAmount(dto.getAmount());
        transaction.setDescription(dto.getDescription());
        transaction.setReferenceType(dto.getReferenceType());
        transaction.setReferenceId(dto.getReferenceId());

        BankTransaction saved = transactionRepository.save(transaction);

        // Update account balance
        BigDecimal newBalance = transactionRepository.getBalanceByAccount(account.getId());
        account.setCurrentBalance(newBalance);
        bankAccountRepository.save(account);

        return saved;
    }

    public List<BankTransaction> getTransactionsByAccount(Long accountId) {
        return transactionRepository.findByBankAccountIdOrderByCreatedAtDesc(accountId);
    }

    public List<BankTransaction> getTransactions() {
        return transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public BigDecimal getAccountBalance(Long accountId) {
        return transactionRepository.getBalanceByAccount(accountId);
    }

    public BankSummaryDTO getTotalSummary() {
        return transactionRepository.getTotalSummary().stream().map(row -> new BankSummaryDTO((BigDecimal) row[0], (BigDecimal) row[1], (BigDecimal) row[2])).toList().getFirst();
    }

    public List<TransactionCategoryDTO> getTransactionCategory() {
        return bankAccountRepository.findAllTransactionCategories().stream().map(
                row -> new TransactionCategoryDTO(row.getCategoryID(),
                        row.getCategoryName(),
                        row.getCategoryType())).
                toList();
    }

    public List<BankAccountDTO> getAllAccounts() {
        return bankAccountRepository.findAllAccounts();
    }
}
