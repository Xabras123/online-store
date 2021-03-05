package co.com.aruma.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import co.com.aruma.entity.Bill;



public interface BillRepository extends ReactiveCrudRepository<Bill, String>{

	//Mono<Purchanse> findByIdAndState(String id, int state);
	//@Query(value = "{email : ?0}")
	//Mono<Purchanse> findByEmail(String userEmail);


}
