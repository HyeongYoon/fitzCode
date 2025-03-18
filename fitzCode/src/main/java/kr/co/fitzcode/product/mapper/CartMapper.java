package kr.co.fitzcode.product.mapper;

import kr.co.fitzcode.common.dto.CartDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {
    List<CartDTO> getCartProductsByUserId(int userId);
    void addProductToCart(CartDTO cartDTO);
    int getCartCount(@Param("userId") int userId);
}
