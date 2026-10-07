package com.edstem.sample.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edstem.sample.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class ProductApiTest {

  private static final String PRODUCTS = "/api/v1/products";

  @Autowired private MockMvc mockMvc;

  @Test
  void seededCatalogIsPagedWithTotals() throws Exception {
    mockMvc
        .perform(get(PRODUCTS).param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content.length()").value(10))
        .andExpect(jsonPath("$.data.page.totalElements", greaterThanOrEqualTo(100)))
        .andExpect(jsonPath("$.data.page.totalPages", greaterThanOrEqualTo(10)));
  }

  @Test
  void allFiltersCombineInOneRequest() throws Exception {
    mockMvc
        .perform(
            get(PRODUCTS)
                .param("category", "electronics")
                .param("minPriceCents", "1000")
                .param("maxPriceCents", "90000")
                .param("inStock", "true")
                .param("name", "electronics")
                .param("sort", "priceCents,desc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[*].category", everyItem(is("Electronics"))))
        .andExpect(jsonPath("$.data.content[*].priceCents", everyItem(greaterThanOrEqualTo(1000))))
        .andExpect(jsonPath("$.data.content[*].priceCents", everyItem(lessThanOrEqualTo(90000))))
        .andExpect(jsonPath("$.data.content[*].stock", everyItem(greaterThan(0))));
  }

  @Test
  void pageSizeIsCappedAt100() throws Exception {
    mockMvc
        .perform(get(PRODUCTS).param("size", "500"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.page.size").value(100));
  }

  @Test
  void invalidFiltersAndSortReturn400() throws Exception {
    mockMvc
        .perform(get(PRODUCTS).param("minPriceCents", "500").param("maxPriceCents", "100"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.details.priceRangeValid[0]").exists());
    mockMvc
        .perform(get(PRODUCTS).param("sort", "colour"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.details.sort[0]").exists());
  }
}
