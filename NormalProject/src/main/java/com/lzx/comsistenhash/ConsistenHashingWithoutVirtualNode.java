package com.lzx.comsistenhash;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 不带虚拟节点的一致性hash算法
 * 重点 1.如何造一个hash环 ，2.如何在哈希环上映射服务器节点，3.如何找到对应节点
 */
public class ConsistenHashingWithoutVirtualNode {
    /**
     * 待添加入hash环的服务器列表
     */
    private static String[] servers={"192.168.0.0:111", "192.168.0.1:111",
            "192.168.0.2:111", "192.168.0.3:111", "192.168.0.4:111"};
    /**
     * key标识服务器的hash值，value表示服务器
     */
    private static SortedMap<Integer,String> sortedMap = new TreeMap<Integer, String>();

    /**
     * 程序初始化，将所有服务器加入到sortedMap中
     */
    static {
        for (int i =0 ;i<servers.length ;i++){
            int hash =getHash(servers[i]);
            System.out.println("["+servers[i]+"]加入集合中，其hash值为"+hash);
            sortedMap.put(hash,servers[i]);
        }
        System.out.println();
    }

    /**
     * 得到当前路由到的节点
     * @param key
     * @return
     */
    private static String getServer(String key){
        //得到该key的hash值
        int hash = getHash(key);
        //得到大于该hash值的所有map
        SortedMap<Integer ,String> subMap = sortedMap.tailMap(hash);
        if(subMap.isEmpty()){
            //如果没有比该key的hash值大的，则从第一个node开始
            Integer i = sortedMap.firstKey();
            //返回对应的服务器
            return sortedMap.get(i);
        }else {
            //第一个key就是顺时针过去距离node最近的那个节点
            Integer i = subMap.firstKey();
            return subMap.get(i);
        }
    }

    /**
     *  32位的 Fowler-Noll-Vo 哈希算法
     *      https://en.wikipedia.org/wiki/Fowler–Noll–Vo_hash_function
     * @param str
     * @return
     */
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }

    public static void main(String[] args) {
        String[] keys ={"太阳","月亮" ,"星星"};
        for (int i = 0; i <keys.length ; i++) {
            System.out.println("["+keys[i]+"] 的hash值为:"+getHash(keys[i]) +" ,被路由到节点["+getServer(keys[i])+"]");
        }
    }
}
