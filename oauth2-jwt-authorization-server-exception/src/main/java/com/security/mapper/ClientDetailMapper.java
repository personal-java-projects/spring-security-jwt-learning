package com.security.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClientDetailMapper {

    String insertClientDetail(ClientDetail clientDetail);

    ClientDetail selectClientDetailByClientId(@Param("clientId") String clientId);
}
