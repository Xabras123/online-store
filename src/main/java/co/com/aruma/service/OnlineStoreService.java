package co.com.aruma.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.com.aruma.dto.BillDTO;
import co.com.aruma.dto.ClientDTO;
import co.com.aruma.dto.ProductDTO;
import co.com.aruma.dto.ProductDTOOut;
import co.com.aruma.dto.PurchanseDTO;
import co.com.aruma.dto.Response;
import co.com.aruma.entity.Product;
import co.com.aruma.entity.Purchanse;
import co.com.aruma.repository.BillRepository;
import co.com.aruma.repository.ProductRepository;
import co.com.aruma.repository.PurchanseRepository;
import co.com.aruma.utils.LogTransaction;
import reactor.core.publisher.Mono;


@Service
@Transactional
public class OnlineStoreService {
	
	private static final String PURCHANSE_NOT_FOUND = "Purchanse not found";
	@Value("${application.maxUpdateTime}") int maxUpdateTime;
	@Value("${application.maxDeleteTime}") int maxDeleteTime;
	@Value("${application.minimumPurchanse}") int minimumPurchanse;
	
	
	@Value("${application.ivaValue}") float ivaValue;
	@Value("${application.deletePurchanseTax}") float deletePurchanseTax;
	
	//private static final String PRODUCT_NOT_FOUND = "product not found";
	
	
	@Autowired
	PurchanseRepository purchanseRepository;
	
	@Autowired
	BillRepository billRepository;
	
	@Autowired
	ProductRepository productRepository;
	
	//@Autowired
	//EncryptAndDecrypt encryptAndDecrypt;
	
	public Mono<Response> purchanseProduct(@Valid PurchanseDTO purchanseDTO) {
		
		purchanseDTO.setProductsId(generateHashMap(purchanseDTO.getProductsToBuy()));

		List<String> productsIds = getProductsIds(purchanseDTO.getProductsId());
		
		return this.productRepository.findByIdIn(productsIds).collectList().onErrorResume(Mono::error)
				.flatMap(products -> {
					
					if(products.size() < purchanseDTO.getProductsId().size()) {
						
						Response res = Response.builder().status(false).message("some products on the list doesnt exist!" ).build();
						return Mono.just(res);
					}
					
					if(veryfyAmountPurchanse(purchanseDTO.getProductsId(), products)) {
						
						float productsCosts = calculateProductCosts(purchanseDTO.getProductsId(), products);
						float deliveryCosts = calculateDeliveryCosts(productsCosts, purchanseDTO.getClientDTO().getDeliveryAddress());
						float taxCosts = calculateTaxCosts(productsCosts, ivaValue);
						
						if(productsCosts < minimumPurchanse) {
							Response res = Response.builder().status(false).message("Minimum purchanse has to be above $" + minimumPurchanse).build();
							return Mono.just(res);
						}

						ArrayList<Product> updatedProducts = (ArrayList<Product>) updateProducts(products, purchanseDTO.getProductsId());
						
						return this.productRepository.saveAll(updatedProducts).collectList().onErrorResume(Mono::error)
								.flatMap(productsUpdated -> {
									
								
										return this.purchanseRepository.save(mapPurchanseDTOToEntity(purchanseDTO, deliveryCosts, taxCosts, productsCosts))
								    			.flatMap(savedPurchanse ->{
								    				
				
								    		    				
					    		    				return  Mono.just(Response.builder()
					    		                    		.status(true)
					    		                    		.message("OK")
					    		                    		.data(mapEnityToBill(savedPurchanse))
					    		                            .build());
								    		    		
								    	});
										
								}).onErrorResume(e -> {

									e.printStackTrace();
									Response res = Response.builder().status(false).message("BAD_REQUEST " + e.getMessage()).build();
									return Mono.just(res);

								});
						
					}else {
						
						Response res = Response.builder().status(false).message("There isnt enough products to satify order" ).build();
						return Mono.just(res);
						
					}

					
					
				}).onErrorResume(e -> {

					e.printStackTrace();
					Response res = Response.builder().status(false).message("Error ocured: " + e.getMessage()).build();
					return Mono.just(res);
					
					

				});
    		

	}
	




	public Mono<Response> editPurchanse(String purchanseId, @Valid ArrayList<ProductDTO> purchanseDTO) {
		
		HashMap<String, Integer> productsToEditHash  = generateHashMap(purchanseDTO);
		
		return this.purchanseRepository.findByIdAndActive(purchanseId, true).switchIfEmpty(Mono.error(new Exception(PURCHANSE_NOT_FOUND))).
		        flatMap(purchanse -> {
		        	
		        		Date currentDate = new Date();
		        	 
			            if(calculateHourDiference(currentDate, purchanse.getPurchanseDate()) > maxUpdateTime) {
			            	
							Response res =Response.builder().status(false).message("Maximum time to edit a product list is " + maxUpdateTime + " hours").build();
							return Mono.just(res);
			            }
		            	

		            	List<String> productsIds = getProductsIds(productsToEditHash);
		        		return this.productRepository.findByIdIn(productsIds).collectList().onErrorResume(Mono::error)
		        				.flatMap(products -> {
		        					
		        					
		        					if(products.size() < productsToEditHash.size()) {
		        						
		        						Response res = Response.builder().status(false).message("some products on the list doesnt exist!" ).build();
		        						return Mono.just(res);
		        					}
		        					
		        					if(!veryfyAmountPurchanse2(purchanse.getProductsPurchansed(), productsToEditHash, products)) {
		        						
		        						Response res = Response.builder().status(false).message("There isnt enough products to satify order" ).build();
		        						return Mono.just(res);
		        					}
		        					
	        						float productsCosts = calculateProductCosts(productsToEditHash, products);
	        						float deliveryCosts = purchanse.getDeliveryCosts();
	        						float taxCosts = calculateTaxCosts(productsCosts, ivaValue);
	        						
	        						if(productsCosts < purchanse.getProductsCosts()) {
		        						Response res = Response.builder().status(false).message("new products list Must have atleast the same value as the previous one" ).build();
		        						return Mono.just(res);
	        						}
		        					
	        						ArrayList<Product> updatedProducts = (ArrayList<Product>) updateProducts2(products, purchanse.getProductsPurchansed(), productsToEditHash);

	        						
	        						return this.productRepository.saveAll(updatedProducts).collectList().onErrorResume(Mono::error)
	        								.flatMap(productsUpdated -> {
	        									
	        									
	        										purchanse.setDeliveryCosts(deliveryCosts);
	        										purchanse.setProductsCosts(productsCosts);
	        										purchanse.setTaxCosts(taxCosts);
	        										purchanse.setProductsPurchansed(productsToEditHash);
	        								
	        										return this.purchanseRepository.save(purchanse)
	        								    			.flatMap(updatedPurchanse ->{
	        								    				
	        				
	        								    		    				
	        					    		    				return  Mono.just(Response.builder()
	        					    		                    		.status(true)
	        					    		                    		.message("OK")
	        					    		                    		.data(mapEnityToBill(updatedPurchanse))
	        					    		                            .build());
	        								    		    		
	        								    	});
	        										
	        								}).onErrorResume(e -> {

	        									e.printStackTrace();
	        									Response res = Response.builder().status(false).message("BAD_REQUEST " + e.getMessage()).build();
	        									return Mono.just(res);

	        								});
		        					
		        				}).onErrorResume(e -> {

		        					e.printStackTrace();
		        					Response res = Response.builder().status(false).message("Error ocured: " + e.getMessage()).build();
		        					return Mono.just(res);
		        					
		        					

		        				});
		            		

		        }).onErrorResume(e -> {
					Response res =Response.builder().status(false).message(e.getMessage()).build();
					return Mono.just(res);
				});	
	}
	
	
	public Mono<Response> deletePurchanse(String purchanseId) {
		
		
		return this.purchanseRepository.findByIdAndActive(purchanseId, true).switchIfEmpty(Mono.error(new Exception(PURCHANSE_NOT_FOUND))).
		        flatMap(purchanse -> {
		        	        
		            	

		            	List<String> productsIds = getProductsIds(purchanse.getProductsPurchansed());
		        		return this.productRepository.findByIdIn(productsIds).collectList().onErrorResume(Mono::error)
		        				.flatMap(products -> {
		        					
		        					
	        						ArrayList<Product> updatedProducts = (ArrayList<Product>) updateProducts3(products, purchanse.getProductsPurchansed());

	        						
	        						return this.productRepository.saveAll(updatedProducts).collectList().onErrorResume(Mono::error)
	        								.flatMap(productsUpdated -> {
	        									
	        										purchanse.setActive(false);
	        								
	        										return this.purchanseRepository.save(purchanse)
	        								    			.flatMap(deletedPurchanse ->{
	        								    				
	        								    				
	        									        		boolean billPurchanseDeleteTaxBand = false;
	        									        		float billPurchanseDeleteTax = 0;
	        									        		Date currentDate = new Date();
	        									        	 
	        										            if(calculateHourDiference(currentDate, purchanse.getPurchanseDate()) > maxDeleteTime) {
	        										            	billPurchanseDeleteTaxBand = true;
	        										            	billPurchanseDeleteTax = purchanse.getProductsCosts() * deletePurchanseTax;
	        										            }
	        								    				returnUserMoney(billPurchanseDeleteTaxBand);
	        								    				
	        								    				
	        								    				float refoundedAmount = purchanse.getProductsCosts() + purchanse.getTaxCosts() + purchanse.getDeliveryCosts() - billPurchanseDeleteTax;
	        					    		    				return  Mono.just(Response.builder()
	        					    		                    		.status(true)
	        					    		                    		.message("Ok, Your $" + refoundedAmount + " has been refounded and the refound tax is $" +  billPurchanseDeleteTax)
	        					    		                    		.data(mapEnityToBill(deletedPurchanse))
	        					    		                            .build());
	        								    		    		
	        								    	});
	        										
	        								}).onErrorResume(e -> {

	        									e.printStackTrace();
	        									Response res = Response.builder().status(false).message("BAD_REQUEST " + e.getMessage()).build();
	        									return Mono.just(res);

	        								});
		        					
		        				}).onErrorResume(e -> {

		        					e.printStackTrace();
		        					Response res = Response.builder().status(false).message("Error ocured: " + e.getMessage()).build();
		        					return Mono.just(res);
		        					
		        					

		        				});
		            		

		        }).onErrorResume(e -> {
					Response res =Response.builder().status(false).message(e.getMessage()).build();
					return Mono.just(res);
				});	
	}
	

	
	public Mono<Response> dataInitializer(int amountOfProducts){
		
		String[] productName = new String[]{"Balon de futbol", "vajlla", "Tenis Nike", "PlayStation", "Juego Sabanas", "Silla", "Maleta Viaje", "Lapiz"};
		String[] productColor = new String[]{"Lila", "Azul", "Verde", "Rojo", "Rosado", "Fucsia", "Negro", "Blanco"};
		ArrayList<Product> products = new ArrayList<Product>();
		String productFinalName = "";
		Random r = new Random();
		
		
		for(int i = 0; i < amountOfProducts; i++) {
			
			
			r = new Random();
			int productHigh = productName.length - 1;
			String randomProductName = productName[r.nextInt(productHigh-0) + 0];
			
			r = new Random();
			productHigh = productColor.length - 1;
			String randomProductColor = productColor[r.nextInt(productHigh-0) + 0];
			
			
			r = new Random();
			productHigh = 1000;
			int randomProductSerial = r.nextInt(productHigh-0) + 0;
			
			r = new Random();
			productHigh = 500000;
			int randomPrice = r.nextInt(productHigh-1) + 1;
			
			r = new Random();
			productHigh = 50;
			int randomAmount = r.nextInt(productHigh-1) + 1;
			
			productFinalName = randomProductName + " " + randomProductColor + " " + randomProductSerial;
			
			products.add(Product
					.builder()
					.name( productFinalName )
					.price(randomPrice)
					.amount(randomAmount)
					.active(true)
					.build());
			
			
		}
		
		return this.productRepository.saveAll(products).collectList().onErrorResume(Mono::error)
				.flatMap(savedProducts -> {
					
					Response res = Response.builder().status(true).message("Ok").data(mapEntitiToDTO(savedProducts)).build();
					return Mono.just(res);
					
				}).onErrorResume(e -> {

					e.printStackTrace();
					Response res = Response.builder().status(false).message("Error ocured: " + e.getMessage()).build();
					return Mono.just(res);
					
					

				});		
	}
	
	
	private BillDTO mapEnityToBill(Purchanse purchanse) {
		
		return BillDTO.builder()
				.id(purchanse.getId().toString())
				.payingMethod(purchanse.getPayingMethod())
				.productsCosts(purchanse.getProductsCosts())
				.deliveryCosts(purchanse.getDeliveryCosts())
				.taxCosts(purchanse.getTaxCosts())
				.productsPurchansed(purchanse.getProductsPurchansed())
				.purchanseDate(purchanse.getPurchanseDate())
				.clientInfo(ClientDTO.builder()
						.documentId(purchanse.getClientDocumentId())
						.deliveryAddress(purchanse.getDeliveryAddress())
						.cellphone(purchanse.getClientCellphone())
						.email(purchanse.getClientEmail())
						.build())
				.build();
	}
	
	

	
	private ArrayList<ProductDTOOut> mapEntitiToDTO(List<Product> savedProducts) {
		
		ArrayList<ProductDTOOut> products = new ArrayList<ProductDTOOut>();
		
		for(Product p : savedProducts)
			products.add(ProductDTOOut.builder()
					.id(p.getId().toString())
					.name(p.getName())
					.price(p.getPrice())
					.amount(p.getAmount())
					.build());
		return products;
	}





	private List<String> getProductsIds(HashMap<String, Integer> purchanseDTO) {
		List<String> productsIds = new ArrayList<String>();
		for ( String productId : purchanseDTO.keySet() ) {
			productsIds.add(productId);
		}
		
		return productsIds;

	}



	private float calculateTaxCosts(float productsCosts, float ivaValue) {
		return productsCosts * ivaValue;
	}






	private float calculateProductCosts(HashMap<String, Integer> productsId, List<Product> products) {
		
		float totalCosts = 0;
		for(Product product : products) {
			totalCosts += product.getPrice() * productsId.get(product.getId().toString());
		}
		return totalCosts;
	}



	private boolean veryfyAmountPurchanse( HashMap<String, Integer> productsToPurchanse, List<Product> products) {
		
		
		for(Product product : products) {
			
			if(productsToPurchanse.get(product.getId().toString()) > product.getAmount()) {
				return false;
			}
			
		}
			
		return true;
	}
	
	
	private List<Product> updateProducts(List<Product> products, HashMap<String, Integer> productToPurchanse) {
		
		//List<Product> updatedProducts = new ArrayList<Product> ();
		for(Product product : products) {
			
			product.setAmount(product.getAmount() - productToPurchanse.get(product.getId().toString()));
		}
			
		return products;
	}



	private float calculateDeliveryCosts(float accumulativePrice, String userAddress ) {
		
		
		if(accumulativePrice >= 100000) {
			return 0;
		}
		Random r = new Random();
		int low = 10000;
		int high = 50000;
		int result = r.nextInt(high-low) + low;
		return result;
	}



	//private Bill mapBillDTOToEntity(@Valid PurchanseDTO purchanseDTO, float deliveryCosts) {}
	private Purchanse mapPurchanseDTOToEntity(@Valid PurchanseDTO purchanseDTO, float deliveryCosts, float taxCosts, float productsCosts) {
		

		return Purchanse
				.builder()
				.payingMethod(purchanseDTO.getPayingMethod())
				.productsCosts(productsCosts)
				.deliveryCosts(deliveryCosts)
				.taxCosts(taxCosts)
				.purchanseDate(new Date())
				.active(true)
				.productsPurchansed(purchanseDTO.getProductsId())
				.clientDocumentId(purchanseDTO.getClientDTO().getDocumentId())
				.deliveryAddress(purchanseDTO.getClientDTO().getDeliveryAddress())
				.clientCellphone(purchanseDTO.getClientDTO().getCellphone())
				.clientEmail(purchanseDTO.getClientDTO().getEmail())
				.build();
	}
	
	
	private boolean veryfyAmountPurchanse2(HashMap<String, Integer> oldProductsPurchansed,
			HashMap<String, Integer> newProductsPurchansed, List<Product> products) {
		
		
		for(Product product : products) {
			
			if (oldProductsPurchansed.containsKey(product.getId().toString())) {
				if(newProductsPurchansed.get(product.getId().toString()) > product.getAmount() + oldProductsPurchansed.get(product.getId().toString())) {
					return false;
				}
		    } else {
				if(newProductsPurchansed.get(product.getId().toString()) > product.getAmount() ) {
					return false;
				}
		    }

			
		}
			
		return true;
	}


	private List<Product> updateProducts2(List<Product> products, HashMap<String, Integer> oldProductsToPurchanse, HashMap<String, Integer> newProductsToPurchanse) {
		
		for(Product product : products) {
			if(oldProductsToPurchanse.containsKey(product.getId().toString()))
				product.setAmount(product.getAmount() + oldProductsToPurchanse.get(product.getId().toString()) - newProductsToPurchanse.get(product.getId().toString()));
			else
				product.setAmount(product.getAmount() - newProductsToPurchanse.get(product.getId().toString()));

		}
			
		return products;
	}
	
	private float calculateHourDiference(Date date1, Date date2) {
		
        long diffInMillies = Math.abs(date1.getTime() - date2.getTime());
        long diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
        
        return diff / 60;
	}
	

	

	
	private void returnUserMoney(boolean billPurchanseDeleteTax) {
		// TODO Auto-generated method stub
		
	}



	private ArrayList<Product> updateProducts3(List<Product> products, HashMap<String, Integer> productsPurchansed) {
		// TODO Auto-generated method stub
		//List<Product> updatedProducts = new ArrayList<Product> ();
		for(Product product : products) {
			
			product.setAmount(product.getAmount() + productsPurchansed.get(product.getId().toString()));
		}
			
		return (ArrayList<Product>) products;
	}
	
	private HashMap<String, Integer>generateHashMap(ArrayList<ProductDTO> products) {
		
		HashMap<String, Integer> productsMap = new HashMap<String, Integer>();
		ArrayList<ProductDTO> productsNormilized = new ArrayList<ProductDTO>();	
		ArrayList<ProductDTO> auxlist = products;
		
		ArrayList<Boolean>  auxBandlist = new ArrayList<Boolean>();	
		for(int i = 0; i < products.size(); i++) { 
			auxBandlist.add(false);
		}
		
		for(int i = 0; i < products.size(); i++) {
			
			productsNormilized.add(products.get(i));
			auxBandlist.set(i, true);
			for(int j = 0; j < auxlist.size(); j++) {
				if( i != j && products.get(i) == auxlist.get(j) && !auxBandlist.get(j)) {
					productsNormilized.get(i).setAmount(productsNormilized.get(i).getAmount() + products.get(j).getAmount());
					auxBandlist.set(j, true);
				}
				
			}
			
		}
		for(ProductDTO p : productsNormilized) {
			productsMap.put(p.getProductId(), p.getAmount());
			
		}
		
		return productsMap;
	}

	//El pingolin
	public Mono<Response> ping( HttpHeaders hh) {
		
		LogTransaction log = new LogTransaction(hh);
        log.startTransaction("online store ping", null);
    	log.endTransaction(null);
        return Mono.just(Response.builder().status(true).message("Service is working...").data(null).build());
    }

}