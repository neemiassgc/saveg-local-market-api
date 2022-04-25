package com.api.service;

import com.api.entity.Product;
import com.api.repository.ProductRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductService {

    private final ProductRepository  productRepository;
    private final ForeignProductHttpService foreignProductHttpService;
    private final DomainMapper domainMapper;

    public Product saveByEanCode(@NonNull final String eanCode) {
        return productRepository.findByEanCode(eanCode)
            .or(() -> {
                final Optional<Product> fetchedProduct = this.foreignProductHttpService
                    .fetchByEanCode(eanCode)
                    .map(this.domainMapper::mapToProduct);

                fetchedProduct.ifPresent(productRepository::save);

                return fetchedProduct;
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public Product save(@NonNull final Product product) {
        return productRepository.save(product);
    }

    public Product findByEanCode(@NonNull final String eanCode) {
        return productRepository.findByEanCode(eanCode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }
}