package com.lzx.bloomfilter;

import java.util.BitSet;

/**
 * 自定义简易布隆过滤器
 */
public class SimpleBloomFilter {

    private static final int DEFAULT_SIZE = 2<<24;
    private static final int[] seeds = new int[]{7, 11, 13, 31, 37, 61};

    private BitSet bits = new BitSet(DEFAULT_SIZE);
    private SimpleHash[] func = new SimpleHash[seeds.length];

    public SimpleBloomFilter() {
        //创建多个hash函数
        for (int i = 0; i < seeds.length; i++) {
            func[i] = new SimpleHash(DEFAULT_SIZE, seeds[i]);
        }
    }

    /**
     * 添加元素到布隆过滤器中
     *
     * @param value
     */
    public void put(String value) {
        for (SimpleHash f : func) {
            bits.set(f.hash(value), true);
        }
    }

    /**
     * 判断布隆过滤器中是否包含指定元素
     */
    public boolean mightContail(String value) {
        if (value == null) {
            return false;
        }
        boolean ret = true;
        for (SimpleHash f : func) {
            ret = ret && bits.get(f.hash(value));
        }
        return ret;
    }

    static class SimpleHash {
        private int cap;
        private int seed;

        public SimpleHash(int cap, int seed) {
            this.cap = cap;
            this.seed = seed;
        }

        public int hash(String value) {
            int result = 0;
            int len = value.length();
            for (int i = 0; i < len; i++) {
                result = seed * result + value.charAt(i);
            }
            return (cap - 1) & result;
        }
    }

    public static void main(String[] args) {
        SimpleBloomFilter sbf = new SimpleBloomFilter();
        for (int i = 0; i < 1_000_000; i++) {
            sbf.put("" + i);
        }
        int count = 0;
        //判断是否在布隆过滤器中
        for (int i = 0; i < 1_000_000 + 10000; i++) {
            if (sbf.mightContail("" + i)) {
                count++;
            }
        }
        System.out.println("已匹配数量:" + count);
    }
}

