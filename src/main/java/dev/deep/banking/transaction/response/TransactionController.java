package dev.deep.banking.transaction.response;

import com.itextpdf.text.DocumentException;
import dev.deep.banking.config.JwtService;
import dev.deep.banking.transaction.TransactionService;
import dev.deep.banking.transaction.request.FundsTransferRequest;
import dev.deep.banking.transaction.request.TransactionHistoryRequest;
import dev.deep.banking.universal.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    private final JwtService jwtService;

    @Autowired
    public TransactionController(TransactionService transactionService, JwtService jwtService) {
        this.transactionService = transactionService;
        this.jwtService = jwtService;
    }

    @PostMapping("/send-funds")
    public ResponseEntity<ApiResponse> transferFunds(@RequestBody @Validated FundsTransferRequest request) {
        transactionService.transferFunds(request);
        return new ResponseEntity<>(new ApiResponse("Funds transferred"), HttpStatus.OK);
    }

    /**
     * This controller fetches all transaction based on a range of date and also implements pagination to help reduce load time.
     */
    @PostMapping()
    public ResponseEntity<ApiResponse> generateTransactionHistory(@RequestHeader("Authorization") String jwt, @RequestParam int size, @RequestParam int page, @RequestBody @Validated TransactionHistoryRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        return new ResponseEntity<>(new ApiResponse("Transaction history",
                transactionService.getTransactionHistoryByUserId(request, jwtService.extractUserIdFromToken(jwt), pageable)), HttpStatus.OK);
    }

    /**
     * This controller generates monthly or yearly account statement in pdf format.
     */
    @PostMapping(value = "/transaction-report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateTransactionStatement(
            @RequestHeader("Authorization") String jwt,
            @RequestBody TransactionHistoryRequest request) throws DocumentException {

        int userId = jwtService.extractUserIdFromToken(jwt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("inline").filename("transaction_report.pdf").build());

        ByteArrayOutputStream outputStream = transactionService.generateTransactionStatement(request, userId);
        return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
    }
}

