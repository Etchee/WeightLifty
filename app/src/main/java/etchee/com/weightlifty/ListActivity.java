package etchee.com.weightlifty;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import java.util.List;

/**
 * Created by rikutoechigoya on 2017/03/30.
 */

public class ListActivity extends AppCompatActivity {

    private ListView listview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        //for the empty view
        View emptyView= findViewById(R.id.view_empty);
        listview = (ListView)findViewById(R.id.listview_workout);
        listview.setEmptyView(emptyView);
    }
}
