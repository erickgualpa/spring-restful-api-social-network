package com.egm.rest.webservices.restfulwebservices.user;

import java.net.URI;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
 
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class UserJPAResource {

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PostRepository postRepository;
	
	// GET /users
	@GetMapping("/jpa/users")
	public List<User> retrieveAllUsers() {
		return userRepository.findAll();
		
	}
	
	// GET /users/{id}
	@GetMapping("/jpa/users/{id}")
	public EntityModel<User> retrieveUser(@PathVariable int id) {
		Optional<User> user = userRepository.findById(id);
		
		if(!user.isPresent()) {
			throw new UserNotFoundException("id-"+ id);
		}
		
		//HATEOAS RetrieveAllUsers
		EntityModel<User> resource = EntityModel.of(user.get());
		WebMvcLinkBuilder linkTo = 
				linkTo(methodOn(this.getClass()).retrieveAllUsers());
		
		resource.add(linkTo.withRel("all-users"));
		
		
		return resource;
	}
	
	
	// CREATE
	// input - details of user
	// output - CREATED & Return the created URI
	@PostMapping("/jpa/users")
	public ResponseEntity<Object> createUser(@Valid @RequestBody User user) {
		User savedUser = userRepository.save(user);
		
		// /user/{id} savedUser.getId()
		URI location = ServletUriComponentsBuilder
			.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(savedUser.getId()).toUri();
		
		return ResponseEntity.created(location).build();
		
	}
	
	// DELETE /users/{id}
	@DeleteMapping("/jpa/users/{id}")
	public void deleteUser(@PathVariable int id) {
		userRepository.deleteById(id);
	}
	
	// GET Sample i18n message
	@GetMapping(path = "/jpa/hello-world-i18n")
	public String helloWorldi18n() {
		return messageSource.getMessage("good.morning.message", null, LocaleContextHolder.getLocale());
	}
	
	// GET /jpa/users/{id}/posts
	@GetMapping("/jpa/users/{id}/posts")
	public List<Post> retrieveAllPostsForUser(@PathVariable int id) {
		Optional<User> userOptional =  userRepository.findById(id);
		if (!userOptional.isPresent()) {
			throw new UserNotFoundException("id-"+ id);
		}
		return userOptional.get().getPosts();		
	}
	
	// CREATE
	// input - details of user
	// output - CREATED & Return the created URI
	@PostMapping("/jpa/users/{id}/posts")
	public ResponseEntity<Object> createPost(@PathVariable int id, @RequestBody Post post) {		
		Optional<User> userOptional =  userRepository.findById(id);
		if (!userOptional.isPresent()) {
			throw new UserNotFoundException("id-"+ id);
		}
		
		User user = userOptional.get();
		
		post.setUser(user);
		
		postRepository.save(post);
		
		// /user/{id} savedUser.getId()
		URI location = ServletUriComponentsBuilder
			.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(post.getId()).toUri();
		
		return ResponseEntity.created(location).build();
		
	}

}
