package co.com.aruma.controller;

import java.util.ArrayList;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import co.com.aruma.dto.ProductDTO;
import co.com.aruma.dto.PurchanseDTO;
import co.com.aruma.dto.Response;
import co.com.aruma.service.OnlineStoreService;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/online-store")
public class OnlineStoreController {
	
	@Autowired 
	OnlineStoreService onlineStoreervice;
	
	@PostMapping("/purchanse-product")
	public Mono<Response> purchanseProduct(@Valid @RequestBody PurchanseDTO purchanseDTO){
		
		return this.onlineStoreervice.purchanseProduct(purchanseDTO);
	}
	
	
	@PostMapping("/edit-purchanse/{purchanse-id}")
	public Mono<Response> editPurchanse(@Valid @RequestBody ArrayList<ProductDTO> purchanseDTO, @PathVariable("purchanse-id") String purchanseId){
		
		return this.onlineStoreervice.editPurchanse(purchanseId, purchanseDTO);
	}
	
	@DeleteMapping("/delete-purchanse/{purchanse-id}")
	public Mono<Response> deletePurchanse(@PathVariable("purchanse-id") String purchanseId){
		
		return this.onlineStoreervice.deletePurchanse(purchanseId);
	}
	
    @GetMapping("/ping")
    public Mono<Response> ping( ServerWebExchange exchange) {
        return this.onlineStoreervice.ping( exchange.getRequest().getHeaders());
    }
	
	@PostMapping("/initialize-data/{amount}")
	public Mono<Response> initializeData(@PathVariable("amount") int amount){
		
		return this.onlineStoreervice.dataInitializer(amount);
	}
	


}