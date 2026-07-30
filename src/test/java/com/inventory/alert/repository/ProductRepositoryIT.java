package com.inventory.alert.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.inventory.alert.entity.Product;
import com.inventory.alert.support.AbstractMySQLContainerIT;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProductRepositoryIT extends AbstractMySQLContainerIT {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findBySku_andActiveBelowMinimum() {
        Product product = new Product("REPO-1", "Repo Product", null, new BigDecimal("3.00"), 2, 5, true);
        Product saved = productRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(productRepository.findBySku("REPO-1")).isPresent();
        assertThat(productRepository.findActiveBelowMinimum(PageRequest.of(0, 10)).getContent())
                .extracting(Product::getSku)
                .contains("REPO-1");
    }
}
