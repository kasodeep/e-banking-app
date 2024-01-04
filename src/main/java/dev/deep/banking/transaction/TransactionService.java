package dev.deep.banking.transaction;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import dev.deep.banking.account.Account;
import dev.deep.banking.account.AccountService;
import dev.deep.banking.exception.ResourceNotFoundException;
import dev.deep.banking.exception.ValueMismatchException;
import dev.deep.banking.notification.NotificationSenderService;
import dev.deep.banking.notification.emailNotification.request.FundsAlertNotificationRequest;
import dev.deep.banking.transaction.request.FundsTransferRequest;
import dev.deep.banking.transaction.request.TransactionHistoryRequest;
import dev.deep.banking.transaction.response.TransactionHistoryResponse;
import dev.deep.banking.transaction.response.TransactionType;
import dev.deep.banking.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final TransactionRepository transactionRepository;

    private final AccountService accountService;

    private final UserService userService;

    private final NotificationSenderService notificationSenderService;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, AccountService accountService, UserService userService, NotificationSenderService notificationSenderService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.userService = userService;
        this.notificationSenderService = notificationSenderService;
    }

    @Transactional
    public void transferFunds(FundsTransferRequest request) {
        if (!request.senderAccountNumber().equals(request.receiverAccountNumber())) {
            Account senderAccount = accountService.accountExistsAndIsActivated(request.senderAccountNumber());

            if (ENCODER.matches(request.transactionPin(), senderAccount.getTransactionPin())) {
                Account receiverAccount = accountService.accountExistsAndIsActivated(request.receiverAccountNumber());

                accountService.debitAccount(senderAccount, request.amount());
                accountService.creditAccount(receiverAccount, request.amount());
                saveNewTransaction(request, senderAccount, receiverAccount);
                notificationSenderService.sendCreditAndDebitNotification(new FundsAlertNotificationRequest(senderAccount.getUserId(), receiverAccount.getUserId(), senderAccount.getAccountBalance(), receiverAccount.getAccountBalance(), request.amount()));
                return;
            }
            throw new ValueMismatchException("Incorrect transaction pin!");
        }
        throw new IllegalArgumentException("Sender account cannot be recipient account!");
    }

    /**
     * This method save a new transaction after completion, it is an asynchronous process because the method using it doesn't need it response.
     */
    @Async
    public void saveNewTransaction(FundsTransferRequest request, Account senderAccount, Account receiverAccount) {
        transactionRepository.save(
                new Transaction(request.senderAccountNumber(),
                        request.receiverAccountNumber(),
                        request.amount(),
                        generateTransactionReference(),
                        request.narration(),
                        TransactionStatus.SUCCESS,
                        userService.getUserByUserId(senderAccount.getUserId()).getFullName(),
                        userService.getUserByUserId(receiverAccount.getUserId()).getFullName())
        );
    }

    /**
     * Generates random reference number it keeps generating until it gets a unique value.
     */
    private String generateTransactionReference() {
        final String VALUES = "abcdefghijklmnopqrstuvwxyz0123456789";
        final int referenceNumberLength = 12;
        StringBuilder builder = new StringBuilder(referenceNumberLength);

        do {
            for (int i = 0; i < referenceNumberLength; i++) {
                builder.append(VALUES.charAt(SECURE_RANDOM.nextInt(VALUES.length())));
            }
        } while (transactionRepository.existsByReferenceNum(builder.toString()));
        return builder.toString();
    }

    /**
     * This method returns a list of transactions for a particular account by userId
     */
    public List<TransactionHistoryResponse> getTransactionHistoryByUserId(TransactionHistoryRequest request, int userId, Pageable pageable) {
        Account userAccount = accountService.getAccountByUserId(userId);
        Slice<Transaction> transactions = transactionRepository.findAllByStatusAndCreatedAtBetweenAndSenderAccountNumberOrReceiverAccountNumber(
                TransactionStatus.SUCCESS,
                request.startDateTime(),
                request.endDateTime(),
                userAccount.getAccountNumber(),
                userAccount.getAccountNumber(),
                pageable
        );
        if (transactions.getContent().isEmpty()) throw new ResourceNotFoundException("No transactions!");
        return formatTransactions(transactions.getContent(), userAccount.getAccountNumber());
    }

    private List<Transaction> getTransactionHistoryByUserId(TransactionHistoryRequest request, int userId) {
        Account userAccount = accountService.getAccountByUserId(userId);
        List<Transaction> transactions = transactionRepository.findAllByStatusAndCreatedAtBetweenAndSenderAccountNumberOrReceiverAccountNumber(
                TransactionStatus.SUCCESS,
                request.startDateTime(),
                request.endDateTime(),
                userAccount.getAccountNumber(),
                userAccount.getAccountNumber()
        );

        if (transactions.isEmpty()) throw new ResourceNotFoundException("No transactions found!");
        return transactions;
    }

    /**
     * This method formats the transactions into the desired format which classifies each transaction into either credit and debit for easier understanding.
     */
    private List<TransactionHistoryResponse> formatTransactions(List<Transaction> transactions, String userAccountNumber) {
        List<TransactionHistoryResponse> transactionHistoryResponses = new ArrayList<>();
        transactions.forEach(
                transaction -> {
                    TransactionHistoryResponse transactionHistoryResponse = new TransactionHistoryResponse();
                    transactionHistoryResponse.setTransactionDateTime(transaction.getCreatedAt());
                    transactionHistoryResponse.setAmount(transaction.getAmount());
                    transactionHistoryResponse.setReceiverName(transaction.getReceiverName());
                    transactionHistoryResponse.setSenderName(transaction.getSenderName());
                    transactionHistoryResponse.setTransactionType(checkTransactionType(transaction, userAccountNumber));
                    transactionHistoryResponses.add(transactionHistoryResponse);
                }
        );
        return transactionHistoryResponses;
    }

    public TransactionType checkTransactionType(Transaction transaction, String userAccountNumber) {
        if (transaction.getReceiverAccountNumber().equals(userAccountNumber)) {
            return TransactionType.CREDIT;
        } else if (transaction.getSenderAccountNumber().equals(userAccountNumber)) {
            return TransactionType.DEBIT;
        }
        throw new IllegalArgumentException("Error processing cannot determine transaction type");
    }

    /**
     * This method generates an account statement for a particular account by userId, month, year and returns it as a pdf file.
     */
    public ByteArrayOutputStream generateTransactionStatement(TransactionHistoryRequest request, int userId) throws DocumentException {
        Account account = accountService.getAccountByUserId(userId);
        List<Transaction> transactions = getTransactionHistoryByUserId(request, userId);
        return formatTransactionHistoryToDocument(request, transactions, account);
    }

    private ByteArrayOutputStream formatTransactionHistoryToDocument(TransactionHistoryRequest request, List<Transaction> transactions, Account userAccount) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        document.add(new Paragraph("Account Statement for " + request.startDateTime()));
        document.add(new Paragraph("Account Number: " + userAccount.getAccountNumber()));
        document.add(new Paragraph("Account Holder: " + userService.getUserByUserId(userAccount.getUserId()).getFullName()));
        document.add(Chunk.NEWLINE);

        Font boldFont = new Font(Font.FontFamily.COURIER, 12, Font.BOLD);
        PdfPTable table = new PdfPTable(new float[]{5, 5, 5, 5, 5, 5});

        table.addCell(new PdfPCell(new Phrase("Reference Number", boldFont)));
        table.addCell(new PdfPCell(new Phrase("Transaction Date", boldFont)));
        table.addCell(new PdfPCell(new Phrase("Amount", boldFont)));
        table.addCell(new PdfPCell(new Phrase("Sender", boldFont)));
        table.addCell(new PdfPCell(new Phrase("Recipient", boldFont)));
        table.addCell(new PdfPCell(new Phrase("Description", boldFont)));

        transactions.forEach(transaction -> {
            table.addCell(new PdfPCell(new Phrase(transaction.getReferenceNum())));
            table.addCell(new PdfPCell(new Phrase(transaction.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f", transaction.getAmount()))));
            table.addCell(new PdfPCell(new Phrase(transaction.getSenderName())));
            table.addCell(new PdfPCell(new Phrase(transaction.getReceiverName())));
            table.addCell(new PdfPCell(new Phrase(transaction.getDescription())));
        });

        document.add(table);
        document.close();
        return outputStream;
    }
}

