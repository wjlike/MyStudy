package com.like.test.demo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import com.baidu.aip.ocr.AipOcr;



public class Sample {

    // TODO APP_ID/AK/SK
    public static final String APP_ID = "23552975";
    public static final String API_KEY = "CS6wkhbrmjhiu9KUx2f6E51t";
    public static final String SECRET_KEY = "useckvAI6kfMLhBDGOjwfAVrpgSNNI5o";
    private static final String dir = "/Users/like/Desktop/work/";

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        // 初始化一个AipOcr
        AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<>();
        options.put("detect_direction", "true");
        options.put("detect_risk", "false");

        List<String> strings = FileUtils.readLines(new File("/Users/like/Desktop/test.txt"), "UTF-8");
        List<String> sqllist = new ArrayList<>();
       strings.forEach(str->{
           String[] strs = str.split("\\|");
           String url = strs[1];
           String id = strs[0];

           String filename = System.currentTimeMillis()+".jpg";

           ImgDownload.downloadHttpUrl(url,dir,filename);

           // 识别身份证正面（正面图片为本地图片，即：C:\Users\yy150\Pictures\正.jpg）
           JSONObject frontres = client.idcard(dir+filename, "front", options);

           boolean isRight = "normal".equals(frontres.get("image_status"));
           String sql = "";
           if(isRight){
                sql = "update agy_la_approvalinfo_fl set file_type = 'idback' where edor_no = "+id+";";
           }else {
                sql = "update agy_la_approvalinfo_fl set file_type = 'idright' where edor_no = "+id+";";
           }
           new File(dir+filename).deleteOnExit();
           System.out.println(sql);
           sqllist.add(sql);
       });
       FileUtils.writeLines(new File(dir+"test.sql"),sqllist);

    }


}
