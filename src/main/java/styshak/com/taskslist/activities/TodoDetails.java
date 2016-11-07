package styshak.com.taskslist.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import styshak.com.taskslist.R;
import styshak.com.taskslist.enums.PriorityType;
import styshak.com.taskslist.objects.AppContext;
import styshak.com.taskslist.objects.TodoDocument;

public class TodoDetails extends AppCompatActivity {

    public static final int NAME_LENGTH = 10;
    public static final int REQUEST_IMAGE_CAPTURE = 11;
    public static final String IMAGE_PATH = "com.styshak.taskslist.activities.TodoDetails.ImagePath";
    public static final String PHOTOS_TO_SAVE = "com.styshak.taskslist.activities.TodoDetails.PhotosToSave";
    private int currentTodoIndex;
    private EditText todoDetails;
    private TodoDocument todoDocument;
    private List<TodoDocument> listDocuments;
    private int actionType;
    private MenuItem menuPriority, delete;
    private PriorityType currentPriorityType;
    private String imageFolderPath;
    private Cursor imageCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_details);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        todoDetails = (EditText) findViewById(R.id.toDoDetails);
        listDocuments = ((AppContext) getApplicationContext()).getListDocuments();
        actionType = getIntent().getExtras().getInt(AppContext.ACTION_TYPE);
        prepareDocument(actionType);
        setTitle("");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    private void prepareDocument(int actionType) {
        switch (actionType) {
            case AppContext.ACTION_NEW_TASK:
                todoDocument = new TodoDocument();
                break;
            case AppContext.ACTION_UPDATE:
                currentTodoIndex = (int)getIntent().getExtras().get(AppContext.DOC_POSITION);
                todoDocument = listDocuments.get(currentTodoIndex);
                todoDetails.setText(todoDocument.getContent());
                imageFolderPath = getImageFolderPath();
                break;
            default:
                break;
        }
        currentPriorityType = todoDocument.getPriorityType();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_details, menu);
        menuPriority = menu.findItem(R.id.menu_priority);
        delete = menu.findItem(R.id.delete);
        if (actionType == AppContext.ACTION_NEW_TASK) {
            delete.setEnabled(false);
        } else {
            delete.setEnabled(true);
        }
        MenuItem menuItem = menuPriority.getSubMenu().getItem(todoDocument.getPriorityType().getIndex());
        menuItem.setChecked(true);
        return true;
    }

    private String getDocumentName(String docContent) {
        int docContentLength = docContent.trim().length();
        if(docContentLength <= NAME_LENGTH) {
            return docContent.substring(0, docContentLength);
        } else {
            return docContent.substring(0, NAME_LENGTH) + "...";
        }
    }

    private void onSaveDocument(boolean withClose) {
        String docContent = todoDetails.getText().toString();
        String docName = getDocumentName(docContent);
        UUID uuid = todoDocument.getId() == null ? UUID.randomUUID() : todoDocument.getId();
        todoDocument.setId(uuid);
        todoDocument.setName(docName);
        todoDocument.setContent(docContent);
        todoDocument.setPriorityType(currentPriorityType);
        imageFolderPath = getImageFolderPath();
        todoDocument.setImageFolderPath(imageFolderPath);
        todoDocument.setCreateDate(new Date());

        SharedPreferences sharedPref = getSharedPreferences(String.valueOf(uuid), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(AppContext.FIELD_ID, todoDocument.getId().toString());
        editor.putString(AppContext.FIELD_NAME, todoDocument.getName());
        editor.putString(AppContext.FIELD_CONTENT,todoDocument.getContent());
        editor.putInt(AppContext.FIELD_PRIORITY_TYPE, todoDocument.getPriorityType().getIndex());
        editor.putString(AppContext.FIELD_IMAGE_FOLDER_PATH, imageFolderPath);
        editor.putLong(AppContext.FIELD_CREATE_DATE, todoDocument.getCreateDate().getTime());
        editor.commit();
        if (actionType == AppContext.ACTION_NEW_TASK) {
            Intent intent = new Intent(AppContext.RECEIVER_ADD_DOCUMENT);
            intent.putExtra(AppContext.DOC_TO_ADD, todoDocument);
            LocalBroadcastManager.getInstance(TodoDetails.this).sendBroadcast(intent);
            if(!withClose) {
                actionType = AppContext.ACTION_UPDATE;
                delete.setEnabled(true);
            }
        } else {
            listDocuments.set(currentTodoIndex, todoDocument);
            Intent intent = new Intent(AppContext.RECEIVER_REFRESH_LISTVIEW);
            LocalBroadcastManager.getInstance(TodoDetails.this).sendBroadcast(intent);
        }
        if(withClose) {
            finish();
            overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        }
    }

    private void onDeleteDocument() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete_doc);

        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.setPositiveButton(R.string.delete,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(AppContext.RECEIVER_DELETE_DOCUMENT);
                        intent.putExtra(AppContext.DOC_TO_DELETE, todoDocument);
                        LocalBroadcastManager.getInstance(TodoDetails.this).sendBroadcast(intent);
                        imageFolderPath = null;
                        finish();
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showConfirmSaveMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_save_doc);

        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.setPositiveButton(R.string.save,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onSaveDocument(false);
                        makePhoto();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:{
                onDeleteDocument();
                return true;
            }
            case android.R.id.home:{
                finish();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                return true;
            }
            case R.id.save:{
                onSaveDocument(true);
                return true;
            }
            case R.id.menu_priority_low:
            case R.id.menu_priority_middle:
            case R.id.menu_priority_high: {
                item.setChecked(true);
                currentPriorityType = PriorityType.values()[Integer.valueOf(item.getTitleCondensed().toString())];
                return true;
            }
            case R.id.menu_take_photo: {
                if(actionType == AppContext.ACTION_NEW_TASK) {
                    showConfirmSaveMessage();
                } else {
                    makePhoto();
                }
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void makePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = Uri.fromFile(getImagePath());
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            int id = getLastImageId();
            removeImage(id);
        }
    }

    private int getLastImageId(){
        final String[] imageColumns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
        final String imageOrderBy = MediaStore.Images.Media._ID+" DESC";
        imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);
        if(imageCursor.moveToFirst()){
            int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
            return id;
        }else{
            return 0;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageCursor != null) {
            imageCursor.close();
        }
    }

    private void removeImage(int id) {
        ContentResolver cr = getContentResolver();
        cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media._ID + "=?", new String[]{ Long.toString(id) } );
    }

    private File getImagePath() {
        return new File(getImageFolderPath() + File.separator
                + UUID.randomUUID() + ".jpg");
    }

    public void openGallery(View view) {
        Intent galleryActivity = new Intent(this, GalleryActivity.class);
        galleryActivity.putExtra(AppContext.OPEN_GALLERY, todoDocument);
        startActivity(galleryActivity);
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    private String getImageFolderPath() {
        if(todoDocument != null && todoDocument.getImageFolderPath() != null) {
            return todoDocument.getImageFolderPath();
        } else {
            File directory = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    getPackageName());
            if (!directory.exists()) {
                directory.mkdirs();
            }
            return directory.getAbsolutePath();
        }
    }
}
