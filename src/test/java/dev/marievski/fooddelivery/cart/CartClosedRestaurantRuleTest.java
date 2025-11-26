package dev.marievski.fooddelivery.cart;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Правило п.60: если ресторан закрыт, в корзину добавлять нельзя.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CartClosedRestaurantRuleTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Test
    void addItem_toClosedRestaurant_returns409() throws Exception {
        // 1) user
        var userRes = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"U","email":"u1@example.com","role":"CUSTOMER"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        Long userId = om.readTree(userRes.getResponse().getContentAsString()).get("id").asLong();

        // 2) restaurant (closed)
        var restRes = mvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"ClosedOne","cuisine":"ITALIAN","open":false}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        Long restaurantId = om.readTree(restRes.getResponse().getContentAsString()).get("id").asLong();

        // 3) menu with one variation
        var menuRes = mvc.perform(post("/restaurants/{id}/menu", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Dish","description":"d","variations":[{"label":"V","price":350.00,"cookingMinutes":8,"available":true}]}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode menuJson = om.readTree(menuRes.getResponse().getContentAsString());
        Long variationId = menuJson.get("variations").get(0).get("id").asLong();

        // 4) try add to cart -> 409 + наше ApiError
        mvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "userId": %d, "variationId": %d, "quantity": 1 }
                                """.formatted(userId, variationId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("Ресторан закрыт")))
                .andExpect(jsonPath("$.path", is("/cart/items")));
    }
}
