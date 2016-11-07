package styshak.com.taskslist.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;

import styshak.com.taskslist.R;
import styshak.com.taskslist.adapters.GalleryImageAdapter;
import styshak.com.taskslist.objects.ImageItem;
import styshak.com.taskslist.objects.AppContext;
import styshak.com.taskslist.objects.TodoDocument;
import styshak.com.taskslist.utils.ImageUtils;

public class GalleryActivity extends AppCompatActivity {

    private GridView gridView;
    private GalleryImageAdapter galleryAdapter;
    private TodoDocument todoDocument;
    private ArrayList<ImageItem> imageItems;
    private String imageFolderPath;
    private GalleryClickListener itemClickListener = new GalleryClickListener();
    private BroadcastReceiver deleteImageReceiver = new DeleteImageReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        todoDocument = (TodoDocument) getIntent().getExtras().get(AppContext.OPEN_GALLERY);
        imageFolderPath = todoDocument.getImageFolderPath();
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setEmptyView(findViewById(R.id.emptyGalleryView));
        if(savedInstanceState == null || !savedInstanceState.containsKey(AppContext.GALLERY_ACTIVITY_INSTANCE_STATE)) {
            new LoadDocumentsTask().execute();
        } else {
            imageItems = savedInstanceState.getParcelableArrayList(AppContext.GALLERY_ACTIVITY_INSTANCE_STATE);
            galleryAdapter = new GalleryImageAdapter(GalleryActivity.this, R.layout.gallery_grid_item, imageItems);
            gridView.setAdapter(galleryAdapter);
            galleryAdapter.notifyDataSetChanged();
            setTitle(getResources().getString(R.string.gallery_name) + " ("+imageItems.size() + ")");
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        gridView.setOnItemClickListener(itemClickListener);
        LocalBroadcastManager.getInstance(this).registerReceiver(deleteImageReceiver,
                new IntentFilter(AppContext.RECEIVER_DELETE_IMAGE));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(AppContext.GALLERY_ACTIVITY_INSTANCE_STATE, imageItems);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(deleteImageReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GalleryClickListener implements  AdapterView.OnItemClickListener  {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ImageItem item = (ImageItem) parent.getItemAtPosition(position);
            Intent intent = new Intent(GalleryActivity.this, GalleryDetailActivity.class);
            intent.putExtra(AppContext.IMAGE_ITEM, item);
            intent.putExtra(AppContext.IMAGE_POSITION, position);
            intent.putExtra(AppContext.GALLERY_SIZE, imageItems.size());
            startActivity(intent);
            overridePendingTransition(R.anim.pull_in_right,
                    R.anim.push_out_left);
        }
    }

    private class DeleteImageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(AppContext.RECEIVER_DELETE_IMAGE)) {
                ImageItem removedItem = intent.getParcelableExtra(AppContext.IMAGE_ITEM);
                if (removedItem != null) {
                    galleryAdapter.remove(removedItem);
                    imageItems.remove(removedItem);
                    new File(removedItem.getImagePath()).delete();
                }
                galleryAdapter.notifyDataSetChanged();
                setTitle(getResources().getString(R.string.gallery_name) + " (" + imageItems.size() + ")");
            }
        }
    }

    private void lockScreenOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void unlockScreenOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    private class LoadDocumentsTask extends AsyncTask<Void, String, Void> {

        private ProgressDialog dialog;
        private File galerryFolder;
        private File[] images;

        @Override
        protected void onPreExecute() {
            lockScreenOrientation();
            int max = 0;
            if(imageFolderPath != null) {
                try {
                    galerryFolder = new File(imageFolderPath);
                    images = galerryFolder.listFiles();
                    max = images.length;
                } catch (Exception e) {

                }
            }
            if (max == 0) return;

            dialog = new ProgressDialog(GalleryActivity.this);
            dialog.setMax(max);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage(getResources().getString(R.string.loading));
            if (!dialog.isShowing()) {
                dialog.show();
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            gridView.setVisibility(View.VISIBLE);
            if (dialog != null && dialog.isShowing()) {
                galleryAdapter = new GalleryImageAdapter(GalleryActivity.this, R.layout.gallery_grid_item, imageItems);
                gridView.setAdapter(galleryAdapter);
                setTitle(getResources().getString(R.string.gallery_name) + " ("+imageItems.size() + ")");
                dialog.dismiss();
            } else {
                setTitle(getResources().getString(R.string.gallery_name) + " ("+imageItems.size() + ")");
            }
            unlockScreenOrientation();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            dialog.setProgress(Integer.valueOf(values[0]));
            dialog.setMessage(getResources().getString(R.string.loading)+"\""+values[1]+"...\"");
        }

        @Override
        protected Void doInBackground(Void... params) {
            int i = 0;
            imageItems = new ArrayList<>();
            if(imageFolderPath != null) {
                try {
                    for (File image : images) {
                        Bitmap bitmap = ImageUtils.decodeBitmapFromPath(image.getAbsolutePath(), 15);
                        if (bitmap != null) {
                            imageItems.add(new ImageItem(bitmap, image.getAbsolutePath()));
                        }
                        publishProgress(""+i, image.getName());
                        i++;
                    }
                } catch (Exception e) {

                }
            }
            return null;
        }
    }
}
