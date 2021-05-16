package com.like.study.flink.wc;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class StreamWordCount {
    public static void main(String[] args) throws Exception {
        //创建流处理执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();

        String filePath = "E:\\hello.txt";

        //从文件中读取数据
        DataStream<String> dataStream = env.readTextFile(filePath);
        //基于数据流进行计算
        DataStream<Tuple2<String, Integer>> dataStream1 = dataStream.flatMap(new WordCount.MyflatMapper())
                .keyBy(0)
                .sum(1);
        dataStream1.print();

        env.execute();
    }
}
