// This file is auto-generated, don't edit it. Thanks.

// import com.aliyun.auth.credentials.Credential;
// import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
// import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
// import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
// import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.google.gson.Gson;
// import darabonba.core.client.ClientOverrideConfiguration;
//
// import java.io.BufferedReader;
// import java.io.FileReader;
// import java.nio.charset.StandardCharsets;
// import java.util.Properties;
// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.ConcurrentHashMap;

public class SendSms {
    public static void main(String[] args) throws Exception {
        // ObjectMapper objectMapper = new ObjectMapper();
        //
        // System.out.println("开始");
        //
        // ConcurrentHashMap<String, String> props = new ConcurrentHashMap<>();
        // {
        //     Properties properties = new Properties();
        //     FileReader fileReader = new FileReader("config/application.properties", StandardCharsets.UTF_8);
        //     BufferedReader bufferedReader = new BufferedReader(fileReader);
        //     try (fileReader; bufferedReader){
        //         properties.load(bufferedReader);
        //         System.out.println(objectMapper.valueToTree(properties).toPrettyString());
        //
        //         String targetKeyPrefix = "app.alibaba.sms.";
        //         properties.entrySet().parallelStream().forEach(entry -> {
        //             String key = entry.getKey().toString();
        //             if (key.startsWith(targetKeyPrefix)) {
        //                 props.put(key.substring(targetKeyPrefix.length()), (String) properties.get(key));
        //             }
        //         });
        //     }
        // }
        //
        // // HttpClient Configuration
        // /*HttpClient httpClient = new ApacheAsyncHttpClientBuilder()
        //         .connectionTimeout(Duration.ofSeconds(10)) // Set the connection timeout time, the default is 10 seconds
        //         .responseTimeout(Duration.ofSeconds(10)) // Set the response timeout time, the default is 20 seconds
        //         .maxConnections(128) // Set the connection pool size
        //         .maxIdleTimeOut(Duration.ofSeconds(50)) // Set the connection pool timeout, the default is 30 seconds
        //         // Configure the proxy
        //         .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<your-proxy-hostname>", 9001))
        //                 .setCredentials("<your-proxy-username>", "<your-proxy-password>"))
        //         // If it is an https connection, you need to configure the certificate, or ignore the certificate(.ignoreSSL(true))
        //         .x509TrustManagers(new X509TrustManager[]{})
        //         .keyManagers(new KeyManager[]{})
        //         .ignoreSSL(false)
        //         .build();*/
        //
        // // Configure Credentials authentication information, including ak, secret, token
        // StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
        //     .accessKeyId(props.get("access-key-id"))
        //     .accessKeySecret(props.get("access-key-secret"))
        //     //.securityToken("<your-token>") // use STS token
        //     .build());
        //
        // // Configure the Client
        // AsyncClient client = AsyncClient.builder()
        //     .region("cn-hangzhou") // Region ID
        //     //.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
        //     .credentialsProvider(provider)
        //     //.serviceConfiguration(Configuration.create()) // Service-level configuration
        //     // Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
        //     .overrideConfiguration(
        //         ClientOverrideConfiguration.create()
        //             .setEndpointOverride("dysmsapi.aliyuncs.com")
        //         //.setConnectTimeout(Duration.ofSeconds(30))
        //     )
        //     .build();
        //
        // // Parameter settings for API request
        // SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
        //     .signName(props.get("sign-name"))
        //     .templateCode(props.get("template-code"))
        //     .templateParam("{\"code\":\""+ 775825 +"\"}")
        //     .phoneNumbers(props.get("temp-test-phone-number"))
        //     // Request-level configuration rewrite, can set Http request parameters, etc.
        //     // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
        //     .build();
        //
        // // Asynchronously get the return value of the API request
        // CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
        // // Synchronously get the return value of the API request
        // SendSmsResponse resp = response.get();
        // System.out.println(new Gson().toJson(resp));
        // // Asynchronous processing of return values
        // /*response.thenAccept(resp -> {
        //     System.out.println(new Gson().toJson(resp));
        // }).exceptionally(throwable -> { // Handling exceptions
        //     System.out.println(throwable.getMessage());
        //     return null;
        // });*/
        //
        // // Finally, close the client
        // client.close();
    }

}
