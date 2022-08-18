package com.api.service;

import com.api.components.ProductSessionInstance;
import com.api.entity.Product;
import com.api.pojo.DomainUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductExternalService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ProductSessionInstance productSessionInstance;

    private HttpHeaders buildHttpHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        return httpHeaders;
    }

    private MultiValueMap<String, String> buildMultipartBodyWithBarcode(final String barcode) {
        final MultiValueMap<String, String> multipartBody = new LinkedMultiValueMap<>(6);
        multipartBody.add("p_request", productSessionInstance.getSessionInstance().getAjaxIdentifier());
        multipartBody.add("p_flow_id", "171");
        multipartBody.add("p_flow_step_id", "2");
        multipartBody.add("p_instance", productSessionInstance.getSessionInstance().getSessionId());
        multipartBody.add("p_debug", "");
        multipartBody.addAll("p_arg_names", List.of("P2_CURSOR", "P2_LOJA_ID", "P2_COD1"));
        multipartBody.addAll("p_arg_values", List.of("B", "221", barcode));
        return multipartBody;
    }

    public Optional<Product> fetchByBarcode(final String barcode) {
        Objects.requireNonNull(barcode);

        final HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);

        log.info("Fetching for information about a product by barcode "+barcode);
        return restTemplate.execute(
            "/wwv_flow.show",
            HttpMethod.POST,
            restTemplate.httpEntityCallback(httpEntity, String.class),
            (clientHttpResponse) -> {
                final String json = DomainUtils.readFromInputStream(clientHttpResponse.getBody());

                try {
                    final Product newProductToPersist = objectMapper.readValue(json, Product.class);

                    return Optional.of(newProductToPersist);
                }
                catch (InvalidDefinitionException | IllegalStateException | NullPointerException exception) {
                    if (exception instanceof IllegalStateException)
                        if (exception.getMessage().equals("Item name is empty"))
                            return Optional.empty();

                    productSessionInstance.reloadSessionInstance();
                    return fetchByBarcode(barcode);
                }
            }
        );
    }
}
