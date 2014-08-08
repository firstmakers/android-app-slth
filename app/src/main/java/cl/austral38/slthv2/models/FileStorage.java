package cl.austral38.slthv2.models;

/**
 * Created by eDelgado on 09-06-14.
 */
public class FileStorage {
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    private String file ;
    private boolean selected;

    public FileStorage(){}

}
