
package sdpp_backend.controller;

import sdpp_backend.model.CargoModel;
import sdpp_backend.service.CargoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cargos")
public class CargoController {

    private final CargoService service;

    public CargoController(CargoService service) {
        this.service = service;
    }

    @GetMapping
    public List<CargoModel> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public CargoModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public CargoModel salvar(@RequestBody CargoModel cargo) {
        return service.salvar(cargo);
    }

    @PutMapping("/{id}")
    public CargoModel atualizar(
            @PathVariable Long id,
            @RequestBody CargoModel cargo) {

        CargoModel existente = service.buscarPorId(id);

        existente.setNome(cargo.getNome());
        existente.setDescricao(cargo.getDescricao());

        return service.salvar(existente);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}

