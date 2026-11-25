# 따닥 서버 샘플 프로젝트

서버 팀원이 따닥 서버의 아키텍처와 코드 스타일을 코드로 읽고 이해하기 위한 샘플이다. 서버의 ADR(`DDD_14_TTADAK/server/docs/adr/`)이 정한 규칙을 작은 가짜 도메인 "가게(상품과 주문)"에 모두 적용했다.

- 이 샘플의 정책(`docs/`)은 **가짜**다. 따닥의 정책은 위키가 정한다.
- 규칙의 원천은 서버의 ADR이다. 이 샘플과 ADR이 다르면 ADR이 맞다.
- 서버의 ADR은 아직 제안 상태다. ADR이 바뀌면 이 샘플도 바뀐다.

## 실행

```bash
./gradlew bootRun     # H2 메모리 DB로 뜬다. Docker가 필요 없다
./gradlew check       # 포맷 검사(Spotless)와 전체 테스트(ArchUnit 포함)
```

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI 문서: http://localhost:8080/v3/api-docs

## 읽는 순서

0. `docs/ttadak-apply-guide.md`: 여기서 정한 것을 따닥 서버에 적용하는 방법
1. `docs/domain/01-store.md`: 용어와 규칙(R)
2. `docs/spec/01-store-api.md`: API와 수용 기준(AC)
3. `product/domain/`, `order/domain/`: 규칙이 사는 곳
4. `order/application/OrderService`: 흐름과 트랜잭션
5. `order/presentation/`: 요청과 응답
6. `src/test/`: 규칙이 테스트로 어떻게 확인되는가

## 구조

```
com.ttadak
├── shared/                    서버의 shared를 복사했다 (기능 오류 코드를 더하고 GlobalExceptionHandler를 web으로 옮겼다)
│   ├── entity/                BaseEntity, EntityStatus
│   ├── error/                 ErrorCode, CustomException, FieldViolation
│   ├── response/              ApiResponse, ErrorMessage
│   └── web/                   GlobalExceptionHandler
├── product/                   상품 도메인
│   ├── presentation/          ProductController, ProductControllerDocs(Swagger)
│   │   └── dto/               ProductRegisterRequest, ProductPriceModifyRequest, ProductResponse
│   ├── application/           ProductService(유스케이스, 트랜잭션)
│   │   └── dto/               NewProductContent, NewProductSales, NewProductShipping, ModifyProductPrice(응용 계층 입력 DTO)
│   ├── implement/             ProductReader(읽기), ProductManager(쓰기)
│   ├── domain/                Product, Price, ShippingPolicy
│   └── infrastructure/        ProductRepository
└── order/                     주문 도메인
    ├── presentation/          OrderController, OrderControllerDocs(Swagger)
    │   └── dto/               OrderCreateRequest, OrderResponse
    ├── application/           OrderService
    │   └── dto/               NewOrderItem, NewOrderReceiver(응용 계층 입력 DTO)
    ├── implement/             OrderReader, OrderManager
    ├── domain/                Order, OrderState, Receiver
    └── infrastructure/        OrderRepository
```

먼저 도메인으로 나누고, 도메인 안을 계층으로 나눈다. 한 도메인의 코드는 한 폴더에 모인다.

### 도메인 사이의 의존

```
order ──▶ product        (product는 order를 모른다)
```

| order가 쓰는 것 | 왜 |
|---|---|
| `product.domain.Product` | `OrderService`만 쓴다. 재고를 줄인 상품의 id와 단가 **값**을 꺼내 주문에 넘긴다. `order.domain`과 `OrderManager`는 상품의 타입을 모른다 |
| `product.implement.ProductManager` | 재고를 줄이고 늘린다. 상품의 변경은 상품 도메인이 맡는다. 다른 도메인의 서비스(`ProductService`)는 부르지 않는다(ADR-0003 4번) |

- 의존은 한 방향이다. `product`가 `order`를 참조하면 순환 검사(`LayerArchitectureTest`)가 실패한다.
- 다른 도메인에서 쓰는 것은 도메인 객체와 구현 계층의 컴포넌트까지다. 다른 도메인의 응용 계층과 표현 계층은 쓰지 않는다.

### VO는 도메인 사이에 공유하지 않는다

**다른 개념이면 분리한다. 도메인 사이에 VO를 공유하지 않는다.** 다른 도메인에서는 필요한 값만 받아 자기 도메인의 것으로 가진다.

상품의 `Price`(지금 파는 상품 하나의 가격)와 주문의 `totalPrice`(주문할 때 확정된 총액)는 다른 개념이다. `totalPrice`는 지금은 VO로 만들지 않고 `int`로 둔다.

### 애그리거트는 Id로 참조한다

애그리거트는 다른 애그리거트를 객체 연관관계(`@ManyToOne` 등)로 가리키지 않고 **Id만 가진다.** `Order`는 `productId`를 가진다. 두 애그리거트가 함께 바뀌어야 하면 구현 계층이 각각에게 시킨다.

## 요청 하나를 따라가기: 주문

```
POST /orders  {productId, quantity, receiverName, receiverPhone, receiverAddress}   ← 클라이언트의 모양(평평하다)
  │
  ▼ OrderController.create
  │   @Valid로 형식만 확인한다. 규칙은 판단하지 않는다
  │   request.toNewOrderItem() → NewOrderItem(productId, quantity)          주문 항목: 무엇을 얼마나
  │   request.toNewOrderReceiver() → NewOrderReceiver(name, phone, address)  받는 사람
  │
  ▼ OrderService.create(NewOrderItem item, NewOrderReceiver receiver, int memberId)  @Transactional
  │   ProductManager.decreaseStock(productId, quantity)    1. 재고를 먼저 확보한다
  │     ├ ProductReader.findForChange(productId)       바꾸려고 읽는다. 조회와 "없음" 처리는 Reader가 한다
  │     └ product.decreaseStock(quantity)              수량과 재고는 Product가 판단한다
  │   OrderManager.create(productId, 단가, quantity, name, phone, address)    2. 주문을 만든다
  │     ├ new Receiver(name, phone, address)         VO를 만든다. 값이 틀리면 여기서 거절된다
  │     ├ Order.place(productId, 단가, quantity, receiver, now)    수량을 확인하고 주문 금액을 계산한다
  │     ├ orderRepository.save(order)
  │     └ log.info("[OrderManager] 주문 - orderId={}, productId={}")    개인정보는 남기지 않는다
  │
  ▼ OrderController
      OrderResponse.from(order)                 정적 팩토리와 private 빌더로 응답을 만든다
      ApiResponse.success(...)
```

재고가 모자라면 `Product.decreaseStock`이 `CustomException(PRODUCT_OUT_OF_STOCK)`을 던지고 트랜잭션이 되돌아간다. 던지는 쪽은 로그를 남기지 않는다. `GlobalExceptionHandler`가 409 응답을 만들고 로그를 한 번 남긴다.

## ADR 대응표

### ADR-0003 아키텍처

| # | 규칙 | 어디서 보나 | 지키는 것 |
|---|---|---|---|
| 1 | 의존은 표현 → 응용 → (구현) → 인프라 한 방향 | `LayerArchitectureTest`의 첫 규칙 | ArchUnit |
| 2 | 도메인은 다른 계층을 참조하지 않는다 | `product/domain/`, `order/domain/` 전체 | ArchUnit |
| 3 | 순환 의존 금지 | `LayerArchitectureTest`의 순환 규칙 | ArchUnit |
| 4 | 응용 계층 클래스끼리 부르지 않는다 | `OrderService`는 `ProductService`를 부르지 않고 `ProductManager`를 쓴다 | ArchUnit |
| 5 | 표현 계층은 규칙을 판단하지 않는다 | 컨트롤러는 변환과 위임만 한다 | 리뷰 |
| 6 | 응용 계층은 흐름과 트랜잭션만 | `OrderService`에 if문이 없다 | 리뷰 |
| 7 | 인프라는 규칙을 판단하지 않는다 | 리포지토리는 조회와 잠금만 한다 | 리뷰 |
| 8 | 요청 DTO를 응용 계층에 넘기지 않는다 | `OrderCreateRequest`에서 `NewOrderItem`과 `NewOrderReceiver`를 꺼내 넘긴다 | ArchUnit |
| 9 | 입력 DTO는 도메인 개념대로 묶는다 | 아래 "응용 계층에 넘기는 값" | 리뷰 |
| 10 | 표현 계층은 상태 변경 메서드를 부르지 않는다 | 컨트롤러와 응답 DTO는 getter와 `isSoldOut()`만 쓴다 | 리뷰 |
| 11 | 응용 계층은 엔티티를 반환한다 | `ProductService.register`, `OrderService.create`, `findOrder` | 리뷰 |
| 12 | 표현 계층이 읽을 값은 응용 계층이 미리 읽는다 | 응답은 애그리거트 하나의 값만 쓴다. 다른 애그리거트는 Id로 응답한다 | 리뷰 |
| 13, 14 | 응답 DTO는 `from`/`of`와 private 빌더 | `ProductResponse`, `OrderResponse`(중첩 레코드 포함) | 리뷰 |
| 15 | 겪지 않은 문제에 추상화를 만들지 않는다 | 리포지토리 인터페이스(포트)가 없다. `Validator`는 쓸 곳이 생길 때 만든다 | 리뷰 |
| 16 | 객체에게 판단을 시킨다(TDA) | `product.decreaseStock()`, `order.cancel()`, `order.isCanceled()` | 리뷰 |
| 17 | 결합도는 낮게, 응집도는 높게 | 재고 규칙이 모두 `Product` 안에 있다 | 리뷰 |

### 응용 계층에 넘기는 값

응용 계층에는 요청 DTO의 모양이 아니라 **도메인 개념**으로 묶어 넘긴다. 필드 수와 관계없이, 적어도 개념으로 묶는다.

- `memberId`, `ownerId`처럼 개념에 속하지 않는 **단순 식별자는 그대로 넘긴다.**
- `productId`처럼 **그 개념의 일부인 id는 입력 DTO에 담을 수 있다.** 예: `NewOrderItem(productId, quantity)`, `ModifyProductPrice(productId, price)`
- 조회나 취소처럼 id 하나만 필요하면 id를 그대로 넘긴다.

| 유스케이스 | 클라이언트의 모양 | 응용 계층의 시그니처 | 이유 |
|---|---|---|---|
| 주문 | `productId, quantity, receiverName, receiverPhone, receiverAddress` (평평하다) | `create(NewOrderItem item, NewOrderReceiver receiver, int memberId)` | 값 다섯 개가 "주문 항목(무엇을 얼마나)"과 "받는 사람" 두 개념이다 |
| 상품 등록 | `name, description, stock, price, shippingFee, dispatchDays` (평평하다) | `register(NewProductContent content, NewProductSales sales, NewProductShipping shipping, int memberId)` | 값 여섯 개가 "상품 소개", "판매 조건", "배송 조건" 세 개념이다 |
| 가격 수정 | 경로의 `productId`, 본문의 `price` | `modifyPrice(ModifyProductPrice modifyProductPrice, int ownerId)` | 무엇을(상품) 어떻게(가격) 바꾸는지가 하나의 요청이다 |
| 조회, 취소 | 경로의 id | `findOrder(Long orderId)`, `cancel(Long orderId)` | 단순한 식별자는 그대로 넘긴다(ADR-0003 8번) |

### 이름 짓기

| 대상 | 규칙 | 예 |
|---|---|---|
| 컨트롤러, `ControllerDocs`, 서비스의 메서드 | 같은 이름을 쓴다 | `register`, `modifyPrice`, `findProduct`, `create`, `findOrder`, `cancel` |
| `Manager`의 메서드 | 서비스와 같은 이름 | `ProductManager.register`, `OrderManager.create` |
| `Reader`의 메서드 | `find`로 시작하고 업무의 조건이나 목적을 붙인다. `ForUpdate`, `WithLock`, `FetchJoin` 같은 인프라 용어는 `infrastructure`에서만 쓴다 | `ProductReader.find`, `findForChange`, `OrderReader.findForCancel`. 삭제되지 않은(`ACTIVE`) 것만 찾는 것은 기본이라 이름에 적지 않는다. 인프라: `ProductRepository.findByIdAndStatusForUpdate`, `OrderRepository.findByIdForUpdate` |
| 도메인 메서드 | 업무의 동사 | `Product.register`, `Order.place` |
| 생성에 쓰는 입력 DTO | `New<개념>` | `NewOrderItem`, `NewProductSales` |
| 수정에 쓰는 입력 DTO | `Modify<개념>` | `ModifyProductPrice` |
| 요청 DTO | `<도메인>…<동사>Request` | `ProductRegisterRequest`, `ProductPriceModifyRequest`, `OrderCreateRequest` |
| 변환 메서드 | `to<입력 DTO 이름>` | `toNewOrderItem()`, `toModifyProductPrice(productId)` |
| 로그 접두어 | 로그를 남기는 클래스 이름 | `[ProductManager]`, `[OrderService]` |

#### 예시: 개념으로 묶어서 넘기기

요청 DTO의 필드를 하나씩 늘어놓아 넘기지 않는다. 요청 DTO가 개념별 입력 DTO(`application/dto`)로 묶어서 내린다.

```java
// ProductRegisterRequest: 평평한 필드 여섯 개를 세 개념으로 묶는다
public NewProductContent toNewProductContent() {
    return new NewProductContent(name, description);
}

public NewProductSales toNewProductSales() {
    return new NewProductSales(stock, price);
}

public NewProductShipping toNewProductShipping() {
    return new NewProductShipping(shippingFee, dispatchDays);
}

// ProductController
Product product = productService.register(
    request.toNewProductContent(),
    request.toNewProductSales(),
    request.toNewProductShipping(),
    memberId
);
```

```java
// 피한다: 같은 타입이 나란히 있어 순서가 바뀌어도 컴파일된다
productService.register(
    request.name(),
    request.description(),
    request.stock(),
    request.price(),
    request.shippingFee(),
    request.dispatchDays()
);
```

#### 예시: 경로의 id와 본문을 함께 넘길 때

요청 DTO의 변환 메서드가 경로의 id를 받아, 본문의 값과 합친 입력 DTO 하나를 만든다.

```java
public record ProductPriceModifyRequest(
    int price
) {

    public ModifyProductPrice toModifyProductPrice(Long productId) {
        return new ModifyProductPrice(productId, price);
    }

}

// ProductController: PUT /products/{productId}/price
productService.modifyPrice(request.toModifyProductPrice(productId), ownerId);
```

#### 애그리거트 루트는 VO를 받는다

구현 계층이 VO를 만들고, 루트의 팩토리와 상태 변경 메서드는 완성된 VO를 받는다. 구현 계층은 `application/dto`를 참조할 수 없으므로 서비스가 입력 DTO에서 값을 꺼내 넘긴다. DDD의 필수 원칙이 아니라 이 프로젝트의 컨벤션이다.

```java
// OrderManager
Receiver receiver = new Receiver(receiverName, receiverPhone, receiverAddress);
Order order = Order.place(productId, unitPrice, quantity, receiver, LocalDateTime.now()); // unitPrice는 int

// Order(루트)
order.totalPrice = unitPrice * quantity; // 루트가 계산한다
order.receiver = requireNonNull(receiver, "receiver");
```

- VO는 구현 계층(`XxxManager`)이 만들어 루트에 넘긴다.
- 현재 시각(`LocalDateTime.now()`)은 구현 계층에서 넣는다. 한 트랜잭션에서 여러 객체가 같은 시각을 공유해야 하면 응용 서비스가 한 번 읽어 넘긴다.
- 도메인은 `now()`를 부르지 않고 시각을 값으로 받는다.
- 값의 불변식은 VO의 생성자가 지킨다. 어디서 `new`를 해도 잘못된 VO는 만들어지지 않는다.
- 루트는 필요한 VO가 있는지(null이 아닌지)를 `requireNonNull`로 확인하고, 자신의 불변식을 지킨다.
- 요청 DTO는 VO를 만들지 않고 입력 DTO까지만 만든다.
- 루트의 매개변수는 개념 단위로 짧아진다. 구현 계층의 메서드는 값을 풀어서 받으므로 같은 타입의 순서에 주의한다.

### 구현 계층의 컴포넌트

서비스는 흐름과 트랜잭션만 가지고, 세부 작업은 구현 계층(`implement/`)의 컴포넌트에 맡긴다. 서비스는 리포지토리를 직접 쓰지 않는다.

| 컴포넌트 | 맡는 일 | 샘플의 예 |
|---|---|---|
| `XxxReader` | 읽기. 조회, "없음" 처리. 단순 조회와 바꾸려고 읽는 조회를 나눈다. 어떻게 읽는지(잠금 등)는 리포지토리가 정한다 | `ProductReader.find`, `findForChange`, `OrderReader.find`, `findForCancel` |
| `XxxManager` | 쓰기. VO와 도메인 객체를 만들거나 일을 시키고 저장한다 | `ProductManager.register`, `OrderManager.create`, `cancel` |
| (필요해지면) `XxxValidator` 등 | 읽기나 쓰기에 넣기 애매한 일(여러 대상을 보는 검증 등) | 샘플에는 아직 없다. 필요해질 때 일에 맞는 이름으로 만든다 |

- 여러 서비스가 같은 일을 하면 구현 계층의 컴포넌트를 함께 쓴다. `ProductManager`는 `ProductService`와 `OrderService`가 함께 쓴다. 서비스끼리는 부르지 못한다(4번).
- 규칙은 컴포넌트가 아니라 도메인 객체가 판단한다. `OrderManager.cancel`은 `order.cancel(now)`에게 시키기만 한다.

트랜잭션 위치는 아직 정리 중이다. 주문은 응용 계층(`OrderService`)에 `@Transactional`이 있고, 상품의 등록과 가격 수정은 `ProductManager`에 있다.

### ADR-0006 코드 컨벤션

| # | 규칙 | 어디서 보나 |
|---|---|---|
| 1.1, 1.5 | 이름은 용어 표의 말로 짓는다 | `docs/domain/01-store.md`의 용어 표 ↔ 클래스와 메서드 이름 |
| 1.6, 1.7 | 업무의 말로 이름을 짓고 `setXxx`를 쓰지 않는다 | `place`, `cancel`, `decreaseStock`, `register` |
| 2.1 | 로그에 개인정보를 남기지 않는다 | 주문 로그에 받는 사람 정보가 없다 |
| 2.3 | 던지거나 남기거나 하나만 | 도메인은 던지기만 하고 `GlobalExceptionHandler`만 남긴다 |
| 2.5 | `[클래스명] 명사형 설명 - key=value` | `[OrderManager] 주문 - orderId=1, productId=1` |
| 3.1 | 업무 상태는 `status`와 다른 이름 | `Order.state`(`OrderState`). `status`는 `BaseEntity`의 삭제 상태다 |
| 3.2 | 팩토리는 도메인 동사로 | `Product.register`, `Order.place` |
| 3.3, 3.4 | `var`는 테스트에서만 | `src/main`은 모두 명시적 타입이다 |
| C2 | 매개변수 2개 이상은 줄바꿈하면 한 줄에 하나 | `Order.place(...)`, `ProductService.register(...)` |
| C3~C6 | 엔티티: `BaseEntity` 상속, `protected` 기본 생성자, 매개변수 있는 생성자 없음, 팩토리에서 `new` | `Product`, `Order` |
| C7 | 응답 DTO는 레코드와 `@Builder(access = PRIVATE)`, 중첩 레코드도 같다 | `OrderResponse.ReceiverResponse` |

### ADR-0007 오류 응답

| # | 규칙 | 어디서 보나 |
|---|---|---|
| 1~4 | 성공과 오류 모두 `ApiResponse`. 오류의 HTTP 상태 = `error.status` | 모든 컨트롤러 |
| 10, 11 | 기능 오류 코드는 `<기능>_<사유>`이고 기능별로 묶는다 | `ErrorCode`의 `// PRODUCT`, `// ORDER` |
| 14 | 업무 오류는 `CustomException`으로 던진다 | `Product.decreaseStock`, `Order.cancel`, 각 `Reader` |
| 15 | 도메인의 입력 검증 실패는 `CustomException.validation(field, reason)` | `Price`, `ShippingPolicy`, `Receiver`, `Product.register`, `Order.place` |

### 검증의 위치

**불변식은 도메인이 지키고, 요청 검증은 같은 규칙을 미리 알려준다.**

**값의 규칙은 그 값을 가진 객체가 검사한다. 사용자 입력의 오류는 `CustomException`, 코드의 실수로만 생기는 `null`은 `requireNonNull`로 막는다.**

| 대상 | 검사하는 곳 | 방법 |
|---|---|---|
| VO 안의 값 | VO의 생성자 | `CustomException.validation` |
| 루트가 직접 가진 값 | 루트 | `CustomException.validation` |
| 루트가 받는 VO가 `null`인지 | 루트 | `requireNonNull`. 구현 계층이 VO를 만들므로 `null`은 코드의 버그다(500) |

"API가 아닌 곳에서 만들어도 이 값이면 안 되는가?"로 판단한다. 그렇다면 도메인과 요청 DTO에 두고, 아니라면 요청 DTO에만 둔다.

### Swagger 문서의 분리

API의 설명(`@Operation`, `@Parameter`)은 `XxxControllerDocs`(추상 클래스)에 둔다. 컨트롤러는 그 클래스를 상속하고 `@Override`로 매핑만 가지며, Swagger 어노테이션을 달지 않는다. 요청과 응답 DTO의 필드 설명과 예시는 DTO에 `@Schema`로 단다. 오류 코드는 `@Operation`의 설명에 적는다.

```java
public record ProductPriceModifyRequest(
    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    @Schema(description = "가격", example = "100000")
    int price
) {
}
```

```java
@Tag(name = "Order", description = "주문")
public abstract class OrderControllerDocs {
    @Operation(summary = "주문", description = "...")
    public abstract ApiResponse<OrderResponse> create(int memberId, OrderCreateRequest request);
}

@RestController
class OrderController extends OrderControllerDocs {
    @Override
    @PostMapping
    public ApiResponse<OrderResponse> create(
        @RequestHeader int memberId,
        @Valid @RequestBody OrderCreateRequest request
    ) { ... }
}
```

### ADR-0004 테스트 전략

| 종류 | 파일 | 확인하는 것 | 해당 규칙 |
|---|---|---|---|
| 단위 | `PriceTest`, `ProductTest`, `OrderTest` | 규칙의 진리표 전부. 경계값 세 점(재고 3에 수량 2, 3, 4) | V2, V4.1, N4(매개변수화) |
| 응용 계층 통합 | `ProductServiceTest`, `OrderServiceTest` | 흐름이 DB에 반영되는가(주문과 취소의 재고 변화 포함), "없음" 처리 | E1.3(실제 DB), V4.2(조합을 반복하지 않는다) |
| 동시성 | `OrderConcurrencyTest` | 재고 5에 주문 10건 → 성공은 정확히 5건, 재고 0 | E3(불변식만, 누가 성공했는지는 보지 않는다) |
| 표현 계층 | `ProductControllerTest`, `OrderControllerTest` | HTTP 약속(응답 모양, 검증 실패의 필드). 응용 계층은 대역이다 | V3 |
| E2E | `StoreScenarioTest` | 대표 흐름 하나(등록 → 주문 → 취소 → 재고 복구) | V5 |
| 구조 | `LayerArchitectureTest` | ADR-0003의 1~4번, 8번 | |

모든 테스트에 공통으로 적용한 것:

- 이름은 "조건 + 행동 → 결과" 한 문장이다(N2). 기술 용어(상태 코드, 예외 클래스)를 이름에 쓰지 않는다.
- `@Covers("R-1.6")`로 확인하는 요구의 ID를 단다(N2.2).
- 준비, 실행, 검증을 빈 줄로 나눈다(N3).
- 객체는 `ProductFixture`, `OrderFixture`로 만들고, 결과를 좌우하는 값만 테스트에 직접 적는다(E4).
- 도메인에는 시각을 값으로 넘긴다(E2). 구현 계층이 `LocalDateTime.now()`를 읽는다. 여러 객체가 같은 시각을 공유해야 하면 서비스가 읽어 넘긴다.
- `@IntegrationTest`와 `@E2ETest`는 매 테스트 전에 DB를 비운다(`DatabaseCleaner`, E5). 롤백에 기대지 않는다.

## 서버와 다른 점

| 항목 | 서버 | 샘플 | 이유 |
|---|---|---|---|
| Spring Security | 있다 | 없다 | 인증 방식이 아직 정해지지 않았다. 샘플을 바로 띄우려고 뺐다 |
| 기본 DB | 로컬은 compose의 PostgreSQL | H2 메모리 | Docker 없이 바로 띄우려고 |
| Swagger | `webmvc-api`(문서만) | `webmvc-ui`(화면 포함) | 팀원이 화면으로 보려고 |
| `DatabaseCleaner` | 아직 없다 | 있다 | ADR-0004 E5를 보여 주려고. 서버에 옮길지는 팀이 정한다 |
| `GlobalExceptionHandler`의 위치 | `shared/error/` | `shared/web/` | `shared` 안의 순환 의존을 없애려고(아래 "샘플을 만들며 찾은 것") |
| Flyway | 운영에서 켠다 | 없다 | 샘플에 운영 환경이 없다 |

## ADR에 없어서 샘플이 정한 것 (확인이 필요하다)

아래는 ADR이 정하지 않아서 샘플이 임의로 정했다. 서버에 그대로 가져가기 전에 팀이 정한다.

| 항목 | 샘플의 선택 |
|---|---|
| 다른 도메인을 쓰는 범위 | 도메인 객체와 구현 계층의 컴포넌트(`ProductManager`)까지. 컨텍스트 사이의 통신 방식(이벤트, 조회 인터페이스)은 ADR-0003의 "정하지 않은 것" 1번으로 아직 열려 있다 |
| DTO의 위치 | 계층마다 `dto/`(`presentation/dto`, `application/dto`). VO(`Receiver`, `Price`, `ShippingPolicy`)는 구현 계층이 만들어 루트에 넘긴다 |
| 요청 DTO → 개념 변환 | 요청 DTO의 `toNewOrderItem()`, `toNewOrderReceiver()`, `toNewProductContent()` 등 |
| 구현 계층 클래스의 이름 | 읽기는 `XxxReader`, 쓰기는 `XxxManager`. 그 밖의 일은 필요해질 때 일에 맞는 이름(`XxxValidator` 등) |
| 표현 계층이 인프라를 직접 써도 되는가 | ADR-0003 1번의 "건너뛰어도 된다"에 따라 ArchUnit은 허용한다. 샘플에서 실제로 쓰는 곳은 없다 |
| 반환할 값이 없는 유스케이스 | `OrderService.cancel`은 `void`다. ADR-0003 11번은 "반환한다면 엔티티"로 읽었다 |
| 재고의 동시성 | 비관적 잠금(`SELECT ... FOR UPDATE`). 상품을 바꾸는 흐름(재고 감소와 복구, 가격 수정)은 모두 `ProductReader.findForChange`로 읽는다. 단순 조회는 잠그지 않는다 |
| 다른 도메인의 상태 변경 | 상품은 `ProductManager`가, 주문은 `OrderManager`가 바꾸고 `OrderService`가 한 트랜잭션에서 조율한다. 초과 판매를 바로 막아야 해서 애그리거트 둘을 함께 바꾸는 것을 의도한 예외로 둔다 |
| 도메인이 `CustomException`을 던지는가 | 던진다. ADR-0007의 "정하지 않은 것" 1번(도메인이 `shared`를 참조해도 되는가)은 아직 열려 있다 |
| Fixture의 모양 | `ProductFixture.product().stock(3).create()` 같은 빌더 |

## 샘플을 만들며 찾은 것

- 서버의 `shared`에 순환 의존이 있다: `shared.error.GlobalExceptionHandler` → `shared.response.ApiResponse` → `shared.error.ErrorCode`. ADR-0003 3번과 부딪힌다.
- 샘플은 `GlobalExceptionHandler`를 `shared/web/`으로 옮겨 이 순환을 없앴다. 이제 의존은 `web` → `response` → `error` 한 방향이다. 순환 검사는 도메인 사이(`com.ttadak.(*)`)와 도메인 안의 패키지 사이(`com.ttadak.(*).(*)`, `shared`, `product`, `order` 모두)에 건다.
- 서버는 아직 옮기지 않았다. 옮길지는 팀이 정한다.
