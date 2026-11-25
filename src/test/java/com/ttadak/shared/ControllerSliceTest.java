package com.ttadak.shared;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.TestConstructor;

/**
 * 표현 계층 테스트. 컨트롤러가 의존하는 응용 계층은 테스트가 목으로 대체한다. {@code MockMvcTester}는 테스트가 주입받는다.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@WebMvcTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public @interface ControllerSliceTest {

    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] value() default {};

}
