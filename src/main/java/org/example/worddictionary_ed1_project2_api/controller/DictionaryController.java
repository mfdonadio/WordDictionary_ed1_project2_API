package org.example.worddictionary_ed1_project2_api.controller;

import org.example.worddictionary_ed1_project2_api.dto.WordRequest;
import org.example.worddictionary_ed1_project2_api.dto.WordResponse;
import org.example.worddictionary_ed1_project2_api.model.Word;
import org.example.worddictionary_ed1_project2_api.service.DictionaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    //-------- POST: insertar nueva palabra ---------
    @PostMapping("/palabra")
    public ResponseEntity<WordResponse> insert(@RequestBody WordRequest request){
        WordResponse response = dictionaryService.insert(request);
        return ResponseEntity.ok(response);
    }

    //------- PUT: actualizar palabras existentes ---
    @PutMapping("/palabra/{id}")
    public ResponseEntity<WordResponse> update( @PathVariable Integer id, @RequestBody WordRequest request){
        WordResponse response = dictionaryService.update(id, request);
        //Si no exitiese, construye una respuesta basada en HTTP vacía, esto lo hago para que el programa pueda admitir este tipo de fallos
        if(response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }

    //----------- DELETE: eliminar palabras --------
    //1. DELETE POR PALABRA
    @DeleteMapping("/palabra/{palabra}")
    public ResponseEntity<Void> delete(@PathVariable String palabra){
        boolean deleted =  dictionaryService.delete(palabra);
        if(!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().build();
    }

    //2. DELETE POR ID
    @DeleteMapping("/palabra/id/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id){
        boolean deleted =  dictionaryService.deleteById(id);
        if(!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().build();
    }

    //-------- GET: busqueda de palabras (exacta) ---------
    //1. GET POR PALABRA
    @GetMapping("/palabra/{palabra}")
    public ResponseEntity<WordResponse> get(@PathVariable String palabra){
        WordResponse response = dictionaryService.search(palabra);
        if(response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }

    //2. GET POR ID
    @GetMapping("/palabra/id/{id}")
    public ResponseEntity<WordResponse> getById(@PathVariable int id){
        WordResponse response = dictionaryService.searchById(id);
        if(response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }

    //-------- GET: busqueda de palabras por prefijo ---------
    @GetMapping("/prefijo/{prefijo}")
    public ResponseEntity<List<WordResponse>> getByPrefix(
            @PathVariable String prefijo, @RequestParam(required = false) Integer limite,
            @RequestParam(defaultValue = "alfabeto") String ordenarPor,
            @RequestParam(defaultValue = "asc") String orden){

        List<WordResponse> response = dictionaryService.serchByPrefix(prefijo, limite, ordenarPor, orden);
        return ResponseEntity.ok(response);
    }

    //-------- GET: busqueda de palabras por comodin ---------
    @GetMapping("/comodin/{patron}")
    public ResponseEntity<List<WordResponse>> getByWildcard(
            @PathVariable String patron, @RequestParam(required = false) Integer limite,
            @RequestParam(defaultValue = "alfabeto") String ordenarPor,
            @RequestParam(defaultValue = "asc") String orden){

        List<WordResponse> response = dictionaryService.searchByWildcard(patron, limite, ordenarPor, orden);
        return ResponseEntity.ok(response);
    }

    //-------- GET: TopK palabras ---------
    @GetMapping("/top")
    public ResponseEntity<List<WordResponse>> topK(
            @RequestParam(defaultValue = "10") int k,
            @RequestParam(defaultValue = "Frecuencia") String ordenarPor,
            @RequestParam(defaultValue = "desc") String orden){

        List<WordResponse> response = dictionaryService.topK(k, ordenarPor, orden);
        return ResponseEntity.ok(response);
    }

    //------- GET: Exportar Archivo CSV -------
    @GetMapping("/exportar")
    public ResponseEntity<String> exportar() {
        dictionaryService.saveCSV();
        return ResponseEntity.ok("Diccionario exportado correctamente a diccionario.csv");
    }
}
