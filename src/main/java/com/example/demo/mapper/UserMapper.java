package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface  UserMapper {

    //添加数据
    Long insert(User user);

    User selectById(Long id);

}
