package com.api.service;

import com.api.entity.Product;
import com.api.projection.SimpleProductWithStatus;
import com.api.repository.ProductRepository;
import com.api.service.interfaces.ProductExternalService;
import com.api.service.interfaces.ProductService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductExternalService productExternalService;

    @Transactional(propagation = Propagation.REQUIRED)
    public SimpleProductWithStatus getByBarcodeAndSaveIfNecessary(@NonNull final String barcode) {
        final Optional<Product> productOptional = productRepository.findByBarcode(barcode);

        if (productOptional.isPresent())
            return productOptional.get().toSimpleProductWithStatus(HttpStatus.OK);

        final Product newProduct = productExternalService.fetchByBarcode(barcode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        save(newProduct);
        return newProduct.toSimpleProductWithStatus(HttpStatus.CREATED);
    }

    @Override
    public void save(@NonNull final Product product) {
        productRepository.save(product);
    }

    @Override
    public List<Product> findAllWithLatestPrice() {
        return productRepository.findAllWithLastPrice();
    }

    @Override
    public List<Product> findAll(@NonNull Sort sort) {
        return productRepository.findAll(sort);
    }

    @Override
    public Page<Product> findAll(@NonNull Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> findAllByUsernameIgnoreCaseContaining(@NonNull String username, @NonNull Pageable pageable) {
        return username.isEmpty() ?
            new PageImpl<>(Collections.emptyList()) :
            productRepository.findAllByDescriptionIgnoreCaseContaining(username, pageable);
    }
}