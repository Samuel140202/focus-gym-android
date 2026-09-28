package sv.edu.catolica.dc;

import android.os.Parcel;
import android.os.Parcelable;

public class Exercise implements Parcelable {

    private String id;
    private String name;
    private int series;
    private int reps;
    private int restSeconds;
    private int imageRes;
    private boolean done;

    // === Constructores añadidos para compatibilidad ===
    public Exercise() { }

    /** Usado por RoutineEditorActivity y ExerciseEditDialogFragment */
    public Exercise(String name, int series, int reps, int restSeconds) {
        this(null, name, series, reps, restSeconds, 0);
    }

    /** Tu constructor original (DetailRoutineActivity, mock con ícono) */
    public Exercise(String id, String name, int series, int reps, int restSeconds, int imageRes) {
        this.id = id;
        this.name = name;
        this.series = series;
        this.reps = reps;
        this.restSeconds = restSeconds;
        this.imageRes = imageRes;
        this.done = false;
    }

    // --- Getters y Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSeries() { return series; }
    public void setSeries(int series) { this.series = series; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public int getRestSeconds() { return restSeconds; }
    public void setRestSeconds(int restSeconds) { this.restSeconds = restSeconds; }

    public int getImageRes() { return imageRes; }
    public void setImageRes(int imageRes) { this.imageRes = imageRes; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }

    // --- Parcelable implementation ---
    protected Exercise(Parcel in) {
        id = in.readString();
        name = in.readString();
        series = in.readInt();
        reps = in.readInt();
        restSeconds = in.readInt();
        imageRes = in.readInt();
        done = in.readByte() != 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(series);
        dest.writeInt(reps);
        dest.writeInt(restSeconds);
        dest.writeInt(imageRes);
        dest.writeByte((byte) (done ? 1 : 0));
    }

    @Override public int describeContents() { return 0; }

    public static final Creator<Exercise> CREATOR = new Creator<Exercise>() {
        @Override public Exercise createFromParcel(Parcel in) { return new Exercise(in); }
        @Override public Exercise[] newArray(int size) { return new Exercise[size]; }
    };
}
