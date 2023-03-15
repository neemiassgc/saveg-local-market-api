package com.api.service;

import com.api.entity.Product;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class CacheManager {

    private final Set<Product> source = new TreeSet<>(Comparator.comparing(Product::getDescription));
    private final ConcurrentMap<String, List<UUID>> cache = new ConcurrentHashMap<>();

    public final void put(final String key, List<Product> value) {
        source.addAll(value);
        final List<UUID> uuidList = value.stream().map(Product::getId).collect(Collectors.toList());
        cache.put(key, uuidList);
    }

    public final Optional<List<Product>> get(final String key) {
        try {
            final List<Product> products = Optional.ofNullable(cache.get(key))
                .orElseThrow()
                .stream()
                .map(uuid -> findByUUID(uuid).orElseThrow())
                .collect(Collectors.toList());
            return Optional.of(products);
        }
        catch (NoSuchElementException ex) {
            return Optional.empty();
        }
    }

    private Optional<Product> findByUUID(final UUID uuid) {
        for (final Product product : source)
            if (product.getId().equals(uuid))
                return Optional.of(product);
        return Optional.empty();
    }
}
