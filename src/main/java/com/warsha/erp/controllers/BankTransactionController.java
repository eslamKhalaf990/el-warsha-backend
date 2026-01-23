package com.warsha.erp.controllers;

import com.warsha.erp.dtos.BankAccountDTO;
import com.warsha.erp.dtos.BankSummaryDTO;
import com.warsha.erp.dtos.BankTransactionDTO;
import com.warsha.erp.dtos.TransactionCategoryDTO;
import com.warsha.erp.entities.BankTransaction;
import com.warsha.erp.services.BankTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/bank")
public class BankTransactionController {

    @Autowired
    private BankTransactionService transactionService;

    @PostMapping("/transaction")
    public ResponseEntity<BankTransaction> addTransaction(@RequestBody BankTransactionDTO dto) {
        BankTransaction transaction = transactionService.createTransaction(dto);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/transactions/{accountId}")
    public ResponseEntity<List<BankTransaction>> getTransactions(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountId));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<BankTransaction>> getTransactions() {
        return ResponseEntity.ok(transactionService.getTransactions());
    }

    @GetMapping("/balance/{accountId}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getAccountBalance(accountId));
    }

    @GetMapping("/summary")
    public ResponseEntity<BankSummaryDTO> getSummary() {
        BankSummaryDTO summary = transactionService.getTotalSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccountDTO>> getAllAccounts(
            @RequestParam(name = "password", required = false) String password) {

        // If password is null, service returns regular accounts
        // If password is correct, service returns all accounts
        List<BankAccountDTO> accounts = transactionService.getAllAccounts(password);

        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/transactionCategories")
    public ResponseEntity<List<TransactionCategoryDTO>> getTransactionCategories() {
        List<TransactionCategoryDTO> accounts = transactionService.getTransactionCategory();
        return ResponseEntity.ok(accounts);
    }

    @DeleteMapping("/resetTransactions")
    public ResponseEntity<String> resetSystem() {
        transactionService.hardResetSystem();
        return ResponseEntity.ok("System reset successful: All transactions deleted and balances updated to 0.");
    }
}
