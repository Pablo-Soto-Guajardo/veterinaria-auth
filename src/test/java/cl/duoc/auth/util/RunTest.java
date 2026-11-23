package cl.duoc.auth.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RunTest {
  @Test
  void aceptaRunConDigitoCorrecto() {
    assertTrue(Run.esValido("190110222"));
    assertTrue(Run.esValido("201234565"));
  }

  @Test
  void rechazaDigitoIncorrectoOFormatoInvalido() {
    assertFalse(Run.esValido("190110223"));
    assertFalse(Run.esValido("19.011.022-2"));
    assertFalse(Run.esValido(""));
  }

  @Test
  void calculaDigitoK() {
    assertEquals("K", Run.calcularDigito("10000013"));
  }
}