package co.com.aruma.dto;

import java.util.HashMap;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
public class ProductDTO {
	
	
	@NotBlank(message = "please provide a productId")
	private String productId;
	
	@Min(value = 1, message = "please provide a valid amount (1 - 99999)")
	@Max(value = 99999, message = "please provide a valid verification number (1 - 99999)")
	@NotNull(message = "please provide a product amount")
	private int amount;

	

}
