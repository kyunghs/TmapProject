package com.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.myapplication.models.Bookmark;
import com.myapplication.R;

import java.util.ArrayList;

public class BookmarkAdapter extends ArrayAdapter<Bookmark> {

    public BookmarkAdapter(@NonNull Context context, ArrayList<Bookmark> bookmarks) {
        super(context, 0, bookmarks);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // 재사용할 ViewHolder 패턴 사용
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bookmark_list_item, parent, false);
        }

        // 현재 아이템 가져오기
        Bookmark currentBookmark = getItem(position);

        // TextView에 데이터 설정
        TextView textView1 = convertView.findViewById(R.id.item_text_1);
        TextView textView2 = convertView.findViewById(R.id.item_text_2);

        if (currentBookmark != null) {
            textView1.setText(currentBookmark.getTitle());
            textView2.setText(currentBookmark.getSubtitle());
        }

        return convertView;
    }
}