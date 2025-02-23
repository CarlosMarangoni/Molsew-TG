package com.carlos.costura.api.controller;

import com.carlos.costura.domain.exception.ConflictException;
import com.carlos.costura.domain.exception.PageNotFoundException;
import com.carlos.costura.domain.model.*;
import com.carlos.costura.domain.model.dto.*;
import com.carlos.costura.domain.repository.CategoryRepository;
import com.carlos.costura.domain.repository.PostRepository;
import com.carlos.costura.domain.repository.UserRepository;
import com.carlos.costura.domain.service.PostService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/posts")
public class PostsController {

    private PostService postService;

    private PostRepository postRepository;

    private UserRepository userRepository;

    private CategoryRepository categoryRepository;

    @GetMapping
    public Page<PostOutput> getAllPosts(Pageable pageable){
        Page<Post> postList = postRepository.findAll(pageable);
        List<PostOutput> postListDto = postList.stream().map(PostOutput::toOutput).collect(Collectors.toList());
        int pageSize = pageable.getPageSize();
        long pageOffset = pageable.getOffset();
        long total = pageOffset + postListDto.size() + (postListDto.size() == pageSize ? pageSize : 0);
        Page<PostOutput> postPaginated = new PageImpl<PostOutput>(postListDto,pageable,total);
        return postPaginated;
    }

    @GetMapping("/categories/name/{category}")
    public Page<PostOutput> getAllPostsByCategory(@PathVariable String category,Pageable pageable){
        Category foundCategory = categoryRepository.findByName(category).orElseThrow(() -> new PageNotFoundException("Categoria não encontrada."));
        List<Post> postList = postRepository.findAllByCategory(foundCategory);
        List<PostOutput> postListDto = postList.stream().map(PostOutput::toOutput).collect(Collectors.toList());
        int pageSize = pageable.getPageSize();
        long pageOffset = pageable.getOffset();
        long total = pageOffset + postListDto.size() + (postListDto.size() == pageSize ? pageSize : 0);
        Page<PostOutput> postPaginated = new PageImpl<PostOutput>(postListDto,pageable,total);
        return postPaginated;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories(){
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<Category> getAllCategories(@PathVariable Long categoryId){
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new PageNotFoundException("Categoria não encontrada."));
        return ResponseEntity.ok(category);
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> addCategory(@RequestBody Category category,UriComponentsBuilder uriComponentsBuilder){
       if (categoryRepository.findByName(category.getName()).isPresent()){
           throw new ConflictException("Categoria já existe.");
       }
       category.setName(category.getName().toUpperCase(Locale.ROOT));
        Category savedCategory = categoryRepository.save(category);
        UriComponents uriComponents = uriComponentsBuilder.path("/categories/{id}").buildAndExpand(savedCategory.getId());
        var location = uriComponents.toUri();
        return ResponseEntity.created(location).body(savedCategory);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PostOutput> getPost(@PathVariable Long id){
        Post post = postRepository.findById(id).orElseThrow(() -> new PageNotFoundException("Página não encontrada."));
        return ResponseEntity.ok(PostOutput.toOutput(post));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<PostOutput>> getPostByUser(@PathVariable Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new PageNotFoundException("Página não encontrada."));
        List<Post> post = postRepository.findAllByUser(user);
        return ResponseEntity.ok(post.stream().map(PostOutput::toOutput).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<PostOutput> addPost(@RequestParam(name = "file",required = false) MultipartFile imageFile,
                                              @Valid @RequestPart("post") PostForm postForm,@RequestParam(name = "moldes",required = true)List<MultipartFile> moldes,UriComponentsBuilder uriComponentsBuilder) throws MessagingException, IOException {
        Post savedPost = postService.save(postForm,imageFile,moldes);
        UriComponents uriComponents = uriComponentsBuilder.path("/posts/{id}").buildAndExpand(savedPost.getId());
        var location = uriComponents.toUri();
        return ResponseEntity.created(location).body(PostOutput.toOutput(savedPost));
    }

    @PostMapping("/{postId}/item")
    public ResponseEntity<PostOutput> addItem(@RequestBody PostItemForm postItemForm, @PathVariable Long postId){
        Post post = postService.addItem(postItemForm,postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(PostOutput.toOutput(post));
    }


    @PostMapping("/{postId}/comment")
    public ResponseEntity<PostOutput> addComment(@PathVariable Long postId,
                                                    @RequestBody @Valid CommentForm commentForm, UriComponentsBuilder uriComponentsBuilder){
        Post commentedPost = postService.addComment(commentForm,postId);
        UriComponents uriComponents = uriComponentsBuilder.path("/posts/{postId}").buildAndExpand(commentedPost.getId());
        var location = uriComponents.toUri();
        return ResponseEntity.created(location).body(PostOutput.toOutput(commentedPost));
    }

//    @PostMapping("/{postId}/like")
//    public void addLike(@PathVariable Long postId){
//        postService.addLike(postId);
//    }


    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId){
        postService.delete(postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}/item/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long postId,@PathVariable Integer itemId){
        postService.deleteItem(postId,itemId);
        return ResponseEntity.noContent().build();
    }
}
