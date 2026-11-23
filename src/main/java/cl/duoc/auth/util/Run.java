package cl.duoc.auth.util;

// Valida el dígito verificador del RUN chileno (módulo 11).
// Es la misma regla de calcularDigitoVerificador en el frontend.
public final class Run {
  private Run() {}

  public static String normalizar(String run) {
    return run == null ? "" : run.trim().toUpperCase();
  }

  public static String calcularDigito(String cuerpo) {
    int suma = 0;
    int multiplicador = 2;
    for (int i = cuerpo.length() - 1; i >= 0; i--) {
      suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
      multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
    }
    int resto = 11 - (suma % 11);
    if (resto == 11) return "0";
    if (resto == 10) return "K";
    return String.valueOf(resto);
  }

  public static boolean esValido(String valor) {
    String run = normalizar(valor);
    if (!run.matches("^[0-9]{7,8}[0-9K]$")) return false;
    String cuerpo = run.substring(0, run.length() - 1);
    String digito = run.substring(run.length() - 1);
    return calcularDigito(cuerpo).equals(digito);
  }
}