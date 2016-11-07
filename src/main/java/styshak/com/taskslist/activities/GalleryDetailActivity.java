package styshak.com.taskslist.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import styshak.com.taskslist.R;
import styshak.com.taskslist.objects.ImageItem;
import styshak.com.taskslist.objects.AppContext;
import styshak.com.taskslist.utils.ImageUtils;


public class GalleryDetailActivity extends AppCompatActivity {

    private Toolbar topToolbar;
    private ImageView imageView;
    private int galleryListSize, imagePosition;
    private String imagePath;
    private ImageItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_details);

        item = getIntent().getParcelableExtra(AppContext.IMAGE_ITEM);
        imagePath = item.getImagePath();
        galleryListSize = getIntent().getIntExtra(AppContext.GALLERY_SIZE, 0);
        imagePosition = getIntent().getIntExtra(AppContext.IMAGE_POSITION, 0);
        imageView = (ImageView) findViewById(R.id.gallery_detail_image);
        Bitmap photo = ImageUtils.decodeBitmapFromPath(imagePath, 2);
        imageView.setImageBitmap(photo);
        initToolBar();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    private void initToolBar() {
        topToolbar = (Toolbar) findViewById(R.id.top_toolbar);
        setSupportActionBar(topToolbar);
        setTitle(getResources().getString(R.string.gallery_title));
        topToolbar.setSubtitle(imagePosition + 1 + "/" + galleryListSize);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionBar actionBar = getSupportActionBar();
                if(actionBar.isShowing()) {
                    actionBar.hide();
                } else {
                    actionBar.show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                return true;
            }
            case R.id.gallery_details_delete:{
                deleteImage();
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GalleryDetailActivity.this);
        builder.setMessage(R.string.confirm_delete_image);
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.setPositiveButton(R.string.delete,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(AppContext.RECEIVER_DELETE_IMAGE);
                        intent.putExtra(AppContext.IMAGE_ITEM, item);
                        LocalBroadcastManager.getInstance(GalleryDetailActivity.this).sendBroadcast(intent);
                        finish();
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_details, menu);
        return true;
    }
}
