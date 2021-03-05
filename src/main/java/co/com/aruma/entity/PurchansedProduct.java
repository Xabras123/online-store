package co.com.aruma.entity;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;



@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchansedProduct {
	
	@Field("name") private Product name;
	@Field("price") private float price;
	@Field("amount") private int amount;    
    
}