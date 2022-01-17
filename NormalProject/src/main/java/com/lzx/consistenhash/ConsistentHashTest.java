package com.lzx.consistenhash;


import lombok.Data;
import org.junit.Test;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
@Data
abstract class DistributeHash {
    /**
     * hash<->物理节点
     */
    private TreeMap<Long, String> virtualNodes = new TreeMap<Long, String>();
    /**
     * 物理节点至虚拟节点的复制倍数
     */
    private int virtualCopies = 64;
    /**
     * key 机器 ，value执行的的次数
     */
    private Map<String, Integer> objectNodeMap = new TreeMap<>();

    public void addPhysicalNode(String key) {
        for (int index = 0; index < virtualCopies; index++) {
            long hash = hash(key + "#" + Integer.toString(index));
            virtualNodes.put(hash, key);
        }
    }

    /**
     * 删除物理节点
     *
     * @param key
     */
    public void removePhysicalNode(String key) {
        for (int index = 0; index < virtualCopies; index++) {
            long hash = hash(key + "#" + Integer.toString(index));
            virtualNodes.remove(hash);
        }
    }

    /**
     * 具体请求落在那一台机器上
     *
     * @return
     */
    public String getObjectNode(String object) {
        long hash = hash(object);
        SortedMap<Long, String> tailMap = virtualNodes.tailMap(hash);
        Long key = tailMap.isEmpty() ? virtualNodes.firstKey() : tailMap.firstKey();
        return virtualNodes.get(key);
    }

    /**
     * 对象与节点映射(类比于某一具体的请求转发到那一台服务器)
     *
     * @return
     */

    public void objectNodeMap(String label, int objectMin, int objectMax) {
        // 执行 (objectMax-objectMin+1)次请求
        for (int object = objectMin; object <= objectMax; object++) {
            String nodeIP = getObjectNode(Integer.toString(object));
            // 该机器执行了多少次
            Integer count = objectNodeMap.get(nodeIP);
            //记录机器执行的请求数
            objectNodeMap.put(nodeIP, (count == null ? 0 : count + 1));
        }
        //打印
        double totalCount = objectMax - objectMin + 1;
        System.out.println("=======" + label + "=======");
        objectNodeMap.entrySet().stream().forEach(item -> {
            long percent = (int) (100 * item.getValue() / totalCount);
            System.out.println("IP=" + item.getKey() + ": RATE=" + percent + "%");
        });
    }



    public abstract Long hash(String key);
}

class ConsistentHashWithVirtualNode extends DistributeHash {

    // 32位的 Fowler-Noll-Vo 哈希算法
    // https://en.wikipedia.org/wiki/Fowler–Noll–Vo_hash_function
    @Override
    public Long hash(String key) {
        final int p = 16777619;
        Long hash = 2166136261L;

        for (int ind = 0, num = key.length(); ind < num; ind++) {
            hash = (hash ^ key.charAt(ind)) * p;
        }

        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }
}

public class ConsistentHashTest {
    /**
     * 标准方差
     */
    public double getstd(Map<String, Integer> objectnodes) {
        double sum = 0;
        int cnt = 0;
        for (Map.Entry<String, Integer> entry : objectnodes.entrySet()) {
            sum += entry.getValue();
            cnt++;
        }

        double average = sum / cnt;

        int total = 0;
        for (Map.Entry<String, Integer> entry : objectnodes.entrySet()) {
            total += (entry.getValue() - average) * (entry.getValue() - average);
        }

        double standardDeviation = Math.sqrt(total / cnt);

        return standardDeviation;
    }

    @Test
    public void test() {
        Map<String, Integer> objectNodes = new TreeMap<>();
        DistributeHash ch = new ConsistentHashWithVirtualNode();
        ch.setVirtualCopies(2048);
        ch.addPhysicalNode("192.168.100.101");
        ch.addPhysicalNode("192.168.100.102");
        ch.addPhysicalNode("192.168.100.103");
        ch.addPhysicalNode("192.168.100.104");
        ch.addPhysicalNode("192.168.100.105");
        ch.addPhysicalNode("192.168.100.106");
        ch.addPhysicalNode("192.168.100.107");
        ch.addPhysicalNode("192.168.100.108");
        ch.addPhysicalNode("192.168.100.109");
        ch.addPhysicalNode("192.168.100.110");
        /**
         * 对象与节点映射
         */
        ch.objectNodeMap("10台服务器，单台" + ch.getVirtualCopies() + "个虚拟节点", 1, 1000_000);
        objectNodes = ch.getObjectNodeMap();
        System.out.println("标准方差为:" + getstd(objectNodes));
    }
}