package org.example.worddictionary_ed1_project2_api.structures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//CLASE RECICLADA DEL PROYECTO 01 (con cambios minimos --> agregue top K)


public class MyPriorityQueue<T> {
    //Creamos la lista y el comparador
    private final ArrayList<T> heap;
    private Comparator<T> comparador;

    //Ahora bien, necesitamos variables para el analisis, osea metricas
    private long intercambiosTotales;
    private long intercambiosUltimaOperacion; //Para avaluar una operacin en especifica
    private long insercionesContadas;
    private long extraccionesContadas;

    //Constructores
    //En caso no sepamos la cantidad de elementos a ingresar, es dinamico
    public MyPriorityQueue(Comparator<T> comparador){
        this.heap = new ArrayList<>();
        this.comparador = comparador;
    }

    //En caso sepamos cuantos elementos vayan a ingresar, es estático (se reserva el espacio exacto, mas eficiente)
    public MyPriorityQueue(Comparator<T> comparador, int capacidadInicial){
        this.heap = new ArrayList<>(capacidadInicial);
        this.comparador = comparador;
    }

    //=================================== INSERCIÓN ===============================
    public void insertar(T valor) {
        intercambiosUltimaOperacion = 0; //Inicializamos el contador
        heap.add(valor); //Agregamos el valor al arreglo, en la última posición
        insercionesContadas++; //Aumentamos el contador de inserciones
        subirHeap(heap.size() - 1); //Acomodamos el valor apropiadamente, mandamos el indice del ultimo elemento
    }

    //subirHeap: como su nombre lo indica, sube el elemento recién insertado hasta su posicion correcta
    private void subirHeap(int indice){
        while(indice > 0){ //Iteramos hasta 0 porque el elemento intentara subir hasta esta posicion (la mas alta en prioridad)
            int padre = (indice - 1)/2; //Relación matemática para encontrar el padre, vista en la clase : ((i - 1)/2)
            //Si el hijo tiene mayor prioridad que el padre, intercambiamos
            if(comparador.compare(heap.get(indice),  heap.get(padre)) > 0){ //Si el resultado de compararlos es > 0, significa  que el hijo tiene mayor prioridad
                //Entonces se realiza el intercambio
                T tmp = heap.get(indice); //Se crea una variable temporal con el indice del elemento
                heap.set(indice, heap.get(padre)); //Mueve el valor de padre a la posicion 'indice'
                heap.set(padre, tmp); //Mueve el valor de 'tmp', osea el valor del indice, a la posicion del padre
                intercambiosUltimaOperacion++;
                intercambiosTotales++;
                indice = padre;
            } else {
                break;
            }
        }
    }

    //=================================== EXTRACCIÓN ===============================
    //Su objetivo es extraer y devolver el elemento de mayor prioridad
    public T extraer() {
        if(heap.isEmpty()) return null;

        intercambiosUltimaOperacion = 0; //Volvemos a inicilizar el contador
        extraccionesContadas++; //Aumentamos el contador de extracciones

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
            if(mayorPrioridad != indice){
                //Entonces se realiza el intercambio
                T tmp = heap.get(indice); //Se crea una variable temporal con el indice del elemento
                heap.set(indice, heap.get(mayorPrioridad)); //Mueve el valor de mayorPrioridad a la posicion 'indice'
                heap.set(mayorPrioridad, tmp); //Mueve el valor de 'tmp', osea el valor del indice, a la posicion de mayorPrioridad
                intercambiosUltimaOperacion++;
                intercambiosTotales++;
                indice = mayorPrioridad;
            } else{
                break;
            }
        }
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
    lo hicimos por nbuena practica
     */
    public void modificarPrioridad(T elemento){
        int indice = buscarIndice(elemento); //Buscamos el indice del valor viejo
        if (indice == -1) return; //Si el indice no existe, no hay nada que modificar

        //Ahora, deberiamos de intentar subir primero el valor, en caso de que la prioridad lo permita
        //De lo contrario, lo bajamos
        subirHeap(indice);
        bajarHeap(indice);
    }

    private int buscarIndice(T valor){
        for(int i = 0; i < heap.size(); i++){
            if(heap.get(i).equals(valor)) return i; //Si el valor de 'i' es igual al valor buscado, retornamos la posicion 'i'
        }
        return -1; //Sino, el valor no existe
    }

    //=============================== OBTENER TOP K ===============================================
    // Retorna los K elementos de mayor prioridad SIN destruir el heap original
    public List<T> topK(int k) {
        // Hacemos una copia para no destruir el heap original
        MyPriorityQueue<T> copia = new MyPriorityQueue<>(this.comparador);
        for(T elemento : this.heap){
            copia.insertar(elemento);
        }

        List<T> resultado = new ArrayList<>();
        for(int i = 0; i < k && !copia.estaVacia(); i++){
            resultado.add(copia.extraer());
        }
        return resultado;
    }

    //=================================== LO BUENARDO: CAMBIAR ENTRE MAX Y MIN HEAP ===============================
    //El programa tiene que poder alternar entre Max y Min heap cuando sea.

    //Primero, hay que crear otro comparador
    public void setComparator(Comparator<T> nuevoComparador){
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

    //=================================== ESTADOS E INFO ADICIONAL ===============================
    public boolean estaVacia()  { return heap.isEmpty(); }
    public int     tamanio()    { return heap.size(); }

    //Devuelve una copia de los elementos en orden del array interno.
    public List<T> obtenerTodos() { return new ArrayList<>(heap); }

    public void limpiar() {
        heap.clear();
        resetearMetricas();
    }

    //================================== COSAS IMPORTANTES PARA LAS METRICAS ======================
    public long getIntercambiosTotales()  { return intercambiosTotales; }
    public long getIntercambiosUltimaOp() { return intercambiosUltimaOperacion; }
    public long getInsercionesCont()      { return insercionesContadas; }
    public long getExtraccionesCont()     { return extraccionesContadas; }

    public void resetearMetricas() {
        intercambiosTotales  = 0;
        intercambiosUltimaOperacion = 0;
        insercionesContadas     = 0;
        extraccionesContadas     = 0;
    }

    public void estadisticas() {
        System.out.println("=".repeat(45));
        System.out.println("  Estadisticas Cola de Prioridad");
        System.out.println("=".repeat(45));
        System.out.printf("  Elementos actuales  : %d%n",   tamanio());
        System.out.printf("  Inserciones         : %,d%n",  insercionesContadas);
        System.out.printf("  Extracciones        : %,d%n",  extraccionesContadas);
        System.out.printf("  Intercambios totales    : %,d%n",  intercambiosTotales);
        System.out.printf("  Proximo a salir     : %s%n",
                heap.isEmpty() ? "vacia" : heap.get(0).toString());
        System.out.println("=".repeat(45));
    }

    //Getter del comparador ---> implementacion necesaria dadas las pruebas
    public Comparator<T> getComparator() { return this.comparador; }

}

