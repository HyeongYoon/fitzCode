package kr.co.fitzcode.admin.service;

import kr.co.fitzcode.admin.dto.ProductDetailDTO;
import kr.co.fitzcode.admin.dto.ProductImageDTO;
import kr.co.fitzcode.admin.dto.ProductSizeDTO;
import kr.co.fitzcode.admin.mapper.ProductDetailMapper;
import kr.co.fitzcode.common.enums.ProductSize;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductDetailServiceImpl implements ProductDetailService {

    private final ProductDetailMapper productDetailMapper;

    @Override
    public ProductDetailDTO getProductDetail(Long productId) {
        ProductDetailDTO product = productDetailMapper.findProductDetailById(productId);
        if (product == null) {
            return null;
        }

        List<ProductImageDTO> images = productDetailMapper.findProductImagesById(productId);
        System.out.println("Images retrieved: " + (images != null ? images.size() : "null"));
        if (images != null) {
            for (ProductImageDTO img : images) {
                if (img.getImageUrl() == null || img.getImageUrl().isEmpty()) {
                    img.setImageUrl("/images/fallback.jpg");
                }
            }
        }
        product.setImages(images != null ? images : new ArrayList<>());

        List<ProductSizeDTO> sizes = productDetailMapper.findSizesByProductId(productId);
        if (sizes != null) {
            for (ProductSizeDTO size : sizes) {
                size.getSizeDescription();
            }
        }
        product.setSizes(sizes != null ? sizes : new ArrayList<>());

        // 카테고리별 사이즈 필터링
        Integer categoryId = product.getCategoryId();
        boolean isShoeCategory = isShoeCategory(categoryId);
        boolean isClothingCategory = isClothingCategory(categoryId);

        List<ProductSizeDTO> allSizes = getAllSizes(isShoeCategory, isClothingCategory, product.getSizes());
        product.setAllSizes(allSizes);
        product.setSizes(new ArrayList<>(allSizes)); // sizes를 allSizes로 초기화

        return product;
    }

    // 신발 카테고리 여부 확인
    private boolean isShoeCategory(Integer categoryId) {
        if (categoryId == null) return false;
        // 상위 카테고리 (category_id = 1) 및 하위 카테고리 (4, 5, 6, 10, 11, 12)
        return categoryId == 1 || categoryId == 4 || categoryId == 5 || categoryId == 6 ||
                categoryId == 10 || categoryId == 11 || categoryId == 12;
    }

    // 의류 카테고리 여부 확인
    private boolean isClothingCategory(Integer categoryId) {
        if (categoryId == null) return false;
        // 상의 (category_id = 2) 및 하위 카테고리 (7, 8, 9)
        // 하의 (category_id = 3) 및 하위 카테고리 (14, 15, 16)
        return (categoryId == 2 || categoryId == 7 || categoryId == 8 || categoryId == 9) ||
                (categoryId == 3 || categoryId == 14 || categoryId == 15 || categoryId == 16);
    }

    @Override
    public List<ProductSizeDTO> getAllSizes(boolean isShoeCategory, boolean isClothingCategory, List<ProductSizeDTO> existingSizes) {
        List<ProductSizeDTO> allSizes = new ArrayList<>();
        for (ProductSize size : ProductSize.values()) {
            // 신발 카테고리: 신발 사이즈(1~9)만 포함
            if (isShoeCategory && size.getCode() <= 9) {
                ProductSizeDTO dto = new ProductSizeDTO();
                dto.setSizeCode(size.getCode());
                // 기존 사이즈 데이터가 있으면 재고 반영
                if (existingSizes != null) {
                    for (ProductSizeDTO existing : existingSizes) {
                        if (existing.getSizeCode() != null && existing.getSizeCode().equals(size.getCode())) {
                            dto.setProductSizeId(existing.getProductSizeId());
                            dto.setStock(existing.getStock() != null ? existing.getStock() : 0);
                            break;
                        }
                    }
                }
                if (dto.getStock() == null) dto.setStock(0); // 데이터 없으면 0
                allSizes.add(dto);
            }
            // 의류 카테고리: 의류 사이즈(10~15)만 포함
            else if (isClothingCategory && size.getCode() >= 10) {
                ProductSizeDTO dto = new ProductSizeDTO();
                dto.setSizeCode(size.getCode());
                // 기존 사이즈 데이터가 있으면 재고 반영
                if (existingSizes != null) {
                    for (ProductSizeDTO existing : existingSizes) {
                        if (existing.getSizeCode() != null && existing.getSizeCode().equals(size.getCode())) {
                            dto.setProductSizeId(existing.getProductSizeId());
                            dto.setStock(existing.getStock() != null ? existing.getStock() : 0);
                            break;
                        }
                    }
                }
                if (dto.getStock() == null) dto.setStock(0); // 데이터 없으면 0
                allSizes.add(dto);
            }
        }
        System.out.println("isShoeCategory: " + isShoeCategory + ", isClothingCategory: " + isClothingCategory + ", allSizes size: " + allSizes.size());
        return allSizes;
    }

    @Override
    public void updateDiscountedPrice(Long productId, Integer discountedPrice) {
        if (discountedPrice == null) {
            discountedPrice = 0;
        }
        productDetailMapper.updateDiscountedPrice(productId, discountedPrice);
    }

    @Override
    public List<ProductSizeDTO> getSizesByProductId(Long productId) {
        return productDetailMapper.findSizesByProductId(productId);
    }

    @Override
    public void updateSizes(Long productId, List<ProductSizeDTO> sizes) {
        if (sizes != null) {
            for (ProductSizeDTO dto : sizes) {
                dto.setProductId(productId);
                System.out.println("Processing size: productSizeId=" + dto.getProductSizeId() + ", sizeCode=" + dto.getSizeCode() + ", stock=" + dto.getStock());
                if (dto.getSizeCode() == null) {
                    System.err.println("sizeCode is null for productId=" + productId + ", dto=" + dto);
                    throw new IllegalArgumentException("sizeCode cannot be null");
                }
                if (dto.getProductSizeId() == null) {
                    productDetailMapper.insertProductSize(dto);
                } else {
                    productDetailMapper.updateProductSizeStock(dto);
                }
            }
        }
    }
}