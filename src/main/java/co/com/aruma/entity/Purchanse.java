package co.com.aruma.entity;

import java.util.Date;
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
@Document(collection =  "Purchanse")
public class Purchanse {
	
    @Id
    @Field(value = "_id")
    public ObjectId id;
	@Field("payingMethod") private String payingMethod;
	@Field("productsCosts") private float productsCosts;
	@Field("deliveryCosts") private float deliveryCosts;
	@Field("taxCosts") private float taxCosts;
	@Field("productsPurchansed") private HashMap<String, Integer> productsPurchansed;
	@Field("purchanseDate") private Date purchanseDate;
	@Field("active") private boolean active;

    
}