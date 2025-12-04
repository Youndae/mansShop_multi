package com.example.moduleapi.controller.order;

import com.example.moduleconfig.properties.IamportProperties;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 아임포트 결제 API 처리를 위한 컨트롤러
 */
@Tag(name = "Payment Controller")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final IamportProperties iamportProperties;

    private IamportClient iamportClient;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(iamportProperties.getKey(), iamportProperties.getSecret());
    }

    @Operation(hidden = true)
    @PostMapping("/iamport/{imp_uid}")
    public IamportResponse<Payment> paymentIamportResponse(@PathVariable(name = "imp_uid") String imp_uid) throws IamportResponseException, IOException {

        return iamportClient.paymentByImpUid(imp_uid);
    }

}
