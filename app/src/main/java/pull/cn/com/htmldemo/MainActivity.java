package pull.cn.com.htmldemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView list = (ListView) findViewById(R.id.listview);


        List<String> data = new ArrayList<>();
        data.add("http://7xlovk.com2.z0.glb.qiniucdn.com/upload/net/banner/wechat/3%E5%90%881/23.jpg");
        data.add("http://7xlovk.com2.z0.glb.qiniucdn.com/upload/net/banner/wechat/3%E5%90%881/33.jpg");
        data.add("http://7xlovk.com2.z0.glb.qiniucdn.com/upload/net/banner/wechat/1.jpg");
        data.add("http://7xlovk.com2.z0.glb.qiniucdn.com/upload/net/banner/wechat/2.jpg");
        data.add("http://7xlovk.com2.z0.glb.qiniucdn.com/upload/net/banner/wechat/3.jpg");
        data.add("http://7xlovk.com2.z0.glb.qiniucdn.com/upload/net/banner/wechat/4.jpg");
        data.add("http://7xlovk.com2.z0.glb.qiniucdn.com/upload/net/%E6%99%AF%E5%8C%BA%E6%96%B9%E5%9B%BE/%E4%B8%89%E4%BA%9A.png");
        data.add("http://7xlovk.com2.z0.glb.qiniucdn.com/upload/net/%E6%99%AF%E5%8C%BA%E6%96%B9%E5%9B%BE/%E6%9D%AD%E5%B7%9E.png");
        data.add("http://7xlovk.com2.z0.glb.qiniucdn.com/upload/net/banner/wechat/3%E5%90%881/23.jpg");
        data.add("http://7xlovk.com2.z0.glb.qiniucdn.com/upload/net/banner/wechat/3%E5%90%881/33.jpg");

        list.setAdapter(new ListAdapter(this,data));
    }

}
