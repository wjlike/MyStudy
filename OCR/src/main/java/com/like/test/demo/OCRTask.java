package com.like.test.demo;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import com.baidu.aip.ocr.AipOcr;

public class OCRTask implements Runnable {


    private AipOcr client;
    private String id;
    private String idCardPath;
    private HashMap<String, String> options = new HashMap<>();
    private static final String dir = "/Users/like/Desktop/work/";


    public OCRTask(String path,String id,AipOcr client){
        this.idCardPath = path;
        this.client = client;
        this.id = id;
        options.put("detect_direction", "true");
        options.put("detect_risk", "false");
    }

    @Override
    public void run() {
        // 识别身份证正面（正面图片为本地图片，即：C:\Users\yy150\Pictures\正.jpg）
        JSONObject frontres = client.idcard(idCardPath, "front", options);

        boolean isRight = "normal".equals(frontres.get("image_status"));
        String sql = "";
        if(isRight){
            sql = "update agy_la_approvalinfo_fl set file_type = 'idback' where edor_no = "+id+";";
        }else {
            sql = "update agy_la_approvalinfo_fl set file_type = 'idright' where edor_no = "+id+";";
        }
        new File(idCardPath).deleteOnExit();
        System.out.println(sql);
        try {
            FileUtils.writeLines(new File(dir+"test.sql"), Collections.singleton(sql));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
