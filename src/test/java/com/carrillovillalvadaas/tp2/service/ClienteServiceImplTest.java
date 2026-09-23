package com.carrillovillalvadaas.tp2.service;

import com.carrillovillalvadaas.tp2.model.Cliente;
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
 * Pruebas unitarias de ClienteServiceImpl.
 * Se mockea ClienteRepository: no se levanta contexto de Spring ni base de datos real,
 * solo se prueba la lógica propia de la clase de servicio.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente cliente;
    private UUID clienteId;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        cliente = Cliente.builder()
                .id(clienteId)
                .nombre("Elias Villalba")
                .cuil(20123456789L)
                .email("billygay@mail.com")
                .direccion("Calle 123")
                .telefono("3884000000")
                .build();
    }

    @Test
    void crearCliente_deberiaGuardarCliente_cuandoNoExisteDuplicado() {
        // Arrange
        when(clienteRepository.existsByCuilOrEmail(cliente.getCuil(), cliente.getEmail())).thenReturn(false);
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        // Act
        Cliente resultado = clienteService.crearCliente(cliente);

        // Assert
        assertThat(resultado).isEqualTo(cliente);
        verify(clienteRepository).save(cliente);
    }

    @Test
    void crearCliente_deberiaLanzarExcepcion_cuandoExisteCuilOEmailDuplicado() {
        // Arrange
        when(clienteRepository.existsByCuilOrEmail(cliente.getCuil(), cliente.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> clienteService.crearCliente(cliente))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un cliente");

        // Nunca debe llegar a guardar si detectó duplicado
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_deberiaRetornarCliente_cuandoExiste() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        Cliente resultado = clienteService.obtenerPorId(clienteId);

        assertThat(resultado).isEqualTo(cliente);
    }

    @Test
    void obtenerPorId_deberiaLanzarExcepcion_cuandoNoExiste() {
        UUID idInexistente = UUID.randomUUID();
        when(clienteRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerPorId(idInexistente))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cliente no encontrado");
    }

    @Test
    void obtenerPorCuil_deberiaRetornarCliente_cuandoExiste() {
        when(clienteRepository.findByCuil(cliente.getCuil())).thenReturn(Optional.of(cliente));

        Cliente resultado = clienteService.obtenerPorCuil(cliente.getCuil());

        assertThat(resultado).isEqualTo(cliente);
    }

    @Test
    void listarTodos_deberiaRetornarListaCompleta() {
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));

        List<Cliente> resultado = clienteService.listarTodos();

        assertThat(resultado).hasSize(1).containsExactly(cliente);
    }

    @Test
    void actualizarCliente_deberiaModificarSoloCamposPermitidos() {
        // Arrange: lo que ya existe en la "base"
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cliente cambios = Cliente.builder()
                .nombre("Juan Actualizado")
                .email("nuevo@mail.com")
                .direccion("Nueva Direccion 456")
                .telefono("3884111111")
                .build();

        // Act
        Cliente resultado = clienteService.actualizarCliente(clienteId, cambios);

        // Assert: se actualizaron los campos modificables...
        assertThat(resultado.getNombre()).isEqualTo("Juan Actualizado");
        assertThat(resultado.getEmail()).isEqualTo("nuevo@mail.com");
        // ...y el CUIL (no editable por este método) se mantuvo intacto
        assertThat(resultado.getCuil()).isEqualTo(20123456789L);
    }

    @Test
    void eliminarPorId_deberiaBorrarCliente_cuandoExiste() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        clienteService.eliminarPorId(clienteId);

        verify(clienteRepository).delete(cliente);
    }
}
