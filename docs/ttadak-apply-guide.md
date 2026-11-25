# 따닥 서비스 적용 가이드

샘플 프로젝트에서 정한 규칙과 수정 사항을 따닥 서버에 옮기기 위한 정리다.

- 규칙의 원천은 서버의 ADR이다. 아래에서 ADR과 달라지는 것은 ADR에도 반영한다.
- 서버 코드와 ADR 원문은 이 정리를 만들며 확인하지 않았다. 충돌 여부는 적용할 때 확인한다.
- 예시의 상품 설명, 배송비, 출고 소요일, 가격 수정은 설명용 가짜 정책이다. 옮기지 않는다.

## 1. 정한 규칙

| # | 규칙 |
|---|---|
| D1 | 한 줄은 100자까지다 |
| D2 | record 컴포넌트는 짧아도 한 줄에 하나씩 쓴다 |
| D3 | record 컴포넌트의 어노테이션은 한 줄에 하나씩 쓰고, 필드는 그 아랫줄에 쓴다. 어노테이션이 있는 record는 컴포넌트 사이를 한 줄 띄운다 |
| D4 | 메서드 매개변수와 호출 인자는 100자를 넘을 때만 줄을 나눈다 |
| D5 | 구현 계층이 VO를 만들고, 애그리거트 루트는 VO를 받는다 |
| D6 | 요청 DTO는 필드 수와 관계없이 개념별 입력 DTO로 묶어서 내린다. 단순 식별자는 그대로 넘길 수 있고, 개념의 일부인 id는 입력 DTO에 담을 수 있다 |
| D7 | 경로의 id와 본문은 요청 DTO의 변환 메서드에서 입력 DTO 하나로 합친다 |
| D8 | API 설명은 `XxxControllerDocs`에, 요청과 응답 DTO의 필드 설명과 예시는 `@Schema`로 단다 |
| D9 | 불변식은 도메인이 지키고, 요청 검증은 같은 규칙을 미리 알려준다 |
| D10 | 현재 시각은 구현 계층에서 넣는다. 한 트랜잭션에서 여러 객체가 공유하면 응용 서비스가 읽어 넘긴다 |
| D11 | 애그리거트는 다른 애그리거트를 연관관계 없이 Id로만 참조한다 |
| D12 | 컨트롤러, `ControllerDocs`, 서비스, `Manager`의 메서드 이름을 같게 짓는다 |
| D13 | `ForUpdate` 같은 인프라 용어는 `infrastructure`의 이름에만 쓴다. 구현 계층의 메서드는 업무의 말로 짓는다 |
| D14 | 다른 개념이면 분리한다. 도메인 사이에 VO를 공유하지 않는다 |

## 2. 포맷 (D1~D4)

### 바꿀 파일

1. `config/spotless/eclipse-formatter.xml`

| 설정 (`org.eclipse.jdt.core.formatter.` 생략) | 값 | 비고 |
|---|---|---|
| `lineSplit` | 100 | 130에서 변경 |
| `comment.line_length` | 100 | 130에서 변경 |
| `alignment_for_record_components` | 49 | 48에서 변경. 항상 줄을 나눈다 |
| `join_wrapped_lines` | false | 그대로 둔다 |
| `use_on_off_tags` | true | 그대로 둔다 |

매개변수와 인자의 정렬 설정(48)은 바꾸지 않았다.

2. `.editorconfig`: `[*.java]`의 `max_line_length = 100`

3. `build.gradle.kts`: `spotless.java`에서 `eclipse()` 바로 다음에 단계를 넣는다.

```kotlin
eclipse().configFile("config/spotless/eclipse-formatter.xml")
// 아래 커스텀 단계의 코드를 바꾸면 숫자를 올린다. 그래야 Spotless가 모든 파일을 다시 검사한다.
bumpThisNumberIfACustomStepChanges(3)
custom("recordAnnotationNewLine", RecordAnnotationNewLine())
```

파일 맨 아래에 클래스를 넣는다.

```kotlin
// record 컴포넌트의 어노테이션은 한 줄에 하나씩 두고, 필드는 그 아랫줄에 둔다. 어노테이션이 있는 record는 컴포넌트 사이를
// 한 줄 띄운다. Eclipse의 통합 옵션(insert_new_line_after_annotation)은 메서드 매개변수까지 나누고, record 전용 옵션은
// 효과가 없어서 Eclipse 다음 단계에서 record 헤더만 고친다.
class RecordAnnotationNewLine : com.diffplug.spotless.FormatterFunc, java.io.Serializable {

    override fun apply(source: String): String {
        val recordHeader =
            Regex("""^( *)(?:(?:public|protected|private|static) )*record \w+(?:<.*>)?\($""")
        val result = mutableListOf<String>()
        val components = mutableListOf<String>()
        var headerEnd: String? = null

        for (line in source.split("\n")) {
            val end = headerEnd
            if (end == null) {
                recordHeader.find(line)?.let { headerEnd = it.groupValues[1] + ")" }
                result.add(line)
            } else if (line.startsWith(end)) {
                result.addAll(format(components))
                result.add(line)
                components.clear()
                headerEnd = null
            } else {
                components.add(line)
            }
        }

        // 닫는 괄호를 찾지 못하면 그대로 둔다.
        return (result + components).joinToString("\n")
    }

    private fun format(lines: List<String>): List<String> {
        val text = """"(?:\\.|[^"\\])*""""
        val annotation = """@[\w.]+(?:\((?:[^()"]|$text|\((?:[^()"]|$text)*\))*\))?"""
        // 들여쓰기, 어노테이션들, 필드. Eclipse가 필드를 이미 다음 줄로 넘겼으면 필드는 없다.
        val annotatedLine = Regex("""^( +)($annotation(?: $annotation)*)(?: ([^@ ].*))?$""")

        val split = lines.flatMap { line ->
            val match = annotatedLine.find(line) ?: return@flatMap listOf(line)
            val (indent, annotations, component) = match.destructured
            val parts = Regex(annotation).findAll(annotations).map { it.value }.toList()

            (parts + listOfNotNull(component.ifEmpty { null })).map { indent + it }
        }

        if (split.none { it.trimStart().startsWith("@") }) {
            return split
        }

        var depth = 0
        return split.flatMapIndexed { index, line ->
            // 문자열 안의 괄호와 쉼표는 세지 않는다. 괄호나 꺾쇠가 모두 닫힌 곳의 쉼표가 컴포넌트의 끝이다.
            val code = line.replace(Regex(text), "")
            depth += code.count { it in "({<" } - code.count { it in ")}>" }
            val endsComponent = depth == 0 && code.trimEnd().endsWith(",")
            val needsBlankLine = endsComponent && split.getOrNull(index + 1)?.isNotBlank() == true

            if (needsBlankLine) listOf(line, "") else listOf(line)
        }
    }
}
```

### 왜 커스텀 단계인가

- record 전용 옵션 `insert_new_line_after_annotation_on_record_parameter`는 샘플에서 효과가 없었다.
- 통합 옵션 `insert_new_line_after_annotation`은 동작하지만 `@PathVariable Long id` 같은 메서드 매개변수까지 나눈다.
- 람다로 쓴 커스텀 단계는 Gradle이 직렬화하지 못해 실패했다. 그래서 `Serializable` 클래스다.

### 시도했지만 쓰지 않은 설정

| 설정 | 쓰지 않은 이유 |
|---|---|
| `join_wrapped_lines=true` | 빌더 체인, AssertJ 체인까지 한 줄로 합친다 |
| `alignment_for_arguments_in_annotation=48` | 닫는 괄호가 뒤에 붙고 `@Schema` DTO와 `@CsvSource`의 모양이 흐트러진다 |
| 위 설정 + `parentheses_positions_in_annotation` | `@ApiResponses({`의 괄호까지 나뉜다 |

### 알아둘 한계

- 직접 나눈 줄은 100자 안에 들어가도 합쳐지지 않는다. D4의 "짧으면 한 줄"은 작성자와 리뷰가 지킨다.
- 긴 호출은 인자보다 `.` 앞에서 먼저 나뉠 수 있다. 인자를 직접 나눠 두면 유지된다.
- 중첩 어노테이션(`@ApiResponses` 등)은 긴 한 줄로 합쳐진다. 지금 샘플은 쓰지 않는다. 쓰게 되면 그 부분을 `// @formatter:off`와 `// @formatter:on`으로 감싼다.
- 100자 제한은 주석에도 적용되어 긴 Javadoc의 줄 끊김이 어색해질 수 있다.
- IDE의 자동 포맷은 커스텀 단계를 실행하지 않는다. 기준은 `./gradlew spotlessApply`다.

## 3. VO와 입력 DTO (D5~D7, D10)

흐름:

```
요청 DTO ──toXxx()──▶ 입력 DTO(application/dto) ──▶ Service(값을 꺼냄) ──▶ Manager(VO 생성, 현재 시각) ──▶ 애그리거트 루트
```

- D5와 D10은 DDD의 필수 원칙이 아니라 팀 컨벤션이다. ADR에도 컨벤션으로 적는다.
- VO는 구현 계층(`XxxManager`)이 만들어 루트에 넘긴다.
- 현재 시각(`LocalDateTime.now()`)은 구현 계층에서 넣는다. 한 트랜잭션에서 여러 객체가 같은 시각을 공유해야 하면 응용 서비스가 한 번 읽어 넘긴다.
- 도메인은 `now()`를 부르지 않고 시각을 값으로 받는다.
- 구현 계층은 `application/dto`를 참조할 수 없어(`LayerArchitectureTest`) 서비스가 값을 꺼내 넘긴다. 같은 타입의 매개변수 순서에 주의한다.
- 요청 DTO는 VO를 만들지 않는다. 입력 DTO까지만 만든다.
- 값의 불변식은 VO의 생성자가 지키고, 루트는 필요한 VO가 있는지와 자신의 불변식을 지킨다. VO를 밖에서 받으므로 `requireNonNull`을 빠뜨리지 않는다.
- 루트의 상태로 계산하는 값(`totalPrice`)은 루트 안에서 만든다.
- 입력 DTO를 넘기는 것은 요청 DTO를 응용 계층에 넘기지 않는 규칙(ADR-0003 8번)과 맞는다.

묶어서 내리기(D6):

| 넘기는 값 | 방법 | 예 |
|---|---|---|
| 요청의 값 | 필드가 적어도 개념으로 묶는다 | `NewProductSales(stock, price)` |
| 개념의 일부인 id | 입력 DTO에 담을 수 있다 | `NewOrderItem(productId, quantity)`, `ModifyProductPrice(productId, price)` |
| 개념에 속하지 않는 단순 식별자 | 그대로 넘긴다 | `memberId`, `ownerId` |
| id 하나만 필요한 조회, 취소 | 그대로 넘긴다 | `findOrder(orderId)`, `cancel(orderId)` |

```java
Product product = productService.register(
    request.toNewProductContent(),
    request.toNewProductSales(),
    request.toNewProductShipping(),
    memberId
);
```

경로의 id와 합치기(D7):

```java
public ModifyProductPrice toModifyProductPrice(Long productId) {
    return new ModifyProductPrice(productId, price);
}

Product product = productService.modifyPrice(request.toModifyProductPrice(productId));
```

VO를 만들어 루트에 넘기기(D5, D10):

```java
// Manager
Receiver receiver = new Receiver(receiverName, receiverPhone, receiverAddress);
Order order = Order.place(productId, unitPrice, quantity, receiver, LocalDateTime.now());

// 루트
order.receiver = requireNonNull(receiver, "receiver");
```

## 애그리거트의 Id 참조 (D11)

```java
// 전: 객체 연관관계
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "product_id")
private Product product;

// 후: Id만 가진다
@Column(nullable = false)
private Long productId;
```

- 루트는 다른 애그리거트의 상태를 바꾸지 않는다. 상품은 `ProductManager`가, 주문은 `OrderManager`가 바꾸고 `OrderService`가 조율한다.
- 두 애그리거트를 한 트랜잭션에서 바꾼다. 에반스의 DDD Reference는 애그리거트 사이는 비동기로 맞추라고 하므로, 초과 판매를 바로 막기 위한 의도한 예외로 ADR에 적는다.
- `Order.place`는 `Product` 대신 `productId`와 주문 시점의 단가 값(`int`)을 받고, 주문 금액은 직접 계산한다.
- **순서:** 재고 확인과 감소 → 주문 생성 → 저장. 뒤에서 실패하면 트랜잭션이 재고를 되돌린다. 취소는 `order.cancel`이 성공한 다음에 재고를 되돌린다.
- 재고 확인과 감소는 `Product.decreaseStock` 하나에 둔다. 먼저 확인하고 나중에 줄이지 않는다. 확인만 따로 트랜잭션으로 나누지도 않는다.
- `decreaseStock`, `increaseStock`은 1보다 작은 수량을 스스로 거절한다. 다른 객체의 검사에 기대지 않는다.
- fetch join이 필요 없어진다. 응답에는 `productId`만 담는다. 상품 이름이 필요하면 따로 조회해 조합해야 하며, **API 응답이 바뀌므로 클라이언트와 맞춘다.**
- 객체 연관관계를 없애면 JPA가 외래 키 제약을 만들지 않는다. 서버에서 제약이 필요하면 스키마에 직접 둔다.
- 재고 규칙(주문하면 줄고 취소하면 돌아온다)은 이제 도메인 단위 테스트가 아니라 서비스 통합 테스트로 확인한다.

## VO를 공유하지 않기 (D14)

> **다른 개념이면 분리한다. 도메인 사이에 VO를 공유하지 않는다.** 다른 도메인에서는 필요한 값만 받아 자기 도메인의 것으로 가진다.

- 판단 기준은 모양이 아니라 **개념**이다. 필드와 검사가 같아 보여도 뜻과 바뀌는 이유가 다르면 합치지 않는다.
- 상품의 `Price`(지금 파는 상품 하나의 가격)와 주문의 `totalPrice`(주문할 때 확정된 총액)는 다른 개념이다.
- **결정:** `totalPrice`는 지금은 VO로 만들지 않고 `int`로 둔다. `OrderAmount` 같은 VO는 만들지 않았다.
- 두 도메인의 값은 `OrderService`가 연결한다(`product.getPrice().amount()`). `order.domain`과 `OrderManager`는 상품의 타입을 import하지 않는다.
- VO가 보장하던 검사는 값을 받는 쪽으로 옮긴다. `Order`는 단가가 0 이상인지 직접 검사한다.
- 컬럼 `total_amount`와 응답 필드 `totalPrice`는 그대로다.

## 재고의 동시성

- **문제:** 잠금 없이 읽으면 여러 주문이 같은 재고를 읽고 계산한 값을 덮어쓴다. 샘플에서 재고 5에 주문 10건이 모두 성공하고 재고는 4가 남았다. `@Transactional`만으로는 막지 못했다.
- **해결:** 비관적 잠금. 규칙은 `Product.decreaseStock`에 그대로 둔다.
- 조회를 둘로 나눈다. 단순 조회 `ProductReader.find`는 잠그지 않고, 바꾸려고 읽는 `findForChange`는 잠근다.
- 상품 행을 바꾸는 **모든** 흐름이 `findForChange`를 쓴다. 가격 수정이 잠그지 않고 읽으면 예전 재고로 덮어쓸 수 있다.
- `findForChange`는 트랜잭션 안에서 부른다.
- 샘플의 동시성 테스트는 동시 주문만 확인한다. 취소나 가격 수정이 섞인 상황은 테스트하지 않았다. H2에서 확인했으므로 서버는 실제 DB로 다시 확인한다.

## 이름 짓기 (D12)

| 대상 | 규칙 | 예 |
|---|---|---|
| 컨트롤러, `ControllerDocs`, 서비스, `Manager` | 같은 이름 | `register`, `create`, `cancel` |
| 조회 | 서비스와 컨트롤러는 `findXxx`, `Reader`는 `find`에 업무의 조건이나 목적을 붙인다 | `findOrder`, `find`, `findForChange`, `findForCancel` |
| 기본 조건 | 삭제되지 않은(`ACTIVE`) 것만 찾는 것은 기본이다. 이름에 `Active`를 적지 않는다 | `ProductReader.find` |
| 인프라 용어 (D13) | `ForUpdate`, `WithLock`, `FetchJoin`은 리포지토리에서만 | `ProductRepository.findByIdAndStatusForUpdate` ← `ProductReader.findForChange` |
| 도메인 | 업무의 동사 | `Order.place` |
| 입력 DTO | 생성은 `New<개념>`, 수정은 `Modify<개념>` | `NewOrderItem`, `ModifyProductPrice` |
| 요청 DTO | `<도메인>…<동사>Request` | `OrderCreateRequest` |
| 로그 접두어 | 로그를 남기는 클래스 이름 | `[OrderManager]` |

## 4. API 문서 (D8)

| 위치 | 어노테이션 |
|---|---|
| `XxxControllerDocs` | `@Tag`, `@Operation`, `@Parameter` |
| 요청과 응답 DTO | `@Schema(description, example)` |
| 컨트롤러 | Swagger 어노테이션을 달지 않는다 |

- 기준 코드: `product/presentation/ProductControllerDocs.java`
- 오류 코드와 상태는 `@Operation`의 `description`에 적는다.

## 5. 검증 (D9)

> 불변식은 도메인이 지키고, 요청 검증은 같은 규칙을 미리 알려준다.

판단 기준: **"API가 아닌 곳에서 만들어도 이 값이면 안 되는가?"**

| 답 | 두는 곳 | 예 |
|---|---|---|
| 그렇다 | 도메인 + 요청 DTO | 빈 상품 이름, 음수 가격 |
| 아니다 | 요청 DTO만 | 이 API에서만 필수인 값, 입력 형식 |

- 도메인은 `CustomException.validation(field, reason)`으로 던진다. `field` 이름은 요청 필드 이름과 맞춘다.
- 적용할 때 요청 DTO의 검증을 하나씩 도메인과 대조한다. 샘플은 `Receiver`의 세 값이 요청에서만 검사되고 있어 compact 생성자에 검사를 넣었다.
- 객체의 메서드는 검사를 끝낸 다음에 자신의 상태를 바꾼다. 여러 객체를 바꾸는 흐름에서 뒤가 실패하면 트랜잭션으로 되돌린다. 샘플은 통합 테스트로 확인한다.
- `@Embeddable` record의 compact 생성자에 검사를 넣으면 DB에서 읽을 때도 실행될 수 있다. 서버에서는 기존 데이터에 빈 값이 있는지 먼저 확인한다.

> 값의 규칙은 그 값을 가진 객체가 검사한다. 사용자 입력의 오류는 `CustomException`, 코드의 실수로만 생기는 `null`은 `requireNonNull`로 막는다.

- VO 안의 값은 VO의 생성자가, 루트가 직접 가진 값은 루트가 `CustomException`으로 검사한다.
- 루트가 받는 VO의 `null`은 `requireNonNull`로 막는다. 구현 계층이 VO를 만들므로 `null`은 코드의 버그이고 500이 맞다.
- 사용자 입력 값에는 `requireNonNull`, `Assert.hasText`를 쓰지 않는다. 현재 `GlobalExceptionHandler`에서 400이 아닌 500이 된다.
- 루트의 값 검사는 `validate(...)` 하나에 모으고, 검사마다 메서드를 만들지 않는다.
- null과 공백 검사를 줄이려면 Spring의 `StringUtils.hasText`를 쓸 수 있다. 샘플 코드에는 아직 적용하지 않았다(선택 사항).

## 6. 적용 순서

- [ ] ADR에 D1~D14 중 새로 정하거나 바뀌는 것을 반영한다
- [ ] 포맷 설정 3개 파일을 바꾼다
- [ ] 100자 안에 들어가는데 나뉘어 있는 매개변수와 인자를 직접 합친다
- [ ] `./gradlew spotlessApply` 후 바뀐 코드를 훑어본다. **포맷만 따로 커밋한다**
- [ ] 요청 DTO의 VO 생성 메서드를 없애고 구현 계층에서 VO를 만든다. 루트의 팩토리는 VO를 받는다
- [ ] 서비스의 `LocalDateTime.now()` 중 여러 객체가 공유하지 않는 것은 구현 계층으로 옮긴다
- [ ] 애그리거트 사이의 객체 연관관계를 Id 참조로 바꾼다
- [ ] 다른 도메인의 VO를 쓰는 곳을 찾아 값만 받도록 바꾼다
- [ ] 계층별 메서드와 DTO 이름을 D12에 맞춘다
- [ ] 요청의 값을 개념별 입력 DTO로 묶는다(필드 수와 관계없이). 단순 식별자는 그대로 둔다
- [ ] `ControllerDocs`와 요청 DTO의 `@Schema`를 채운다
- [ ] 도메인의 불변식 검사와 요청 검증이 같은 기준인지 맞춘다
- [ ] `./gradlew check`

## 7. 아직 정하지 않았거나 확인할 것

| 항목 | 내용 |
|---|---|
| 삭제된 상품의 주문 취소 | 취소할 때 `ProductReader.findForChange`는 `ACTIVE` 상품만 찾는다. 상품 삭제 기능이 생기면 삭제된 상품의 주문을 취소할 수 있는지 정한다 |
| 트랜잭션 위치 | 주문은 `OrderService`에, 상품 등록과 가격 수정은 `ProductManager`에 `@Transactional`이 있다. `OrderManager.cancel`에는 서비스와 겹쳐 있다. 한 계층으로 정한다 |
| 헤더의 id | `memberId`, `ownerId`가 `int`다. 다른 식별자는 `Long`이다. 아직 권한 확인에 쓰지 않는다 |
| 조회 범위 | `ProductReader.find`와 `findForChange`는 `ACTIVE`만 찾는다. `OrderReader`는 상태를 거르지 않는다. 서버에서는 기본 조건을 모든 `Reader`에 맞출지 정한다 |
