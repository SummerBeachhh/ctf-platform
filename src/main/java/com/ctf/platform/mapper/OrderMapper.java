package com.ctf.platform.mapper;

import com.ctf.platform.entity.Order;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders (user_id, amount, status, trade_no, create_time) VALUES (#{userId}, #{amount}, #{status}, #{tradeNo}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Order order);

    @Select("SELECT * FROM orders WHERE id = #{id}")
    Order findById(Integer id);

    @Select("SELECT * FROM orders WHERE trade_no = #{tradeNo}")
    Order findByTradeNo(String tradeNo);

    @Update("UPDATE orders SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Integer id, @Param("status") String status);
}
