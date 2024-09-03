package sg.edu.nus.iss.voucher.core.workflow.entity;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Campaign {
	
	@Id
	@UuidGenerator(style = UuidGenerator.Style.AUTO)
	private String campaignId;

}
