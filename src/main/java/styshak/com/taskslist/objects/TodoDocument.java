package styshak.com.taskslist.objects;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import styshak.com.taskslist.enums.PriorityType;

/**
 * Created by Sergey on 17.09.2016.
 */
public class TodoDocument implements Serializable {

    private UUID id;
    private String name;
    private String content;
    private Date createDate;
    private PriorityType priorityType = PriorityType.LOW;
    private boolean checked;
    private String imageFolderPath;

    public TodoDocument() {

    }

    public TodoDocument(String name, String content, Date createDate, PriorityType priorityType, String imageFolderPath) {
        this.name = name;
        this.content = content;
        this.createDate = createDate;
        this.priorityType = priorityType;
        this.imageFolderPath = imageFolderPath;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public PriorityType getPriorityType() {
        return priorityType;
    }

    public void setPriorityType(PriorityType priorityType) {
        this.priorityType = priorityType;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getImageFolderPath() {
        return imageFolderPath;
    }

    public void setImageFolderPath(String imageFolderPath) {
        this.imageFolderPath = imageFolderPath;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoDocument that = (TodoDocument) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
