package com.carrillovillalvadaas.tp2.service;

import com.carrillovillalvadaas.tp2.dto.AdherenteRequestDto;
import com.carrillovillalvadaas.tp2.dto.ClienteRequestDto;
import com.carrillovillalvadaas.tp2.dto.ClienteResponseDto;
import com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException;
import com.carrillovillalvadaas.tp2.model.Cliente;
import com.carrillovillalvadaas.tp2.model.Parentesco;
import com.carrillovillalvadaas.tp2.model.TipoCliente;
import com.carrillovillalvadaas.tp2.repository.ClienteRepository;
import com.carrillovillalvadaas.tp2.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la implementación de servicios de clientes ({@link ClienteServiceImpl}).
 * <p>
 * Emplea Mockito para simular el comportamiento de {@link ClienteRepository}, aislando
 * la lógica de negocio, el manejo de DTOs de entrada/salida y las validaciones de duplicados.
 * </p>
 *
 * @author Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente clienteEntity;
    private ClienteRequestDto requestDto;
    private UUID clienteId;

    /**
     * Configuración inicial previa a cada prueba.
     * Inicializa las entidades de base de datos y los DTOs de prueba.
     */
    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();

        clienteEntity = Cliente.builder()
                .id(clienteId)
                .nombre("Elias Villalba")
                .cuil(20123456789L)
                .email("elias@mail.com")
                .direccion("Calle 123")
                .telefono("3884000000")
                .build();

        requestDto = new ClienteRequestDto();
        requestDto.setNombre("Elias Villalba");
        requestDto.setCuil("20123456789");
        requestDto.setEmail("elias@mail.com");
        requestDto.setDireccion("Calle 123");
        requestDto.setTelefono("3884000000");
    }

    /**
     * Verifica que un cliente se registre y guarde correctamente cuando no existen duplicados previos.
     */
    @Test
    void crearCliente_deberiaGuardarCliente_cuandoNoExisteDuplicado() {
        when(clienteRepository.existsByCuilOrEmail(20123456789L, "elias@mail.com")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteEntity);

        ClienteResponseDto resultado = clienteService.crearCliente(requestDto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Elias Villalba");
        verify(clienteRepository).save(any(Cliente.class));
    }

    /**
     * Verifica que se lance una excepción cuando se intenta registrar un cliente
     * con un CUIL o correo electrónico que ya se encuentra registrado en el sistema.
     */
    @Test
    void crearCliente_deberiaLanzarExcepcion_cuandoExisteCuilOEmailDuplicado() {
        when(clienteRepository.existsByCuilOrEmail(20123456789L, "elias@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.crearCliente(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un cliente");

        verify(clienteRepository, never()).save(any());
    }

    /**
     * Verifica que se retorne el {@link ClienteResponseDto} correcto al buscar un cliente por su UUID.
     */
    @Test
    void obtenerPorId_deberiaRetornarCliente_cuandoExiste() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteEntity));

        ClienteResponseDto resultado = clienteService.obtenerPorId(clienteId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Elias Villalba");
    }

    /**
     * Verifica que se lance la excepción {@link RecursoNoEncontradoException} al buscar un ID inexistente.
     */
    @Test
    void obtenerPorId_deberiaLanzarExcepcion_cuandoNoExiste() {
        UUID idInexistente = UUID.randomUUID();
        when(clienteRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerPorId(idInexistente))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Cliente no encontrado");
    }

    /**
     * Verifica que la consulta general retorne la lista completa de clientes mapeados a DTOs.
     */
    @Test
    void listarTodos_deberiaRetornarListaCompleta() {
        when(clienteRepository.findAll()).thenReturn(List.of(clienteEntity));

        List<ClienteResponseDto> resultado = clienteService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Elias Villalba");
    }

    /**
     * Verifica que se ejecute la eliminación de un cliente existente de manera exitosa.
     */
    @Test
    void eliminarPorId_deberiaBorrarCliente_cuandoExiste() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteEntity));

        clienteService.eliminarPorId(clienteId);

        verify(clienteRepository).delete(clienteEntity);
    }
    @Test
    void crearAdherente_deberiaVincularAlTitular() {
        AdherenteRequestDto dto = AdherenteRequestDto.builder()
                .nombre("Laura Ruiz").cuil("27345556667").email("laura@mail.com")
                .telefono("3881112233").direccion("Calle 123").parentesco(Parentesco.CONYUGE)
                .build();

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteEntity));
        when(clienteRepository.existsByCuilOrEmail(27345556667L, "laura@mail.com")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponseDto resultado = clienteService.crearAdherente(clienteId, dto);

        assertThat(resultado.getTipoCliente()).isEqualTo("ADHERENTE");
        assertThat(resultado.getParentesco()).isEqualTo("CONYUGE");
    }

    @Test
    void crearAdherente_deberiaLanzarExcepcion_cuandoElTitularEsAdherente() {
        clienteEntity.setTipoCliente(TipoCliente.ADHERENTE);
        AdherenteRequestDto dto = AdherenteRequestDto.builder()
                .nombre("X").cuil("27345556667").email("x@mail.com")
                .telefono("1").direccion("d").parentesco(Parentesco.HIJO).build();

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteEntity));

        assertThatThrownBy(() -> clienteService.crearAdherente(clienteId, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Un adherente no puede tener adherentes");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crearAdherente_deberiaLanzar404_cuandoTitularNoExiste() {
        UUID inexistente = UUID.randomUUID();
        AdherenteRequestDto dto = AdherenteRequestDto.builder()
                .nombre("X").cuil("27345556667").email("x@mail.com")
                .telefono("1").direccion("d").parentesco(Parentesco.HIJO).build();

        when(clienteRepository.findById(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.crearAdherente(inexistente, dto))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}