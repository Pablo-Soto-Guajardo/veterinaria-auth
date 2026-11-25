package cl.duoc.auth.model;

// Roles definidos en el Anexo 1 de la Evaluación 3.
// Spring Security los recibe como ROLE_ADMIN, ROLE_VENDEDOR y ROLE_CLIENTE.
public enum Rol {
  ADMIN,
  VENDEDOR,
  CLIENTE
}