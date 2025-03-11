package kr.co.fitzcode.order.service;

import kr.co.fitzcode.common.dto.AddressDTO;
import kr.co.fitzcode.common.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    List<AddressDTO> getUserAddress(int userId);

    boolean checkIfAddressExistsForUser(int userId, String addressLine1, String postalCode);

    void addNonDefaultAddressForUser(int userId, AddressDTO addressDTO);

    int getAddressIdUsingAddressLine1AndPostalCode(String addressLine1, String postalCode, int userId);

    int insertNewOrder(OrderDTO orderDTO);
}
