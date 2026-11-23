# Veterinaria San Marcos – Auth API

Backend de **autenticación y usuarios** de la tienda Veterinaria San Marcos
(DSY1104 Desarrollo FullStack II – Evaluación 3, Grupo 5).

| Dato | Valor |
|------|-------|
| Puerto | `8081` |
| Base de datos | MySQL `veterinaria_auth` (tabla `usuario` y `refresh_token`) |
| Versión de la API | `/api/v1` |
| Swagger | http://localhost:8081/swagger-ui/index.html |
| Tecnologías | Java 17, Spring Boot 4.1.2, Spring Security, Spring Data JPA, JJWT 0.12.6, springdoc 3.2.0 |

Este servicio es el **único que firma** los JWT. El backend
[veterinaria-tienda](../veterinaria-tienda) solo los valida con el mismo `JWT_SECRET` (Guía 17).

## Roles

| Rol | Acceso |
|-----|--------|
| `ADMIN` | Acceso total (usuarios, productos, categorías, órdenes, reportes). |
| `VENDEDOR` | Ver productos y órdenes (lista y detalle). |
| `CLIENTE` | Solo la tienda: comprar y ver sus compras. |

## Endpoints

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/api/v1/auth/login` | Público | Entrega JWT (15 min) + refresh token (8 h) |
| POST | `/api/v1/auth/registro` | Público | Crea un CLIENTE e inicia su sesión |
| POST | `/api/v1/auth/refresh` | Público (con refresh token) | Renueva el JWT. El refresh token es de un solo uso (rotación) |
| POST | `/api/v1/auth/logout` | Público (con refresh token) | Revoca el refresh token |
| GET | `/api/v1/usuarios/me` | Autenticado | Perfil propio |
| PUT | `/api/v1/usuarios/me` | Autenticado | Editar perfil propio (no permite cambiar rol) |
| GET | `/api/v1/usuarios` | ADMIN | Listar usuarios |
| GET | `/api/v1/usuarios/{run}` | ADMIN | Buscar usuario |
| POST | `/api/v1/usuarios` | ADMIN | Crear usuario con cualquier rol |
| PUT | `/api/v1/usuarios/{run}` | ADMIN | Editar usuario |
| DELETE | `/api/v1/usuarios/{run}` | ADMIN | Eliminar usuario (no el propio ni el último ADMIN) |

Respuestas de error: `400` datos inválidos, `401` sin token / token inválido / credenciales
incorrectas, `403` rol sin permiso, `404` no existe, `409` regla de negocio (RUN o correo repetido).

## Seguridad implementada

- Contraseñas guardadas con **BCrypt** (nunca en texto).
- **JWT HS256** con `issuer` = `veterinaria-auth`, `subject` = correo y claim `rol`.
- **Refresh token** aleatorio (32 bytes), guardado solo como hash SHA-256, de un solo uso.
  Si alguien reutiliza un refresh token ya usado, se revocan todas las sesiones de ese usuario.
- Sesión **STATELESS**: el servidor no guarda sesiones, cada solicitud trae su JWT.
- **CORS** solo para el origen del frontend (`APP_CORS_ALLOWED_ORIGIN`).
- Secretos por **variables de entorno**: nada sensible queda en Git.

## Ejecutar en Windows (PowerShell)

Requisitos: JDK 17 o superior y MySQL (o MariaDB) encendido.

```powershell
# 1. Generar UNA vez el secreto compartido (Guía 17) y usar el mismo valor en Tienda
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$secretoJwt = [Convert]::ToBase64String($bytes)

# 2. Variables de esta terminal
$env:JWT_SECRET = $secretoJwt
$env:AUTH_DB_USER = "root"
$env:AUTH_DB_PASSWORD = "TU_CLAVE_MYSQL"

# 3. Iniciar (la base veterinaria_auth se crea sola si no existe)
.\mvnw.cmd spring-boot:run
```

Cuentas de demostración que se crean al iniciar:

| Rol | Correo | Contraseña |
|-----|--------|------------|
| Administrador | admin@duoc.cl | admin123 |
| Vendedor | vendedor@duoc.cl | vende123 |
| Cliente | cliente@gmail.com | cliente1 |

## Pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas usan una base **H2 en memoria** (no necesitan MySQL):

- `RunTest`: validación del dígito verificador del RUN.
- `AuthServiceTest`: lógica de login, registro y refresh con **Mockito**.
- `AuthApiIntegrationTest`: levanta el backend y prueba por HTTP la matriz 200 / 401 / 403,
  la rotación del refresh token y el rechazo de un token alterado.

En la carpeta `postman/` hay una colección para probar los endpoints.

## Despliegue en AWS (Guía 18)

`src/main/resources/application-prod.properties` hace que el servicio escuche solo en
`127.0.0.1:8081`. En EC2 se ejecuta con systemd y un `EnvironmentFile` protegido
(ver el README del frontend para los pasos completos con Nginx).