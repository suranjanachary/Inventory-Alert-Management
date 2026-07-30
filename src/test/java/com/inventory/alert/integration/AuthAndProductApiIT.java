package com.inventory.alert.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.alert.support.AbstractMySQLContainerIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndProductApiIT extends AbstractMySQLContainerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginWithSeededAdmin_thenCreateProductAsAdmin() throws Exception {
        String token = login("admin@inventory.local", "AdminPass123!");

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "IT-SKU-1",
                                  "name": "Integration Widget",
                                  "price": 12.50,
                                  "availableQuantity": 20,
                                  "minimumQuantity": 5,
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("IT-SKU-1"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void viewerCannotCreateProduct() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Read Only",
                                  "email": "viewer-it@example.com",
                                  "password": "password123",
                                  "role": "VIEWER"
                                }
                                """))
                .andExpect(status().isCreated());

        String viewerToken = login("viewer-it@example.com", "password123");

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "FORBIDDEN",
                                  "name": "Nope",
                                  "price": 1.00,
                                  "availableQuantity": 1,
                                  "minimumQuantity": 0
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequest_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void purchaseAndSale_flowCreatesHistory() throws Exception {
        String token = login("admin@inventory.local", "AdminPass123!");

        MvcResult created = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "IT-STOCK-1",
                                  "name": "Stock Item",
                                  "price": 5.00,
                                  "availableQuantity": 10,
                                  "minimumQuantity": 8,
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long productId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/v1/inventory/sale")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 3,
                                  "remarks": "integration sale"
                                }
                                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantityAfter").value(7));

        mockMvc.perform(get("/api/v1/inventory/history/" + productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].transactionType").value("SALE"));

        mockMvc.perform(get("/api/v1/alerts/pending")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void validationFailure_returnsFieldErrors() throws Exception {
        String token = login("admin@inventory.local", "AdminPass123!");

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "",
                                  "name": "",
                                  "price": -1,
                                  "availableQuantity": -5,
                                  "minimumQuantity": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void resolveAlert_pathRequiresAuth() throws Exception {
        mockMvc.perform(put("/api/v1/alerts/1/resolve"))
                .andExpect(status().isUnauthorized());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = body.get("accessToken").asText();
        assertThat(token).isNotBlank();
        return token;
    }
}
