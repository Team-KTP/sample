package com.ttadak.product.fixture;

import com.ttadak.product.domain.Price;
import com.ttadak.product.domain.Product;
import com.ttadak.product.domain.ShippingPolicy;

public final class ProductFixture {

    private String name = "기본 상품";
    private int stock = 10;
    private int price = 1000;
    private String description = "기본 설명";
    private int shippingFee = 3000;
    private int dispatchDays = 1;

    private ProductFixture() {
    }

    public static ProductFixture product() {
        return new ProductFixture();
    }

    public ProductFixture name(String name) {
        this.name = name;
        return this;
    }

    public ProductFixture stock(int stock) {
        this.stock = stock;
        return this;
    }

    public ProductFixture price(int price) {
        this.price = price;
        return this;
    }

    public Product create() {
        return Product.register(
            name,
            description,
            stock,
            new Price(price),
            new ShippingPolicy(shippingFee, dispatchDays)
        );
    }

}
