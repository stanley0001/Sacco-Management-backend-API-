package com.example.demo.erp.communication.services;

import com.example.demo.erp.communication.parsitence.models.singleSmsModel;
import com.infobip.ApiClient;
import com.infobip.ApiException;
import com.infobip.Configuration;
import com.infobip.api.SendSmsApi;
import com.infobip.model.*;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;

@Service
@Log4j2
public class InfoBidApiService {
    public InfoBidApiService() throws IOException {
    }

    //API Documentation  https://github.com/infobip/infobip-api-java-client
    public void auth() {
        log.info("Infobip auth ..");
        ApiClient apiClient = new ApiClient();
        apiClient.setApiKeyPrefix("App");
        apiClient.setApiKey("efefc803db3050b38668f95f4d2c3d7f-f2b92714-10fb-43f5-9b62-80d9600a86f1");
        apiClient.setBasePath("https://yrx881.api.infobip.com");
        Configuration.setDefaultApiClient(apiClient);
    }


    public SmsResponse send1(singleSmsModel customSms) {
        log.info("Infobip sending ..");
        try {
            this.auth();
        }catch (Exception e){
            log.warn("Error authenticating Infobip: {}",e.getMessage());
        }


        SendSmsApi sendSmsApi = new SendSmsApi();
        SmsResponse response = null;
        SmsTextualMessage smsMessage = new SmsTextualMessage()
                .from("STAN")
                .addDestinationsItem(new SmsDestination().to(customSms.getContact()))
                .text(customSms.getMessage());

        SmsAdvancedTextualRequest smsMessageRequest = new SmsAdvancedTextualRequest().messages(
                Collections.singletonList(smsMessage)
        );

        //sending
        try {

            response = sendSmsApi.sendSmsMessage(smsMessageRequest);
            log.info("sent message {}", response);
        } catch (ApiException apiException) {
            // HANDLE THE EXCEPTION
            log.info("Errors code {}, header {}, body{} ", apiException.getCode(), apiException.getResponseHeaders(), apiException.getResponseBody());
        }

        return response;
    }

    public void getStatus(String bulkId, String messageId, int limit) throws ApiException {
        this.auth();

        SendSmsApi sendSmsApi = new SendSmsApi();
        Integer numberOfReportsLimit = 10;
        log.info("Getting status");
        SmsDeliveryResult deliveryReports = sendSmsApi.getOutboundSmsMessageDeliveryReports(bulkId, messageId, numberOfReportsLimit);

        for (SmsReport report : deliveryReports.getResults()) {
            System.out.println(report.getMessageId() + " - " + report.getStatus().getName());
        }
        log.info("Status check complete");
        //return sendSmsApi.getOutboundSmsMessageDeliveryReports(bulkId,messageId,limit);
    }


    //CUSTOM SMS SENDER
    public SmsResponse send(singleSmsModel customSms) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\r\n     \"templateId\": \"string\",\r\n     \"notificationType\": \"SMS\",\r\n     \"" +
                "to\": [\""+customSms.getContact()+"\"],\r\n     \"name\": \"string\",\"item\": \"subject\",\r\n " +
                "    \"message\": \""+customSms.getMessage()+"\",\r\n     \"amount\": \"string\",\"originatingAddress\": \"string\",\"destinationAddress\": \"string\",\r\n     \"schedule\": true,\"scheduleTime\": \"2022-02-02T12:02:25.647Z\"\r\n     }");
        Request request = new Request.Builder()
                .url("https://parcel-pp.herokuapp.com/Communication/sendNotification")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        log.info("request body: {}",body);
        log.info("the request: {}",request);
        log.info("response body: {}",response);
        SmsResponse res=null;


        return res;
    }

}
