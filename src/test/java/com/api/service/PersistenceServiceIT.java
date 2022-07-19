package com.api.service;

import static com.api.projection.Projection.*;
import static org.assertj.core.api.Assertions.*;

import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@SpringBootTest
@Transactional
public class PersistenceServiceIT {

    @Autowired
    private PersistenceService persistenceServiceUnderTest;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PriceRepository priceRepository;

    private final String GLOBAL_BARCODE = "7897534852624";

    @Nested
    class FindProductByBarcodeTest {

        @Test
        @DisplayName("When limit is zero then should return all products from db")
        void should_return_all_products_from_db() {
            final int limit = 0;

            final ProductWithManyPrices actualProduct = persistenceServiceUnderTest.findProductByBarcode(GLOBAL_BARCODE, limit);

            assertThat(actualProduct).isNotNull();
            assertThat(actualProduct).extracting(ProductBase::getDescription).isEqualTo("ALCOOL HIG AZULIM 50");
            assertThat(actualProduct).extracting(ProductBase::getBarcode).isEqualTo("7897534852624");
            assertThat(actualProduct).extracting(ProductBase::getSequenceCode).isEqualTo(137513);
            assertThat(actualProduct.getPrices()).isNotNull();
            assertThat(actualProduct.getPrices()).hasSize(4);
            assertThat(actualProduct.getPrices()).extracting(PriceWithInstant::getValue)
                .containsExactly(
                    new BigDecimal("5.65"), new BigDecimal("9.90"), new BigDecimal("10.75"), new BigDecimal("7.50")
                );
        }

        @Test
        @DisplayName("Should return a few products from drop with a given limit")
        void should_return_a_few_products_from_db() {
            final int limit = 2;

            final ProductWithManyPrices actualProduct = persistenceServiceUnderTest.findProductByBarcode(GLOBAL_BARCODE, limit);

            assertThat(actualProduct).isNotNull();
            assertThat(actualProduct).extracting(ProductBase::getDescription).isEqualTo("ALCOOL HIG AZULIM 50");
            assertThat(actualProduct).extracting(ProductBase::getBarcode).isEqualTo("7897534852624");
            assertThat(actualProduct).extracting(ProductBase::getSequenceCode).isEqualTo(137513);
            assertThat(actualProduct.getPrices()).isNotNull();
            assertThat(actualProduct.getPrices()).hasSize(2);
            assertThat(actualProduct.getPrices()).extracting(PriceWithInstant::getValue)
                .containsExactly(new BigDecimal("5.65"), new BigDecimal("9.90"));

        }

        @Test
        @DisplayName("Should fetch from the external service and save it in the db")
        void should_return_a_product_from_the_external_service() {
            final String barcode = "7891095005178";

            final ProductWithLatestPrice actualProduct = persistenceServiceUnderTest.findProductByBarcode(barcode, 0);
            final long actualProductsCount = productRepository.count();
            final long actualPricesCount = priceRepository.count();

            assertThat(actualProduct).isNotNull();
            assertThat(actualProduct).extracting(ProductBase::getDescription).isEqualTo("AMEND YOKI");
            assertThat(actualProduct).extracting(ProductBase::getBarcode).isEqualTo("7891095005178");
            assertThat(actualProduct).extracting(ProductBase::getSequenceCode).isEqualTo(8769);
            assertThat(actualProduct.getLatestPrice()).isNotNull();
            assertThat(actualProductsCount).isEqualTo(12);
            assertThat(actualPricesCount).isEqualTo(67);
        }

        @Test
        @DisplayName("When no products can be found then should throw an exception")
        void when_no_products_can_be_found_should_throw_an_exception() {
            final String nonExistentBarcode = "134810923434";

            final Throwable actualThrowable = catchThrowable(() -> {
                persistenceServiceUnderTest.findProductByBarcode(nonExistentBarcode, 0);
            });

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
                assertThat(exception.getReason()).isEqualTo("Product not found");
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });
        }
    }
}
