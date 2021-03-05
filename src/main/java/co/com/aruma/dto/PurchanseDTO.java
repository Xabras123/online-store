package co.com.aruma.dto;

import java.util.ArrayList;
import java.util.HashMap;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PurchanseDTO {
	
	private HashMap<String, Integer> productsId;
	
	@Valid 
	@NotEmpty(message = "please provide products to buy")
	private ArrayList<ProductDTO> productsToBuy;
	
	@NotBlank(message = "please provide a paying method")
	private String payingMethod;
	
	@Valid 
	private ClientDTO clientDTO;
	

}
