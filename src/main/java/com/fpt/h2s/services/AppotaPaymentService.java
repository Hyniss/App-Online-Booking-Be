package com.fpt.h2s.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.h2s.models.exceptions.NoImplementationException;
import com.fpt.h2s.services.commands.requests.Payment;
import com.fpt.h2s.utilities.Mappers;
import com.fpt.h2s.utilities.MoreStrings;
import com.fpt.h2s.utilities.QueryBuilder;
import com.squareup.okhttp.*;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class AppotaPaymentService implements BankingService {
    
    private String token;
    private final String partnerCode = "APPOTAPAY";
    private final String apiKey = "FJcmF8uj2ISveL5FvvNk4pnp8xrhINz8";
    private final String secretKey = "XAonJgy14YhtePEITXhyBS2unjfJLAV3";
    
    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private long expiredAt;

    @Override
    public Purchase.Response createPayRequest(Purchase.Request request) {
        throw new NoImplementationException();
    }

    @Override
    public Purchase.Status checkPaymentStatus(Purchase.StatusRequest transactionNo) {
        throw new NoImplementationException();
    }

    @Override
    public void refund(Refund.Request request) {
        throw new NoImplementationException();
    }

    @Override
    @SneakyThrows
    public Transfer.Response createTransferRequest(Transfer.Request request) {
        final Map<String, Object> paramMap = buildBillMap((Payment.TransferMoneyRequest) request);

        final OkHttpClient client = new OkHttpClient();
        final MediaType mediaType = MediaType.parse("application/json");
        final RequestBody requestBody = RequestBody.create(mediaType, Mappers.jsonOf(paramMap));
        final Request appotaRequest = new Request.Builder()
            .url("https://ebill.dev.appotapay.com/api/v1/service/transfer/make")
            .method("POST", requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-APPOTAPAY-AUTH", this.getToken())
            .build();

        final Response response = client.newCall(appotaRequest).execute();
        final ResponseBody responseBody = response.body();
        final String body = responseBody.string();
        responseBody.close();

        return Mappers.mapToObjectFrom(body, Payment.TransferMoneyResponse.class);
    }

    @Override
    public boolean isCardValid(CardDetail.CheckRequest request) {
        return false;
    }

    private String createSignature(final Map<String, Object> params) {
        final String hashData = QueryBuilder.builder().addParams(params).build();
        return MoreStrings.HMacSha256(hashData, this.secretKey);
    }
    
    private String getToken() {
        if (this.token == null || this.isTokenExpired()) {
            this.expiredAt = (long) Math.floor((float) System.currentTimeMillis() / 1000) + 3000;
            this.token = this.generateToken();
        }
        return this.token;
    }
    
    private boolean isTokenExpired() {
        return (long) Math.floor((float) System.currentTimeMillis() / 1000) >= this.expiredAt;
    }
    
    @SneakyThrows
    private String generateToken() {
        final long currentTimestamp = (long) Math.floor((float) System.currentTimeMillis() / 1000);
        final Map<String, Object> header = new LinkedHashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        header.put("cty", "appotapay-api;v=1");
        
        final String source = Mappers.jsonOf(header);
        final String encodedHeader = MoreStrings.base64url(source);
        
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", this.partnerCode);
        payload.put("api_key", this.apiKey);
        payload.put("jti", this.apiKey + currentTimestamp);
        payload.put("exp", currentTimestamp + 3000);
        final String encodedPayload = MoreStrings.base64url(Mappers.jsonOf(payload));
        
        final String sourceToken = encodedHeader + "." + encodedPayload;
        final Mac mac = Mac.getInstance("HmacSHA256");
        final SecretKeySpec key = new SecretKeySpec(this.secretKey.getBytes(), "HmacSHA256");
        mac.init(key);
        
        final String signature = Base64.encodeBase64String(mac.doFinal(sourceToken.getBytes()));
        
        return sourceToken + "." + signature;
    }

    @NotNull
    private Map<String, Object> buildBillMap(Payment.TransferMoneyRequest request) {
        final Map<String, Object> paramMap = new TreeMap<>();
        paramMap.put("bankCode", request.getBankCode());
        paramMap.put("accountNo", request.getAccountNo());
        paramMap.put("accountType", request.getAccountType());
        paramMap.put("accountName", request.getAccountName());
        paramMap.put("amount", request.getAmount());
        paramMap.put("message", request.getMessage());
        paramMap.put("feeType", "payer");
        paramMap.put("partnerRefId", MoreStrings.randomNumber(12));
        paramMap.put("signature", this.createSignature(paramMap));
        return paramMap;
    }

}
