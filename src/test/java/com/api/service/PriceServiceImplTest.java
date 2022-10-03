package com.api.service;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.repository.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.*;

import static java.time.Month.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.*;

final class PriceServiceImplTest {

    private PriceRepository priceRepositoryMock;
    private PriceServiceImpl priceServiceUnderTest;

    @BeforeEach
    void setup() {
        priceServiceUnderTest = new PriceServiceImpl(priceRepositoryMock = mock(PriceRepository.class));
    }

    @Nested
    class FindByIdTest {

        @Test
        @DisplayName("Should throw NullPointerException")
        void when_id_is_null_then_throw_an_exception() {
            final Throwable actualThrowable = catchThrowable(() -> priceServiceUnderTest.findById(null));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException with NOT FOUND EXCEPTION")
        void when_id_does_not_exist_then_should_throw_an_exception() {
            final UUID nonExistentId = UUID.fromString("4843ca41-2532-4247-bae4-16e61b8108cc");
            given(priceRepositoryMock.findById(eq(nonExistentId))).willReturn(Optional.empty());

            final Throwable actualThrowable = catchThrowable(() -> priceServiceUnderTest.findById(nonExistentId));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(ResponseStatusException.class);
            assertThat((ResponseStatusException) actualThrowable).satisfies(exception -> {
                assertThat(exception.getReason()).isEqualTo("Price not found");
                assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });

            verify(priceRepositoryMock, times(1)).findById(eq(nonExistentId));
            verify(priceRepositoryMock, only()).findById(eq(nonExistentId));
        }

        @Test
        @DisplayName("Should return a price successfully")
        void when_id_exists_then_should_return_a_price_successfully() {
            final UUID existingId = UUID.fromString("5b17f3d7-5fd7-4564-a994-23613d993a57");
            final Price expectedPrice = Resources.LIST_OF_PRICES.get(0);
            given(priceRepositoryMock.findById(eq(existingId))).willReturn(Optional.of(expectedPrice));

            final Price actualPrice = priceServiceUnderTest.findById(existingId);

            assertThat(actualPrice).isNotNull();
            assertThat(actualPrice).isEqualTo(expectedPrice);

            verify(priceRepositoryMock, times(1)).findById(eq(existingId));
            verify(priceRepositoryMock, only()).findById(eq(existingId));
        }
    }

    @Nested
    class FindByProductBarcodeTest {

        @Test
        @DisplayName("Should throw NullPointerException")
        void when_barcode_is_null_then_throw_an_exception() {
            final Sort orderByCreationDateDesc = Sort.by("creationDate").descending();
            final Throwable actualThrowable =
                catchThrowable(() -> priceServiceUnderTest.findByProductBarcode(null, orderByCreationDateDesc));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException")
        void when_sort_is_null_then_throw_an_exception() {
            final String targetProductBarcode = "7891000055120";
            final Throwable actualThrowable =
                catchThrowable(() -> priceServiceUnderTest.findByProductBarcode(targetProductBarcode, (Sort) null));

            assertThat(actualThrowable).isNotNull();
            assertThat(actualThrowable).isInstanceOf(NullPointerException.class);
        }
    }

    private static class Resources {

        private final static List<Price> LIST_OF_PRICES = List.of(
            new Price(UUID.fromString("5b17f3d7-5fd7-4564-a994-23613d993a57"), new BigDecimal("13.35"), null, null),
            new Price(UUID.fromString("4d2bd6d8-7cf0-4e4e-b439-4582e63f4526"), new BigDecimal("5.5"), null, null),
            new Price(UUID.fromString("8fc79bdd-f587-4894-9596-7c9298c4d7df"), new BigDecimal("10.09"), null, null),
            new Price(UUID.fromString("4b8bce95-aa20-4917-bfda-2f7ad89b7a91"), new BigDecimal("7.5"), null, null),
            new Price(UUID.fromString("9229c9be-8af1-4e82-a0db-5e7f16388171"), new BigDecimal("2.47"), null, null)
        );

        private static Month extractMonthFromInstant(final Instant instant) {
            return instant.atOffset(ZoneOffset.UTC).getMonth();
        }

        private final static Comparator<Price> ORDER_BY_INSTANT_DESC =
            Comparator.comparing(Price::getInstant).reversed();

        private final static Product PRODUCT = Product.builder()
            .description("ACHOC PO NESCAU 800G")
            .barcode("7891000055120")
            .sequenceCode(29250)
            .build();

        static {
            LIST_OF_PRICES.forEach(PRODUCT::addPrice);

            final Instant january = LocalDateTime.of(2022, JANUARY, 1, 12, 0).toInstant(ZoneOffset.UTC);
            final Instant february = LocalDateTime.of(2022, FEBRUARY, 1, 12, 0).toInstant(ZoneOffset.UTC);
            final Instant march = LocalDateTime.of(2022, MARCH, 1, 12, 0).toInstant(ZoneOffset.UTC);
            final Instant april = LocalDateTime.of(2022, APRIL, 1, 12, 0).toInstant(ZoneOffset.UTC);
            final Instant may = LocalDateTime.of(2022, MAY, 1, 12, 0).toInstant(ZoneOffset.UTC);

            LIST_OF_PRICES.get(1).setInstant(january);
            LIST_OF_PRICES.get(0).setInstant(february);
            LIST_OF_PRICES.get(4).setInstant(march);
            LIST_OF_PRICES.get(2).setInstant(april);
            LIST_OF_PRICES.get(3).setInstant(may);
        }
    }
}
