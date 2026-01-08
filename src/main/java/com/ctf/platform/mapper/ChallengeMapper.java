package com.ctf.platform.mapper;

import com.ctf.platform.entity.Category;
import com.ctf.platform.entity.Challenge;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface ChallengeMapper {
    List<Challenge> findAll();
    int countAll();
    List<Challenge> findPage(@Param("offset") int offset, @Param("limit") int limit);
    int countByCategoryId(Integer categoryId);
    List<Challenge> findPageByCategoryId(@Param("categoryId") Integer categoryId, @Param("offset") int offset, @Param("limit") int limit);
    List<Category> findAllCategories();
    Challenge findById(Integer id);
    List<Challenge> findByCategoryId(Integer categoryId);

    @Insert("INSERT INTO challenge (title, description, category_id, points, flag, attachment_url, sort_order, is_vip) VALUES (#{title}, #{description}, #{categoryId}, #{points}, #{flag}, #{attachmentUrl}, #{sortOrder}, #{isVip})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Challenge challenge);

    @Update("UPDATE challenge SET sort_order = #{sortOrder} WHERE id = #{id}")
    void updateSortOrder(Integer id, Integer sortOrder);

    @Update("UPDATE challenge SET title = #{title}, description = #{description}, category_id = #{categoryId}, points = #{points}, flag = #{flag}, is_vip = #{isVip}, attachment_url = #{attachmentUrl} WHERE id = #{id}")
    void update(Challenge challenge);

    @Delete("DELETE FROM challenge WHERE id = #{id}")
    void deleteById(Integer id);
}
