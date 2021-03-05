package co.com.aruma.entity;

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
@Document(collection =  "product")
public class Product {
	
	
    @Id
    @Field(value = "_id")
    public ObjectId id;

	@Field("name") private String name;
	@Field("price") private float price;
	@Field("amount") private int amount;
	
	@Field("active") private boolean active;

	
    
    //@Builder.Default
    //@Field("accountStatus") private int accountStatus = 1;
    
}