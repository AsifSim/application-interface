package com.sim.spriced.application.service.stateEngine;

import com.sim.spriced.platform.commons_management_layer.Models.MessageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class PlatformDbClient {

    private final RestTemplate restTemplate;
    private final String readBaseUrl;   // e.g., "http://platform/entity"
    private final String writeUrl;      // e.g., "http://platform/spriced/platform"

    public PlatformDbClient(RestTemplate restTemplate,
                            @Value("${platform.read.url}") String readBaseUrl,
                            @Value("${platform.write.url}") String writeUrl) {
        this.restTemplate = restTemplate;
        this.readBaseUrl = readBaseUrl;
        this.writeUrl = writeUrl;
    }

    /**
     * Read records using a named filter.
     * @param entity      logical entity name (e.g., "payout_request")
     * @param filterName  predefined filter name (e.g., "byId")
     * @param params      filter parameters as a map (key -> value, single value per key)
     * @return list of maps representing rows
     */
    public List<Map<String, Object>> read(String entity, String filterName, Map<String, Object> params) {
        // Convert Map<String, Object> to MultiValueMap<String, Object> (each value as a single-element list)
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        params.forEach((k, v) -> body.add(k, v));

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                readBaseUrl + "/" + entity + "/" + filterName,
                HttpMethod.POST,
                new HttpEntity<>(body),
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );
        return response.getBody();
    }

    /**
     * Insert or update a record using the platform's MessageModel.
     * @param entity    logical entity name
     * @param record    column-name -> value map (must include the primary key for updates)
     * @param operation "insert" or "update"
     * @return the response MessageModel (can be inspected for errors or generated keys)
     */
    public MessageModel write(String entity, Map<String, Object> record, String operation) {
        MessageModel message = MessageModel.builder()
                .entity(entity)
                .operation(operation)
                .requestData(record)
                // optional fields can be set from context if needed:
                // .batchId(...)
                // .workflowId(...)
                // .version(...)
                // .userId(...)
                .build();

        return restTemplate.postForObject(writeUrl, message, MessageModel.class);
    }
}