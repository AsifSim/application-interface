package com.sim.spriced.application.service.stateEngine;

import com.sim.spriced.application.service.autosegmentation.stateEngine.exception.AlternateTransitionException;
import com.sim.spriced.platform.commons_management_layer.Models.MessageModel;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StateTransitionService {

    private final PlatformDbClient dbClient;
    private final StateMachineEngine engine;
    private final String entityName;      // from TBRD, e.g., "payout_request"
    private final String stateColumn;     // from TBRD, e.g., "payout_status"
    private final String idColumn;        // from TBRD, e.g., "request_id"

    public StateTransitionService(PlatformDbClient dbClient,
                                  StateMachineEngine engine,
                                  @Value("${state.entityName}") String entityName,
                                  @Value("${state.stateColumn}") String stateColumn,
                                  @Value("${state.idColumn}") String idColumn) {
        this.dbClient = dbClient;
        this.engine = engine;
        this.entityName = entityName;
        this.stateColumn = stateColumn;
        this.idColumn = idColumn;
    }

    /**
     * Load a record by its ID and execute a command.
     * @return the final record map after persistence
     */
    public Map<String, Object> processCommand(String id, String command, Map<String, Object> context) {
        // 1. Load the record
        List<Map<String, Object>> records = dbClient.read(entityName, "byId", Map.of(idColumn, id));
        if (records.isEmpty()) {
            throw new EntityNotFoundException("Record not found: " + id);
        }
        Map<String, Object> record = records.get(0);
        String currentState = (String) record.get(stateColumn);

        // 2. Apply state machine (handles alternate transitions)
        String cmd = command;
        while (true) {
            try {
                String newState = engine.fire(currentState, cmd, context);
                record.put(stateColumn, newState);
                // 3. Write the updated record back
                MessageModel response = dbClient.write(entityName, record, "update");
                if (response.getError() != null) {
                    throw new RuntimeException("DB update failed: " + response.getError());
                }
                return record;
            } catch (AlternateTransitionException e) {
                cmd = e.getAlternateCommand();
                // The guard's rule may have already executed side effects; continue loop.
            }
        }
    }

    /**
     * Create a new record with the initial state.
     * @return the record map after creation (may contain generated keys)
     */
    public Map<String, Object> createRecord(Map<String, Object> record) {
        record.put(stateColumn, engine.getInitialState());
        MessageModel response = dbClient.write(entityName, record, "insert");
        if (response.getError() != null) {
            throw new RuntimeException("Record creation failed: " + response.getError());
        }
        // The response might contain the newly created record in responseList.
        // For simplicity, we return the input record (or we could parse response).
        return record;
    }
}