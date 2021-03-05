package co.com.aruma.dto;

import java.util.Date;
import java.util.HashMap;



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
public class BillDTO {
	

    public String id;
	private String payingMethod;
	private float productsCosts;
	private float deliveryCosts;
	private float taxCosts;
	private HashMap<String, Integer> productsPurchansed;
	private Date purchanseDate;
	private ClientDTO clientInfo;

}
