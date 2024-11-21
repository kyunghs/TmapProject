package com.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import java.util.ArrayList;

public class BookmarkList extends AppCompatActivity {

    private ListView bookmarkListView;
    private ArrayList<Bookmark> bookmarks;
    private BookmarkAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark_edit_bottom_sheet); // 메인 레이아웃 파일

        bookmarkListView = findViewById(R.id.bookmark_list);
        bookmarks = new ArrayList<>();

        // 예시 데이터 추가
        bookmarks.add(new Bookmark("기흥역 지웰 푸르지오", "용인시 기흥구 구갈동"));
        bookmarks.add(new Bookmark("아마존카", "서울시 영등포구 여의도동"));
        bookmarks.add(new Bookmark("제주도", "아름다운 섬"));

        // 어댑터 설정
        adapter = new BookmarkAdapter(this, bookmarks);
        bookmarkListView.setAdapter(adapter);
    }
}