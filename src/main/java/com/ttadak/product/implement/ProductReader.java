package com.ttadak.product.implement;

import com.ttadak.product.domain.Product;
import com.ttadak.product.infrastructure.ProductRepository;
import com.ttadak.shared.entity.EntityStatus;
import com.ttadak.shared.error.CustomException;
import com.ttadak.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ProductReader {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Product find(Long productId) {
        return productRepository.findByIdAndStatus(productId, EntityStatus.ACTIVE)
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    // 재고나 가격을 바꾸려고 읽는다. 응용 계층이나 Manager의 트랜잭션 안에서 부른다.
    public Product findForChange(Long productId) {
        return productRepository.findByIdAndStatusForUpdate(productId, EntityStatus.ACTIVE)
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

}
