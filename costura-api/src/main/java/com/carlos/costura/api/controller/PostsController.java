package com.carlos.costura.api.controller;

import com.carlos.costura.domain.exception.PageNotFoundException;
import com.carlos.costura.domain.model.Comment;
import com.carlos.costura.domain.model.Post;
import com.carlos.costura.domain.model.Purchase;
import com.carlos.costura.domain.model.User;
import com.carlos.costura.domain.model.dto.CommentForm;
import com.carlos.costura.domain.model.dto.CommentOutput;
import com.carlos.costura.domain.model.dto.PostForm;
import com.carlos.costura.domain.model.dto.PostOutput;
import com.carlos.costura.domain.repository.PostRepository;
import com.carlos.costura.domain.repository.UserRepository;
import com.carlos.costura.domain.service.PostService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/posts")
public class PostsController {

    private PostService postService;

    private PostRepository postRepository;

    private UserRepository userRepository;


    @GetMapping
    public ResponseEntity<List<PostOutput>> getAllPosts(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user = userRepository.findById(user.getId()).orElseThrow(() -> new PageNotFoundException("Página não encontrada"));
        List<Post> posts = postRepository.findAllByUserFollowed(user);
        return ResponseEntity.ok(posts.stream().map(PostOutput::toOutput).collect(Collectors.toList()));
    }


    @GetMapping("/{id}")
    public ResponseEntity<PostOutput> getPost(@PathVariable Long id){
        Post post = postRepository.findById(id).orElseThrow(() -> new PageNotFoundException("Página não encontrada."));
        return ResponseEntity.ok(PostOutput.toOutput(post));
    }

    @PostMapping
    public ResponseEntity<PostOutput> addPost(@RequestParam(name = "file",required = false) MultipartFile imageFile,
                                              @Valid @RequestPart("post") PostForm postForm, UriComponentsBuilder uriComponentsBuilder){
        Post savedPost = postService.save(postForm,imageFile);
        UriComponents uriComponents = uriComponentsBuilder.path("/posts/{id}").buildAndExpand(savedPost.getId());
        var location = uriComponents.toUri();
        return ResponseEntity.created(location).body(PostOutput.toOutput(savedPost));
    }

    @PostMapping("/picture")
    public ResponseEntity<Void> uploadPicture(@RequestParam(name = "file") MultipartFile file){

        URI uri = postService.uploadPostPicture(file);

        return ResponseEntity.created(uri).build();

    }


    @PostMapping("/{postId}/comment")
    public ResponseEntity<CommentOutput> addComment(@PathVariable Long postId,
                                                    @RequestBody CommentForm commentForm, UriComponentsBuilder uriComponentsBuilder){
        Comment savedComment = postService.addComment(commentForm,postId);
        UriComponents uriComponents = uriComponentsBuilder.path("/posts/{postId}/comment/{id}").buildAndExpand(savedComment.getPost().getId(),savedComment.getId());
        var location = uriComponents.toUri();
        return ResponseEntity.created(location).body(CommentOutput.toOutput(savedComment));
    }

    @PostMapping("/{postId}/like")
    public void addLike(@PathVariable Long postId){
        postService.addLike(postId);
    }

    @PostMapping("/{postId}/buy")
    public ResponseEntity<Purchase> buyPost(@PathVariable Long postId){
        Purchase purchase = postService.buy(postId);

        return null;
    }
}
