package styshak.com.taskslist.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import styshak.com.taskslist.R;
import styshak.com.taskslist.adapters.TodoAdapter;
import styshak.com.taskslist.objects.AppContext;
import styshak.com.taskslist.objects.TodoDocument;
import styshak.com.taskslist.objects.TodoListComparator;

public class TodoList extends AppCompatActivity  {

    private ListView listViewTasks;
    private EditText txtSearch;
    private ArrayList<TodoDocument> listDocuments;
    private Intent intent;
    private TodoAdapter todoAdapter;
    private MenuItem menuSort, menuDelete, menuCreate;
    private Comparator<TodoDocument> comparator = TodoListComparator.getDateComparator();

    private CheckboxListener checkboxListener = new CheckboxListener();
    private BroadcastReceiver refreshDocumentReceiver = new RefreshDocumentReceiver();
    private BroadcastReceiver deleteDocumentReceiver = new DeleteDocumentReceiver();
    private BroadcastReceiver addDocumentReceiver = new AddDocumentReceiver();
    private List<TodoDocument> documentsForDelete = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        listViewTasks = (ListView) findViewById(R.id.listTasks);
        listViewTasks.setOnItemClickListener(new ListViewClickListener());
        listViewTasks.setEmptyView(findViewById(R.id.emptyView));
        listViewTasks.setLayoutAnimation(createAnimation());

        listDocuments = ((AppContext) getApplicationContext()).getListDocuments();

        txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtSearch.addTextChangedListener(new TextChangeListener());
        intent = new Intent(this, TodoDetails.class);

        LocalBroadcastManager.getInstance(this).registerReceiver(deleteDocumentReceiver,
                new IntentFilter(AppContext.RECEIVER_DELETE_DOCUMENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshDocumentReceiver,
                new IntentFilter(AppContext.RECEIVER_REFRESH_LISTVIEW));
        LocalBroadcastManager.getInstance(this).registerReceiver(addDocumentReceiver,
                new IntentFilter(AppContext.RECEIVER_ADD_DOCUMENT));

        todoAdapter = new TodoAdapter(this, R.id.todo_name, listDocuments, checkboxListener);
        listViewTasks.setAdapter(todoAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(deleteDocumentReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshDocumentReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(addDocumentReceiver);
    }

    private LayoutAnimationController createAnimation() {

        AnimationSet set = new AnimationSet(true);
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(300);
        set.addAnimation(animation);
        return new LayoutAnimationController(set, 0.3f);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshListView();
    }

    private void checkControlsActive() {
        if (menuSort == null || menuDelete == null)
            return;
        if (listDocuments.isEmpty()) {
            menuDelete.setEnabled(false);
            menuSort.setEnabled(false);
            menuCreate.setEnabled(true);
            txtSearch.setEnabled(false);
        } else {
            menuDelete.setEnabled(!documentsForDelete.isEmpty());
            menuSort.setEnabled(documentsForDelete.isEmpty());
            menuCreate.setEnabled(documentsForDelete.isEmpty());
            txtSearch.setEnabled(documentsForDelete.isEmpty());
        }
    }

    private void sort() {
        Collections.sort(listDocuments, comparator);
        todoAdapter = new TodoAdapter(this, R.id.todo_name, listDocuments, checkboxListener);
        listViewTasks.setAdapter(todoAdapter);
        todoAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_list, menu);
        menuSort = menu.findItem(R.id.menu_sort);
        menuDelete = menu.findItem(R.id.menu_delete_check);
        menuCreate = menu.findItem(R.id.add_task);
        checkControlsActive();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.isChecked()) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.add_task: {
                Bundle bundle = new Bundle();
                bundle.putInt(AppContext.ACTION_TYPE, AppContext.ACTION_NEW_TASK);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.pull_in_right,
                        R.anim.push_out_left);
                return true;
            }
            case R.id.menu_sort_name: {
                comparator = TodoListComparator.getNameComparator();
                sort();
                item.setChecked(true);
                return true;
            }
            case R.id.menu_sort_date: {
                comparator = TodoListComparator.getDateComparator();
                sort();
                item.setChecked(true);
                return true;
            }
            case R.id.menu_sort_priority: {
                comparator = TodoListComparator.getPriorityComparator();
                sort();
                item.setChecked(true);
                return true;
            }
            case R.id.menu_delete_check: {
                if (!documentsForDelete.isEmpty()) {
                    Intent intent = new Intent(AppContext.RECEIVER_DELETE_DOCUMENT);
                    intent.putExtra(AppContext.DOCS_TO_DELETE, new ArrayList<>(documentsForDelete));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    documentsForDelete.clear();
                }
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void clearSearch(View view) {
        txtSearch.setText("");
    }

    private class ListViewClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TodoDocument todoDocument = (TodoDocument) parent.getAdapter().getItem(position);
            if(todoDocument != null && !todoDocument.isChecked()) {
                Bundle bundle = new Bundle();
                bundle.putInt(AppContext.ACTION_TYPE, AppContext.ACTION_UPDATE);
                int currentTodoIndex = AppContext.getListPosition(listDocuments, todoDocument);
                bundle.putInt(AppContext.DOC_POSITION, currentTodoIndex);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.pull_in_right,
                        R.anim.push_out_left);
            }
        }
    }

    private class TextChangeListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (todoAdapter != null) {
                todoAdapter.getFilter().filter(s);
            }
        }
    }

    private void refreshListView() {
        documentsForDelete.clear();
        todoAdapter.notifyDataSetChanged();
        checkControlsActive();
        setTitle(getResources().getString(R.string.app_name) + " ("+listDocuments.size() + ")");
        if(listDocuments.size() == 0) {
            txtSearch.setText("");
        }
        todoAdapter.getFilter().filter(txtSearch.getText());
    }

    private class RefreshDocumentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshListView();
        }
    }

    private class AddDocumentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            TodoDocument addDocument = (TodoDocument)intent.getSerializableExtra(AppContext.DOC_TO_ADD);
            todoAdapter.add(addDocument);
            listDocuments.add(addDocument);
        }
    }

    private class DeleteDocumentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            TodoDocument removedDocument = (TodoDocument)intent.getSerializableExtra(AppContext.DOC_TO_DELETE);

            if (removedDocument != null) {// если 1 документ на удаление
                if(removedDocument.getImageFolderPath() != null) {
                    deleteTodoGallery(new File(removedDocument.getImageFolderPath()));
                }
                todoAdapter.remove(removedDocument);
                listDocuments.remove(removedDocument);
                getCurrentTodoFile(removedDocument).delete();
            } else {// если несколько документов для удаления
                final List<TodoDocument> docToDeleteList = (List<TodoDocument>)intent.getSerializableExtra(AppContext.DOCS_TO_DELETE);
                AlertDialog.Builder builder = new AlertDialog.Builder(TodoList.this);
                builder.setMessage(getResources().getString(R.string.confirm_delete_docs) + " (" + docToDeleteList.size() + ")");
                builder.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            refreshListView();
                        }
                    });
                builder.setPositiveButton(R.string.delete,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            for (TodoDocument todoDocument : docToDeleteList) {
                                if(todoDocument.getImageFolderPath() != null) {
                                    deleteTodoGallery(new File(todoDocument.getImageFolderPath()));
                                }
                                todoAdapter.remove(todoDocument);
                                listDocuments.remove(todoDocument);
                                getCurrentTodoFile(todoDocument).delete();
                            }
                            refreshListView();
                        }
                    });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    private File getCurrentTodoFile(TodoDocument todoDocument) {
        String filePath = ((AppContext) getApplicationContext()).getPrefsDir() + "/"
                + todoDocument.getId() + ".xml";
        return new File(filePath);
    }

    private void deleteTodoGallery(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteTodoGallery(child);
        fileOrDirectory.delete();
    }

    private class CheckboxListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            CheckBox checkBox = (CheckBox) v;
            TodoDocument todoDocument = (TodoDocument) checkBox.getTag();
            todoDocument.setChecked(checkBox.isChecked());

            RelativeLayout ve = (RelativeLayout)v.getParent();

            TextView txtTodoName = (TextView)ve.findViewById(R.id.todo_name);
            TextView txtTodoDate = (TextView)ve.findViewById(R.id.todo_date);

            if (checkBox.isChecked()) {
                documentsForDelete.add(todoDocument);
                txtTodoName.setTextColor(Color.LTGRAY);
                txtTodoDate.setTextColor(Color.LTGRAY);
            } else {
                documentsForDelete.remove(todoDocument);
                txtTodoName.setTextColor(Color.BLACK);
                txtTodoDate.setTextColor(Color.BLACK);
            }
            checkControlsActive();
        }
    }
}
