package co.com.aruma.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import co.com.aruma.dto.Response;
import co.com.aruma.entity.Purchanse;
import reactor.core.publisher.Mono;



public interface PurchanseRepository extends ReactiveCrudRepository<Purchanse, String>{

	Mono<Purchanse> findByIdAndActive(String purchanseId, boolean b);


	//@Query(value = "{email : ?0}")
	//Mono<Purchanse> findByEmail(String userEmail);
	



}
