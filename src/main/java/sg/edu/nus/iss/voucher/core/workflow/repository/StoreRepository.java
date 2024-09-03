package sg.edu.nus.iss.voucher.core.workflow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sg.edu.nus.iss.voucher.core.workflow.entity.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {

	Page<Store> findByIsDeletedFalse(Pageable pageable);
}
