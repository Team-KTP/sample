package com.ttadak.product.presentation;

import com.ttadak.product.application.ProductService;
import com.ttadak.product.domain.Product;
import com.ttadak.product.presentation.dto.ProductPriceModifyRequest;
import com.ttadak.product.presentation.dto.ProductRegisterRequest;
import com.ttadak.product.presentation.dto.ProductResponse;
import com.ttadak.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/products")
@RequiredArgsConstructor
@RestController
class ProductController extends ProductControllerDocs {

    private final ProductService productService;

    @Override
    @PostMapping
    public ApiResponse<ProductResponse> register(
        @RequestHeader int memberId,
        @Valid @RequestBody ProductRegisterRequest request
    ) {
        Product product = productService.register(
            request.toNewProductContent(),
            request.toNewProductSales(),
            request.toNewProductShipping(),
            memberId
        );

        return ApiResponse.success(ProductResponse.from(product));
    }

    @Override
    @PutMapping("/{productId}/price")
    public ApiResponse<Void> modifyPrice(
        @PathVariable Long productId,
        @RequestHeader int ownerId,
        @Valid @RequestBody ProductPriceModifyRequest request
    ) {
        productService.modifyPrice(request.toModifyProductPrice(productId), ownerId);

        return ApiResponse.success();
    }

    @Override
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> findProduct(@PathVariable Long productId) {
        Product product = productService.findProduct(productId);

        return ApiResponse.success(ProductResponse.from(product));
    }

}
