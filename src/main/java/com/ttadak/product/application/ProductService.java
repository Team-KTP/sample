package com.ttadak.product.application;

import com.ttadak.product.application.dto.ModifyProductPrice;
import com.ttadak.product.application.dto.NewProductContent;
import com.ttadak.product.application.dto.NewProductSales;
import com.ttadak.product.application.dto.NewProductShipping;
import com.ttadak.product.domain.Product;
import com.ttadak.product.implement.ProductManager;
import com.ttadak.product.implement.ProductReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductReader productReader;
    private final ProductManager productManager;

    public Product register(
        NewProductContent content,
        NewProductSales sales,
        NewProductShipping shipping,
        int memberId
    ) {
        // 회원 조회 했다 가정.
        return productManager.register(
            content.name(),
            content.description(),
            sales.stock(),
            sales.price(),
            shipping.shippingFee(),
            shipping.dispatchDays()
        );
    }

    public void modifyPrice(ModifyProductPrice modifyProductPrice, int ownerId) {
        // NOTE: 관리자 권한 체크 했다 가정.
        productManager.modifyPrice(modifyProductPrice.productId(), modifyProductPrice.price());

        log.info(
            "[ProductService] 상품 가격 수정 - productId={}, price={}",
            modifyProductPrice.productId(),
            modifyProductPrice.price()
        );
    }

    public Product findProduct(Long productId) {
        return productReader.find(productId);
    }

}
