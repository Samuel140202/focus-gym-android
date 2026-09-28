package sv.edu.catolica.dc;

import java.util.ArrayList;

public class RoutinePack {
    public Routine routine;
    public ArrayList<Exercise> exercises;
    public RoutinePack() {}
    public RoutinePack(Routine r, ArrayList<Exercise> exs){ this.routine=r; this.exercises=exs; }
}
