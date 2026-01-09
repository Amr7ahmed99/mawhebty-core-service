package io.mawhebty.controllers;

import io.mawhebty.api.v1.mawhebty.platform.AbstractMawhebtyPlatformController;
import io.mawhebty.api.v1.mawhebtyPlatform.PostsApi;
import io.mawhebty.api.v1.resources.mawhebtyPlatform.*;
import io.mawhebty.dtos.responses.*;
import io.mawhebty.exceptions.ResourceNotFoundException;
import io.mawhebty.models.User;
import io.mawhebty.services.PostService;
import io.mawhebty.services.auth.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController("PostsPlatformController")
@RequiredArgsConstructor
public class PostsController extends AbstractMawhebtyPlatformController
        implements PostsApi {

    private final PostService postService;
    private final CurrentUserService currentUserService;


    @Override
    public ResponseEntity<PaginatedListResponseResource> getPosts(
            Integer categoryId,
            Integer subCategoryId,
            BigDecimal ownerId,
            Integer typeId,
            String search,
            Integer page,
            Integer perPage,
            String sortBy
    ) {
        try {

            // Validate pagination
            if (page == null || page < 1) page = 1;
            if (perPage == null || perPage < 1) perPage = 10;
            if (perPage > 50) perPage = 50;

            // Get posts with filtering
            PaginatedListResponseResource response = postService.getPosts(
                    categoryId,
                    subCategoryId,
                    ownerId,
                    typeId,
                    search,
                    page,
                    perPage,
                    sortBy
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving posts", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<PostWithRelatedResponseResource> getPostById(Integer id) {
        try {

            // Get post with related posts
            PostWithRelatedResponseResource response = postService.getPostById(id.longValue());

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            log.error("Post not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving post", e);
            throw e;
        }
    }

    
}