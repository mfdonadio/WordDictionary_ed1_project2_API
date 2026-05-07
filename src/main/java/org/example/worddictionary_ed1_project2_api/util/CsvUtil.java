package org.example.worddictionary_ed1_project2_api.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.example.worddictionary_ed1_project2_api.model.Word;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

//Importantisima para crear el diccionario y exportar el mismo desde csv

@Component
public class CsvUtil {

    private static final String CSV_PATH = "diccionario.csv";
    private static final String[] HEADERS = {"id", "palabra", "significado", "frecuencia"};

    // Lee el CSV y retorna la lista de palabras para cargar en las estructuras al arrancar
    public List<Word> importCSV() {
        List<Word> palabras = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(CSV_PATH))) {
            String[] linea;
            boolean primeraLinea = true;

            while ((linea = reader.readNext()) != null) {
                // Saltamos el header
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }

                int id = Integer.parseInt(linea[0].trim());
                String palabra = linea[1].trim();
                String significado = linea[2].trim();
                int frecuencia = Integer.parseInt(linea[3].trim());

                palabras.add(new Word(id, palabra, significado, frecuencia));
            }

        } catch (Exception e) {
            // Si el archivo no existe todavia, retornamos lista vacia
            System.out.println("No se encontro el archivo CSV, se iniciara con diccionario vacio.");
        }
        return palabras;
    }

    // Escribe todas las palabras al CSV --- se llama cada vez que hay un cambio
    public void exportCSV(List<Word> words) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(CSV_PATH))) {
            // Escribimos el header
            writer.writeNext(HEADERS);

            // Escribimos cada palabra
            for (Word p : words) {
                writer.writeNext(new String[]{
                        String.valueOf(p.getId()),
                        p.getWord(),
                        p.getDefinition(),
                        String.valueOf(p.getFrequency())
                });
            }

        } catch (Exception e) {
            System.out.println("Error al exportar el diccionario: " + e.getMessage());
        }
    }
}
