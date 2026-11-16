package com.pe.pivot;

import static org.assertj.core.api.Assertions.assertThat;

import com.pe.pivot.credit.MsCreditApplication;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lightweight smoke tests for {@link MsCreditApplication}.
 *
 * <p>These tests intentionally avoid loading a Spring context: the runtime configuration depends
 * on external infrastructure (Config Server, Vault and MongoDB) that is not available during unit
 * testing.</p>
 */
@DisplayName("MsCreditApplication bootstrap configuration")
class MsCreditApplicationTests {

  @Test
  @DisplayName("Is annotated as a Spring Boot application")
  void isAnnotatedAsSpringBootApplication() {
    // Act
    boolean annotated = MsCreditApplication.class
            .isAnnotationPresent(SpringBootApplication.class);

    // Assert
    assertThat(annotated).isTrue();
  }

  @Test
  @DisplayName("Declares a public static main method")
  void declaresAPublicStaticMainMethod() throws NoSuchMethodException {
    // Act
    Method main = MsCreditApplication.class.getMethod("main", String[].class);

    // Assert
    assertThat(Modifier.isPublic(main.getModifiers())).isTrue();
    assertThat(Modifier.isStatic(main.getModifiers())).isTrue();
    assertThat(main.getReturnType()).isEqualTo(Void.TYPE);
  }
}
