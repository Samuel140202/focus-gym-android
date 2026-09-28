package sv.edu.catolica.dc;

import java.io.Serializable;

/** Modelo del selector  */
public class SelectorExercise implements Serializable {
    public enum Group { PECHO, ESPALDA, HOMBRO, PIERNA, BRAZO, CORE }
    public enum Level { PRINCIPIANTE, INTERMEDIO, AVANZADO }

    public final String id;
    public final String nombre;
    public final String media;
    public final Group grupo;
    public final Level nivel;

    public SelectorExercise(String id, String nombre, Group g, Level n, String media) {
        this.id = id;
        this.nombre = nombre;
        this.grupo = g;
        this.nivel = n;
        this.media = media;
    }
}
