package com.sana_esencia.usuarios_service.service;

import com.sana_esencia.usuarios_service.dto.BeneficiarioRequest;
import com.sana_esencia.usuarios_service.entity.Beneficiario;
import com.sana_esencia.usuarios_service.repository.BeneficiarioRepository;
import com.sana_esencia.usuarios_service.repository.UsuarioRepository;
import com.sana_esencia.usuarios_service.validation.RutValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BeneficiarioService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final UsuarioRepository usuarioRepository;

    public BeneficiarioService(
            BeneficiarioRepository beneficiarioRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.beneficiarioRepository = beneficiarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Beneficiario> listarPorPropietario(String propietarioEmail, boolean soloActivos) {
        String emailNormalizado = propietarioEmail.trim().toLowerCase();
        return soloActivos
                ? beneficiarioRepository.findByPropietarioEmailAndActivoTrueOrderByNombreAsc(emailNormalizado)
                : beneficiarioRepository.findByPropietarioEmailOrderByNombreAsc(emailNormalizado);
    }

    @Transactional(readOnly = true)
    public Beneficiario obtenerPorId(Integer id) {
        return beneficiarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiario no encontrado con id: " + id));
    }

    @Transactional
    public Beneficiario registrarOReactivar(BeneficiarioRequest request) {
        String emailNormalizado = request.getPropietarioEmail().trim().toLowerCase();

        if (!usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new IllegalArgumentException("El usuario titular no existe");
        }

        if (!RutValidator.esValido(request.getRut())) {
            throw new IllegalArgumentException("El RUT del beneficiario no es válido");
        }

        String rutNormalizado = RutValidator.normalizar(request.getRut());

        Optional<Beneficiario> existenteOpt =
                beneficiarioRepository.findByPropietarioEmailAndRut(emailNormalizado, rutNormalizado);

        if (existenteOpt.isPresent()) {
            Beneficiario existente = existenteOpt.get();
            if (existente.isActivo()) {
                throw new IllegalArgumentException("Ya tienes un beneficiario activo registrado con ese RUT");
            }
            // Reactivar y actualizar datos si estaba desactivado
            existente.setNombre(request.getNombre().trim());
            existente.setFechaNacimiento(request.getFechaNacimiento());
            existente.setParentesco(request.getParentesco().trim());
            existente.setActivo(true);
            return beneficiarioRepository.save(existente);
        }

        Beneficiario nuevo = new Beneficiario(
                emailNormalizado,
                request.getNombre().trim(),
                rutNormalizado,
                request.getFechaNacimiento(),
                request.getParentesco().trim()
        );

        return beneficiarioRepository.save(nuevo);
    }

    @Transactional
    public Beneficiario actualizar(Integer id, BeneficiarioRequest request) {
        Beneficiario actual = obtenerPorId(id);

        if (!RutValidator.esValido(request.getRut())) {
            throw new IllegalArgumentException("El RUT del beneficiario no es válido");
        }

        String rutNormalizado = RutValidator.normalizar(request.getRut());

        // Validar conflicto si cambió el RUT y ya existe otro beneficiario con ese RUT para el mismo titular
        if (!actual.getRut().equals(rutNormalizado)) {
            Optional<Beneficiario> conflicto = beneficiarioRepository
                    .findByPropietarioEmailAndRut(actual.getPropietarioEmail(), rutNormalizado);
            if (conflicto.isPresent()) {
                throw new IllegalArgumentException("Ya existe otro beneficiario registrado con ese RUT");
            }
        }

        actual.setNombre(request.getNombre().trim());
        actual.setRut(rutNormalizado);
        actual.setFechaNacimiento(request.getFechaNacimiento());
        actual.setParentesco(request.getParentesco().trim());

        return beneficiarioRepository.save(actual);
    }

    @Transactional
    public Beneficiario cambiarEstadoActivo(Integer id, boolean activo) {
        Beneficiario actual = obtenerPorId(id);
        actual.setActivo(activo);
        return beneficiarioRepository.save(actual);
    }
}