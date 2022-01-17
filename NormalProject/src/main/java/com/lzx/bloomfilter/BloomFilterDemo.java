package com.lzx.bloomfilter;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

public class BloomFilterDemo {
    public static void main(String[] args) {
        //总数
        int total = 1000_000;
        BloomFilter<CharSequence> bf =BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8),total,0.0002);
        //初始化数据到过滤器中
        for (int i = 0; i <total ; i++) {
            bf.put(""+i);
        }
        // 判断是否存在过滤器中
        int count = 0 ;
        for (int i = 0; i < total+10000 ; i++) {
            if(bf.mightContain(""+i)){
                count++;
            }
        }
        System.out.println("已匹配数量 "+count);

    }
}
