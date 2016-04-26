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

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

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
            new Sample("GLES 1.0 Background Sample", GLES1_Background_Activity.class),
            new Sample("GLES 1.0 Triangle Sample", GLES1_Triangle_Activity.class),
            new Sample("GLES 1.0 Drawing Mode Sample", GLES1_DrawElements_Activity.class),
            new Sample("GLES 1.0 Viewport Sample", GLES1_Viewport_Activity.class),
            new Sample("GLES 1.0 Color Triangle Sample", GLES1_ColorTriangle_Activity.class),
            new Sample("GLES 1.0 Maxwell Triangle Sample", GLES1_Maxwell_Triangle_Activity.class),
            new Sample("GLES 1.0 Projection Sample", GLES1_Projection_Activity.class),
            new Sample("GLES 1.0 Push/Pop Sample", GLES1_PushPop_Activity.class),
            new Sample("GLES 1.0 Transform Sample", GLES1_Transform_Activity.class),
            new Sample("GLES 1.0 Depth Test Sample", GLES1_DepthTest_Activity.class),
            new Sample("GLES 1.0 Blending Sample", GLES1_Blending_Activity.class),
            new Sample("GLES 1.0 Texture Sample", GLES1_Texture_Activity.class),
            new Sample("GLES 2.0 Draw Maxwell Triangle", GLES2_Maxwell_Triangle_Activity.class),
            new Sample("GLES 2.0 Vertex Buffer Sample", GLES2_Vertex_Buffer_Activity.class),
            new Sample("GLES 2.0 Use GLBuffer Sample", GLES2_Use_GLBuffer_Activity.class),
            new Sample("GLES 2.0 Ball Sample", GLES2_Ball_Activity.class),
            new Sample("GLES 2.0 Block Ball Sample", GLES2_BlockBall_Activity.class),
            new Sample("GLES 2.0 Draw Texture Sample", GLES2_Texture_Activity.class),
            new Sample("GLES 2.0 Draw Transparent Texture", GLES2_Transparent_Texture_Activity.class),
            new Sample("GLES 2.0 Draw Multiple Texture", GLES2_Multiple_Texture_Activity.class),
            new Sample("GLES 2.0 Firework Sample", GLES2_Firework_Activity.class),
            new Sample("GLES 2.0 Pixelated Picture Sample", GLES2_Pixelated_Picture_Activity.class),
            new Sample("GLES 2.0 Lighting Effect Sample", GLES2_Light_Effect_Activity.class),
            new Sample("GLES 2.0 Load Obj Samples", LoadObjActivity.class),
            new Sample("GLES 2.0 Portal Sample", GLES2_Portal_Activity.class),
            new Sample("GLES 2.0 Mirror Sample", GLES2_Mirror_Activity.class),
            new Sample("GLES 2.0 Shadow Samples", ShadowActivity.class),
            new Sample("GLES 2.0 Image Processing", GLES2_Image_Processing_Activity.class),
            new Sample("GLES 2.0 Post-Processing Sample", GLES2_Post_Processing_Activity.class),
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
