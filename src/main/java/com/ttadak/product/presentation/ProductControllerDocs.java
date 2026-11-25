package com.ttadak.product.presentation;

import com.ttadak.product.presentation.dto.ProductPriceModifyRequest;
import com.ttadak.product.presentation.dto.ProductRegisterRequest;
import com.ttadak.product.presentation.dto.ProductResponse;
import com.ttadak.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product", description = "상품 등록, 조회, 가격 수정")
public abstract class ProductControllerDocs {

    @Operation(summary = "상품 등록", description = """
        새 상품을 등록하고 등록한 상품을 돌려준다.

        - 이름과 설명은 비어 있을 수 없다.
        - 재고, 가격, 배송비는 0 이상이다.
        - 출고 소요일은 1 이상이다.
        - 재고를 0으로 등록하면 품절 상태가 된다.
        """)
    public abstract ApiResponse<ProductResponse> register(
        @Parameter(in = ParameterIn.HEADER, description = "요청한 회원의 id", example = "1") int memberId,
        ProductRegisterRequest request
    );

    @Operation(summary = "상품 조회", description = "id로 상품 하나를 조회한다. 없으면 PRODUCT_NOT_FOUND(404)다.")
    public abstract ApiResponse<ProductResponse> findProduct(
        @Parameter(description = "상품 id", example = "1") Long productId
    );

    @Operation(summary = "상품 가격 수정", description = """
        상품의 가격을 바꾼다. 돌려주는 값은 없다.

        - 가격은 0 이상이다.
        - 이미 한 주문의 주문 금액은 바뀌지 않는다.
        - 상품이 없으면 PRODUCT_NOT_FOUND(404)다.
        """)
    public abstract ApiResponse<Void> modifyPrice(
        @Parameter(description = "상품 id", example = "1") Long productId,
        @Parameter(in = ParameterIn.HEADER, description = "상품 소유자의 id", example = "1") int ownerId,
        ProductPriceModifyRequest request
    );

}
