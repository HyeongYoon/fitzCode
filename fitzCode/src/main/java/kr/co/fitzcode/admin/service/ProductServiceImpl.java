package kr.co.fitzcode.admin.service;

import kr.co.fitzcode.admin.mapper.AdminProductMapper;
import kr.co.fitzcode.common.dto.ProductCategoryDTO;
import kr.co.fitzcode.common.dto.ProductDTO;
import kr.co.fitzcode.common.dto.ProductSizeDTO;
import kr.co.fitzcode.common.enums.ProductSize;
import kr.co.fitzcode.common.enums.ProductStatus;
import kr.co.fitzcode.common.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final AdminProductMapper productMapper;
    private final S3Service s3Service;

    // 상품 추가
    @Override
    @Transactional
    public void addProduct(ProductDTO productDTO, MultipartFile mainImageFile, List<MultipartFile> additionalImageFiles) {
        // 메인 이미지 업로드
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            String mainImageUrl = s3Service.uploadFile(mainImageFile, "product-images/main");
            productDTO.setImageUrl(mainImageUrl);
        }

        // 추가 이미지 업로드
        if (additionalImageFiles != null && !additionalImageFiles.isEmpty()) {
            List<String> additionalImageUrls = s3Service.uploadFiles(additionalImageFiles, "product-images/additional");
            productDTO.setAdditionalImages(additionalImageUrls);
        }

        // 상품 기본 정보 인서트
        productMapper.insertProduct(productDTO);
        Long productId = productDTO.getProductId(); // 인서트 후 생성된 ID 가져오기

        // 추가 이미지 삽입
        if (productDTO.getAdditionalImages() != null && !productDTO.getAdditionalImages().isEmpty()) {
            int imageOrder = 1;
            for (String imageUrl : productDTO.getAdditionalImages()) {
                productMapper.insertProductImage(productId, imageUrl, imageOrder++);
            }
        }

        // 사이즈별 재고 넣기
        if (productDTO.getProductSizes() != null && !productDTO.getProductSizes().isEmpty()) {
            for (ProductSizeDTO size : productDTO.getProductSizes()) {
                productMapper.insertProductSize(productId, size.getSizeCode(), size.getStock());
            }
        }
    }

    // 전체 상품 조회 (페이지네이션, 정렬, 검색 적용)
    @Override
    public List<ProductDTO> getAllProducts(int page, int pageSize, String sort, String keyword) {
        int offset = (page - 1) * pageSize;
        return productMapper.getAllProducts(offset, pageSize, sort, keyword);
    }

    // 특정 카테고리 상품 조회 (페이지네이션, 정렬, 검색 적용)
    @Override
    public List<ProductDTO> getProductsByCategory(Long categoryId, int page, int pageSize, String sort, String keyword) {
        int offset = (page - 1) * pageSize;
        return productMapper.getProductsByCategory(categoryId, offset, pageSize, sort, keyword);
    }

    // 특정 상위 카테고리의 모든 하위 카테고리 상품 조회 (페이지네이션, 정렬, 검색 적용)
    @Override
    public List<ProductDTO> getProductsByParentCategory(Long parentCategoryId, int page, int pageSize, String sort, String keyword) {
        int offset = (page - 1) * pageSize;
        return productMapper.getProductsByParentCategory(parentCategoryId, offset, pageSize, sort, keyword);
    }

    // 전체 상품 개수 조회 (검색 적용)
    @Override
    public int countAllProducts(String keyword) {
        return productMapper.countAllProducts(keyword);
    }

    // 특정 카테고리 상품 개수 조회 (검색 적용)
    @Override
    public int countProductsByCategory(Long categoryId, String keyword) {
        return productMapper.countProductsByCategory(categoryId, keyword);
    }

    // 특정 상위 카테고리 상품 개수 조회 (검색 적용)
    @Override
    public int countProductsByParentCategory(Long parentCategoryId, String keyword) {
        return productMapper.countProductsByParentCategory(parentCategoryId, keyword);
    }

    // 상위 카테고리 목록 조회
    @Override
    public List<ProductCategoryDTO> getParentCategories() {
        return productMapper.getParentCategories();
    }

    // 특정 상위 카테고리의 하위 카테고리 목록 조회
    @Override
    public List<ProductCategoryDTO> getChildCategories(Long parentId) {
        return productMapper.getChildCategories(parentId);
    }

    // 상위 카테고리 -> 사이즈 조회
    @Override
    public List<Map<String, Object>> getSizesByParentCategory(Long parentId) {
        if (parentId == 1) { // 신발
            return Arrays.stream(ProductSize.values())
                    .filter(size -> size.getCode() <= 9)
                    .map(size -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", size.getCode());
                        map.put("description", size.getDescription());
                        return map;
                    })
                    .collect(Collectors.toList());
        } else if (parentId == 2 || parentId == 3) { // 상의 또는 하의
            return Arrays.stream(ProductSize.values())
                    .filter(size -> size.getCode() >= 10)
                    .map(size -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", size.getCode());
                        map.put("description", size.getDescription());
                        return map;
                    })
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    // 엑셀 업로드
    @Override
    @Transactional
    public void uploadExcel(MultipartFile excelFile, List<MultipartFile> imageFiles) throws Exception {
        List<ProductDTO> products = new ArrayList<>();

        try (InputStream is = excelFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 모든 필수 필드가 비어 있으면 빈 행으로 간주하고 무시
                String productName = getCellValue(row.getCell(0));
                double price = getNumericCellValue(row.getCell(3));
                String mainImageUrl = getCellValue(row.getCell(6));
                String statusStr = getCellValue(row.getCell(7));

                if (productName.isEmpty() && price <= 0 && mainImageUrl.isEmpty() && statusStr.isEmpty()) {
                    continue; // 빈 행 무시
                }

                ProductDTO product = new ProductDTO();

                // 상품명 (필수)
                if (productName.isEmpty()) throw new IllegalArgumentException("상품명 누락 (행 " + (i + 1) + ")");
                product.setProductName(productName);

                // 설명 (선택)
                product.setDescription(getCellValue(row.getCell(1)));

                // 브랜드 (선택)
                product.setBrand(getCellValue(row.getCell(2)));

                // 가격 (필수)
                if (price <= 0) throw new IllegalArgumentException("유효하지 않은 가격 (행 " + (i + 1) + ")");
                product.setPrice((int) price);

                // 할인가 (선택)
                double discountedPrice = getNumericCellValue(row.getCell(4));
                product.setDiscountedPrice(discountedPrice > 0 ? (int) discountedPrice : null);

                // 카테고리 ID (선택)
                long categoryId = (long) getNumericCellValue(row.getCell(5));
                if (categoryId > 0) {
                    if (!isValidCategory(categoryId)) throw new IllegalArgumentException("유효하지 않은 카테고리 ID: " + categoryId + " (행 " + (i + 1) + ")");
                    product.setCategoryId(categoryId);
                }

                // 메인 이미지 (필수) - URL로 직접 입력
                if (mainImageUrl.isEmpty()) throw new IllegalArgumentException("메인 이미지 URL 누락 (행 " + (i + 1) + ")");
                product.setImageUrl(mainImageUrl);

                // Status (필수) - 숫자 또는 문자열 처리
                if (statusStr.isEmpty()) throw new IllegalArgumentException("상태 누락 (행 " + (i + 1) + ")");
                ProductStatus status;
                if (statusStr.matches("\\d+")) {
                    int statusCode = Integer.parseInt(statusStr);
                    status = ProductStatus.fromCode(statusCode);
                    if (status == null) throw new IllegalArgumentException("유효하지 않은 상태 코드: " + statusStr + " (행 " + (i + 1) + ")");
                } else {
                    try {
                        status = ProductStatus.valueOf(statusStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("유효하지 않은 상태: " + statusStr + " (행 " + (i + 1) + ")");
                    }
                }
                product.setStatus(status);

                // 추가 이미지 (선택) - URL로 직접 입력
                List<String> additionalImages = new ArrayList<>();
                int sizeStartIndex = 8; // I열 (추가 이미지 URL 1 시작)
                while (sizeStartIndex < row.getLastCellNum() && row.getCell(sizeStartIndex) != null && !isNumericCell(row.getCell(sizeStartIndex))) {
                    String additionalImageUrl = getCellValue(row.getCell(sizeStartIndex));
                    if (!additionalImageUrl.isEmpty()) {
                        additionalImages.add(additionalImageUrl);
                    }
                    sizeStartIndex++;
                }
                product.setAdditionalImages(additionalImages);

                // 사이즈별 재고 (선택) - M열(13)부터 고정 시작
                List<ProductSizeDTO> sizes = new ArrayList<>();
                int sizeStartColumn = 12; // M열 (13)에서 고정 시작 (0-based index)
                while (sizeStartColumn < row.getLastCellNum() - 1 && row.getCell(sizeStartColumn) != null) {
                    int sizeCode = (int) getNumericCellValue(row.getCell(sizeStartColumn));
                    int stock = (int) getNumericCellValue(row.getCell(sizeStartColumn + 1));
                    if (sizeCode > 0 && stock >= 0) {
                        ProductSizeDTO size = new ProductSizeDTO();
                        size.setSizeCode(sizeCode);
                        size.setStock(stock);
                        sizes.add(size);
                    }
                    sizeStartColumn += 2;
                }
                product.setProductSizes(sizes);

                products.add(product);
            }

            // DB 저장
            for (ProductDTO product : products) {
                productMapper.insertProduct(product);
                Long productId = product.getProductId();
                if (product.getAdditionalImages() != null && !product.getAdditionalImages().isEmpty()) {
                    int imageOrder = 1;
                    for (String imageUrl : product.getAdditionalImages()) {
                        productMapper.insertProductImage(productId, imageUrl, imageOrder++);
                    }
                }
                for (ProductSizeDTO size : product.getProductSizes()) {
                    productMapper.insertProductSize(productId, size.getSizeCode(), size.getStock());
                }
            }
        }
    }

    // 카테고리 유효성
    private boolean isValidCategory(Long categoryId) {
        return productMapper    .countCategoryById(categoryId) > 0;
    }

    // 셀 값 가져오기 (문자열)
    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> "";
        };
    }

    // 셀 값 가져오기 (숫자)
    private double getNumericCellValue(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        return 0;
    }

    // 셀이 숫자인지
    private boolean isNumericCell(Cell cell) {
        return cell != null && cell.getCellType() == CellType.NUMERIC;
    }
}