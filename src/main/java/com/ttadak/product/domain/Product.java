package com.ttadak.product.domain;

import static java.util.Objects.requireNonNull;

import com.ttadak.shared.entity.BaseEntity;
import com.ttadak.shared.error.CustomException;
import com.ttadak.shared.error.ErrorCode;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Product extends BaseEntity {

    private String name;
    private String description;
    private int stock;

    @Embedded
    private Price price;

    @Embedded
    private ShippingPolicy shippingPolicy;

    public static Product register(
        String name,
        String description,
        int stock,
        Price price,
        ShippingPolicy shippingPolicy
    ) {
        validate(name, description, stock);

        Product product = new Product();

        product.name = name;
        product.description = description;
        product.stock = stock;
        product.price = requireNonNull(price, "price");
        product.shippingPolicy = requireNonNull(shippingPolicy, "shippingPolicy");

        return product;
    }

    public void modifyPrice(Price price) {
        this.price = requireNonNull(price, "price");
    }

    public void decreaseStock(int quantity) {
        validateQuantity(quantity);
        if (stock < quantity) {
            throw new CustomException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }
        stock -= quantity;
    }

    public void increaseStock(int quantity) {
        validateQuantity(quantity);
        stock += quantity;
    }

    public boolean isSoldOut() {
        return stock == 0;
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw CustomException.validation("quantity", "수량은 1 이상이어야 합니다.");
        }
    }

    private static void validate(String name, String description, int stock) {
        if (name == null || name.isBlank()) {
            throw CustomException.validation("name", "상품 이름은 비어 있을 수 없습니다.");
        }
        if (description == null || description.isBlank()) {
            throw CustomException.validation("description", "상품 설명은 비어 있을 수 없습니다.");
        }
        if (stock < 0) {
            throw CustomException.validation("stock", "재고는 0 이상이어야 합니다.");
        }
    }

}
