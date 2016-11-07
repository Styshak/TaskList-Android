package styshak.com.taskslist.objects;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ImageItem implements Parcelable {
    private transient Bitmap image;
    private String imagePath;

    protected ImageItem(Parcel in) {
        imagePath = in.readString();
    }

    public static final Creator<ImageItem> CREATOR = new Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel in) {
            return new ImageItem(in);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public ImageItem(Bitmap image, String imagePath) {
        super();
        this.image = image;
        this.imagePath = imagePath;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageItem imageItem = (ImageItem) o;

        return imagePath.equals(imageItem.imagePath);

    }

    @Override
    public int hashCode() {
        return imagePath.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        /*if(image != null) {
            dest.writeValue(image);
        }*/
        if(imagePath != null) {
            dest.writeString(imagePath);
        }
    }
}