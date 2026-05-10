package org.example.worddictionary_ed1_project2_api.structures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//CLASE RECICLADA DEL PROYECTO 01 --> con cambios claro
public class MyPriorityQueue<T> {
    //Creamos la lista y el comparador
    private final ArrayList<T> heap;
    private Comparator<T> comparador;


    //Constructores
    //En caso no sepamos la cantidad de elementos a ingresar, es dinamico
    public MyPriorityQueue(Comparator<T> comparador) {
        validarComparador(comparador);
        this.heap = new ArrayList<>();
        this.comparador = comparador;
    }

    //En caso sepamos cuantos elementos vayan a ingresar, es estático (se reserva el espacio exacto, mas eficiente)
    public MyPriorityQueue(Comparator<T> comparador, int capacidadInicial){
        this.heap = new ArrayList<>(capacidadInicial);
        this.comparador = comparador;
    }

    //Ahora, cuando hagamos el rehash y tengamos que reconstruir (por ejemplo)
    private MyPriorityQueue(Comparator<T> comparador, List<T> datosIniciales) {
        validarComparador(comparador);
        this.heap = new ArrayList<>(datosIniciales);
        this.comparador = comparador;
        reconstruirHeap();
    }

    //--------------------- INSERCIÓN --------------------------
    public void insertar(T valor) {
        heap.add(valor); //Agregamos el valor al arreglo, en la última posición
        subirHeap(heap.size() - 1); //Acomodamos el valor apropiadamente, mandamos el indice del ultimo elemento
    }

    //subirHeap: como su nombre lo indica, sube el elemento recién insertado hasta su posicion correcta
    private int subirHeap(int indice){
        while(indice > 0){ //Iteramos hasta 0 porque el elemento intentara subir hasta esta posicion (la mas alta en prioridad)
            int padre = (indice - 1)/2; //Relación matemática para encontrar el padre, vista en la clase : ((i - 1)/2)
            //Si el hijo tiene mayor prioridad que el padre, intercambiamos
            if(comparador.compare(heap.get(indice),  heap.get(padre)) > 0){ //Si el resultado de compararlos es > 0, significa  que el hijo tiene mayor prioridad
                //Entonces se realiza el intercambio
                intercambiar(indice, padre);
                indice = padre;
            } else {
                break;
            }
        }
        return indice;
    }

    //------------------- EXTRACCIÓN -------------------------
    //Su objetivo es extraer y devolver el elemento de mayor prioridad
    public T extraer() {
        if(heap.isEmpty()) return null;

        T raiz = heap.get(0); //La raiz es el primer elemento de la lista

        //Como lo vimos en clase, movemos el ultimo elemento a la posicion de la raiz, eliminamos la raiz y luego bajamos el elemento
        T ultimo = heap.remove(heap.size() - 1); //Guardamos el ultimo valor en una variable temporal, y ;a removemos de la lista

        if(!heap.isEmpty()){ //Si el heap NO eta vacio
            heap.set(0, ultimo); //Movemos el ultiimo valor a la primera posicion
            bajarHeap(0); //Bajamos el ultimo elemento a su nueva posicion
        }
        return raiz;
    }

    //bajarHeap: 'baja' el elemento de la raiz a su posicion correcta en la cola de prioridad
    private void bajarHeap(int indice){
        int tamanio = heap.size();

        while(true){
            int mayorPrioridad = indice; //El de mayor prioridad siempre es el primer elemento
            int hijoIzq = 2 * indice + 1; //Razón matematica para encontrar la posicion del hijo izquierdo, visto en clase
            int hijoDer = 2 * indice + 2; //Razón matematica para encontrar la posicion del hijo derecho, visto en clase

            /*Si el indice del hijo izquierdo es menor que el tamaño del arreglo y si al comparar
            el valor del hijo izquierdo con el valor del elemento con mayor prioridad, la diferencia es mayor a 0...
            entonces el hijo izquierdo tiene una mayor prioridadd
             */
            if(hijoIzq < tamanio &&
                    comparador.compare(heap.get(hijoIzq), heap.get(mayorPrioridad)) > 0){
                mayorPrioridad = hijoIzq;
            }

              /*Lo mismo que con el izquierdo, si el indice del hijo derecho es menor que el tamaño del arreglo y
              si al comparar el valor del hijo derecho con el valor del elemento con mayor prioridad, la diferencia
              es mayor a 0... entonces el hijo derecho tiene una mayor prioridadd
             */
            if(hijoDer < tamanio &&
                    comparador.compare(heap.get(hijoDer), heap.get(mayorPrioridad)) > 0){
                mayorPrioridad = hijoDer;
            }

            /*Por ultimo, si el valor de posicion del indice (el mayor prioritario), difieere del valor
            de posicion de la variable 'mayorPrioridad', hacemos un intercambio y seteamos (hundimos)
            el valor del indice con el de 'mayorPrioridad'
             */
            if(mayorPrioridad == indice) break;
             //Intercambiamos
            intercambiar(indice, mayorPrioridad);
            indice = mayorPrioridad;
        }
    }

    //Uso el intercambio mas de una vez, asi que ahora le hare su propia funcion para recilcar codigo
    private void intercambiar(int i, int j){
        T tmp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, tmp);
    }

    //=================================== PEEK ===============================
    //Normal en las colas, peek devuelve el elemento de mayor prioridad (primero en la lista) sin extraerlo.
    public T peek(){
        return heap.isEmpty() ? null : heap.get(0); //Si la cola esta vacia, retorna nulo, sino, retorna el primer elemento.
    }

    //=================================== MODIFICAR PRIORIDAD ===============================
    //Suponiendo que se quiera modificar la prioridad de un elemento. Darle mas o menos prioridad en cualquier momento

    /*
    Mejoramos este metodo, porque no habiamos considerado de que el dato 'nuevo' y 'viejo' podrian ser el mismo... solo
    lo hicimos por buena practica
     */
    public void modificarPrioridad(T elemento){
        int indice = buscarIndice(elemento); //Buscamos el indice del valor viejo
        if (indice == -1) return; //Si el indice no existe, no hay nada que modificar

        //Ahora, deberiamos de intentar subir primero el valor, en caso de que la prioridad lo permita
        //De lo contrario, lo bajamos
        int nievoIndice = subirHeap(indice);
        bajarHeap(nievoIndice);
    }

    private int buscarIndice(T valor){
        for(int i = 0; i < heap.size(); i++){
            if(heap.get(i).equals(valor)) return i; //Si el valor de 'i' es igual al valor buscado, retornamos la posicion 'i'
        }
        return -1; //Sino, el valor no existe
    }

    //----------------- OBTENER TOP K -----------------------------
    // Retorna los K elementos de mayor prioridad SIN destruir el heap original
    public List<T> topK(int k) {
        //Iniciamos una lisra en la cual colocar el resultado
        List<T> resultado = new ArrayList<>();
        //En caso de que K <= 0 o que la cola este vacia... retornamos el resultaado vacio.
        if(k <= 0 || heap.isEmpty()) return resultado;

        // Hacemos una copia para no destruir el heap original
        MyPriorityQueue<T> copia = new MyPriorityQueue<>(this.comparador);
        for(T elemento : this.heap){
            copia.insertar(elemento);
        }


        for(int i = 0; i < k && !copia.estaVacia(); i++){
            resultado.add(copia.extraer());
        }
        return resultado;
    }


    //--------------------- COMPARATOR --------------------------
    public void setComparator(Comparator<T> nuevoComparador){
        validarComparador(nuevoComparador);
        this.comparador = nuevoComparador;
        reconstruirHeap(); //Aqui mandamos a reconstruir el heap
    }

    //Usando el comparador creado anteriormente, el actual, hacemos la reconstruccion del heap
    /*Usaremos un algoritmo que encontramos eficiente, el cual implementamos desde documentacion de StackOverflow
    y videos, el mismo se llama: **HEAPIFY DE FLOYD**
     */
    private void reconstruirHeap(){
        /*(heap.size() / 2) - 1 es una formula que, segun la teoria, encuentra el ultimo nodo que tiene al menos
        un hijo, es decir que evita empezar por las hojas, ya que eso haria que bajarHeap no haga nada.
        Ademas, empezamos de atras para adelante para que la parte 'baja' del arbol se arregle antes que la cima, osea
        un enfoque BOTTOM  - UP
         */
        for( int i = (heap.size() / 2) - 1; i >= 0; i--){
            bajarHeap(i);
        }
    }

    //Esta validacion se me hace una buena practica, lo vi en una implementacion y creo que no esta de mas
    private void validarComparador(Comparator<T> comparador){
        if(comparador == null) {
            throw new IllegalArgumentException("El comparador no puede ser null.");
        }
    }

    //----------------------- ESTADOS --------------------------
    public boolean estaVacia() {
        return heap.isEmpty();
    }
    public int tamanio() {
        return heap.size();
    }

    //Devuelve una copia de los elementos en orden del array interno.
    public List<T> obtenerTodos() {
        return new ArrayList<>(heap);
    }

    public void limpiar() {
        heap.clear();
    }

    //--------------------- GETTER del COMPARADOR --------------------
    public Comparator<T> getComparator() { return this.comparador; }

}

