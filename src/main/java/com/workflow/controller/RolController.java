package com.workflow.controller;

import com.workflow.document.Rol;
import com.workflow.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolRepository rolRepository;

    @GetMapping
    public List<Rol> listar() {
        return rolRepository.findAll();
    }
}
