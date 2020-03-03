package com.youzan.mobile.enjoyplugin;

import java.util.*;

/**
 * 最近缓存
 *
 * @param <K>
 * @param <V>
 */
public class EnjoyLRU<K, V> {

    private LinkedHashMap<K, V> cacheHashMap;
    private int cacheSize;
    private static volatile EnjoyLRU instance;

    public static <K, V> EnjoyLRU<K, V> getInstance() {
        if (instance == null) {
            synchronized (EnjoyLRU.class) {
                if (instance == null) {
                    instance = new EnjoyLRU<K, V>(5);
                }
            }
        }

        return instance;
    }


    private EnjoyLRU() {

    }

    private EnjoyLRU(int size) {
        this.cacheSize = size;
        cacheHashMap = new LinkedHashMap<K, V>(16, 0.75F, false) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return cacheSize + 1 == cacheHashMap.size();
            }
        };
    }

    public synchronized V get(K key) {
        return cacheHashMap.get(key);
    }

    public synchronized void put(K key, V value) {
        cacheHashMap.put(key, value);
    }

    public synchronized void clear() {
        cacheHashMap.clear();
    }

    public synchronized int usedSize() {
        return cacheHashMap.size();
    }

    public synchronized Object loadCacheValueList() {
//        List<V> temp = new ArrayList<>();
        return cacheHashMap.values();
//        for (K k : cacheHashMap.keySet()) {
//            temp.add(cacheHashMap.get(k));
//        }
//        return temp;
    }

    public Set<Map.Entry<K,V>> loadEntrySet(){
        return cacheHashMap.entrySet();
    }

}
