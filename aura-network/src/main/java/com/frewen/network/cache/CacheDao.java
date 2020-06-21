package com.frewen.network.cache;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * @filename: CacheDao
 * @introduction:
 * @author: Frewen.Wong
 * @time: 2020/6/21 00:36
 * @version: 1.0.0
 * @copyright: Copyright ©2020 Frewen.Wong. All Rights Reserved.
 */
@Dao
public interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long save(CacheBean cacheBean);

    /**
     * 注意，冒号后面必须紧跟参数名，中间不能有空格。大于小于号和冒号中间是有空格的。
     * select *from cache where【表中列名】 =:【参数名】------>等于
     * where 【表中列名】 < :【参数名】 小于
     * where 【表中列名】 between :【参数名1】 and :【参数2】------->这个区间
     * where 【表中列名】like :参数名----->模糊查询
     * where 【表中列名】 in (:【参数名集合】)---->查询符合集合内指定字段值的记录
     * 如果是一对多,这里可以写List<Cache>
     *
     * @param key
     *
     * @return CacheBean
     */
    @Query("select *from cache where `key` = :key")
    CacheBean getCache(String key);

    /**
     * 只能传递对象昂,删除时根据Cache中的主键 来比对的
     *
     * @param cacheBean
     */
    @Delete
    int delete(CacheBean cacheBean);

    /**
     * 只能传递对象昂,更新时根据Cache中的主键 来比对的
     *
     * @param cacheBean
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(CacheBean cacheBean);

}
