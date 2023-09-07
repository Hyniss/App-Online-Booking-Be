package com.fpt.h2s.configurations;

import ananta.utility.StringEx;
import com.fpt.h2s.utilities.MoreStrings;
import com.google.api.client.util.SecurityUtils;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.http.HttpTransportOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class FirebaseConfiguration {
    //    @Value("${firebase.TYPE}")
    private final String type = "service_account";

    //    @Value("${firebase.PROJECT_ID}")
    private final String projectId = "h2s-se1513-e3cdf";

    //    @Value("${firebase.PRIVATE_KEY_ID}")
    private final String privateKeyId = "c153e630daad2fc4a70fecef8ec010d5f89aefad";

    //    @Value("${firebase.PRIVATE_KEY}")
    private final String privateKey = "-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCWy67CDpdUEQA3\\no81kFtqM1yEO7DkUeKSlySDTAGceogOV1Cveml0UgjPfGgOEfUX/v6Aj4tP6T8JW\\nfogQqaiDxJcNDGUBNxnQk8qoM3wETXDCakK11mc9xBfVjQl2auIDCA6rszS8Bjut\\noCU2kyzDRPaDrA7ZvmLMzYAsPN7DAUUJ1m0rn6DtUFrhutvD/7Gj25r6ZKAOA1eY\\niW+JsaOkHdwM5UY6R6FZgVYj/N2Tro8rJWG+EKSlJFsU1AotRogzjmyEuHoe8XHV\\nlQmRt4KIH0LLpERbskKN5wnpITw5nc5NlityZtmqYmaSZyBh1nGNDF3h7b2HJtKT\\n7NBRhxaLAgMBAAECggEASq0tqy5cpKYSrHsy8LkcnBjBZ0l2bvvb1boHxlZxusrS\\n6bT70K4zID/mfjq8uTTsoF1+aacgANrUy863hUDvkemeQDj+IB7wasl119w/M0ZC\\nbBUBWlmGISFmCDxjDkCaEpQ/56nEmMShczdWlnNF/KG/Tq+7XvuYpAG00rOGQZ0x\\nRcp/xmMa9rtENSWD+b8X4B0mpn4AwizH6oH5V+C57nfoLC9jDPVMTOHR8mp/Tvid\\nE55Zu+MkfEO9MnuaIROYLheaMjLQQA/WIUwN0dEqCTMrGG1M8kR1cXe5lHUC9wUV\\nEW/KSipaEwN5fQv/TBxuRLxfLn7DfUcVg3DCmvT9aQKBgQDSwHWddDKiXUnGbmXY\\nVUQmGh0cizltzGuOuKn8DuqsGyFHlx+N+NXl9+vWpVjGhq6c3mFUN9962U5GAN3Q\\ntf7pFOOz6Z11Dsroeq0Bo4DGt2E2F1epA+dBj1j8sfr8b3+3tKcrBpu5r2F3Xg5V\\nI+oqkWaLgoPeqfYcc2UvzJ0gnwKBgQC3K9sTj42762OH3bvAalSkqsc4Pj5AWbOo\\n2v/AxBAIlktyB2Swg9NblvvWFekaXCepUdecpbJzMoK0RMoGKHVuoLcqdeoOb1ES\\n/TKD1XQ4dtHASkB++04H+cYF/R0msrK54zDeB8lLBYWOgNbe2aeiXsj2d/H3sdOG\\nrXOtpZSmlQKBgQCmj4qk1RfLcHUFCW0eS00f29WUIIhTuZJr6pAVcGiReqOTo+FB\\nYB8UsBrIukbuJ7VKrwo2+C18NtAAzWCBOcTryyGmqARUZkuA7qtatzDoXDfM+yfB\\noR8clkEa/ULCTRCgTHstqvxaBTy/jStAqLDB0cY8jfrQeHzUwXKAE3M/OwKBgG0i\\nNRHveuvW8GM36jBXafz+P1o0Q5TFnTRi3vR0HHVxTpwu6X/92wT77Sn9vffVpk/M\\nkmEjnDHki1lqjs+idW0nVlp3DK/zBOQCdYF0wR0PNiOSWeEOS8yWtbRFvYOb/r6g\\nLTJsgzMupAdmnh8CoU0tmiC3EDSa/8ODPqPcPrgNAoGBAKxk/T+sSiTl7VZtEGuD\\nWtjBgep6fgR/DvNAxl/lqwrQ6bYjl/2HSO5n3FEmLssWxSeq18t9LZZMoszQA0wt\\n7+hhtWdvt9RCLWm3fBzpIVgWWNTkZGBm1RwzI2YSkS8B/mZmzftRdhHhxdTD3Oeq\\njWsHd/W4/+zXfBT/+Q/TafuE\\n-----END PRIVATE KEY-----\\n";

    //    @Value("${firebase.CLIENT_EMAIL}")
    private final String clientEmail = "firebase-adminsdk-17uln@h2s-se1513-e3cdf.iam.gserviceaccount.com";

    //    @Value("${firebase.CLIENT_ID}")
    private final String clientId = "114972945523567180266";
    //    @Value("${firebase.TOKEN_URI}")
    private final String tokenUri = "https://oauth2.googleapis.com/token";

    //    @Value("${firebase.APP_NAME}")
    private final String appName = "H2S-SE1513";

    @Bean
    @SneakyThrows
    FirebaseMessaging firebaseMessaging() {
        // You might see in most of tutorial, they create new credentials using GoogleCredentials.streamFrom()...
        // to extract value from json file. However, we dont do it because with each environment, we have different
        // config. Therefore, we use CONSUL instead, so we should create credentials by builder class instead.
        final ServiceAccountCredentials credentials = ServiceAccountCredentials
            .newBuilder()
            .setClientId(this.clientId)
            .setClientEmail(this.clientEmail)
            .setPrivateKeyId(this.privateKeyId)
            .setPrivateKey(FirebaseConfiguration.privateKeyFromPkcs8(this.privateKey))
            .setProjectId(this.projectId)
            .setTokenServerUri(URI.create(this.tokenUri))
            .setHttpTransportFactory(new HttpTransportOptions.DefaultHttpTransportFactory())
            .build();
        final FirebaseOptions firebaseOptions = FirebaseOptions.builder().setCredentials(credentials).build();
        final FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions, this.appName);
        return FirebaseMessaging.getInstance(app);
    }

    static PrivateKey privateKeyFromPkcs8(final String privateKeyPkcs8) throws IOException {
        final String key = FirebaseConfiguration.extractPrivateKeyFrom(privateKeyPkcs8);
        final byte[] bytes = Base64.getDecoder().decode(key);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);

        try {
            final KeyFactory keyFactory = SecurityUtils.getRsaKeyFactory();
            return keyFactory.generatePrivate(keySpec);
        } catch (final InvalidKeySpecException | NoSuchAlgorithmException var7) {
            throw new IOException("Unexpected exception reading PKCS#8 data", var7);
        }
    }

    private static String extractPrivateKeyFrom(final String privateKey) {
        // This is how the key looks like in json file: -----BEGIN PRIVATE KEY-----\n[....]-----END PRIVATE KEY-----\n
        // We only need to take value in the [...] and remove all new line symbol '\n'

        final String privateKeyWithLines = MoreStrings.optionalOf(StringEx.between("-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----", privateKey)).orElse(privateKey);
        return MoreStrings.removeLines(privateKeyWithLines);
    }
}
