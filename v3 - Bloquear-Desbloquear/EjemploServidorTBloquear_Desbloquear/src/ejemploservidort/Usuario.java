package ejemploservidort;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

public class Usuario implements Serializable {

    private final PrintWriter pw;
    private Comparator<String> comparador;
    private ArrayList<String> UBloqueados;

    public Usuario(PrintWriter pw) {
        this.pw = pw;
        this.UBloqueados = new ArrayList<>();
    }
    
    public PrintWriter getPw() {
        return pw;
    }

    public void setLista(ArrayList<String> lista) {
        this.UBloqueados = lista;
    }

    public ArrayList<String> getLista() {
        return UBloqueados;
    }

    public boolean insertarLista(String usuario) {
        int tam = UBloqueados.size();
        int indice = buscar(usuario);
        if (indice >= 0) {
            this.UBloqueados.add(indice, usuario);
        } else {
            this.UBloqueados.add((-indice) - 1, usuario);
        }
        return UBloqueados.size() != tam;
    }
    
    private int buscar(String elemento) {
        if (comparador == null) {
            return this.buscarComp(elemento);
        }
        return this.buscarComp2(elemento);
    }
    
    private int buscarComp(String elemento) {
        int indice = 0;
        int tam = UBloqueados.size() - 1;
        while (indice <= tam) {
            int medio = (indice + tam) >>> 1;
            Comparable<String> comp = (Comparable<String>) UBloqueados.get(medio);
            int comp2 = comp.compareTo(elemento);
            if (comp2 < 0) {
                indice = medio + 1;
            } else if (comp2 > 0) {
                tam = medio - 1;
            } else {
                return medio;
            }
        }
        return -(indice + 1);
    }

    private int buscarComp2(String elemento) {
        int indice = 0;
        int tam = UBloqueados.size() - 1;
        while (indice <= tam) {
            int medio = (indice + tam) >>> 1;
            String val = UBloqueados.get(medio);
            int comp = comparador.compare(val, elemento);
            if (comp < 0) {
                indice = medio + 1;
            } else if (comp > 0) {
                tam = medio - 1;
            } else {
                return medio;
            }
        }
        return -(indice + 1);
    }
    
    public Comparator<String> getComparador() {
        return this.comparador;
    }

    public void setComparador(Comparator<String> comparador) {
        this.comparador = comparador;
        Collections.sort(UBloqueados, comparador);
    }

    public boolean quitarLista(String usuario) {
        return this.UBloqueados.remove(usuario);
    }
    
    public String mostrarBloqueados(){
        String bloqueados = "";
        for(int i = 0; i < UBloqueados.size(); i++){
            bloqueados += UBloqueados.get(i) + ",";
        }
        return bloqueados;
    }
    
    public void reemplazar(String nombre, String nombre2){
        int indice = buscar(nombre);
        this.UBloqueados.set(indice, nombre2);
    }

    public String getDato(int indice) {
        return this.UBloqueados.get(indice);
    }

    public int getTam() {
        return this.UBloqueados.size();
    }

    public boolean estaVacio() {
        return this.UBloqueados.isEmpty();
    }

    public boolean contiene(String usuario) {
        return this.UBloqueados.contains(usuario);
    }
}
