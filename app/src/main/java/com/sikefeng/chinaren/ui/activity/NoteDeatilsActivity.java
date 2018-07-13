package com.sikefeng.chinaren.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sikefeng.chinaren.R;
import com.sikefeng.chinaren.databinding.ActivityNoteDeatilsBinding;
import com.sikefeng.chinaren.entity.model.NoteBean;
import com.sikefeng.chinaren.utils.speech.SpeechSynthesizerUtils;

public class NoteDeatilsActivity extends AppCompatActivity {

    private ActivityNoteDeatilsBinding currentBinding;
    private NoteBean noteBean;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        currentBinding = DataBindingUtil.setContentView(this, R.layout.activity_note_deatils);
        noteBean = (NoteBean) getIntent().getSerializableExtra("noteBean");
        currentBinding.setModel(noteBean);
        setToolbar(currentBinding.toolbar);
        currentBinding.toolbar.setOnMenuItemClickListener(onMenuItemClick);
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            switch (menuItem.getItemId()) {
                case R.id.action_edit:
                    Intent editIntent = new Intent(mContext, NewNoteActivity.class);
                    editIntent.putExtra("noteBean", noteBean);
                    startActivity(editIntent);
                    break;
                case R.id.action_share:
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "http://120.78.79.198:8080/AIWeb/sharenote?nid=" + noteBean.getId());
                    shareIntent.setType("text/plain");
                    startActivity(shareIntent);
                    break;
                case R.id.action_settings:
                    SpeechSynthesizerUtils.getInstance(mContext).startSpeak(noteBean.getContent());
                    break;
                case R.id.action_open_web:
                    Intent intent = new Intent();
                    intent.setData(Uri.parse("http://120.78.79.198:8080/AIWeb/sharenote?nid=" + noteBean.getId()));//Url 就是你要打开的网址
                    intent.setAction(Intent.ACTION_VIEW);
                    startActivity(intent); //启动浏览器
                    break;
            }
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void setToolbar(Toolbar toolbar) {
        if (null != toolbar) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            try {
                //给左上角图标的左边加上一个返回的图标
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } catch (NullPointerException e) {
            }
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (SpeechSynthesizerUtils.getInstance(mContext).isPlaying()){
            SpeechSynthesizerUtils.getInstance(mContext).stopSpeak();
        }
    }
}