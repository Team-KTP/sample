package com.ttadak.shared;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

/**
 * E2E 테스트. 실제 서버를 임의의 포트로 띄워 요청부터 DB까지 확인한다. 매 테스트 전에 DB를 비운다. 포트는 테스트가 {@code @LocalServerPort}로
 * 받는다.
 */
@ActiveProfiles("test")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(DatabaseCleaner.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public @interface E2ETest {

}
