package com.carrillovillalvadaas.tp2.event;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del oyente {@link ClienteRegistradoListener}.
 * <p>
 * Verifica que se construya y envíe un email HTML con el botón de activación y
 * que, ante un fallo del SMTP, la excepción no se propague (el alta no se rompe).
 * </p>
 *
 * @author Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
@ExtendWith(MockitoExtension.class)
class ClienteRegistradoListenerTest {

    private static final String FROM = "no-reply@test.local";
    private static final String BASE_URL = "http://localhost:8080";

    @Mock
    private JavaMailSender mailSender;

    private ClienteRegistradoListener listener;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        listener = new ClienteRegistradoListener(mailSender, FROM, BASE_URL);
        mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
    }

    @Test
    void deberiaEnviarEmailHtmlConBotonDeActivacion() throws Exception {
        UUID token = UUID.randomUUID();
        ClienteRegistradoEvent evento = new ClienteRegistradoEvent(
                UUID.randomUUID(), "Elias Villalba", "elias@mail.com", token);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        listener.enviarEmailDeActivacion(evento);

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("elias@mail.com");
        assertThat(mimeMessage.getSubject()).contains("Bienvenido");

        String contenido = (String) mimeMessage.getContent();
        assertThat(contenido).contains("Elias Villalba");
        assertThat(contenido).contains("<a href=\"");
        assertThat(contenido)
                .contains(BASE_URL + "/api/v1/clientes/activar?token=" + token);
    }

    @Test
    void noDeberiaPropagarLaExcepcion_cuandoFallaElSmtp() {
        ClienteRegistradoEvent evento = new ClienteRegistradoEvent(
                UUID.randomUUID(), "Elias Villalba", "elias@mail.com", UUID.randomUUID());
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP no disponible"))
                .when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() -> listener.enviarEmailDeActivacion(evento)).doesNotThrowAnyException();
    }
}
