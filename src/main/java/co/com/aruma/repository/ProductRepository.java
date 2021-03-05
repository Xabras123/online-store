package co.com.aruma.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import co.com.aruma.entity.Product;
import reactor.core.publisher.Flux;



public interface ProductRepository extends ReactiveCrudRepository<Product, String>{

	Flux<Product> findByIdIn(List<String> productIds);
	//@Query(value = "{email : ?0}")
	//Mono<Product> findByEmail(String userEmail);
	@Query(value = "{email : ?0}")
	Flux<Product> updateProducts(List<Product> productsToUpdate);


}
