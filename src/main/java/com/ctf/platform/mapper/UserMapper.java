package com.ctf.platform.mapper;

import com.ctf.platform.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users ORDER BY score DESC")
    List<User> getRankings();

    @Select("SELECT * FROM users")
    List<User> findAll();

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Integer id);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO users (username, password, role, score, is_vip) VALUES (#{username}, #{password}, #{role}, 0, false)")
    void insert(User user);

    @Update("UPDATE users SET is_vip = true WHERE id = #{id}")
    void upgradeToVip(Integer id);

    @Update("UPDATE users SET score = score + #{points} WHERE id = #{id}")
    void addScore(Integer id, Integer points);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(Integer id);
}
