package com.api.service;

import com.api.entity.Price;
import com.api.repository.PriceRepository;
import com.api.repository.ProductRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static com.api.projection.Projection.ProductBase;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PersistenceService {

    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;
    private final ProductExternalService productExternalService;
    private final DomainMapper domainMapper;

    @SuppressWarnings("unchecked")
    public <P extends ProductBase> P findProductByBarcode(@NonNull final String barcode, int limit) {
        final List<Price> priceList =
        priceRepository.findAllByProductBarcode(
            barcode,
            PageRequest.ofSize(limit == 0 ? Integer.MAX_VALUE : limit)
        );

        if (!priceList.isEmpty())
            return (P) domainMapper.toProductWithManyPrices(priceList);

        final Optional<ProductBase> optionalProductBase = productExternalService.fetchByEanCode(barcode);

        final ProductBase productBase = optionalProductBase
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        priceRepository.save(domainMapper.mapToPrice(productBase));

        return (P) productBase;
    }

    @SuppressWarnings("unchecked")
    public <P extends ProductBase> List<P> findAllProducts() {
        final List<Price> priceList = priceRepository.findAll();
        return (List<P>) domainMapper.toProductListWithManyPrices(priceList);
    }

    public <P extends ProductBase> P findProductByBarcode(@NonNull final String barcode) {
        return findProductByBarcode(barcode, Integer.MAX_VALUE);
    }
}