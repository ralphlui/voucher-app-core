package sg.edu.nus.iss.voucher.core.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sg.edu.nus.iss.voucher.core.workflow.entity.Voucher;

public interface VoucherRepository extends JpaRepository<Voucher, String> {

}
