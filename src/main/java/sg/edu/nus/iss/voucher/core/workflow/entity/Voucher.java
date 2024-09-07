package sg.edu.nus.iss.voucher.core.workflow.entity;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
public class Voucher {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.AUTO)
	private String voucherId;
}
