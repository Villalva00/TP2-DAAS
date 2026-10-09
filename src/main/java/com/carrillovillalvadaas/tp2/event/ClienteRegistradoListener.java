package com.carrillovillalvadaas.tp2.event;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;

/**
 * Oyente del evento {@link ClienteRegistradoEvent} encargado de enviar el email
 * HTML de bienvenida con el botón de activación.
 * <p>
 * El método se ejecuta de forma <b>asíncrona</b> ({@link Async}) y recién
 * <b>después del commit</b> de la transacción de alta
 * ({@link TransactionalEventListener} con fase {@code AFTER_COMMIT}), por lo
 * que la respuesta HTTP del alta no espera al SMTP y un fallo de correo jamás
 * revierte el cliente ya persistido.
 * </p>
 * <p>
 * Cualquier excepción del envío se captura y se registra en el log: el alta del
 * cliente permanece válida.
 * </p>
 *
 * @author Villalva Elias Maciel, Carrillo Gonzalo Alejo
 *         Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
@Component
@Slf4j
public class ClienteRegistradoListener {

    private final JavaMailSender mailSender;
    private final String remitente;
    private final String baseUrl;

    /**
     * Construye el oyente inyectando el cliente de correo y la configuración
     * externa de remitente y URL base (sin credenciales hardcodeadas).
     *
     * @param mailSender cliente SMTP de Spring.
     * @param remitente  dirección "from" configurada en {@code app.mail.from}.
     * @param baseUrl    URL base de la API configurada en {@code app.base-url}.
     */
    public ClienteRegistradoListener(JavaMailSender mailSender,
                                     @Value("${app.mail.from}") String remitente,
                                     @Value("${app.base-url}") String baseUrl) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.baseUrl = baseUrl;
    }

    /**
     * Envía el email HTML de activación al cliente registrado.
     *
     * @param evento evento con los datos mínimos del cliente y su token.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviarEmailDeActivacion(ClienteRegistradoEvent evento) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, StandardCharsets.UTF_8.name());
            helper.setFrom(remitente);
            helper.setTo(evento.email());
            helper.setSubject("¡Bienvenido/a al Sistema Bancario! Activá tu cuenta");
            helper.setText(construirHtmlActivacion(evento), true);

            mailSender.send(mensaje);
            log.info("Email de activación enviado a {} (cliente {})", evento.email(), evento.clienteId());
        } catch (Exception ex) {
            log.error("Falló el envío del email de activación al cliente {} ({}). "
                            + "El alta del cliente quedó registrada.", evento.clienteId(), evento.email(), ex);
        }
    }

    /**
     * Construye el cuerpo HTML del correo con el saludo por nombre, el texto de
     * bienvenida y el botón que apunta al endpoint de activación con el token.
     *
     * @param evento datos del cliente registrado.
     * @return el HTML listo para enviar.
     */
    private String construirHtmlActivacion(ClienteRegistradoEvent evento) {
        String url = baseUrl + "/api/v1/clientes/activar?token=" + evento.token();
        return """
                <!DOCTYPE html>
                <html lang="es">
                  <head>
                    <meta charset="UTF-8">
                    <title>Activá tu cuenta</title>
                  </head>
                  <body style="margin:0;padding:0;background-color:#f4f6f8;font-family:Arial,Helvetica,sans-serif;">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f8;padding:24px 0;">
                      <tr>
                        <td align="center">
                          <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:8px;overflow:hidden;">
                            <tr>
                              <td style="background-color:#1b4965;padding:24px;color:#ffffff;font-size:22px;font-weight:bold;">
                                Sistema Bancario
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:32px;color:#333333;font-size:16px;line-height:1.5;">
                                <p style="margin:0 0 16px;">Hola <strong>%s</strong>,</p>
                                <p style="margin:0 0 16px;">
                                  ¡Gracias por registrarte! Para empezar a operar necesitás
                                  activar tu cuenta. Este enlace vence en 24 horas.
                                </p>
                                <p style="margin:24px 0;text-align:center;">
                                  <a href="%s"
                                     style="background-color:#1b4965;color:#ffffff;text-decoration:none;padding:14px 28px;border-radius:6px;font-weight:bold;display:inline-block;">
                                    Activar cuenta
                                  </a>
                                </p>
                                <p style="margin:0;color:#777777;font-size:13px;">
                                  Si el botón no funciona, copiá y pegá este enlace en tu navegador:<br>
                                  <a href="%s" style="color:#1b4965;">%s</a>
                                </p>
                              </td>
                            </tr>
                            <tr>
                              <td style="background-color:#eef2f5;padding:16px;color:#888888;font-size:12px;text-align:center;">
                                Este es un mensaje automático, por favor no lo respondas.
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.formatted(evento.nombre(), url, url, url);
    }
}
