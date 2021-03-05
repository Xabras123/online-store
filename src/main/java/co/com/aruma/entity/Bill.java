package co.com.aruma.entity;

import java.util.HashMap;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
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
@Document(collection =  "bill")
public class Bill {
	
    @Id
    @Field(value = "_id") public ObjectId id;
	@Field("totalPrice") private float price;
	@Field("amount") private int amount;
	@Field("productsPurchansed") private HashMap<Product, Integer> productsPurchansed;
	@Field("purchanseDate") private int purchanseDate;
    
}