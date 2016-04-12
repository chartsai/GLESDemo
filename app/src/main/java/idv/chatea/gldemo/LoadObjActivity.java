package idv.chatea.gldemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO add loading mtllib sample
 */
public class LoadObjActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final String TITLE = "title";

    private static class Sample {
        private String title;
        private Class klass;

        public Sample(String title, Class klass) {
            this.title = title;
            this.klass = klass;
        }
    }

    private static final Sample[] mSamples = {
            new Sample("GLES 2.0 Load Obj Position", GLES2_Load_Obj_Position_Activity.class),
            new Sample("GLES 2.0 Simple Light Obj Sample", GLES2_Light_Obj_Activity.class),
            new Sample("GLES 2.0 Smooth Light Obj Sample", GLES2_Light_Smooth_Obj_Activity.class),
            new Sample("GLES 2.0 Light Texture Obj Sample", GLES2_Light_Texture_Obj_Activity.class),
            new Sample("GLES 2.0 Multiple Light Obj Sample", GLES2_Multiple_Light_Obj_Activity.class),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.listView);

        List<Map<String, Object>> items = createListItems();
        ListAdapter adapter = new SimpleAdapter(this, items, android.R.layout.simple_list_item_1,
                new String[] {TITLE}, new int[] {android.R.id.text1});

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);
    }

    private List<Map<String, Object>> createListItems() {
        List<Map<String, Object>> items = new ArrayList();
        for (int i = 0; i < mSamples.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put(TITLE, mSamples[i].title);
            items.add(map);
        }
        return items;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, mSamples[position].klass);
        startActivity(intent);
    }
}
