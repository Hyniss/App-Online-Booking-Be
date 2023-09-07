package com.fpt.h2s.services;

import ananta.utility.StringEx;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.configurations.ConsulConfiguration;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.models.exceptions.NoImplementationException;
import com.fpt.h2s.services.commands.requests.Payment;
import com.fpt.h2s.utilities.Mappers;
import com.fpt.h2s.utilities.MoreRequests;
import com.fpt.h2s.utilities.MoreStrings;
import com.fpt.h2s.utilities.QueryBuilder;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VNPayService implements BankingService {

    private final ConsulConfiguration consul;

    private String payUrl;
    private String tmnCode;
    private String hashSecret;
    private String apiUrl;
    private String version;

    @PostConstruct
    private void postInit() {
        this.payUrl = this.consul.get("service.payment.vnpay.PAY_URL");
        this.tmnCode = this.consul.get("service.payment.vnpay.TMN_CODE");
        this.hashSecret = this.consul.get("service.payment.vnpay.HASH_SECRET");
        this.apiUrl = this.consul.get("service.payment.vnpay.API_URL");
        this.version = this.consul.get("service.payment.vnpay.VERSION");
    }

    public BankingService.Purchase.Response createPayRequest(final BankingService.Purchase.Request request) {
        final Map<String, Object> queryMap = this.createPayBody((Payment.PurchaseRequest) request);
        final String hashData = QueryBuilder
            .builder()
            .addParams(queryMap)
            .build(MoreStrings::asciiOf);

        final String url = QueryBuilder
            .builder()
            .withUrl(this.payUrl)
            .addParams(queryMap)
            .addSignatureParam("vnp_SecureHash", MoreStrings.hmacSHA512(this.hashSecret, hashData))
            .build(MoreStrings::asciiOf, MoreStrings::asciiOf);

        return Payment.PurchaseResponse.builder().id((String) queryMap.get("vnp_TxnRef")).url(url).build();
    }

    @Override
    @SneakyThrows
    public Purchase.Status checkPaymentStatus(Purchase.StatusRequest request) {
        JsonObject body = getStatusBody(request);

        final URL url = new URL(this.apiUrl);

        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        final DataOutputStream content = new DataOutputStream(connection.getOutputStream());
        content.writeBytes(body.toString());
        content.flush();
        content.close();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        Map<String, Object> response = Mappers.mapToObjectFrom(reader.lines().collect(Collectors.joining()), new TypeReference<>() {
        });
        String status = String.valueOf(response.get("vnp_TransactionStatus"));
        if (status.equals("00")) {
            return Purchase.Status.SUCCEED;
        }
        if (status.equals("01")) {
            return Purchase.Status.PENDING;
        }
        return Purchase.Status.FAILED;
    }

    @NotNull
    private JsonObject getStatusBody(Purchase.StatusRequest request) {
        String vnp_RequestId = MoreStrings.randomStringWithLength(8);
        String vnp_Version = "2.1.0";
        String vnp_Command = "querydr";
        String vnp_TmnCode = tmnCode;
        String vnp_TxnRef = request.getOrderId();
        String vnp_OrderInfo = "Kiem tra ket qua GD OrderId:" + vnp_TxnRef;
        String vnp_TransDate = request.getTransactionDate();

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());

        String vnp_IpAddr = MoreRequests.getIPAddress();

        JsonObject  vnp_Params = new JsonObject ();

        vnp_Params.addProperty("vnp_RequestId", vnp_RequestId);
        vnp_Params.addProperty("vnp_Version", vnp_Version);
        vnp_Params.addProperty("vnp_Command", vnp_Command);
        vnp_Params.addProperty("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.addProperty("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.addProperty("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.addProperty("vnp_TransactionDate", vnp_TransDate);
        vnp_Params.addProperty("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.addProperty("vnp_IpAddr", vnp_IpAddr);

        String hash_Data= String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode, vnp_TxnRef, vnp_TransDate, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);
        String vnp_SecureHash = MoreStrings.hmacSHA512(hashSecret, hash_Data);

        vnp_Params.addProperty("vnp_SecureHash", vnp_SecureHash);
        return vnp_Params;
    }

    @Override
    @SneakyThrows
    public void refund(Refund.Request request) {
        final JsonObject jsonBody = this.createRefundBody((Payment.RefundRequest) request, MoreRequests.getCurrentHttpRequest());
        final URL url = new URL(this.apiUrl);

        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        final DataOutputStream content = new DataOutputStream(connection.getOutputStream());
        content.writeBytes(jsonBody.toString());
        content.flush();
        content.close();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        Map<String, Object> response = Mappers.mapToObjectFrom(reader.lines().collect(Collectors.joining()), new TypeReference<>() {
        });
        Object statusCode = response.getOrDefault("vnp_ResponseCode", "");
        if (statusCode.equals("0")) {
            return;
        }
        if (statusCode.equals("91")) {
            throw ApiException.badRequest("Không tìm thấy giao dịch");
        }
        if (statusCode.equals("94")) {
            throw ApiException.badRequest("Giao dịch đang được xử lí.");
        }
        throw ApiException.badRequest("Giao dịch không thành công.");
    }

    @Override
    public Transfer.Response createTransferRequest(Transfer.Request request) {
        throw new NoImplementationException();
    }

    @Override
    public boolean isCardValid(CardDetail.CheckRequest request) {
        throw new NoImplementationException();
    }

    private JsonObject createRefundBody(final Payment.RefundRequest request, final HttpServletRequest servletRequest) {
        final String requestId = MoreStrings.randomNumber(8);
        final String transactionType = request.getType().getValue();
        final String txnRef = request.getOrderId();
        final String amount = String.valueOf(request.getAmount());
        final String orderInfo = "Hoan tien GD OrderId:" + txnRef;
        final String transactionNo = "";
        final String transactionDate = request.getTransactionDate();
        final String createBy = request.getUser();

        final Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        final String vnp_CreateDate = formatter.format(cld.getTime());

        final String ipAddress = MoreRequests.getIPAddress(servletRequest);

        final JsonObject vnp_Params = new JsonObject();

        vnp_Params.addProperty("vnp_RequestId", requestId);
        vnp_Params.addProperty("vnp_Version", this.version);
        vnp_Params.addProperty("vnp_Command", "refund");
        vnp_Params.addProperty("vnp_TmnCode", this.tmnCode);
        vnp_Params.addProperty("vnp_TransactionType", transactionType);
        vnp_Params.addProperty("vnp_TxnRef", txnRef);
        vnp_Params.addProperty("vnp_Amount", amount);
        vnp_Params.addProperty("vnp_OrderInfo", orderInfo);

        if (Strings.isNotBlank(transactionNo)) {
            vnp_Params.addProperty("vnp_TransactionNo", transactionNo);
        }

        vnp_Params.addProperty("vnp_TransactionDate", transactionDate);
        vnp_Params.addProperty("vnp_CreateBy", createBy);
        vnp_Params.addProperty("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.addProperty("vnp_IpAddr", ipAddress);

        final String hash_Data = String.join("|",
            requestId, this.version, "refund", this.tmnCode,
            transactionType, txnRef, amount, transactionNo,
            transactionDate, createBy, vnp_CreateDate, ipAddress, orderInfo
        );

        vnp_Params.addProperty("vnp_SecureHash", MoreStrings.hmacSHA512(this.hashSecret, hash_Data));
        return vnp_Params;
    }


    private Map<String, Object> createPayBody(final Payment.PurchaseRequest request) {
        final Map<String, Object> params = new TreeMap<>();
        params.put("vnp_Version", this.version);
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", this.tmnCode);
        params.put("vnp_Amount", String.valueOf(request.getAmount() * 100));
        params.put("vnp_CurrCode", "VND");
        if (StringEx.isNotBlank(request.getBankCode())) {
            params.put("vnp_BankCode", request.getBankCode());
        }
        params.put("vnp_OrderType", Payment.PurchaseType.ATM);
        params.put("vnp_Locale", request.getDisplayLanguage().getValue());
        params.put("vnp_IpAddr", MoreRequests.getIPAddress());

        final String vnp_TxnRef = Optional.ofNullable(request.getTransactionId()).orElse(MoreStrings.randomNumber(12));
        params.put("vnp_TxnRef", vnp_TxnRef);
        params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        params.put("vnp_ReturnUrl", StringEx.format(request.getReturnUrl(), vnp_TxnRef));

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        final ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        final ZonedDateTime now = ZonedDateTime.now(zone);
        final String vnp_CreateDate = formatter.format(now);
        params.put("vnp_CreateDate", vnp_CreateDate);

        final String vnp_ExpireDate = formatter.format(now.plusMinutes(15));
        params.put("vnp_ExpireDate", vnp_ExpireDate);

        return params;
    }


}
