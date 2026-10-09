# TP5-B · Issue #8 — Evento de dominio asíncrono y envío de email HTML

> **Rama:** `feature/issue-8-evento-email-activacion`
> **Asignado:** @elias · **Depende de:** #7 · **Estado:** implementado y verificado
> **Fecha de documentación:** 08/10/2026

---

## 1. Contexto

Al crearse un cliente (titular **o** adherente) se publica un **evento de dominio
asíncrono** que envía un correo HTML de bienvenida con el botón de activación. El
envío ocurre **después del commit** de la transacción y en un **hilo aparte**, por
lo que la respuesta HTTP del alta no espera al SMTP y un fallo de correo **no
rompe el alta**: el cliente queda creado igual.

El token de activación (issue #7) sigue sin exponerse en la API; viaja únicamente
por email.

---

## 2. Cambios de funcionamiento (comportamiento observable)

- `POST /api/v1/clientes` y `POST /api/v1/clientes/{titularId}/adherentes` siguen
  devolviendo **201** con el `ClienteResponseDto` (`estado=PENDIENTE_ACTIVACION`),
  **sin token**.
- Tras el commit se publica `ClienteRegistradoEvent` y, en un hilo del pool, se
  envía un **email HTML** al correo del cliente:
  - saludo por nombre,
  - texto de bienvenida (vence en 24 h),
  - botón `<a href="{baseUrl}/api/v1/clientes/activar?token={token}">Activar cuenta</a>`
    + enlace de respaldo en texto.
- Si el SMTP falla (host caído, timeout, credenciales), el error se **registra en
  el log** y **no se propaga**; el cliente y su token ya quedaron persistidos.

---

## 3. Configuración

### 3.1 `pom.xml`

- Nueva dependencia:
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-mail</artifactId>
  </dependency>
  ```

### 3.2 `src/main/resources/application.properties`

Todas las credenciales/parámetros se inyectan por **variables de entorno** (con
valores por defecto seguros para desarrollo). **No hay credenciales en el repo**.

```properties
spring.mail.host=${MAIL_HOST:localhost}
spring.mail.port=${MAIL_PORT:1025}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=${MAIL_SMTP_AUTH:false}
spring.mail.properties.mail.smtp.starttls.enable=${MAIL_STARTTLS:false}
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

app.mail.from=${MAIL_FROM:no-reply@sistemabancario.local}
app.base-url=${APP_BASE_URL:http://localhost:8080}
```

Para desarrollo los defaults apuntan a **Mailpit/MailHog** en `localhost:1025`.
Para Mailtrap/producción, definir `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`,
`MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, `MAIL_STARTTLS`, `MAIL_FROM` y `APP_BASE_URL`.

### 3.3 `docker-compose.yml`

Se agregó el servicio de captura de correo para desarrollo:

```yaml
mailpit:
  image: axllent/mailpit:latest
  container_name: sistema_bancario_mailpit
  restart: unless-stopped
  ports:
    - "1025:1025"   # SMTP
    - "8025:8025"   # UI web: http://localhost:8025
```

---

## 4. Cambios en el código

### 4.1 Archivos nuevos

| Archivo | Descripción |
| :--- | :--- |
| `src/main/java/.../config/AsyncConfig.java` | `@Configuration @EnableAsync` + bean `taskExecutor` (`ThreadPoolTaskExecutor`: core 2, max 5, cola 100, prefijo `async-email-`, `waitForTasksToCompleteOnShutdown`). |
| `src/main/java/.../event/ClienteRegistradoEvent.java` | `record ClienteRegistradoEvent(UUID clienteId, String nombre, String email, UUID token)`. Solo datos primitivos, **sin entidades JPA** (evita lazy loading post-transacción). |
| `src/main/java/.../event/ClienteRegistradoListener.java` | `@Component` con `@Async` + `@TransactionalEventListener(phase = AFTER_COMMIT)`. Construye `MimeMessage` HTML con `MimeMessageHelper`, lo envía con `JavaMailSender` y **captura toda excepción** (`catch (Exception)` → solo `log.error`). |

### 4.2 Archivos modificados

#### `service/impl/ClienteServiceImpl.java`

- Nueva dependencia `private final ApplicationEventPublisher eventPublisher`
  (inyección por `@RequiredArgsConstructor`).
- `generarTokenActivacion(Cliente)` ahora **devuelve `UUID`** (el valor del token)
  además de persistir el `TokenActivacion`.
- `crearCliente(...)` y `crearAdherente(...)`: tras guardar cliente + token llaman
  a `publicarClienteRegistrado(cliente, token)`. Se eliminó el
  `// TODO (TP5-B #8)` que quedaba en `crearAdherente`.
- Nuevo método privado:

```java
private void publicarClienteRegistrado(Cliente cliente, UUID token) {
    eventPublisher.publishEvent(new ClienteRegistradoEvent(
            cliente.getId(), cliente.getNombre(), cliente.getEmail(), token));
}
```

> El evento se publica **dentro** de la transacción `@Transactional`; el oyente
> `AFTER_COMMIT` recién se dispara cuando el commit tuvo éxito. Si la transacción
> hace rollback, no se envía email.

---

## 5. Flujo

```
Controller → ClienteServiceImpl.crearCliente (@Transactional)
   ├─ valida duplicados
   ├─ save(cliente)            → PENDIENTE_ACTIVACION
   ├─ save(tokenActivacion)    → UUID + expiración 24 h
   └─ publishEvent(ClienteRegistradoEvent)   ← dentro de la tx
        │
   [COMMIT] ──► @TransactionalEventListener(AFTER_COMMIT) + @Async
        └─ hilo async-email-N: MimeMessage HTML → JavaMailSender.send
              └─ catch(Exception) → log.error   (el alta ya está confirmada)
```

La respuesta 201 se devuelve sin esperar al envío (que corre en otro hilo).

---

## 6. Tests

### 6.1 `service/ClienteServiceImplTest.java`

- Nuevo mock `@Mock ApplicationEventPublisher eventPublisher`.
- Test nuevo `crearCliente_deberiaPublicarEventoClienteRegistradoConElToken`:
  captura el evento publicado y verifica `clienteId`, `nombre`, `email` y que el
  `token` no sea nulo.
- `crearAdherente_deberiaVincularAlTitular`: además verifica la publicación del
  evento con el email/token del adherente.

### 6.2 `event/ClienteRegistradoListenerTest.java` (nuevo)

- `deberiaEnviarEmailHtmlConBotonDeActivacion`: con un `MimeMessage` real y un
  `JavaMailSender` mockeado verifica destinatario, asunto, nombre, `<a href="..."`
  y la URL `{baseUrl}/api/v1/clientes/activar?token={token}`.
- `noDeberiaPropagarLaExcepcion_cuandoFallaElSmtp`: `MailSendException` en
  `send(...)` → el método **no** lanza excepción.

---

## 7. Criterios de aceptación y verificación

### 7.1 Tests unitarios — `./mvnw test`

```
Tests run: 45, Failures: 0, Errors: 0, Skipped: 0  →  BUILD SUCCESS
```

- `ClienteServiceImplTest`: 12 tests ✅
- `ClienteRegistradoListenerTest`: 2 tests ✅
- `TransaccionServiceImplTest`: 17 ✅ · `CuentaFinancieraServiceImplTest`: 9 ✅
- `LimitesExtraccionPropertiesTest`: 4 ✅ · `Tp2ApplicationTests`: 1 ✅

### 7.2 Prueba E2E con Mailpit (`docker compose up -d mailpit` + app levantada)

| # | Verificación | Resultado |
| :---: | :--- | :--- |
| 1 | `POST /api/v1/clientes` → **201**, `estado=PENDIENTE_ACTIVACION`, sin token en la respuesta, en ~1,1 s | ✅ |
| 2 | Mailpit recibe **1** email para `issue8@example.com`, from `no-reply@sistemabancario.local`, asunto "¡Bienvenido/a al Sistema Bancario! Activá tu cuenta" | ✅ |
| 3 | El HTML (2363 bytes) contiene saludo por nombre, botón `<a href=...>` "Activar cuenta" y la URL `/api/v1/clientes/activar?token=<uuid>` | ✅ |

Cumplido entonces:
- ✅ Al crear un cliente llega un email HTML con el botón de activación.
- ✅ La respuesta HTTP del alta no espera al envío.
- ✅ Si el SMTP falla, el cliente igual queda creado (test unitario).
- ✅ No hay credenciales hardcodeadas en el repo.

---

## 8. Fuera de alcance

- **Canje/activación del token:** el botón apunta a
  `GET /api/v1/clientes/activar?token=...`, endpoint que todavía **no existe**
  (issue posterior). `TokenActivacionRepository.findByToken(UUID)` ya está listo.
- Plantilla con motor de templates (Thymeleaf): se optó por construir el HTML en
  memoria para no sumar dependencias.
