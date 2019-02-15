package com.ly.a316.ly_meetingroommanagement.endActivity.activity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ly.a316.ly_meetingroommanagement.R;
import com.ly.a316.ly_meetingroommanagement.endActivity.fragment.AllFileFragment;
import com.ly.a316.ly_meetingroommanagement.endActivity.fragment.CommFileFragment;
import com.ly.a316.ly_meetingroommanagement.endActivity.object.FileEntity;
import com.ly.a316.ly_meetingroommanagement.endActivity.object.OnUpdateDataListener;
import com.ly.a316.ly_meetingroommanagement.endActivity.util.FileUtils;
import com.ly.a316.ly_meetingroommanagement.endActivity.util.PickerManager;
import com.yalantis.jellytoolbar.listener.JellyListener;
import com.yalantis.jellytoolbar.widget.JellyToolbar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;

public class FilePickerActivity extends AppCompatActivity implements OnUpdateDataListener {

    @BindView(R.id.search_file_toolbar)
    JellyToolbar searchFileToolbar;
    AppCompatEditText editText;
    JellyListener jellyListener = new JellyListener() {
        @Override
        public void onCancelIconClicked() {
            if (TextUtils.isEmpty(editText.getText())) {
                searchFileToolbar.collapse();
            } else {
                editText.getText().clear();
            }
        }
    };
    @BindView(R.id.btn_common)
    Button btnCommon;
    @BindView(R.id.btn_all)
    Button btnAll;
    @BindView(R.id.tv_size)
    TextView tvSize;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    private Fragment commonFileFragment, allFileFragment;
    private boolean isConfirm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);
        ButterKnife.bind(this);
        initview();


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    void initview() {
        WindowManager wm = getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);

        searchFileToolbar.getToolbar().setNavigationIcon(R.drawable.backtwo);
        searchFileToolbar.getToolbar().setTitleMargin(dm.widthPixels / 4, 0, 0, 0);
        searchFileToolbar.getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchFileToolbar.setJellyListener(jellyListener);
        searchFileToolbar.getToolbar().setPadding(0, getStatusBarHeight(), 0, 0);

        editText = (AppCompatEditText) LayoutInflater.from(this).inflate(R.layout.edit_text, null);
        editText.setBackgroundResource(R.color.colorTransparent);
        searchFileToolbar.setContentView(editText);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setFragment(1);
    }

    @OnClick({R.id.btn_common, R.id.btn_all, R.id.tv_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_common:
                setFragment(1);
                btnCommon.setBackgroundResource(R.drawable.no_read_pressed);
                btnCommon.setTextColor(ContextCompat.getColor(this, R.color.white));
                btnAll.setBackgroundResource(R.drawable.already_read);
                btnAll.setTextColor(ContextCompat.getColor(this, R.color.classical_blue));
                break;
            case R.id.btn_all:
                setFragment(2);
                btnCommon.setBackgroundResource(R.drawable.no_read);
                btnCommon.setTextColor(ContextCompat.getColor(this, R.color.classical_blue));
                btnAll.setBackgroundResource(R.drawable.already_read_pressed);
                btnAll.setTextColor(ContextCompat.getColor(this, R.color.white));
                break;
            case R.id.tv_confirm:
                isConfirm = true;
                setResult(101);
                finish();
                break;
        }
    }

    private void setFragment(int type) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        hideFragment(fragmentTransaction);
        switch (type) {
            case 1:
                if (commonFileFragment == null) {
                    commonFileFragment = CommFileFragment.newInstance();//创建对象和new的事项方法不一样 只能调用无参构造
                    ((CommFileFragment) commonFileFragment).setOnUpdateDataListener((OnUpdateDataListener) this);
                    fragmentTransaction.add(R.id.fl_content, commonFileFragment);
                } else {
                    fragmentTransaction.show(commonFileFragment);
                }
                break;
            case 2:
                if (allFileFragment == null) {
                    allFileFragment = AllFileFragment.newInstance();
                    ((AllFileFragment) allFileFragment).setOnUpdateDataListener((OnUpdateDataListener) this);
                    fragmentTransaction.add(R.id.fl_content, allFileFragment);
                } else {
                    fragmentTransaction.show(allFileFragment);
                }
                break;
        }
        fragmentTransaction.commit();
    }

    private void hideFragment(FragmentTransaction transaction) {
        if (commonFileFragment != null) {
            transaction.hide(commonFileFragment);
        }
        if (allFileFragment != null) {
            transaction.hide(allFileFragment);
        }
    }

    private long currentSize;

    @Override
    public void update(long size) {
        currentSize += size;
        tvSize.setText(getString(R.string.already_select, FileUtils.getReadableFileSize(currentSize)));
        String res = "(" + PickerManager.getInstance().files.size() + "/" + PickerManager.getInstance().maxCount + ")";
        tvConfirm.setText(getString(R.string.file_select_res, res));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isConfirm) {
            PickerManager.getInstance().files.clear();
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    void search(String nei) {

        // TODO: 2019/2/15 查询文件没做
        Cursor cursor = this.getContentResolver().query(
//数据源
                MediaStore.Files.getContentUri("external"),
//查询ID和名称
                new String[]{MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.TITLE,MediaStore.Files.FileColumns.DATA,SIZE},
//条件为文件类型
                MediaStore.Files.FileColumns.TITLE + " LIKE ? ",
//类型为“video/mp4”
                new String[]{"%" + nei+ "%" },
//默认排序
                null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(DATA));
                String paths = cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.Files.FileColumns.SIZE));
                String anme = path.substring(path.lastIndexOf("/") + 1);
                Log.i("zjcccc", anme);
            }
        }

       /* ContentResolver resolver = getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.Files.FileColumns.TITLE + "= ? " + nei;
        Cursor cursor = resolver.query(uri,
                new String[]{MediaStore.Files.FileColumns.DATA, SIZE},
                selection,
                null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(DATA));
                String paths = cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.Files.FileColumns.SIZE));
                String anme = path.substring(path.lastIndexOf("/") + 1);
                Log.i("zjc", anme);
            }
        }
        cursor.close();*/
    }
}
