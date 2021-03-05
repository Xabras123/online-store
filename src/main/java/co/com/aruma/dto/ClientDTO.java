package co.com.aruma.dto;

import javax.validation.constraints.NotBlank;

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
public class ClientDTO {
	

	@NotBlank(message = "please provide a documentId")
	private String documentId;

	@NotBlank(message = "please provide a delivery address")
	private String deliveryAddress;
	
	@NotBlank(message = "please provide a cellphone")
	private String cellphone;
	
	@NotBlank(message = "please provide a email")
	private String email;
	

}
