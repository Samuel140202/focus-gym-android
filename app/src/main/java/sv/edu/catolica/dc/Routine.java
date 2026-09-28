package sv.edu.catolica.dc;

import android.os.Parcel;
import android.os.Parcelable;

public class Routine implements Parcelable {


    private String title;
    private String meta;
    private String description;
    private String muscles;
    private String level;
    private int progress;


    public Routine() { }


    public Routine(String title, String meta, String description, String muscles, String level, int progress) {
        this.title = title;
        this.meta = meta;
        this.description = description;
        this.muscles = muscles;
        this.level = level;
        this.progress = progress;
    }


    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    public String getMeta() { return meta; }
    public void setMeta(String meta) { this.meta = meta; }

    public String getMuscles() { return muscles; }
    public void setMuscles(String muscles) { this.muscles = muscles; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }


    protected Routine(Parcel in) {
        title = in.readString();
        meta = in.readString();
        description = in.readString();
        muscles = in.readString();
        level = in.readString();
        progress = in.readInt();
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(meta);
        dest.writeString(description);
        dest.writeString(muscles);
        dest.writeString(level);
        dest.writeInt(progress);
    }

    @Override public int describeContents() { return 0; }

    public static final Creator<Routine> CREATOR = new Creator<Routine>() {
        @Override public Routine createFromParcel(Parcel in) { return new Routine(in); }
        @Override public Routine[] newArray(int size) { return new Routine[size]; }
    };
}
