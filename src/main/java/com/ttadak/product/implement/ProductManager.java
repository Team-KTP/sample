package com.ttadak.product.implement;

import com.ttadak.product.domain.Price;
import com.ttadak.product.domain.Product;
import com.ttadak.product.domain.ShippingPolicy;
import com.ttadak.product.infrastructure.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductManager {

    private final ProductReader productReader;
    private final ProductRepository productRepository;

    @Transactional
    public Product register(
        String name,
        String description,
        int stock,
        int price,
        int shippingFee,
        int dispatchDays
    ) {
        Product product = productRepository.save(
            Product.register(
                name,
                description,
                stock,
                new Price(price),
                new ShippingPolicy(shippingFee, dispatchDays)
            )
        );

        log.info("[ProductManager] 상품 등록 - productId={}", product.getId());

        return product;
    }

    @Transactional
    public void modifyPrice(Long productId, int price) {
        Product product = productReader.findForChange(productId);
        product.modifyPrice(new Price(price));
    }

    @Transactional
    public Product decreaseStock(Long productId, int quantity) {
        Product product = productReader.findForChange(productId);
        product.decreaseStock(quantity);

        return product;
    }

    @Transactional
    public void increaseStock(Long productId, int quantity) {
        Product product = productReader.findForChange(productId);
        product.increaseStock(quantity);
    }

}
