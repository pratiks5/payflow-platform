
package com.payflow.swaggergateway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
public class OpenApiAggregatorController {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final Map<String, String> services;

    private static final List<String> COMPONENT_TYPES = List.of(
            "schemas",
            "responses",
            "parameters",
            "examples",
            "requestBodies",
            "headers",
            "securitySchemes",
            "links",
            "callbacks"
    );

    public OpenApiAggregatorController(
            ObjectMapper objectMapper,
            @Value("${payflow.openapi.services.payment}") String payment,
            @Value("${payflow.openapi.services.account}") String account,
            @Value("${payflow.openapi.services.ledger}") String ledger,
            @Value("${payflow.openapi.services.bank}") String bank,
            @Value("${payflow.openapi.services.reconciliation}") String reconciliation) {

        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().build();

        this.services = Map.of(
                "Payment Service", payment,
                "Account Service", account,
                "Ledger Service", ledger,
                "Bank Simulator", bank,
                "Reconciliation Service", reconciliation
        );
    }

    @GetMapping(
            value = "/payflow-api-docs",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<JsonNode> aggregateOpenApi() {

        List<Mono<JsonNode>> requests = services.values()
                .stream()
                .map(url -> webClient.get()
                        .uri(url + "/v3/api-docs")
                        .retrieve()
                        .bodyToMono(JsonNode.class))
                .toList();

        return Mono.zip(requests, results -> {

            ObjectNode combined = objectMapper.createObjectNode();

            combined.put("openapi", "3.0.1");

            ObjectNode info = combined.putObject("info");
            info.put("title", "PayFlow API Documentation");
            info.put("version", "1.0.0");
            info.put("description",
                    "Combined API documentation for PayFlow microservices");

            combined.putArray("servers")
                    .addObject()
                    .put("url", "http://localhost:8080");

            ObjectNode paths = combined.putObject("paths");
            ObjectNode components = combined.putObject("components");
            ArrayNode tags = combined.putArray("tags");

            int index = 0;

            for (Map.Entry<String, String> service : services.entrySet()) {

                String serviceName = service.getKey();

                JsonNode serviceSpec = (JsonNode) results[index++];

                // Add a collapsible Swagger tag for this service.
                tags.addObject()
                        .put("name", serviceName)
                        .put("description", serviceName + " APIs");

                // Prefix schema and component names to avoid collisions.
                String prefix = serviceName
                        .replaceAll("[^a-zA-Z0-9]", "");

                ObjectNode componentNames =
                        objectMapper.createObjectNode();

                JsonNode sourceComponents =
                        serviceSpec.path("components");

                for (String type : COMPONENT_TYPES) {

                    JsonNode definitions = sourceComponents.path(type);

                    if (definitions.isObject()) {

                        Iterator<String> names = definitions.fieldNames();

                        while (names.hasNext()) {
                            String oldName = names.next();

                            componentNames
                                    .put(type + ":" + oldName,
                                            prefix + "_" + oldName);
                        }
                    }
                }

                // Merge component definitions and rewrite references.
                for (String type : COMPONENT_TYPES) {

                    JsonNode definitions = sourceComponents.path(type);

                    if (!definitions.isObject()) {
                        continue;
                    }

                    ObjectNode target = components.has(type)
                            ? (ObjectNode) components.get(type)
                            : components.putObject(type);

                    Iterator<Map.Entry<String, JsonNode>> fields =
                            definitions.fields();

                    while (fields.hasNext()) {

                        Map.Entry<String, JsonNode> field = fields.next();

                        String newName = componentNames.path(
                                type + ":" + field.getKey()
                        ).asText();

                        JsonNode definition = field.getValue().deepCopy();

                        rewriteReferences(
                                definition,
                                componentNames
                        );

                        target.set(newName, definition);
                    }
                }

                // Merge paths and group operations by service.
                JsonNode sourcePaths = serviceSpec.path("paths");

                if (sourcePaths.isObject()) {

                    Iterator<Map.Entry<String, JsonNode>> pathFields =
                            sourcePaths.fields();

                    while (pathFields.hasNext()) {

                        Map.Entry<String, JsonNode> pathField =
                                pathFields.next();

                        String originalPath = pathField.getKey();

                        String gatewayPath =
                                "/" + service.getValue()
                                        .replaceAll("[^a-zA-Z0-9]", "")
                                        .toLowerCase();

                        // Use actual Gateway route prefixes.
                        gatewayPath = switch (serviceName) {
                            case "Payment Service" -> "/payment";
                            case "Account Service" -> "/account";
                            case "Ledger Service" -> "/ledger";
                            case "Bank Simulator" -> "/bank";
                            default -> "/reconciliation";
                        };

                        String finalPath = gatewayPath + originalPath;

                        JsonNode pathDefinition =
                                pathField.getValue().deepCopy();

                        Iterator<Map.Entry<String, JsonNode>> operations =
                                pathDefinition.fields();

                        while (operations.hasNext()) {

                            Map.Entry<String, JsonNode> operation =
                                    operations.next();

                            String method = operation.getKey();

                            if (List.of(
                                    "get", "post", "put", "delete",
                                    "patch", "options", "head", "trace"
                            ).contains(method.toLowerCase())) {

                                ObjectNode operationNode =
                                        (ObjectNode) operation.getValue();

                                ArrayNode serviceTag =
                                        objectMapper.createArrayNode();

                                serviceTag.add(serviceName);

                                operationNode.set("tags", serviceTag);
                            }

                            rewriteReferences(
                                    operation.getValue(),
                                    componentNames
                            );
                        }

                        rewriteReferences(
                                pathDefinition,
                                componentNames
                        );

                        paths.set(finalPath, pathDefinition);
                    }
                }
            }

            return combined;
        });
    }

    private void rewriteReferences(
            JsonNode node,
            ObjectNode componentNames) {

        if (node.isObject()) {

            ObjectNode object = (ObjectNode) node;

            Iterator<Map.Entry<String, JsonNode>> fields =
                    object.fields();

            while (fields.hasNext()) {

                Map.Entry<String, JsonNode> field = fields.next();

                if (field.getKey().equals("$ref")
                        && field.getValue().isTextual()) {

                    String reference = field.getValue().asText();

                    if (reference.startsWith("#/components/")) {

                        String[] parts = reference.split("/", 4);

                        if (parts.length == 4) {

                            String componentType = parts[2];
                            String oldName = parts[3];

                            String newName = componentNames.path(
                                    componentType + ":" + oldName
                            ).asText();

                            if (!newName.isEmpty()) {
                                object.put(
                                        "$ref",
                                        "#/components/" +
                                                componentType + "/" + newName
                                );
                            }
                        }
                    }

                } else {
                    rewriteReferences(field.getValue(), componentNames);
                }
            }

        } else if (node.isArray()) {

            for (JsonNode child : node) {
                rewriteReferences(child, componentNames);
            }
        }
    }
}
