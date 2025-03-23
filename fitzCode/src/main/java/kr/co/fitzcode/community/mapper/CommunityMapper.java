package kr.co.fitzcode.community.mapper;

import kr.co.fitzcode.common.dto.PostDTO;
import kr.co.fitzcode.common.dto.PostImageDTO;
import kr.co.fitzcode.common.dto.ProductTag;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Mapper
public interface CommunityMapper {
    List<ProductTag> searchProductsByName(String productName);
    void insertPost(PostDTO postDTO);
    void insertPostImage(PostImageDTO postImageDTO);
    void insertPostTags(Map<String, Object> params);
    //    PostDTO getPostDetail(int postId);
    Map<String, Object> getPostDetail(int postId);
    List<ProductTag> getProductTagsByPostId(int postId);
    List<Map<String, Object>>

    getOtherStylesByUserId(Map<String, Object> params);
    List<PostImageDTO> getPostImagesByPostId(int postId);
    List<Map<String, Object>> getAllPosts(String styleCategory);
    PostDTO getPostById(int id);
    void updatePost(PostDTO postDTO, List<Long> productIdList, List<MultipartFile> images) throws IOException;
    List<PostDTO> findByStyleCategory(String styleCategory);
    void deletePost(int postId);
}

