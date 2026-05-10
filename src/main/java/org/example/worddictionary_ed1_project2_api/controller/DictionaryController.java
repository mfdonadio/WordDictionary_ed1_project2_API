package org.example.worddictionary_ed1_project2_api.controller;

import org.example.worddictionary_ed1_project2_api.dto.WordRequest;
import org.example.worddictionary_ed1_project2_api.dto.WordResponse;
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
    public ResponseEntity<?> insert(@RequestBody WordRequest request){
        try {
            WordResponse response = dictionaryService.insert(request);
            return ResponseEntity.ok(response);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //------- PUT: actualizar palabras existentes ---
    //ID en el endpoint
    @PutMapping("/palabra/{id}")
    public ResponseEntity<?> updateByPath( @PathVariable Integer id, @RequestBody WordRequest request){
        try {
            WordResponse response = dictionaryService.update(id, request);
            //Si no existiese, construye una respuesta basada en HTTP vacía, esto lo hago para que el programa pueda admitir este tipo de fallos
            if (response == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(response);
        }catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Tambien uno para que el ID vaya en el body del JSON
    @PutMapping("/palabra")
    public ResponseEntity<?> updateByBody(@RequestBody WordRequest request) {
        if (request == null || request.getId() == null) {
            return ResponseEntity.badRequest().body("Debe enviar el campo id para actualizar la palabra.");
        }
        try {
            WordResponse response = dictionaryService.update(request.getId(), request);
            if (response == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //----------- DELETE: eliminar palabras --------
    //1. DELETE POR PALABRA
    @DeleteMapping({"/palabra/{palabra}","/palabra/eliminar/{palabra}"}) //Doble endpoint permitido, algo que aprendi estudiando el tema
    public ResponseEntity<String> deleteByWord(@PathVariable String palabra){
        boolean deleted =  dictionaryService.delete(palabra);
        if(!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.ok("Palabra " + palabra + " eliminada exitosamente!" );
    }

    //2. DELETE POR ID
    @DeleteMapping({"/palabra/id/{id}", "/palabra/eliminar/id/{id}"})
    public ResponseEntity<String> deleteById(@PathVariable int id){
        boolean deleted =  dictionaryService.deleteById(id);
        if(!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.ok("Palabra con ID " + id + " eliminada exitosamente!" );
    }

    //-------- GET: busqueda de palabras (exacta) ---------
    //1. GET POR PALABRA
    @GetMapping("/palabra/{valor}")
    public ResponseEntity<WordResponse> get(@PathVariable String valor){
        WordResponse response; //Aqui solo instanciamos el objeto de respuesta

        try{
            //Parseamos para ver si es un entero
            int id = Integer.parseInt(valor);
            //De serlo el objeto de respuesta hace la busqueda por ID
            response = dictionaryService.searchById(id);
        } catch (NumberFormatException e){ //De no serlo...
            //Buscamos la palabra por su valor (String)
            response = dictionaryService.search(valor);
        }

        if(response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }

    //2. GET POR ID enrutado
    @GetMapping("/palabra/id/{id}")
    public ResponseEntity<WordResponse> getByIdRoute(@PathVariable int id){
        WordResponse response = dictionaryService.searchById(id);
        if(response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }


    //-------- GET: busqueda de palabras por prefijo ---------
    @GetMapping("/prefijo/{prefijo}")
    public ResponseEntity<List<WordResponse>> getByPrefix(
            @PathVariable String prefijo,
            @RequestParam(required = false) Integer limite,
            @RequestParam(defaultValue = "alfabeto") String ordenarPor,
            @RequestParam(defaultValue = "asc") String orden){

        List<WordResponse> response = dictionaryService.serchByPrefix(prefijo, limite, ordenarPor, orden);
        return ResponseEntity.ok(response);
    }

    //-------- GET: busqueda de palabras por comodin ---------
    @GetMapping("/comodin/{patron}")
    public ResponseEntity<List<WordResponse>> getByWildcard(
            @PathVariable String patron,
            @RequestParam(required = false) Integer limite,
            @RequestParam(defaultValue = "alfabeto") String ordenarPor,
            @RequestParam(defaultValue = "asc") String orden){

        List<WordResponse> response = dictionaryService.searchByWildcard(patron, limite, ordenarPor, orden);
        return ResponseEntity.ok(response);
    }

    //-------- GET: TopK palabras ---------
    @GetMapping("/top")
    public ResponseEntity<List<WordResponse>> topK(
            @RequestParam(defaultValue = "10") int k,
            @RequestParam(defaultValue = "frecuencia") String ordenarPor,
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
