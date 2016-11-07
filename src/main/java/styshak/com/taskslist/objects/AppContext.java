package styshak.com.taskslist.objects;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import styshak.com.taskslist.enums.PriorityType;

public class AppContext extends Application {

    public static final String ACTION_TYPE = "com.styshak.taskslist.AppContext.ActionType";
    public static final String DOC_TO_DELETE = "com.styshak.taskslist.AppContext.DOC_TO_DELETE";
    public static final String DOCS_TO_DELETE = "com.styshak.taskslist.AppContext.DOCS_TO_DELETE";
    public static final String DOC_TO_ADD = "com.styshak.taskslist.AppContext.DOC_TO_ADD";
    public static final String DOC_POSITION = "com.styshak.taskslist.AppContext.DOC_POSITION";
    public static final String IMAGE_ITEM = "com.styshak.taskslist.AppContext.IMAGE_ITEM";
    public static final String IMAGE_POSITION = "com.styshak.taskslist.AppContext.IMAGE_POSITION";
    public static final String GALLERY_SIZE = "com.styshak.taskslist.AppContext.GALLERY_SIZE";
    public static final String GALLERY_ACTIVITY_INSTANCE_STATE = "com.styshak.taskslist.AppContext.GALLERY_ACTIVITY_INSTANCE_STATE";

    public static final int ACTION_NEW_TASK = 0;
    public static final int ACTION_UPDATE = 1;

    public static final String FIELD_ID = "id";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_CREATE_DATE = "createDate";
    public static final String FIELD_PRIORITY_TYPE = "priorityType";
    public static final String FIELD_IMAGE_FOLDER_PATH = "imageFolderPath";
    public static final String OPEN_GALLERY = "openGallery";

    public static final String RECEIVER_DELETE_DOCUMENT = "com.styshak.taskslist.AppContext.DeleteDocument";
    public static final String RECEIVER_ADD_DOCUMENT = "com.styshak.taskslist.AppContext.AddDocument";
    public static final String RECEIVER_REFRESH_LISTVIEW = "com.styshak.taskslist.AppContext.RefreshListDocument";
    public static final String RECEIVER_DELETE_IMAGE = "com.styshak.taskslist.AppContext.DeleteImage";

    private ArrayList<TodoDocument> listDocuments = new ArrayList<TodoDocument>();

    public ArrayList<TodoDocument> getListDocuments() {
        return listDocuments;
    }

    public void setListDocuments(ArrayList<TodoDocument> listDocuments) {
        this.listDocuments = listDocuments;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fillTodoList();
    }

    public String getPrefsDir() {
        return  getApplicationInfo().dataDir + "/" + "shared_prefs";
    }

    private void fillTodoList() {
        File prefsDir = new File(
                ((AppContext) getApplicationContext()).getPrefsDir());

        if (prefsDir.exists() && prefsDir.isDirectory()) {
            String[] list = prefsDir.list();
            for (int i = 0; i < list.length; i++) {
                SharedPreferences sharedPref = getSharedPreferences(
                        list[i].replace(".xml", ""), Context.MODE_PRIVATE);
                TodoDocument todoDocument = new TodoDocument();
                todoDocument.setId(java.util.UUID.fromString(sharedPref.getString(
                        AppContext.FIELD_ID, null)));
                todoDocument.setContent(sharedPref.getString(
                        AppContext.FIELD_CONTENT, null));
                todoDocument.setCreateDate(new Date(sharedPref.getLong(
                        AppContext.FIELD_CREATE_DATE, 0)));
                todoDocument.setName(sharedPref.getString(
                        AppContext.FIELD_NAME, null));
                todoDocument.setImageFolderPath(sharedPref.getString(
                        AppContext.FIELD_IMAGE_FOLDER_PATH, null));
                todoDocument.setPriorityType(PriorityType.values()[sharedPref
                        .getInt(AppContext.FIELD_PRIORITY_TYPE, 0)]);
                listDocuments.add(todoDocument);
            }
        }
    }

    public static int getListPosition(List<TodoDocument> list, TodoDocument todoDocument) {
        return list.indexOf(todoDocument);
    }
}
